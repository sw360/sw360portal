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
import com.siemens.sw360.datahandler.permissions.ProjectPermissions;
import com.siemens.sw360.datahandler.thrift.projects.Project;
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
public class WhenComputeVisibility extends Stage<WhenComputeVisibility> {
    @ExpectedScenarioState
    List<Project> projectList;
    @ProvidedScenarioState
    Boolean isVisible;


    private static String DUMMY_ID = "DAU";
    private static String DUMMY_MAIL = "DAU@dau.com";
    private static String DUMMY_DEP = "definitleyTheWrongDepartment YO HO HO";



    public WhenComputeVisibility the_visibility_is_computed_for_department_$_and_user_group_$(@Quoted String department, @TEnumToString UserGroup userGroup) {
        final User user = new User(DUMMY_ID, DUMMY_MAIL, department).setUserGroup(userGroup);

        isVisible = ProjectPermissions.isVisible(user).apply(getFirstProject());
        return self();
    }

    public WhenComputeVisibility the_visibility_is_computed_for_the_wrong_department_and_the_user_$(@Quoted String mail) {
        final User user = new User(DUMMY_ID, mail, DUMMY_DEP).setUserGroup(UserGroup.USER);

        isVisible = ProjectPermissions.isVisible(user).apply(getFirstProject());
        return self();
    }

    private Project getFirstProject() {
        if (projectList.size() != 1) {
            throw new AssumptionViolatedException("this test can only handle one project, add a 'n-th' release parameter");
        }
        return projectList.get(0);
    }

}
