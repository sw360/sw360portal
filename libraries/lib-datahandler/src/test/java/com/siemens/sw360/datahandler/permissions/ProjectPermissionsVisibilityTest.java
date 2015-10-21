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

import com.siemens.sw360.datahandler.permissions.jgivens.GivenProjectsWithVisibility;
import com.siemens.sw360.datahandler.permissions.jgivens.ThenVisible;
import com.siemens.sw360.datahandler.permissions.jgivens.WhenComputeVisibility;
import com.siemens.sw360.datahandler.thrift.Visibility;
import com.siemens.sw360.datahandler.thrift.users.UserGroup;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import com.tngtech.jgiven.junit.ScenarioTest;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.siemens.sw360.datahandler.thrift.Visibility.*;
import static com.siemens.sw360.datahandler.thrift.users.UserGroup.*;

/**
 * @author johannes.najjar@tngtech.com
 */
@RunWith(DataProviderRunner.class)
public class ProjectPermissionsVisibilityTest extends ScenarioTest<GivenProjectsWithVisibility, WhenComputeVisibility, ThenVisible> {

    public static String theBu = "DE PA RT"; // ME NT
    public static String theDep = "DE PA RT ME NT";
    public static String theOtherDep = "OT TH ER DE";
    public static String theUser = "user1";
    public static String theOtherUser = "anotherUser";

    /**
     * The testing strategy for visibility is as follows:
     * It depends on the UserGroup and Department as well as the user Roles, in the first tests we verify them.
     * We can override a no from these criteria by being in one of the moderator classes. This is the next test block.
     */


    @DataProvider
    public static Object[][] projectVisibilityProvider() {
        // @formatter:off
        return new Object[][] {
                //test otherDeparment
                //test User
                { PRIVATE, theBu, theOtherDep, USER, false },
                { ME_AND_MODERATORS, theBu, theOtherDep, USER, false },
                { BUISNESSUNIT_AND_MODERATORS, theBu, theOtherDep, USER, false },
                { EVERYONE, theBu, theOtherDep, USER, true },
                //test Clearing Admin
                { PRIVATE, theBu, theOtherDep, CLEARING_ADMIN, false },
                { ME_AND_MODERATORS, theBu, theOtherDep, CLEARING_ADMIN, false },
                { BUISNESSUNIT_AND_MODERATORS, theBu, theOtherDep, CLEARING_ADMIN, true },
                { EVERYONE, theBu, theOtherDep, CLEARING_ADMIN, true },
                //test  Admin
                { PRIVATE, theBu, theOtherDep, ADMIN, false },
                { ME_AND_MODERATORS, theBu, theOtherDep, ADMIN, false },
                { BUISNESSUNIT_AND_MODERATORS, theBu, theOtherDep, ADMIN, true },
                { EVERYONE, theBu, theOtherDep, ADMIN, true },
                //test same department
                                //test User
                { PRIVATE, theBu, theDep, USER, false },
                { ME_AND_MODERATORS, theBu, theDep, USER, false },
                { BUISNESSUNIT_AND_MODERATORS, theBu, theDep, USER, true },
                { EVERYONE, theBu, theDep, USER, true },
                //test Clearing Admin
                { PRIVATE, theBu, theDep, CLEARING_ADMIN, false },
                { ME_AND_MODERATORS, theBu, theDep, CLEARING_ADMIN, false },
                { BUISNESSUNIT_AND_MODERATORS, theBu, theDep, CLEARING_ADMIN, true },
                { EVERYONE, theBu, theDep, CLEARING_ADMIN, true },
                //test  Admin
                { PRIVATE, theBu, theDep, ADMIN, false },
                { ME_AND_MODERATORS, theBu, theDep, ADMIN, false },
                { BUISNESSUNIT_AND_MODERATORS, theBu, theDep, ADMIN, true },
                { EVERYONE, theBu, theDep, ADMIN, true },
        };
        // @formatter:on
    }

    @Test
    @UseDataProvider("projectVisibilityProvider")
    public void testVisibility(Visibility visibility, String businessUnit, String department, UserGroup userGroup, boolean expectedVisibility) {
        given().a_project_with_visibility_$_and_business_unit_$(visibility, businessUnit);
        when().the_visibility_is_computed_for_department_$_and_user_group_$(department, userGroup);
        then().the_visibility_should_be(expectedVisibility);

    }

    /**
     * Here are the tests for moderator equivalence
     */
    @DataProvider
    public static Object[][] projectVisibilityCreatedByProvider() {
        // @formatter:off
        return new Object[][] {
                //test otherDeparment
                //test same User
                { PRIVATE, theBu, theUser, theUser, true },
                { ME_AND_MODERATORS, theBu, theUser, theUser, true },
                { BUISNESSUNIT_AND_MODERATORS, theBu, theUser, theUser, true },
                { EVERYONE, theBu, theUser, theUser, true },
                //test different User
                { PRIVATE, theBu, theUser, theOtherUser, false },
                { ME_AND_MODERATORS, theBu, theUser, theOtherUser, false },
                { BUISNESSUNIT_AND_MODERATORS, theBu, theUser, theOtherUser, false },
                { EVERYONE, theBu, theUser, theOtherUser, true },

        };
        // @formatter:on
    }
    @Test
    @UseDataProvider("projectVisibilityCreatedByProvider")
    public void testVisibilityForProjectCreatedBy(Visibility visibility, String bu, String creatingUser, String viewingUser, boolean expectedVisibility) throws Exception {
        given().a_project_with_visibility_$_and_business_unit_$_created_by_$(visibility, bu, creatingUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(viewingUser);
        then().the_visibility_should_be(expectedVisibility);
    }

    @DataProvider
    public static Object[][] projectVisibilityLeadArchitectProvider() {
        // @formatter:off
        return new Object[][] {
                //test otherDeparment
                //test same User
                { PRIVATE, theBu, theUser, theUser, false },
                { ME_AND_MODERATORS, theBu, theUser, theUser, true },
                { BUISNESSUNIT_AND_MODERATORS, theBu, theUser, theUser, true },
                { EVERYONE, theBu, theUser, theUser, true },
                //test different User
                { PRIVATE, theBu, theUser, theOtherUser, false },
                { ME_AND_MODERATORS, theBu, theUser, theOtherUser, false },
                { BUISNESSUNIT_AND_MODERATORS, theBu, theUser, theOtherUser, false },
                { EVERYONE, theBu, theUser, theOtherUser, true },

        };
        // @formatter:on
    }
    @Test
    @UseDataProvider("projectVisibilityLeadArchitectProvider")
    public void testVisibilityForProjectLeadArchitect(Visibility visibility, String bu, String creatingUser, String viewingUser, boolean expectedVisibility) throws Exception {
        given().a_project_with_visibility_$_and_business_unit_$_with_lead_architect_$(visibility, bu, creatingUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(viewingUser);
        then().the_visibility_should_be(expectedVisibility);
    }

    @DataProvider
    public static Object[][] projectVisibilityModeratorProvider() {
        // @formatter:off
        return new Object[][] {
                //test same User
                { PRIVATE, theBu, theUser, theUser, false },
                { ME_AND_MODERATORS, theBu, theUser, theUser, true },
                { BUISNESSUNIT_AND_MODERATORS, theBu, theUser, theUser, true },
                { EVERYONE, theBu, theUser, theUser, true },
                //test different User
                { PRIVATE, theBu, theUser, theOtherUser, false },
                { ME_AND_MODERATORS, theBu, theUser, theOtherUser, false },
                { BUISNESSUNIT_AND_MODERATORS, theBu, theUser, theOtherUser, false },
                { EVERYONE, theBu, theUser, theOtherUser, true },

        };
        // @formatter:on
    }
    @Test
    @UseDataProvider("projectVisibilityModeratorProvider")
    public void testVisibilityForProjectModerator(Visibility visibility, String bu, String creatingUser, String viewingUser, boolean expectedVisibility) throws Exception {
        given().a_project_with_visibility_$_and_business_unit_$_and_moderator_$(visibility, bu, creatingUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(viewingUser);
        then().the_visibility_should_be(expectedVisibility);
    }


    @DataProvider
    public static Object[][] projectVisibilityCoModeratorProvider() {
        // @formatter:off
        return new Object[][] {
                //test same User
                { PRIVATE, theBu, theUser, theUser, false },
                { ME_AND_MODERATORS, theBu, theUser, theUser, true },
                { BUISNESSUNIT_AND_MODERATORS, theBu, theUser, theUser, true },
                { EVERYONE, theBu, theUser, theUser, true },
                //test different User
                { PRIVATE, theBu, theUser, theOtherUser, false },
                { ME_AND_MODERATORS, theBu, theUser, theOtherUser, false },
                { BUISNESSUNIT_AND_MODERATORS, theBu, theUser, theOtherUser, false },
                { EVERYONE, theBu, theUser, theOtherUser, true },

        };
        // @formatter:on
    }
    @Test
    @UseDataProvider("projectVisibilityCoModeratorProvider")
    public void testVisibilityForProjectCoModerator(Visibility visibility, String bu, String creatingUser, String viewingUser, boolean expectedVisibility) throws Exception {
        given().a_project_with_visibility_$_and_business_unit_$_and_comoderator_$(visibility, bu, creatingUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(viewingUser);
        then().the_visibility_should_be(expectedVisibility);
    }


    @DataProvider
    public static Object[][] projectVisibilityContributorProvider() {
        // @formatter:off
        return new Object[][] {
                //test same User
                { PRIVATE, theBu, theUser, theUser, false },
                { ME_AND_MODERATORS, theBu, theUser, theUser, true },
                { BUISNESSUNIT_AND_MODERATORS, theBu, theUser, theUser, true },
                { EVERYONE, theBu, theUser, theUser, true },
                //test different User
                { PRIVATE, theBu, theUser, theOtherUser, false },
                { ME_AND_MODERATORS, theBu, theUser, theOtherUser, false },
                { BUISNESSUNIT_AND_MODERATORS, theBu, theUser, theOtherUser, false },
                { EVERYONE, theBu, theUser, theOtherUser, true },

        };
        // @formatter:on
    }
    @Test
    @UseDataProvider("projectVisibilityContributorProvider")
    public void testVisibilityForProjectContributor(Visibility visibility, String bu, String creatingUser, String viewingUser, boolean expectedVisibility) throws Exception {
        given().a_project_with_visibility_$_and_business_unit_$_and_contributor_$(visibility, bu, creatingUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(viewingUser);
        then().the_visibility_should_be(expectedVisibility);
    }


    @DataProvider
    public static Object[][] projectVisibilityResponsibleProvider() {
        // @formatter:off
        return new Object[][] {
                //test same User
                { PRIVATE, theBu, theUser, theUser, false },
                { ME_AND_MODERATORS, theBu, theUser, theUser, true },
                { BUISNESSUNIT_AND_MODERATORS, theBu, theUser, theUser, true },
                { EVERYONE, theBu, theUser, theUser, true },
                //test different User
                { PRIVATE, theBu, theUser, theOtherUser, false },
                { ME_AND_MODERATORS, theBu, theUser, theOtherUser, false },
                { BUISNESSUNIT_AND_MODERATORS, theBu, theUser, theOtherUser, false },
                { EVERYONE, theBu, theUser, theOtherUser, true },

        };
        // @formatter:on
    }
    @Test
    @UseDataProvider("projectVisibilityResponsibleProvider")
    public void testVisibilityForProjectResponsible(Visibility visibility, String bu, String creatingUser, String viewingUser, boolean expectedVisibility) throws Exception {
        given().a_project_with_visibility_$_and_business_unit_$_and_project_responsible_$(visibility, bu, creatingUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(viewingUser);
        then().the_visibility_should_be(expectedVisibility);
    }
}