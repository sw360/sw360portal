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

import com.liferay.portal.kernel.events.Action;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.common.SW360Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author johannes.najjar@tngtech.com
 */
public class LoginAction extends Action {

    private static final Logger log = LoggerFactory.getLogger(LoginAction.class);

    @Override
    public void run(HttpServletRequest request, HttpServletResponse response) {
        try {
            long userId = getLiferayUserId(request);
            com.liferay.portal.model.User user = UserLocalServiceUtil.getUserById(userId);
            UserUtils userUtils = new UserUtils();
            userUtils.synchronizeUserWithDatabase(user);
        } catch (Exception  e) {
            log.error("Problem with user ", e);
        }
    }

    private static long getLiferayUserId(HttpServletRequest request) {
        long userId = -1;

        Object fromWebKey = request.getAttribute(WebKeys.USER_ID);
        if (fromWebKey != null && fromWebKey instanceof Long) {
            userId = (Long) fromWebKey;
        }

        if (userId <= 0) {
            userId = CommonUtils.toUnsignedInt(request.getRemoteUser());
        }

        return userId;
    }
}
