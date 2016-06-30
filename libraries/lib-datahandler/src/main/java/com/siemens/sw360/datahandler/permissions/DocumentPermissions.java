/*
 * Copyright Siemens AG, 2014-2016. Part of the SW360 Portal Project.
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

import com.google.common.collect.ImmutableSet;
import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.thrift.users.RequestedAction;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.thrift.users.UserGroup;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import static com.siemens.sw360.datahandler.thrift.users.RequestedAction.*;

/**
 * Created by bodet on 16/02/15.
 *
 * @author cedric.bodet@tngtech.com
 */
public abstract class DocumentPermissions<T> {

    protected final T document;
    protected final User user;

    protected DocumentPermissions(T document, User user) {
        this.document = document;
        this.user = user;
    }

    public abstract void fillPermissions(T other, Map<RequestedAction, Boolean> permissions);

    public abstract boolean isActionAllowed(RequestedAction action);

    protected abstract Set<String> getContributors();

    protected abstract Set<String> getModerators();

    protected boolean isContributor() {
        return user != null && CommonUtils.contains(user.email, getContributors());
    }

    protected boolean isModerator() {
        return CommonUtils.contains(user.email, getModerators());
    }

    public void fillPermissions() {
        fillPermissions(document, getPermissionMap());
    }

    public void fillPermissionsInOther(T other) {
        fillPermissions(other, getPermissionMap());
    }

    public Map<RequestedAction, Boolean> getPermissionMap() {
        Map<RequestedAction, Boolean> permissions = new EnumMap<>(RequestedAction.class);
        for (RequestedAction action : values()) {
            permissions.put(action, isActionAllowed(action));
        }
        return permissions;
    }

    protected boolean getStandardPermissions(RequestedAction action) {
        switch (action) {
            case READ:
                return true;
            case WRITE:
            case ATTACHMENTS:
                return PermissionUtils.isUserAtLeast(UserGroup.CLEARING_ADMIN, user) || isContributor() ;
            case DELETE:
            case USERS:
            case CLEARING:
                return PermissionUtils.isAdmin(user) || isModerator();
            default:
                throw new IllegalArgumentException("Unknown action: " + action);
        }
    }

    // useful for tests, maybe this needs to go somewhere else
    public RequestedAction getHighestAllowedPermission (){
        RequestedAction out = null;
        boolean isAllowed = true;

        for (RequestedAction requestedAction : ImmutableSet.of(READ, WRITE, ATTACHMENTS, DELETE, USERS, CLEARING)) {
            isAllowed  = getStandardPermissions(requestedAction);
            if(!isAllowed) return out;
            out=requestedAction;
        }

        return out;
    }

}
