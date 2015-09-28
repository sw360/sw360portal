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

import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.theme.ThemeDisplay;

import javax.portlet.PortletRequest;

/**
 * Class to manage user sessions using Liferay's logged-in user
 *
 * @author cedric.bodet@tngtech.com
 */
public class LifeRayUserSession {
    /**
     * Get the email of the currently logged-in user
     *
     * @param request Java portlet render request
     */
    public static String getEmailFromRequest(PortletRequest request) {
        String email = null;

        // Logged-in user can be fetched from Liferay's ThemeDisplay
        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
        if (themeDisplay.isSignedIn()) {
            com.liferay.portal.model.User user = themeDisplay.getUser();

            // Get email address from user
            if (user != null) {
                email = user.getEmailAddress();
            }
        }
        return email;
    }
}
