/*
 * Copyright Siemens AG, 2015. Part of the SW360 Portal Project.
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
package com.siemens.sw360.portal.common;

import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.users.User;

import java.io.IOException;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.siemens.sw360.portal.users.UserCacheHolder.getUserFromEmail;

/**
 * @author daniele.fognini@tngtech.com
 */
public class JsonHelpers {
    public static JSONObject getProjectResponsible(ThriftJsonSerializer thriftJsonSerializer, Project project) throws JSONException, IOException {
        JSONObject responsible = JSONFactoryUtil.createJSONObject();
        if (project.isSetProjectResponsible()) {
            String projectResponsible = project.getProjectResponsible();
            if (!isNullOrEmpty(projectResponsible)) {
                User userFromEmail=null;
                try {
                    userFromEmail = getUserFromEmail(projectResponsible);
                } catch(Exception ignored) {
                }
                if(userFromEmail!=null) {
                    responsible = toJson(userFromEmail, thriftJsonSerializer);
                }
            }
        }
        return responsible;
    }

    public static JSONObject toJson(Object thriftObject, ThriftJsonSerializer thriftJsonSerializer) throws JSONException, IOException {
        return JSONFactoryUtil.createJSONObject(thriftJsonSerializer.toJson(thriftObject));
    }
}
