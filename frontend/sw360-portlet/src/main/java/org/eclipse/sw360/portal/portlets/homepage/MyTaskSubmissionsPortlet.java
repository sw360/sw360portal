/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.portal.portlets.homepage;

import org.eclipse.sw360.datahandler.common.CommonUtils;
import org.eclipse.sw360.datahandler.thrift.RequestStatus;
import org.eclipse.sw360.datahandler.thrift.ThriftClients;
import org.eclipse.sw360.datahandler.thrift.moderation.ModerationRequest;
import org.eclipse.sw360.datahandler.thrift.moderation.ModerationService;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.portal.common.PortalConstants;
import org.eclipse.sw360.portal.portlets.Sw360Portlet;
import org.eclipse.sw360.portal.users.UserCacheHolder;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import javax.portlet.*;
import java.io.IOException;
import java.util.List;

import static org.apache.log4j.Logger.getLogger;

/**
 * Small homepage portlet
 *
 * @author cedric.bodet@tngtech.com
 * @author gerrit.grenzebach@tngtech.com
 * @author birgit.heydenreich@tngtech.com
 */
public class MyTaskSubmissionsPortlet extends Sw360Portlet {

    private static final Logger log = getLogger(MyTaskSubmissionsPortlet.class);

    public void serveResource(ResourceRequest request, ResourceResponse response) throws IOException, PortletException {
        String action = request.getParameter(PortalConstants.ACTION);

        if (PortalConstants.DELETE_MODERATION_REQUEST.equals(action)) {
            serveDeleteModerationRequest(request, response);
        }
    }
    private void serveDeleteModerationRequest(ResourceRequest request, ResourceResponse response) throws IOException {
        RequestStatus requestStatus = deleteModerationRequest(request, log);
        serveRequestStatus(request, response, requestStatus, "Problem removing moderation request", log);
    }
    public static RequestStatus deleteModerationRequest(PortletRequest request, Logger log) {
        String id = request.getParameter(PortalConstants.MODERATION_ID);
        if (id != null) {
            try {
                ModerationService.Iface client = new ThriftClients().makeModerationClient();
                return client.deleteModerationRequest(id, UserCacheHolder.getUserFromRequest(request));

            } catch (TException e) {
                log.error("Could not delete moderation request from DB", e);
            }
        }
        return RequestStatus.FAILURE;
    }
    @Override
    public void doView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        List<ModerationRequest> moderations = null;

        try {
            final User user = UserCacheHolder.getUserFromRequest(request);
            moderations = thriftClients.makeModerationClient().getRequestsByRequestingUser(user);
        } catch (TException e) {
            log.error("Could not fetch your moderations from backend", e);
        }

        request.setAttribute(PortalConstants.MODERATION_REQUESTS, CommonUtils.nullToEmptyList(moderations));

        super.doView(request, response);
    }
}
