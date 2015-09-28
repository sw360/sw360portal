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
package com.siemens.sw360.portal.users;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portal.security.auth.AutoLogin;
import com.liferay.portal.security.auth.AutoLoginException;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Stub for AutoLogin using Siemens' Single-Sign-On
 *
 * @author cedric.bodet@tngtech.com
 */
public class TestAutoLogin implements AutoLogin {

    private static final Logger log = LoggerFactory.getLogger(TestAutoLogin.class);

    @Override
    public String[] handleException(HttpServletRequest request, HttpServletResponse response, Exception e) throws AutoLoginException {
        log.error("System exception.", e);
        return new String[]{};
    }

    @Override
    public String[] login(HttpServletRequest request, HttpServletResponse response) throws AutoLoginException {
        long companyId = PortalUtil.getCompanyId(request);
        final String emailId = "cedric.bodet@tngtech.com";

        User user = null;
        try {
            user = UserLocalServiceUtil.getUserByEmailAddress(companyId, emailId);
        } catch (SystemException e) {
            log.error("System exception.", e);
        } catch (PortalException e) {
            log.error("Portal exception.", e);
        }

        // If user was found by liferay
        if (user != null) {
            // Create a return credentials object
            return new String[]{
                    String.valueOf(user.getUserId()),
                    user.getPassword(), // Encrypted Liferay password
                    Boolean.TRUE.toString()  // True: password is encrypted
            };
        } else {
            log.error("Could not fetch user " + emailId + ".");
            return new String[]{};
        }
    }
}
