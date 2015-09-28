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

import com.siemens.sw360.datahandler.thrift.RequestSummary;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentService;
import com.siemens.sw360.datahandler.thrift.components.ComponentService;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.portal.common.PortalConstants;
import com.siemens.sw360.portal.portlets.Sw360Portlet;
import com.siemens.sw360.portal.users.UserCacheHolder;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import javax.portlet.*;
import java.io.IOException;
import java.util.Set;

/**
 * @author johannes.najjar@tngtech.com
 */
public class AttachmentVacuum extends Sw360Portlet {

    private static final Logger log = Logger.getLogger(AttachmentVacuum.class);

    @Override
    public void doView(RenderRequest request, RenderResponse response) throws IOException, PortletException {

        // Proceed with page rendering
        super.doView(request, response);
    }

    @Override
    public void serveResource(ResourceRequest request, ResourceResponse response) throws IOException, PortletException {
        String action = request.getParameter(PortalConstants.ACTION);
        if (PortalConstants.CLEANUP.equals(action)) {
            try {
                final RequestSummary requestSummary = cleanUpAttachments(request);
                renderRequestSummary(request, response, requestSummary);
            } catch (TException e) {
                log.error("Something went wrong with the cleanup", e);
            }
        }
    }

    private RequestSummary cleanUpAttachments(ResourceRequest request) throws TException {
        final ComponentService.Iface componentClient = thriftClients.makeComponentClient();
        final AttachmentService.Iface attachmentClient = thriftClients.makeAttachmentClient();

        final Set<String> usedAttachmentIds = componentClient.getUsedAttachmentContentIds();
        final User userFromRequest = UserCacheHolder.getUserFromRequest(request);
        return attachmentClient.vacuumAttachmentDB(userFromRequest, usedAttachmentIds);
    }

}
