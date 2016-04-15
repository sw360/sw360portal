/*
 * Copyright Siemens AG, 2013-2016. Part of the SW360 Portal Project.
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
package com.siemens.sw360.portal.portlets.signup;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.model.Organization;
import com.siemens.sw360.datahandler.thrift.moderation.ModerationService;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.thrift.users.UserGroup;
import com.siemens.sw360.portal.common.PortalConstants;
import com.siemens.sw360.portal.common.UsedAsLiferayAction;
import com.siemens.sw360.portal.portlets.Sw360Portlet;
import com.siemens.sw360.portal.users.UserUtils;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import javax.portlet.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Signup portlet
 *
 * @author alex.borodin@evosoft.com
 */

public class SignupPortlet extends Sw360Portlet {

    private static final Logger log = Logger.getLogger(SignupPortlet.class);

    @Override
    public void doView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        String pageName = request.getParameter(PortalConstants.PAGENAME);
        if (PortalConstants.PAGENAME_SUCCESS.equals(pageName)) {
            include("/html/homepage/signup/success.jsp", request, response);
        } else {
            prepareStandardView(request);
            super.doView(request, response);
        }
    }

    private void prepareStandardView(RenderRequest request) {
        List<UserGroup> userGroups = Arrays.asList(UserGroup.values());
        request.setAttribute(PortalConstants.USER_GROUPS, userGroups);
        List<Organization> organizations = UserUtils.getOrganizations(request);
        request.setAttribute(PortalConstants.ORGANIZATIONS, organizations);
    }

    @UsedAsLiferayAction
    public void createAccount(ActionRequest request, ActionResponse response) throws PortletException, IOException {
        Registrant registrant = new Registrant(request);
        SignupPortletUtils.updateUserFromRequest(request, registrant);
        User newUser = null;
        if (registrant.validateUserData(request)) {
            newUser = createUser(registrant, request);
        }
        if (null != newUser) {
            boolean success = createUserModerationRequest(newUser, request);
            if (success){
                response.setRenderParameter(PortalConstants.PAGENAME, PortalConstants.PAGENAME_SUCCESS);
            }
        } else {
            request.setAttribute(PortalConstants.USER, registrant);
            log.info("Could not create User");
        }

    }

    private boolean createUserModerationRequest(User user, ActionRequest request) {
        ModerationService.Iface client = thriftClients.makeModerationClient();
        try {
            client.createUserRequest(user);
            log.info("Created moderation request for a new user account");
            SessionMessages.add(request, "request_processed", "Moderation request has been sent");

        } catch (TException e) {
            String failMsg = "Could not create user moderation request";
            log.error(failMsg, e);
            SessionMessages.add(request, "request_processed", failMsg);
            return false;
        }
        return true;
    }

    private User createUser(Registrant registrant, PortletRequest request) {
        User user = null;
        try {
            com.liferay.portal.model.User liferayUser = registrant.addLifeRayUser(request);
            if (liferayUser != null) {
                user = UserUtils.synchronizeUserWithDatabase(registrant, thriftClients, registrant::getEmail, UserUtils::fillThriftUserFromThriftUser);
            }
        } catch (PortalException | SystemException e) {
            log.error(e);
        }
        return user;
    }

}
