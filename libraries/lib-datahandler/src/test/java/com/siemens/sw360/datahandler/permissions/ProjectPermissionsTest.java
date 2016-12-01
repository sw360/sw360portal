/*
 * Copyright Siemens AG, 2013-2016. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.datahandler.permissions;

import org.eclipse.sw360.datahandler.permissions.jgivens.GivenProject;
import org.eclipse.sw360.datahandler.permissions.jgivens.ThenHighestAllowedAction;
import org.eclipse.sw360.datahandler.permissions.jgivens.WhenComputePermissions;
import org.eclipse.sw360.datahandler.thrift.users.RequestedAction;
import org.eclipse.sw360.datahandler.thrift.users.UserGroup;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import com.tngtech.jgiven.junit.ScenarioTest;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.eclipse.sw360.datahandler.thrift.users.RequestedAction.*;
import static org.eclipse.sw360.datahandler.thrift.users.UserGroup.*;

/**
 * @author johannes.najjar@tngtech.com
 */

@RunWith(DataProviderRunner.class)
public class ProjectPermissionsTest extends ScenarioTest<GivenProject, WhenComputePermissions, ThenHighestAllowedAction> {

    public static String theUser = "user1";
    public static String theOtherUser = "anotherUser";


    /**
     * See
     * org.eclipse.sw360.datahandler.permissions.DocumentPermissions.getHighestAllowedPermission()
     * for relevant cases
     */
    @DataProvider
    public static Object[][] highestAllowedActionProvider() {
        // @formatter:off
        return new Object[][] {
                //own permissions checks
                //very privileged
                {GivenProject.ProjectRole.CREATED_BY, theUser, theUser, USER, CLEARING },
                {GivenProject.ProjectRole.MODERATOR, theUser, theUser, USER, CLEARING },
                {GivenProject.ProjectRole.PROJECT_RESPONSIBLE, theUser, theUser, USER, CLEARING },
                //less privileged
                {GivenProject.ProjectRole.LEAD_ARCHITECT, theUser, theUser, USER, ATTACHMENTS },
                {GivenProject.ProjectRole.CONTRIBUTOR, theUser, theUser, USER, ATTACHMENTS },

                //strangers: rights increase with user group
                {GivenProject.ProjectRole.CREATED_BY, theUser, theOtherUser, USER, READ },
                {GivenProject.ProjectRole.CREATED_BY, theUser, theOtherUser, CLEARING_ADMIN, ATTACHMENTS },
                {GivenProject.ProjectRole.CREATED_BY, theUser, theOtherUser, ADMIN, CLEARING },
        };
        // @formatter:on
    }

    @Test
    @UseDataProvider("highestAllowedActionProvider")
    public void testHighestAllowedAction(GivenProject.ProjectRole role, String user, String requestingUser, UserGroup requestingUserGroup, RequestedAction highestAllowedAction) throws Exception {
        given().a_project_with_$_$(role,user);
        when().the_highest_allowed_action_is_computed_for_user_$_with_user_group_$(requestingUser, requestingUserGroup);
        then().the_highest_allowed_action_should_be(highestAllowedAction);
    }
}