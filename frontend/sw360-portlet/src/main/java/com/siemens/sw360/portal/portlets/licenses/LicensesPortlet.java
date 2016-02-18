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
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.permissions.PermissionUtils;
import com.siemens.sw360.datahandler.thrift.DocumentState;
import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.SW360Exception;
import com.siemens.sw360.datahandler.thrift.ThriftClients;
import com.siemens.sw360.datahandler.thrift.licenses.*;
import com.siemens.sw360.datahandler.thrift.users.RequestedAction;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.thrift.users.UserGroup;
import com.siemens.sw360.exporter.LicenseExporter;
import com.siemens.sw360.portal.common.PortalConstants;
import com.siemens.sw360.portal.common.UsedAsLiferayAction;
import com.siemens.sw360.portal.portlets.Sw360Portlet;
import com.siemens.sw360.portal.users.UserCacheHolder;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import java.util.*;

import javax.portlet.*;
import java.io.IOException;
import java.util.stream.Collectors;

import static com.siemens.sw360.datahandler.common.CommonUtils.nullToEmptyList;
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
    private List<LicenseType> licenseTypes;

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
            LicenseService.Iface client = thriftClients.makeLicenseClient();
            List<License> licenses = client.getLicenseSummaryForExport();
            List<LicenseType> licenseTypes = client.getLicenseTypeSummaryForExport();

            PortletResponseUtil.sendFile(request, response, "Licenses.xlsx", exporter.makeExcelExport(licenses, licenseTypes), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        } catch (IOException | TException e) {
            log.error("An error occured while generating the Excel export", e);
        }
    }

    @Override
    public void doView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        String pageName = request.getParameter(PAGENAME);
        if (PAGENAME_DETAIL.equals(pageName)) {
            prepareDetailView(request, response);
            include("/html/licenses/detail.jsp", request, response);
        } else if (PAGENAME_EDIT.equals(pageName)) {
            prepareEditView(request, response);
            include("/html/licenses/edit.jsp", request, response);
        } else {
            prepareStandardView(request);
            super.doView(request, response);
        }
    }

    private void prepareEditView(RenderRequest request, RenderResponse response) {

        String id = request.getParameter(LICENSE_ID);
        User user = UserCacheHolder.getUserFromRequest(request);
        LicenseService.Iface client = thriftClients.makeLicenseClient();

        try {
            licenseTypes = client.getLicenseTypes();
            request.setAttribute(LICENSE_TYPE_CHOICE, licenseTypes);
        }catch(TException e){
            log.error("Error fetching license types from backend", e);
        }

        if (id != null) {
            try {
                License license = client.getByID(id, user.getDepartment());
                request.setAttribute(KEY_LICENSE_DETAIL, license);
                addLicenseBreadcrumb(request, response, license);
            } catch (TException e) {
                log.error("Error fetching license details from backend", e);
            }
        } else {
            SessionMessages.add(request, "request_processed", "New License");
            License license = new License();
            request.setAttribute(KEY_LICENSE_DETAIL, license);
        }
    }

    private void prepareStandardView(RenderRequest request) {
        log.debug("Enter license table view");
        List<License> licenses;
        User user = UserCacheHolder.getUserFromRequest(request);
        request.setAttribute(IS_USER_AT_LEAST_CLEARING_ADMIN, PermissionUtils.isUserAtLeast(UserGroup.CLEARING_ADMIN, user) ? "Yes" : "Nope");
        try {
            LicenseService.Iface client = thriftClients.makeLicenseClient();
            licenses = client.getLicenseSummary();
        } catch (TException e) {
            log.error("Could not fetch license summary from backend!", e);
            licenses = new ArrayList<>();
        }

        request.setAttribute(LICENSE_LIST, licenses);
    }

    private void prepareDetailView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        String id = request.getParameter(LICENSE_ID);
        User user = UserCacheHolder.getUserFromRequest(request);
        request.setAttribute(IS_USER_AT_LEAST_CLEARING_ADMIN, PermissionUtils.isUserAtLeast(UserGroup.CLEARING_ADMIN, user) ? "Yes" : "Nope");
        if (id != null) {
            try {
                LicenseService.Iface client = thriftClients.makeLicenseClient();
                License moderationLicense = client.getByIDWithOwnModerationRequests(id, user.getDepartment(), user);

                List<Todo> allTodos = nullToEmptyList(moderationLicense.getTodos());
                List<Todo> addedTodos = allTodos
                        .stream()
                        .filter(todo -> todo.id.startsWith("tmp"))
                        .collect(Collectors.toList());
                List<Todo> currentTodos = allTodos
                        .stream()
                        .filter(todo -> !todo.id.startsWith("tmp"))
                        .collect(Collectors.toList());

                request.setAttribute(ADDED_TODOS_FROM_MODERATION_REQUEST, addedTodos);
                request.setAttribute(DB_TODOS_FROM_MODERATION_REQUEST, currentTodos);

                request.setAttribute(MODERATION_LICENSE_DETAIL, moderationLicense);

                License dbLicense = client.getByID(id, user.getDepartment());
                request.setAttribute(KEY_LICENSE_DETAIL, dbLicense);

                List<Obligation> obligations = client.getObligations();
                request.setAttribute(KEY_OBLIGATION_LIST, obligations);

                addLicenseBreadcrumb(request, response, moderationLicense);

            } catch (TException e) {
                log.error("Error fetching license details from backend", e);
            }
        }
    }

    private void addLicenseBreadcrumb(RenderRequest request, RenderResponse response, License license) {
        PortletURL componentUrl = response.createRenderURL();
        componentUrl.setParameter(PAGENAME, PAGENAME_DETAIL);
        componentUrl.setParameter(LICENSE_ID, license.getId());

        addBreadcrumbEntry(request, license.getShortname(), componentUrl);
    }

    @UsedAsLiferayAction
    public void update(ActionRequest request, ActionResponse response) throws PortletException, IOException {
        License license = new License();
        LicenseService.Iface client = thriftClients.makeLicenseClient();
        String licenseId = request.getParameter(LICENSE_ID);
        User user = UserCacheHolder.getUserFromRequest(request);

        if (!Strings.isNullOrEmpty(licenseId)) {
            try {
                license = client.getByID(licenseId, user.getDepartment());
            } catch (TException e) {
                log.error("Error updating license:", e);
            }
        }

        license = updateLicenseFromRequest(license,request);

        RequestStatus requestStatus;
        try {
            requestStatus = client.updateLicense(license, user, user);
        } catch (TException e) {
            log.error("Could not add or update license:" + e);
            requestStatus = RequestStatus.FAILURE;
        }
        if (!Strings.isNullOrEmpty(licenseId)) {
            response.setRenderParameter(LICENSE_ID, licenseId);
            response.setRenderParameter(PAGENAME, PAGENAME_DETAIL);
            response.setRenderParameter(SELECTED_TAB, "Details");
            setSessionMessage(request, requestStatus, "License", "update");
        } else {
            response.setRenderParameter(PAGENAME, PAGENAME_VIEW);
            setSessionMessage(request, requestStatus, "License", "adde");
        }
    }

    private License updateLicenseFromRequest(License license, ActionRequest request) {
        String text = request.getParameter(License._Fields.TEXT.name());
        String fullname = request.getParameter(License._Fields.FULLNAME.name());
        String shortname = request.getParameter(License._Fields.SHORTNAME.name());
        boolean gpl2compatibility =
                (request.getParameter(License._Fields.GPLV2_COMPAT.toString()) == null) ? false : true;
        boolean gpl3compatibility =
                (request.getParameter(License._Fields.GPLV3_COMPAT.toString()) == null) ? false : true;
        String licenseTypeString =
                request.getParameter(License._Fields.LICENSE_TYPE.toString() + LicenseType._Fields.LICENSE_TYPE.toString());
        license.setText(CommonUtils.nullToEmptyString(text));
        license.setFullname(CommonUtils.nullToEmptyString(fullname));
        license.setShortname((CommonUtils.nullToEmptyString(shortname)));
        license.setGPLv2Compat(gpl2compatibility);
        license.setGPLv3Compat(gpl3compatibility);
        try {
            String licenseTypeDatabaseId = getDatabaseIdFromLicenseType(licenseTypeString);
            license.setLicenseTypeDatabaseId(licenseTypeDatabaseId);
        } catch (TException e) {
            log.error("Could not set licenseTypeDatabaseId:" + e);
        }
        return license;
    }

    private String getDatabaseIdFromLicenseType(String licenseTypeString) throws TException {
        if (licenseTypes == null) {
            LicenseService.Iface client = thriftClients.makeLicenseClient();
            try {
                licenseTypes = client.getLicenseTypes();
            } catch (TException e){
                throw new SW360Exception("Error getting license type list:"+ e);
            }
        }
        for (LicenseType licenseType : licenseTypes) {
            if (licenseType.licenseType.equals(licenseTypeString)) {
                return licenseType.getId();
            }
        }
        throw new SW360Exception("Wrong license type!");
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
                final License license = client.getByID(licenseId,user.getDepartment());

                license.setText(CommonUtils.nullToEmptyString(text));
                final RequestStatus requestStatus = client.updateLicense(license, user, user);

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
        //add temporary id
        todo.setId("tmp" + UUID.randomUUID().toString());
        if (obligationIds != null) {
            for (String obligationId : obligationIds) {
                if (obligationId != null && !obligationId.isEmpty()) {
                    todo.addToObligationDatabaseIds(obligationId);
                }
            }
        } else {
            todo.setObligationDatabaseIds(Collections.emptySet());
        }
        todo.setText(todoText);

        User user = UserCacheHolder.getUserFromRequest(request);

        todo.addToWhitelist(user.getDepartment());

        if (bools != null) {
            List<String> theBools = Arrays.asList(bools);
            todo.setDevelopment(theBools.contains("development"));
            todo.setDistribution(theBools.contains("distribution"));
        } else {
            todo.setDevelopment(false);
            todo.setDistribution(false);
        }
        try {
            LicenseService.Iface client = thriftClients.makeLicenseClient();
            RequestStatus requestStatus = client.addTodoToLicense(todo, licenseID, user);

            setSessionMessage(request, requestStatus, "License", "update");

        } catch (TException e) {
            log.error("Error updating license details from backend", e);
        }
        response.setRenderParameter(LICENSE_ID, licenseID);
        response.setRenderParameter(PAGENAME, PAGENAME_DETAIL);
        response.setRenderParameter(SELECTED_TAB, "Todos");
    }

    @UsedAsLiferayAction
    public void delete(ActionRequest request, ActionResponse response) throws PortletException, IOException {
        RequestStatus requestStatus = deleteLicense(request);
        setSessionMessage(request, requestStatus, "License", "remove");
    }

    private RequestStatus deleteLicense(PortletRequest request) {
        String licenseId = request.getParameter(PortalConstants.LICENSE_ID);
        final User user = UserCacheHolder.getUserFromRequest(request);

        try {
            LicenseService.Iface client = thriftClients.makeLicenseClient();
            return client.deleteLicense(licenseId, user);
        } catch (TException e) {
            log.error("Error deleting license from backend", e);
        }

        return RequestStatus.FAILURE;
    }
}
