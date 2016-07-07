/*
 * Copyright Siemens AG, 2014-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.datahandler.permissions;

import com.siemens.sw360.datahandler.thrift.components.Component;
import com.siemens.sw360.datahandler.thrift.users.RequestedAction;
import com.siemens.sw360.datahandler.thrift.users.User;

import java.util.Map;
import java.util.Set;

import static com.siemens.sw360.datahandler.common.CommonUtils.toSingletonSet;

/**
 * Created by bodet on 16/02/15.
 *
 * @author cedric.bodet@tngtech.com
 */
public class ComponentPermissions extends DocumentPermissions<Component> {

    private final Set<String> createdBy;

    protected ComponentPermissions(Component document, User user) {
        super(document, user);
        //Should depend on permissions of contained releases
        this.createdBy = toSingletonSet(document.createdBy);
    }

    @Override
    public void fillPermissions(Component other, Map<RequestedAction, Boolean> permissions) {
        other.permissions = permissions;
    }

    @Override
    public boolean isActionAllowed(RequestedAction action) {
        return getStandardPermissions(action);
    }

    @Override
    protected Set<String> getContributors() {
        return createdBy;
    }

    @Override
    protected Set<String> getModerators() {
        return createdBy;
    }

}
