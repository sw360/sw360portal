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
import com.tngtech.jgiven.junit.ScenarioTest;
import org.junit.Test;

/**
 * @author johannes.najjar@tngtech.com
 */
public class ProjectPermissionsVisibilityTest extends ScenarioTest<GivenProjectsWithVisibility, WhenComputeVisibility, ThenVisible> {

    public static String theBu  = "DE PA RT"; // ME NT
    public static String theDep  = "DE PA RT ME NT";
    public static String theOtherDep  = "OT TH ER DE";
    public static String theUser = "user1";
    public static String theOtherUser = "anotherUser";

    /**
     *       The testing strategy for visibility is as follows:
     *       It depends on the UserGroup and Department as well as the user Roles, in the first tests we verify them.
     *       We can override a no from these criteria by beeing in one of the moderator classes. This is the next test block.
     */
    
    @Test
    public void test000() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$(Visibility.PRIVATE, theBu);
        when().the_visibility_is_computed_for_department_$_and_user_group_$(theOtherDep, UserGroup.USER);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test001() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$(Visibility.ME_AND_MODERATORS, theBu);
        when().the_visibility_is_computed_for_department_$_and_user_group_$(theOtherDep, UserGroup.USER);
        then().the_visibility_should_be(false);
    }
    
    @Test
    public void test002() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$(Visibility.BUISNESSUNIT_AND_MODERATORS, theBu);
        when().the_visibility_is_computed_for_department_$_and_user_group_$(theOtherDep, UserGroup.USER);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test003() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$(Visibility.EVERYONE, theBu);
        when().the_visibility_is_computed_for_department_$_and_user_group_$(theOtherDep, UserGroup.USER);
        then().the_visibility_should_be(true);
    }

    @Test
    public void test010() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$(Visibility.PRIVATE, theBu);
        when().the_visibility_is_computed_for_department_$_and_user_group_$(theOtherDep, UserGroup.CLEARING_ADMIN);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test011() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$(Visibility.ME_AND_MODERATORS, theBu);
        when().the_visibility_is_computed_for_department_$_and_user_group_$(theOtherDep, UserGroup.CLEARING_ADMIN);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test012() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$(Visibility.BUISNESSUNIT_AND_MODERATORS, theBu);
        when().the_visibility_is_computed_for_department_$_and_user_group_$(theOtherDep, UserGroup.CLEARING_ADMIN);
        then().the_visibility_should_be(true);
    }

    @Test
    public void test013() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$(Visibility.EVERYONE, theBu);
        when().the_visibility_is_computed_for_department_$_and_user_group_$(theOtherDep, UserGroup.CLEARING_ADMIN);
        then().the_visibility_should_be(true);
    }


    @Test
    public void test020() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$(Visibility.PRIVATE, theBu);
        when().the_visibility_is_computed_for_department_$_and_user_group_$(theOtherDep, UserGroup.ADMIN);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test021() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$(Visibility.ME_AND_MODERATORS, theBu);
        when().the_visibility_is_computed_for_department_$_and_user_group_$(theOtherDep, UserGroup.ADMIN);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test022() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$(Visibility.BUISNESSUNIT_AND_MODERATORS, theBu);
        when().the_visibility_is_computed_for_department_$_and_user_group_$(theOtherDep, UserGroup.ADMIN);
        then().the_visibility_should_be(true);
    }

    @Test
    public void test023() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$(Visibility.EVERYONE, theBu);
        when().the_visibility_is_computed_for_department_$_and_user_group_$(theOtherDep, UserGroup.ADMIN);
        then().the_visibility_should_be(true);
    }

    @Test
    public void test100() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$(Visibility.PRIVATE, theBu);
        when().the_visibility_is_computed_for_department_$_and_user_group_$(theDep, UserGroup.USER);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test101() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$(Visibility.ME_AND_MODERATORS, theBu);
        when().the_visibility_is_computed_for_department_$_and_user_group_$(theDep, UserGroup.USER);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test102() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$(Visibility.BUISNESSUNIT_AND_MODERATORS, theBu);
        when().the_visibility_is_computed_for_department_$_and_user_group_$(theDep, UserGroup.USER);
        then().the_visibility_should_be(true);
    }

    @Test
    public void test103() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$(Visibility.EVERYONE, theBu);
        when().the_visibility_is_computed_for_department_$_and_user_group_$(theDep, UserGroup.USER);
        then().the_visibility_should_be(true);
    }

    @Test
    public void test110() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$(Visibility.PRIVATE, theBu);
        when().the_visibility_is_computed_for_department_$_and_user_group_$(theDep, UserGroup.CLEARING_ADMIN);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test111() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$(Visibility.ME_AND_MODERATORS, theBu);
        when().the_visibility_is_computed_for_department_$_and_user_group_$(theDep, UserGroup.CLEARING_ADMIN);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test112() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$(Visibility.BUISNESSUNIT_AND_MODERATORS, theBu);
        when().the_visibility_is_computed_for_department_$_and_user_group_$(theDep, UserGroup.CLEARING_ADMIN);
        then().the_visibility_should_be(true);
    }

    @Test
    public void test113() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$(Visibility.EVERYONE, theBu);
        when().the_visibility_is_computed_for_department_$_and_user_group_$(theDep, UserGroup.CLEARING_ADMIN);
        then().the_visibility_should_be(true);
    }

    @Test
    public void test120() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$(Visibility.PRIVATE, theBu);
        when().the_visibility_is_computed_for_department_$_and_user_group_$(theDep, UserGroup.ADMIN);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test121() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$(Visibility.ME_AND_MODERATORS, theBu);
        when().the_visibility_is_computed_for_department_$_and_user_group_$(theDep, UserGroup.ADMIN);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test122() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$(Visibility.BUISNESSUNIT_AND_MODERATORS, theBu);
        when().the_visibility_is_computed_for_department_$_and_user_group_$(theDep, UserGroup.ADMIN);
        then().the_visibility_should_be(true);
    }

    @Test
    public void test123() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$(Visibility.EVERYONE, theBu);
        when().the_visibility_is_computed_for_department_$_and_user_group_$(theDep, UserGroup.ADMIN);
        then().the_visibility_should_be(true);
    }

    /**
     *     Here are the tests for moderator equivalence 
     */


    @Test
    public void test200() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_created_by_$(Visibility.PRIVATE, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theUser);
        then().the_visibility_should_be(true);
    }
    
    @Test
    public void test201() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_created_by_$(Visibility.ME_AND_MODERATORS, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theUser);
        then().the_visibility_should_be(true);
    }

    @Test
    public void test202() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_created_by_$(Visibility.BUISNESSUNIT_AND_MODERATORS, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theUser);
        then().the_visibility_should_be(true);
    }

    @Test
      public void test203() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_created_by_$(Visibility.EVERYONE, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theUser);
        then().the_visibility_should_be(true);
    }
    
    @Test
    public void test204() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_created_by_$(Visibility.PRIVATE, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theOtherUser);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test205() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_created_by_$(Visibility.ME_AND_MODERATORS, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theOtherUser);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test206() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_created_by_$(Visibility.BUISNESSUNIT_AND_MODERATORS, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theOtherUser);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test207() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_created_by_$(Visibility.EVERYONE, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theOtherUser);
        then().the_visibility_should_be(true);
    }




    @Test
    public void test210() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_with_lead_architect_$(Visibility.PRIVATE, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theUser);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test211() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_with_lead_architect_$(Visibility.ME_AND_MODERATORS, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theUser);
        then().the_visibility_should_be(true);
    }

    @Test
    public void test212() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_with_lead_architect_$(Visibility.BUISNESSUNIT_AND_MODERATORS, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theUser);
        then().the_visibility_should_be(true);
    }

    @Test
    public void test213() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_with_lead_architect_$(Visibility.EVERYONE, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theUser);
        then().the_visibility_should_be(true);
    }

    @Test
    public void test214() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_with_lead_architect_$(Visibility.PRIVATE, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theOtherUser);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test215() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_with_lead_architect_$(Visibility.ME_AND_MODERATORS, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theOtherUser);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test216() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_with_lead_architect_$(Visibility.BUISNESSUNIT_AND_MODERATORS, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theOtherUser);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test217() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_with_lead_architect_$(Visibility.EVERYONE, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theOtherUser);
        then().the_visibility_should_be(true);
    }



    @Test
    public void test220() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_and_moderator_$(Visibility.PRIVATE, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theUser);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test221() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_and_moderator_$(Visibility.ME_AND_MODERATORS, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theUser);
        then().the_visibility_should_be(true);
    }

    @Test
    public void test222() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_and_moderator_$(Visibility.BUISNESSUNIT_AND_MODERATORS, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theUser);
        then().the_visibility_should_be(true);
    }

    @Test
    public void test223() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_and_moderator_$(Visibility.EVERYONE, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theUser);
        then().the_visibility_should_be(true);
    }

    @Test
    public void test224() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_and_moderator_$(Visibility.PRIVATE, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theOtherUser);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test225() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_and_moderator_$(Visibility.ME_AND_MODERATORS, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theOtherUser);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test226() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_and_moderator_$(Visibility.BUISNESSUNIT_AND_MODERATORS, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theOtherUser);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test227() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_and_moderator_$(Visibility.EVERYONE, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theOtherUser);
        then().the_visibility_should_be(true);
    }



    @Test
    public void test230() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_and_comoderator_$(Visibility.PRIVATE, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theUser);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test231() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_and_comoderator_$(Visibility.ME_AND_MODERATORS, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theUser);
        then().the_visibility_should_be(true);
    }

    @Test
    public void test232() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_and_comoderator_$(Visibility.BUISNESSUNIT_AND_MODERATORS, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theUser);
        then().the_visibility_should_be(true);
    }

    @Test
    public void test233() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_and_comoderator_$(Visibility.EVERYONE, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theUser);
        then().the_visibility_should_be(true);
    }

    @Test
    public void test234() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_and_comoderator_$(Visibility.PRIVATE, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theOtherUser);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test235() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_and_comoderator_$(Visibility.ME_AND_MODERATORS, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theOtherUser);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test236() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_and_comoderator_$(Visibility.BUISNESSUNIT_AND_MODERATORS, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theOtherUser);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test237() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_and_comoderator_$(Visibility.EVERYONE, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theOtherUser);
        then().the_visibility_should_be(true);
    }


    @Test
    public void test240() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_and_contributor_$(Visibility.PRIVATE, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theUser);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test241() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_and_contributor_$(Visibility.ME_AND_MODERATORS, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theUser);
        then().the_visibility_should_be(true);
    }

    @Test
    public void test242() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_and_contributor_$(Visibility.BUISNESSUNIT_AND_MODERATORS, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theUser);
        then().the_visibility_should_be(true);
    }

    @Test
    public void test243() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_and_contributor_$(Visibility.EVERYONE, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theUser);
        then().the_visibility_should_be(true);
    }

    @Test
    public void test244() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_and_contributor_$(Visibility.PRIVATE, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theOtherUser);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test245() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_and_contributor_$(Visibility.ME_AND_MODERATORS, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theOtherUser);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test246() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_and_contributor_$(Visibility.BUISNESSUNIT_AND_MODERATORS, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theOtherUser);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test247() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_and_contributor_$(Visibility.EVERYONE, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theOtherUser);
        then().the_visibility_should_be(true);
    }


    @Test
    public void test250() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_and_project_responsible_$(Visibility.PRIVATE, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theUser);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test251() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_and_project_responsible_$(Visibility.ME_AND_MODERATORS, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theUser);
        then().the_visibility_should_be(true);
    }

    @Test
    public void test252() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_and_project_responsible_$(Visibility.BUISNESSUNIT_AND_MODERATORS, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theUser);
        then().the_visibility_should_be(true);
    }

    @Test
    public void test253() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_and_project_responsible_$(Visibility.EVERYONE, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theUser);
        then().the_visibility_should_be(true);
    }

    @Test
    public void test254() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_and_project_responsible_$(Visibility.PRIVATE, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theOtherUser);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test255() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_and_project_responsible_$(Visibility.ME_AND_MODERATORS, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theOtherUser);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test256() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_and_project_responsible_$(Visibility.BUISNESSUNIT_AND_MODERATORS, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theOtherUser);
        then().the_visibility_should_be(false);
    }

    @Test
    public void test257() throws Exception {
        given().a_project_with_visibility_$_and_buisness_unit_$_and_project_responsible_$(Visibility.EVERYONE, theBu, theUser);
        when().the_visibility_is_computed_for_the_wrong_department_and_the_user_$(theOtherUser);
        then().the_visibility_should_be(true);
    }
    
}