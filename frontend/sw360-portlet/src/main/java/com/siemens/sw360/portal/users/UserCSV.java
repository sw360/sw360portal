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
package com.siemens.sw360.portal.users;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.siemens.sw360.portal.portlets.admin.UserPortlet;
import org.apache.commons.csv.CSVRecord;

import javax.portlet.PortletRequest;

import static com.siemens.sw360.portal.users.UserUtils.getRoleConstantFromUserGroup;
import static com.siemens.sw360.portal.users.UserUtils.userGroupFromString;

/**
 * Created by heydenrb on 01.03.16.
 *
 * @author birgit.heydenreich@tngtech.com
 */
public class UserCSV {

    private String givenname;
    private String lastname;
    private String email;
    private String department;
    private String group;
    private String gid;
    private boolean isMale;
    private String hash;
    private boolean wantsMailNotification = true;

    public String getGivenname(){
        return givenname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getEmail() {
        return email;
    }

    public String getDepartment() {
        return department;
    }

    public String getGroup() {
        return group;
    }

    public String getGid() {
        return gid;
    }

    public boolean wantsMailNotification() {
        return wantsMailNotification;
    }

    public UserCSV(CSVRecord record) {
        givenname = record.get(0);
        lastname = record.get(1);
        email = record.get(2);
        department = record.get(3);
        group = record.get(4);
        gid = record.get(5);
        isMale = Boolean.parseBoolean(record.get(6));
        hash = record.get(7);
        if (record.size() > 8) {
            wantsMailNotification = Boolean.parseBoolean((record.get(8)));
        }
    }

    public User addLifeRayUser(PortletRequest request) throws PortalException, SystemException {
        return UserPortlet.addLiferayUser(request, givenname, lastname, email,
                department, getRoleConstantFromUserGroup(userGroupFromString(group)), isMale, gid, hash, true, true);

    }

}
