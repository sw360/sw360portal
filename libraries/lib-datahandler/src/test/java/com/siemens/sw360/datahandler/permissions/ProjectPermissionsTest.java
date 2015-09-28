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
package com.siemens.sw360.datahandler.permissions;

import com.siemens.sw360.datahandler.permissions.jgivens.GivenProject;
import com.siemens.sw360.datahandler.permissions.jgivens.ThenHighestAllowedAction;
import com.siemens.sw360.datahandler.permissions.jgivens.WhenComputePermissions;
import com.siemens.sw360.datahandler.thrift.users.RequestedAction;
import com.siemens.sw360.datahandler.thrift.users.UserGroup;
import com.tngtech.jgiven.junit.ScenarioTest;
import org.junit.Test;

/**
 * @author johannes.najjar@tngtech.com
 */
public class ProjectPermissionsTest extends ScenarioTest<GivenProject, WhenComputePermissions, ThenHighestAllowedAction> {

    public static String theUser = "user1";
    public static String theOtherUser = "anotherUser";

    @Test
    public void test000() throws Exception {
        given().a_project_created_by_$(theUser);
        when().the_highest_allowed_action_is_computed_for_user_$_with_user_group_$(theUser,  UserGroup.USER);
        then().the_highest_allowed_action_should_be(RequestedAction.CLEARING);
    }


    //TODO write much more tests
}