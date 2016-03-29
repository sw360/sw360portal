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
 */package com.siemens.sw360.portal.users;

import com.liferay.portal.NoSuchUserException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.service.*;
import com.liferay.portal.service.persistence.RoleUtil;
import com.liferay.portal.theme.ThemeDisplay;
import org.apache.log4j.Logger;

import javax.portlet.PortletRequest;
import java.util.Locale;

/**
 * @author alex.borodin@evosoft.com
 */
public class UserPortletUtils {
    private static final Logger log = Logger.getLogger(UserPortletUtils.class);
    private UserPortletUtils() {
        // Utility class with only static functions
    }

    /**
     * Copied from https://github.com/fdelprete/CSV_User_Import-portlet/blob/master/docroot/WEB-INF/src/com/fmdp/csvuserimport/portlet/UserServiceImpl.java
     * with slight modifications
     *
     * @author Filippo Maria Del Prete
     * <p/>
     * based on the original work of Paul Butenko
     * http://java-liferay.blogspot.it/2012/09/how-to-make-users-import-into-liferay.html
     */
    private static User addLiferayUser(PortletRequest request, String firstName, String lastName, String emailAddress, long organizationId, long roleId, boolean male, String openId, String password, boolean passwordEncrypted, boolean activateImmediately) {
        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);

        long creatorUserId = themeDisplay.getUserId();
        long companyId = themeDisplay.getCompanyId();

        boolean autoPassword = false;
        String password1 = password;
        String password2 = password;
        boolean autoScreenName = false;
        String screenName = firstName + lastName;
        String middleName = "";
        long facebookId = 0;

        Locale locale = themeDisplay.getLocale();
        int prefixId = 0;
        int suffixId = 0;
        int birthdayMonth = 1;
        int birthdayDay = 1;
        int birthdayYear = 1970;
        String jobTitle = "";
        long[] groupIds = null;
        long[] organizationIds = null;

            if (organizationId != 0) {
                organizationIds = new long[1];
                organizationIds[0] = organizationId;
            }

        long[] roleIds = null;
        if (roleId != 0) {
            roleIds = new long[1];
            roleIds[0] = roleId;
        }
        long[] userGroupIds = null;
        boolean sendEmail = false;
        boolean userbyscreeenname_exists = true;
        boolean userbyemail_exists = true;
        boolean userbyopenid_exists = true;
        User user = null;
        try {
            try {
                user = UserLocalServiceUtil.getUserByScreenName(companyId, screenName);
            } catch (NoSuchUserException nsue) {
                userbyscreeenname_exists = false;
            }
            try {
                user = UserLocalServiceUtil.getUserByEmailAddress(companyId, emailAddress);
            } catch (NoSuchUserException nsue) {
                userbyemail_exists = false;
            }
            try {
                user = UserLocalServiceUtil.getUserByOpenId(companyId, openId);
            } catch (NoSuchUserException nsue) {
                userbyopenid_exists = false;
            }
        } catch (PortalException | SystemException e) {
            log.error(e);
        }
        if (userbyscreeenname_exists || userbyemail_exists || userbyopenid_exists) {
            String msg_exists = "";
            if (userbyscreeenname_exists) {
                msg_exists = msg_exists + "Full name already exists.";
            }
            if (userbyemail_exists) {
                msg_exists = msg_exists + " Email address already exists.";
            }
            if (userbyopenid_exists) {
                msg_exists = msg_exists + " External id already exists.";
            }
            msg_exists = msg_exists.trim();
            log.info(msg_exists);
            SessionMessages.add(request, "request_processed", msg_exists);
            return null;
        }
        try {
            ServiceContext serviceContext = ServiceContextFactory.getInstance(request);
            user = UserLocalServiceUtil.addUser(creatorUserId,
                    companyId,
                    autoPassword,
                    password1,
                    password2,
                    autoScreenName,
                    screenName,
                    emailAddress,
                    facebookId,
                    openId,
                    locale,
                    firstName,
                    middleName,
                    lastName,
                    prefixId,
                    suffixId,
                    male,
                    birthdayMonth,
                    birthdayDay,
                    birthdayYear,
                    jobTitle,
                    groupIds,
                    organizationIds,
                    roleIds,
                    userGroupIds,
                    sendEmail,
                    serviceContext);
            user.setPasswordReset(false);

            if (passwordEncrypted) {
                user.setPassword(password);
                user.setPasswordEncrypted(true);
            }

            Role role = RoleLocalServiceUtil.getRole(roleId);
            RoleUtil.addUser(role.getRoleId(), user.getUserId());
            UserLocalServiceUtil.updateUser(user);
            RoleLocalServiceUtil.updateRole(role);

            UserLocalServiceUtil.updateStatus(user.getUserId(), activateImmediately ? WorkflowConstants.STATUS_APPROVED : WorkflowConstants.STATUS_INACTIVE);
            Indexer indexer = IndexerRegistryUtil.getIndexer(User.class);

            indexer.reindex(user);
        } catch (PortalException | SystemException e) {
            log.error(e);
        }
        return user;
    }

    public static User addLiferayUser(PortletRequest request, String firstName, String lastName, String emailAddress, String organizationName, String roleName, boolean male, String openId, String password, boolean passwordEncrypted, boolean activateImmediately) throws SystemException, PortalException {
        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
        long companyId = themeDisplay.getCompanyId();

        long organizationId = OrganizationLocalServiceUtil.getOrganizationId(companyId, organizationName);
        final Role role = RoleLocalServiceUtil.getRole(companyId, roleName);
        long roleId = role.getRoleId();

        return addLiferayUser(request, firstName, lastName, emailAddress, organizationId, roleId, male, openId, password, passwordEncrypted, activateImmediately);
    }
}
