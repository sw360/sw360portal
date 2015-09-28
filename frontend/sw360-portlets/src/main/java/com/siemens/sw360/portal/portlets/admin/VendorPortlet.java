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
package com.siemens.sw360.portal.portlets.admin;

import com.google.common.collect.Sets;
import com.liferay.portal.kernel.portlet.PortletResponseUtil;
import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.components.ComponentService;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.thrift.vendors.Vendor;
import com.siemens.sw360.datahandler.thrift.vendors.VendorService;
import com.siemens.sw360.exporter.VendorExporter;
import com.siemens.sw360.portal.common.UsedAsLiferayAction;
import com.siemens.sw360.portal.portlets.Sw360Portlet;
import com.siemens.sw360.portal.portlets.components.ComponentPortletUtils;
import com.siemens.sw360.portal.users.UserCacheHolder;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import javax.portlet.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.siemens.sw360.portal.common.PortalConstants.*;

/**
 * Vendor portlet implementation
 *
 * @author Johannes.Najjar@tngtech.com
 */
public class VendorPortlet extends Sw360Portlet {

    private static final Logger log = Logger.getLogger(VendorPortlet.class);

    /**
     * Excel exporter
     */
    private final VendorExporter exporter = new VendorExporter();

    //! Serve resource and helpers
    @Override
    public void serveResource(ResourceRequest request, ResourceResponse response) throws IOException, PortletException {
        String action = request.getParameter(ACTION);

        if (EXPORT_TO_EXCEL.equals(action)) {
            exportExcel(request, response);
        } else if (REMOVE_VENDOR.equals(action)) {
            removeVendor(request, response);
        }
    }

    private void removeVendor(PortletRequest request, ResourceResponse response) throws IOException {
        final RequestStatus requestStatus = ComponentPortletUtils.deleteVendor(request, log);
        serveRequestStatus(request, response, requestStatus, "Problem removing vendor", log);

    }

    private void exportExcel(ResourceRequest request, ResourceResponse response) {
        try {
            VendorService.Iface client = thriftClients.makeVendorClient();
            List<Vendor> vendors = client.getAllVendors();

            PortletResponseUtil.sendFile(request, response, "Vendors.xlsx", exporter.makeExcelExport(vendors), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        } catch (IOException | TException e) {
            log.error("An error occured while generating the Excel export", e);
        }
    }

    //! VIEW and helpers
    @Override
    public void doView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        String pageName = request.getParameter(PAGENAME);
        if (PAGENAME_EDIT.equals(pageName)) {
            prepareVendorEdit(request);
            include("/html/vendors/edit.jsp", request, response);
        } else {
            prepareStandardView(request);
            super.doView(request, response);
        }
    }

    private void prepareVendorEdit(RenderRequest request) throws PortletException {
        String id = request.getParameter(VENDOR_ID);

        if (isNullOrEmpty(id)) {
            throw new PortletException("Component ID not set!");
        }
        try {
            VendorService.Iface client = thriftClients.makeVendorClient();
            Vendor vendor = client.getByID(id);
            request.setAttribute(VENDOR, vendor);


            final ComponentService.Iface componentClient = thriftClients.makeComponentClient();
            final List<Release> releasesFromVendorIds = componentClient.getReleasesFromVendorIds(Sets.newHashSet(id));

            request.setAttribute(RELEASE_LIST, releasesFromVendorIds);


        } catch (TException e) {
            log.error("Problem retrieving vendor");
        }
    }

    private void prepareStandardView(RenderRequest request) throws IOException {
        List<Vendor> vendorList;
        try {
            final User user = UserCacheHolder.getUserFromRequest(request);
            VendorService.Iface vendorClient = thriftClients.makeVendorClient();

            vendorList = vendorClient.getAllVendors();

        } catch (TException e) {
            log.error("Could not get Vendors from backend ", e);
            vendorList = Collections.emptyList();
        }

        request.setAttribute(VENDOR_LIST, vendorList);
    }

    @UsedAsLiferayAction
    public void updateVendor(ActionRequest request, ActionResponse response) throws PortletException, IOException {
        String id = request.getParameter(VENDOR_ID);
        final User user = UserCacheHolder.getUserFromRequest(request);

        if (id != null) {
            try {
                VendorService.Iface client = thriftClients.makeVendorClient();
                Vendor vendor = client.getByID(id);
                ComponentPortletUtils.updateVendorFromRequest(request, vendor);
                RequestStatus requestStatus = client.updateVendor(vendor, user);

                setSessionMessage(request, requestStatus, "Vendor", "update", vendor.getFullname());
            } catch (TException e) {
                log.error("Error fetching release from backend!", e);
            }
        }
    }

}
