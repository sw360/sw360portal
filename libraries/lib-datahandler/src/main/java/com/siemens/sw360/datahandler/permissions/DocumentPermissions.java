/*
 * Copyright Siemens AG, 2014-2016. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.datahandler.permissions;

import com.google.common.collect.ImmutableSet;
import org.eclipse.sw360.datahandler.common.CommonUtils;
import org.eclipse.sw360.datahandler.thrift.users.RequestedAction;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.datahandler.thrift.users.UserGroup;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import static org.eclipse.sw360.datahandler.thrift.users.RequestedAction.*;

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
