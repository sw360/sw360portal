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

package com.siemens.sw360.datahandler.businessrules;

import com.siemens.sw360.datahandler.businessrules.jgivens.GivenReleasesWithFossologyStatus;
import com.siemens.sw360.datahandler.businessrules.jgivens.ThenReleaseClearingState;
import com.siemens.sw360.datahandler.businessrules.jgivens.WhenComputeClearingState;
import com.siemens.sw360.datahandler.thrift.components.ClearingState;
import com.siemens.sw360.datahandler.thrift.components.FossologyStatus;
import com.tngtech.jgiven.junit.ScenarioTest;
import org.junit.Test;

/**
 * @author daniele.fognini@tngtech.com
 */
public class ReleaseClearingStateSummaryComputerTest extends ScenarioTest<GivenReleasesWithFossologyStatus, WhenComputeClearingState, ThenReleaseClearingState> {

    public static final String CLEARING_TEAM = "the project clearing team";
    public static final String ANOTHER_CLEARING_TEAM = "another clearing team";

    @Test
    public void test0() throws Exception {
        given()
                .a_new_release()
                .and()
                .a_new_release();

        when().the_clearing_state_is_computed_for(CLEARING_TEAM);

        then()
                .new_releases_should_be(2).and()
                .under_clearing_should_be(0).and()
                .under_clearing_by_project_team_should_be(0).and()
                .report_available_should_be(0);
    }

    @Test
    public void test1() throws Exception {
        given()
                .a_release_with_fossology_status_$_for_$_and_$_for_$(
                        FossologyStatus.SCANNING, CLEARING_TEAM,
                        FossologyStatus.SENT, ANOTHER_CLEARING_TEAM);

        when().the_clearing_state_is_computed_for(CLEARING_TEAM);

        then()
                .new_releases_should_be(0).and()
                .under_clearing_should_be(0).and()
                .under_clearing_by_project_team_should_be(1).and()
                .report_available_should_be(0);
    }


    @Test
    public void test2() throws Exception {
        given()
                .a_release_with_clearing_status_$_and_fossology_status_$_for_$_and_$_for_$(
                        ClearingState.REPORT_AVAILABLE,
                        FossologyStatus.SCANNING, CLEARING_TEAM,
                        FossologyStatus.REPORT_AVAILABLE, ANOTHER_CLEARING_TEAM)
                .and()
                .a_new_release();

        when().the_clearing_state_is_computed_for(CLEARING_TEAM);

        then()
                .new_releases_should_be(1).and()
                .under_clearing_should_be(0).and()
                .under_clearing_by_project_team_should_be(0).and()
                .report_available_should_be(1);

    }

    @Test
    public void test3() throws Exception {
        given()
                .a_release_with_clearing_status(ClearingState.SENT_TO_FOSSOLOGY)
                .and()
                .a_release_with_fossology_status_$_for_$_and_$_for_$(
                        FossologyStatus.SCANNING, CLEARING_TEAM,
                        FossologyStatus.CLOSED, ANOTHER_CLEARING_TEAM);

        when().the_clearing_state_is_computed_for(CLEARING_TEAM);

        then()
                .new_releases_should_be(0).and()
                .under_clearing_should_be(1).and()
                .under_clearing_by_project_team_should_be(1).and()
                .report_available_should_be(0);

    }

    @Test
    public void test4() throws Exception {
        given()
                .a_release_with_clearing_status_$_and_fossology_status_$_for_$(
                        ClearingState.REPORT_AVAILABLE,
                        FossologyStatus.IN_PROGRESS, ANOTHER_CLEARING_TEAM)
                .and()
                .a_new_release();

        when().the_clearing_state_is_computed_for(CLEARING_TEAM);

        then()
                .new_releases_should_be(1).and()
                .under_clearing_should_be(0).and()
                .under_clearing_by_project_team_should_be(0).and()
                .report_available_should_be(1);

    }

    @Test
    public void test5() throws Exception {
        given()
                .a_release_with_clearing_status_$_and_fossology_status_$_for_$(
                        ClearingState.REPORT_AVAILABLE,
                        FossologyStatus.SENT, ANOTHER_CLEARING_TEAM)
                .and()
                .a_release_with_clearing_status_$_and_fossology_status_$_for_$_and_$_for_$(
                        ClearingState.REPORT_AVAILABLE,
                        FossologyStatus.SCANNING, CLEARING_TEAM,
                        FossologyStatus.SENT, ANOTHER_CLEARING_TEAM)
                .and()
                .a_new_release();

        when().the_clearing_state_is_computed_for(CLEARING_TEAM);

        then()
                .new_releases_should_be(1).and()
                .under_clearing_should_be(0).and()
                .under_clearing_by_project_team_should_be(0).and()
                .report_available_should_be(2);

    }

    @Test
    public void test6() throws Exception {
        given()
                .a_release_with_fossology_status_$_for_$_and_$_for_$_and_$_for_$(
                        FossologyStatus.IN_PROGRESS, CLEARING_TEAM,
                        FossologyStatus.REPORT_AVAILABLE, ANOTHER_CLEARING_TEAM,
                        FossologyStatus.OPEN, "yet " + ANOTHER_CLEARING_TEAM)
                .and()
                .a_new_release()
                .and()
                .a_release_with_clearing_status_$_and_fossology_status_$_for_$_and_$_for_$(
                        ClearingState.REPORT_AVAILABLE,
                        FossologyStatus.IN_PROGRESS, CLEARING_TEAM,
                        FossologyStatus.REPORT_AVAILABLE, ANOTHER_CLEARING_TEAM)
                .and()
                .a_new_release();

        when().the_clearing_state_is_computed_for(CLEARING_TEAM);

        then()
                .new_releases_should_be(2).and()
                .under_clearing_should_be(0).and()
                .under_clearing_by_project_team_should_be(1).and()
                .report_available_should_be(1);

    }

    @Test
    public void test7() throws Exception {
        given()
                .a_release_with_clearing_status(ClearingState.SENT_TO_FOSSOLOGY);

        when().the_clearing_state_is_computed_for(CLEARING_TEAM);

        then()
                .new_releases_should_be(0).and()
                .under_clearing_should_be(1).and()
                .under_clearing_by_project_team_should_be(0).and()
                .report_available_should_be(0);

    }

    @Test
    public void test8() throws Exception {
        given()
                .a_release_with_clearing_status(ClearingState.NEW_CLEARING);

        when().the_clearing_state_is_computed_for(CLEARING_TEAM);

        then()
                .new_releases_should_be(1).and()
                .under_clearing_should_be(0).and()
                .under_clearing_by_project_team_should_be(0).and()
                .report_available_should_be(0);

    }


    @Test
    public void test9() throws Exception {
        given()
                .a_release_with_fossology_status_$_for_$(FossologyStatus.CLOSED, ANOTHER_CLEARING_TEAM);

        when().the_clearing_state_is_computed_for(CLEARING_TEAM);

        then()
                .new_releases_should_be(0).and()
                .under_clearing_should_be(0).and()
                .under_clearing_by_project_team_should_be(0).and()
                .report_available_should_be(1);

    }

    @Test
    public void test90() throws Exception {
        given()
                .a_release_with_fossology_status_$_for_$(FossologyStatus.OPEN, ANOTHER_CLEARING_TEAM);

        when().the_clearing_state_is_computed_for(CLEARING_TEAM);

        then()
                .new_releases_should_be(0).and()
                .under_clearing_should_be(1).and()
                .under_clearing_by_project_team_should_be(0).and()
                .report_available_should_be(0);

    }

    @Test
    public void the_clearing_moves_to_the_right_following_clearing_team_but_is_reset_by_the_global_release_clearing_state() {
        given().a_new_release();

        when().the_clearing_state_is_computed_for(CLEARING_TEAM);

        then().new_releases_should_be(1);

        when().the_release_is_sent_for_clearing_to(CLEARING_TEAM);
        then().new_releases_should_be(0).and().under_clearing_by_project_team_should_be(1);

        when().the_release_is_sent_for_clearing_to(ANOTHER_CLEARING_TEAM);
        then().under_clearing_should_be(0).and().under_clearing_by_project_team_should_be(1);

        when().team_$_sets_fossology_status_to(CLEARING_TEAM, FossologyStatus.CLOSED);
        then().under_clearing_should_be(0).and().under_clearing_by_project_team_should_be(0).and().report_available_should_be(1);

        when().the_release_clearing_state_is_set_to(ClearingState.UNDER_CLEARING);
        then().under_clearing_should_be(1).and().under_clearing_by_project_team_should_be(0).and().report_available_should_be(0);

        when().the_release_clearing_state_is_set_to(ClearingState.NEW_CLEARING);
        then().under_clearing_should_be(0).and().under_clearing_by_project_team_should_be(0).and().report_available_should_be(1);
    }
}