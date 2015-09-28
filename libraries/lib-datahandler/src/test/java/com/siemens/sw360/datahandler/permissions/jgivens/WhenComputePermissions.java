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
package com.siemens.sw360.datahandler.permissions.jgivens;

import com.siemens.sw360.datahandler.TEnumToString;
import com.siemens.sw360.datahandler.permissions.DocumentPermissions;
import com.siemens.sw360.datahandler.permissions.PermissionUtils;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.users.RequestedAction;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.thrift.users.UserGroup;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import org.junit.internal.AssumptionViolatedException;

import java.util.List;

/**
 * @author johannes.najjar@tngtech.com
 */
public class WhenComputePermissions  extends Stage<WhenComputePermissions> {
    @ExpectedScenarioState
    List<Project> projectList;
    @ProvidedScenarioState
    RequestedAction highestAllowedAction;

    private static String DUMMY_ID = "DAU";
    private static String DUMMY_DEP = "definitleyTheWrongDepartment YO HO HO";

    private Project getFirstProject() {
        if (projectList.size() != 1) {
            throw new AssumptionViolatedException("this test can only handle one project, add a 'n-th' release parameter");
        }
        return projectList.get(0);
    }

    public WhenComputePermissions the_highest_allowed_action_is_computed_for_user_$_with_user_group_$(@Quoted String userEmail, @TEnumToString UserGroup userGroup) {
        final User user = new User(DUMMY_ID, userEmail, DUMMY_DEP).setUserGroup(userGroup);

        final DocumentPermissions<Project> projectDocumentPermissions = PermissionUtils.makePermission(getFirstProject(), user);

        highestAllowedAction = projectDocumentPermissions.getHighestAllowedPermission();
        return self();
    }
}
