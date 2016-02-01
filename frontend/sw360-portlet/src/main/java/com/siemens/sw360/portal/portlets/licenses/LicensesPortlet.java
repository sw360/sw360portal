/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License Version 2.0 as published by the
 * Free Software Foundation with classpath exception.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License version 2.0 for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (please see the COPYING file); if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */
package com.siemens.sw360.portal.portlets.licenses;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.liferay.portal.kernel.portlet.PortletResponseUtil;
import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.permissions.PermissionUtils;
import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.licenses.License;
import com.siemens.sw360.datahandler.thrift.licenses.LicenseService;
import com.siemens.sw360.datahandler.thrift.licenses.Obligation;
import com.siemens.sw360.datahandler.thrift.licenses.Todo;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.exporter.LicenseExporter;
import com.siemens.sw360.portal.common.PortalConstants;
import com.siemens.sw360.portal.common.UsedAsLiferayAction;
import com.siemens.sw360.portal.portlets.Sw360Portlet;
import com.siemens.sw360.portal.users.UserCacheHolder;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import javax.portlet.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.siemens.sw360.portal.common.PortalConstants.*;

/**
 * License portlet implementation
 *
 * @author cedric.bodet@tngtech.com
 */
public class LicensesPortlet extends Sw360Portlet {

    private static final Logger log = Logger.getLogger(LicensesPortlet.class);

    /**
     * Excel exporter
     */
    private final LicenseExporter exporter;

    public LicensesPortlet() {
        exporter = new LicenseExporter();
    }

    //! Serve resource and helpers
    @Override
    public void serveResource(ResourceRequest request, ResourceResponse response) throws IOException, PortletException {
        String action = request.getParameter(PortalConstants.ACTION);

        if (PortalConstants.EXPORT_TO_EXCEL.equals(action)) {
            exportExcel(request, response);
        }
    }

    private void exportExcel(ResourceRequest request, ResourceResponse response) {
        try {
            List<License> licenses = thriftClients.makeLicenseClient().getLicenseSummaryForExport();

            PortletResponseUtil.sendFile(request, response, "Licenses.xlsx", exporter.makeExcelExport(licenses), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        } catch (IOException | TException e) {
            log.error("An error occured while generating the Excel export", e);
        }
    }

    @Override
    public void doView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        User user = UserCacheHolder.getUserFromRequest(request);
        request.setAttribute(IS_ADMINUSER, PermissionUtils.isAdmin(user)?"Yes":"Nope");
        if (PAGENAME_DETAIL.equals(request.getParameter(PAGENAME))) {
            log.debug("Enter license detail");
            String id = request.getParameter(LICENSE_ID);
            if (id != null) {
                try {
                    LicenseService.Iface client = thriftClients.makeLicenseClient();
                    License license = client.getByID(id, user.getDepartment());
                    request.setAttribute(KEY_LICENSE_DETAIL, license);
                    List<Obligation> obligations = client.getAllObligations();
                    request.setAttribute(KEY_OBLIGATION_LIST, obligations);

                    request.setAttribute(SELECTED_TAB, request.getParameter(SELECTED_TAB));

                    addLicenseBreadcrumb(request, response, license);
                } catch (TException e) {
                    log.error("Error fetching license details from backend", e);
                }
                include("/html/licenses/detail.jsp", request, response);
            }
        } else {
            log.debug("Enter license table view");
            List<License> licenses;
            try {
                LicenseService.Iface client = thriftClients.makeLicenseClient();
                licenses = client.getLicenseSummary();
            } catch (TException e) {
                log.error("Could not fetch license summary from backend!", e);
                licenses = new ArrayList<>();
            }

            request.setAttribute(LICENSE_LIST, licenses);
            super.doView(request, response);
        }
    }

    private void addLicenseBreadcrumb(RenderRequest request, RenderResponse response, License license) {
        PortletURL componentUrl = response.createRenderURL();
        componentUrl.setParameter(PAGENAME, PAGENAME_DETAIL);
        componentUrl.setParameter(LICENSE_ID, license.getId());

        addBreadcrumbEntry(request, license.getShortname(), componentUrl);
    }

    @UsedAsLiferayAction
    public void updateWhiteList(ActionRequest request, ActionResponse response) throws PortletException, IOException {

        // we get a list of todoDatabaseIds and Booleans and we have to update the whiteList of each todo if it changed
        String licenseId = request.getParameter(LICENSE_ID);
        String[] whiteList = request.getParameterValues("whiteList");
        if (whiteList == null) whiteList = new String[0]; // As empty arrays are not passed as parameters

        final User user = UserCacheHolder.getUserFromRequest(request);

        try {
            LicenseService.Iface client = thriftClients.makeLicenseClient();
            RequestStatus requestStatus = client.updateWhitelist(licenseId, ImmutableSet.copyOf(whiteList), user);

            setSessionMessage(request, requestStatus, "License", "update");

        } catch (TException e) {
            log.error("Error updating whitelist!", e);
        }

        response.setRenderParameter(LICENSE_ID, licenseId);
        response.setRenderParameter(PAGENAME, PAGENAME_DETAIL);
        response.setRenderParameter(SELECTED_TAB, "Todos");
    }

    @UsedAsLiferayAction
    public void changeText(ActionRequest request, ActionResponse response) throws PortletException, IOException {
        String licenseId = request.getParameter(LICENSE_ID);
        String text = request.getParameter(License._Fields.TEXT.name());

        if(!Strings.isNullOrEmpty(licenseId)) {
            try {
                User user = UserCacheHolder.getUserFromRequest(request);
                LicenseService.Iface client = thriftClients.makeLicenseClient();
                final License license = client.getFromID(licenseId);

                license.setText(CommonUtils.nullToEmptyString(text));
                final RequestStatus requestStatus = client.updateLicense(license, user);

                renderRequestStatus(request,response,requestStatus);
            } catch (TException e) {
                log.error("Error updating license", e);
            }
        }
        response.setRenderParameter(LICENSE_ID, licenseId);
        response.setRenderParameter(PAGENAME, PAGENAME_DETAIL);
        response.setRenderParameter(SELECTED_TAB, "LicenseText");
    }

    @UsedAsLiferayAction
    public void addTodo(ActionRequest request, ActionResponse response) throws PortletException, IOException {
        String licenseID = request.getParameter(LICENSE_ID);
        String[] obligationIds = request.getParameterValues("obligations");
        String todoText = request.getParameter("todoText");
        String[] bools = request.getParameterValues("bools");


        Todo todo = new Todo();

        if (obligationIds != null) {
            for (String obligationId : obligationIds) {
                if (obligationId != null && !obligationId.isEmpty())
                    todo.addToObligationDatabaseIds(obligationId);
            }
        } else {
            todo.setObligationDatabaseIds(Collections.emptySet());
        }
        todo.setText(todoText);

        User user = UserCacheHolder.getUserFromRequest(request);

        todo.addToWhitelist(user.getDepartment());

        if (bools != null) {
            List<String> theBools = Arrays.asList(bools);
            todo.setDevelopement(theBools.contains("development"));
            todo.setDistribution(theBools.contains("distribution"));
        } else {
            todo.setDevelopement(false);
            todo.setDistribution(false);
        }
        try {
            LicenseService.Iface client = thriftClients.makeLicenseClient();
            RequestStatus requestStatus = client.addTodoToLicense(todo, licenseID, user);

            setSessionMessage(request, "You do not have the permission to update the license.");

        } catch (TException e) {
            log.error("Error updating license details from backend", e);
        }
        response.setRenderParameter(LICENSE_ID, licenseID);
        response.setRenderParameter(PAGENAME, PAGENAME_DETAIL);
        response.setRenderParameter(SELECTED_TAB, "Todos");
    }
}
