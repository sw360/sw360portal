/*
 * Copyright Siemens AG, 2014-2015. Part of the SW360 Portal Project.
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
package com.siemens.sw360.datahandler.permissions;

import com.siemens.sw360.datahandler.thrift.components.Component;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.licenses.License;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.thrift.users.UserGroup;
import com.siemens.sw360.datahandler.thrift.vendors.Vendor;

/**
 * Created by bodet on 16/02/15.
 *
 * @author cedric.bodet@tngtech.com
 */
public class PermissionUtils {

    public static boolean isNormalUser(User user) {
        return isInGroup(user, UserGroup.USER);
    }

    public static boolean isAdmin(User user) {
        return isInGroup(user, UserGroup.ADMIN);
    }

    public static boolean isClearingAdmin(User user) {
        return isInGroup(user, UserGroup.CLEARING_ADMIN);
    }

    private static boolean isInGroup(User user, UserGroup userGroup) {
        return user != null && user.isSetUserGroup() && user.getUserGroup() == userGroup;
    }

    public static boolean isUserAtLeast(UserGroup group, User user) {
        switch (group) {
            case USER:
                return isNormalUser(user) || isClearingAdmin(user) || isAdmin(user);
            case CLEARING_ADMIN:
                return isClearingAdmin(user) || isAdmin(user);
            case ADMIN:
                return isAdmin(user);
            default:
                throw new IllegalArgumentException("Unknown group: " + group);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> DocumentPermissions<T> makePermission(T document, User user) {
        if (document instanceof License) {
            return (DocumentPermissions<T>) new LicensePermissions((License) document, user);
        } else if (document instanceof Component) {
            return (DocumentPermissions<T>) new ComponentPermissions((Component) document, user);
        } else if (document instanceof Release) {
            return (DocumentPermissions<T>) new ReleasePermissions((Release) document, user);
        } else if (document instanceof Project) {
            return (DocumentPermissions<T>) new ProjectPermissions((Project) document, user);
        } else if (document instanceof Vendor) {
            return (DocumentPermissions<T>) new VendorPermissions((Vendor) document, user);
        } else if (document instanceof User) {
            return (DocumentPermissions<T>) new UserPermissions((User) document, user);
        } else {
            throw new IllegalArgumentException("Invalid input type!");
        }
    }

}
