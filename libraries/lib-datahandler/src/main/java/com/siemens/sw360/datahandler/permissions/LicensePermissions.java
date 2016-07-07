/*
 * Copyright Siemens AG, 2014-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.datahandler.permissions;

import com.siemens.sw360.datahandler.thrift.licenses.License;
import com.siemens.sw360.datahandler.thrift.users.RequestedAction;
import com.siemens.sw360.datahandler.thrift.users.User;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.siemens.sw360.datahandler.thrift.users.UserGroup.CLEARING_ADMIN;

/**
 * Created by bodet on 16/02/15.
 *
 * @author cedric.bodet@tngtech.com
 */
public class LicensePermissions extends DocumentPermissions<License> {


    protected LicensePermissions(License document, User user) {
        super(document, user);
    }

    @Override
    public void fillPermissions(License other, Map<RequestedAction, Boolean> permissions) {
        other.permissions = permissions;
    }

    @Override
    public boolean isActionAllowed(RequestedAction action) {
        switch (action) {
            case READ:
                return true;
            case WRITE:
            case DELETE:
                return PermissionUtils.isUserAtLeast(CLEARING_ADMIN, user);
            default:
                return false;
        }
    }

    @Override
    protected Set<String> getContributors() {
        return Collections.emptySet();
    }

    @Override
    protected Set<String> getModerators() {
        return Collections.emptySet();
    }

}
