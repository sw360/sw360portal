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

import com.siemens.sw360.datahandler.thrift.attachments.Attachment;
import com.siemens.sw360.datahandler.thrift.components.Component;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.portal.common.PortletUtils;

import javax.portlet.PortletRequest;
import java.util.HashSet;

/**
 * Signup portlet utils
 *
 * @author alex.borodin@evosoft.com
 */

public abstract class SignupPortletUtils {
    private SignupPortletUtils() {
        // Utility class with only static functions
    }

    static void updateUserFromRequest(PortletRequest request, User user) {
        for (User._Fields field : User._Fields.values()) {
            switch (field) {
                default:
                    setFieldValue(request, user, field);
            }
        }
    }

    private static void setFieldValue(PortletRequest request, User user , User._Fields field) {
        PortletUtils.setFieldValue(request, user, field, User.metaDataMap.get(field), "");
    }

}
