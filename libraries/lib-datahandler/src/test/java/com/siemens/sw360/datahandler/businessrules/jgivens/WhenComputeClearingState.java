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
package com.siemens.sw360.datahandler.businessrules.jgivens;

import com.siemens.sw360.datahandler.TEnumToString;
import com.siemens.sw360.datahandler.businessrules.ReleaseClearingStateSummaryComputer;
import com.siemens.sw360.datahandler.thrift.components.ClearingState;
import com.siemens.sw360.datahandler.thrift.components.FossologyStatus;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.components.ReleaseClearingStateSummary;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import org.junit.internal.AssumptionViolatedException;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

/**
 * @author daniele.fognini@tngtech.com
 */
public class WhenComputeClearingState extends Stage<WhenComputeClearingState> {
    @ExpectedScenarioState
    List<Release> releaseList;


    @ProvidedScenarioState
    ReleaseClearingStateSummary clearingState;

    private String lastTestedClearingTeam;

    public WhenComputeClearingState the_clearing_state_is_computed_for(@Quoted String clearingTeam) {
        this.lastTestedClearingTeam = clearingTeam;
        clearingState = ReleaseClearingStateSummaryComputer.computeReleaseClearingStateSummary(releaseList, this.lastTestedClearingTeam);
        return self();
    }

    public WhenComputeClearingState team_$_sets_fossology_status_to(@Quoted String clearingTeam, @TEnumToString FossologyStatus fossologyStatus) {
        Release release = getFirstRelease();
        Map<String, FossologyStatus> clearingTeamToFossologyStatus = release.getClearingTeamToFossologyStatus();

        clearingTeamToFossologyStatus.put(clearingTeam, fossologyStatus);

        the_clearing_state_is_computed_for(lastTestedClearingTeam);
        return self();
    }

    public WhenComputeClearingState the_release_is_sent_for_clearing_to(@Quoted String clearingTeam) {
        Release release = getFirstRelease();
        Map<String, FossologyStatus> clearingTeamToFossologyStatus = release.getClearingTeamToFossologyStatus();

        clearingTeamToFossologyStatus.put(clearingTeam, FossologyStatus.SENT);

        the_clearing_state_is_computed_for(lastTestedClearingTeam);
        return self();
    }

    public WhenComputeClearingState the_release_clearing_state_is_set_to(@TEnumToString ClearingState clearingState) {
        Release release = getFirstRelease();
        Mockito.when(release.getClearingState()).thenReturn(clearingState);

        the_clearing_state_is_computed_for(lastTestedClearingTeam);
        return self();
    }

    private Release getFirstRelease() {
        if (releaseList.size() != 1) {
            throw new AssumptionViolatedException("this test can only handle one release, add a 'n-th' release parameter");
        }
        return releaseList.get(0);
    }
}
