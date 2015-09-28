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

import com.google.common.collect.ImmutableMap;
import com.siemens.sw360.datahandler.TEnumToString;
import com.siemens.sw360.datahandler.thrift.components.ClearingState;
import com.siemens.sw360.datahandler.thrift.components.FossologyStatus;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.Quoted;
import com.tngtech.jgiven.annotation.ScenarioState;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.mock;

/**
 * @author daniele.fognini@tngtech.com
 */
public class GivenReleasesWithFossologyStatus extends Stage<GivenReleasesWithFossologyStatus> {
    @ScenarioState
    public List<Release> releaseList = newArrayList();

    private Release release;

    public GivenReleasesWithFossologyStatus a_release_with_fossology_status_$_for_$(@TEnumToString FossologyStatus f1, @Quoted String t1) {
        a_new_release();
        Map<String, FossologyStatus> fossologyStatusPerClearingTeam = ImmutableMap.of(t1, f1);
        Mockito.when(release.getClearingTeamToFossologyStatus()).thenReturn(fossologyStatusPerClearingTeam);
        return self();
    }

    public GivenReleasesWithFossologyStatus a_release_with_fossology_status_$_for_$_and_$_for_$(@TEnumToString FossologyStatus f1, @Quoted String t1, @TEnumToString FossologyStatus f2, @Quoted String t2) {
        a_new_release();
        Map<String, FossologyStatus> fossologyStatusPerClearingTeam = ImmutableMap.of(t1, f1, t2, f2);
        Mockito.when(release.getClearingTeamToFossologyStatus()).thenReturn(fossologyStatusPerClearingTeam);
        return self();
    }

    public GivenReleasesWithFossologyStatus a_release_with_fossology_status_$_for_$_and_$_for_$_and_$_for_$(@TEnumToString FossologyStatus f1, @Quoted String t1, @TEnumToString FossologyStatus f2, @Quoted String t2, @TEnumToString FossologyStatus f3, @Quoted String t3) {
        a_new_release();
        Map<String, FossologyStatus> fossologyStatusPerClearingTeam = ImmutableMap.of(t1, f1, t2, f2, t3, f3);
        Mockito.when(release.getClearingTeamToFossologyStatus()).thenReturn(fossologyStatusPerClearingTeam);
        return self();
    }

    public GivenReleasesWithFossologyStatus a_release_with_clearing_status_$_and_fossology_status_$_for_$_and_$_for_$(@TEnumToString ClearingState clearingState, @TEnumToString FossologyStatus f1, @Quoted String t1, @TEnumToString FossologyStatus f2, @Quoted String t2) {
        a_new_release();
        Map<String, FossologyStatus> fossologyStatusPerClearingTeam = ImmutableMap.of(t1, f1, t2, f2);
        Mockito.when(release.getClearingState()).thenReturn(clearingState);
        Mockito.when(release.getClearingTeamToFossologyStatus()).thenReturn(fossologyStatusPerClearingTeam);
        return self();
    }

    public GivenReleasesWithFossologyStatus a_release_with_clearing_status_$_and_fossology_status_$_for_$(@TEnumToString ClearingState clearingState, @TEnumToString FossologyStatus f1, @Quoted String t1) {
        a_new_release();
        Map<String, FossologyStatus> fossologyStatusPerClearingTeam = ImmutableMap.of(t1, f1);
        Mockito.when(release.getClearingState()).thenReturn(clearingState);
        Mockito.when(release.getClearingTeamToFossologyStatus()).thenReturn(fossologyStatusPerClearingTeam);
        return self();
    }

    public GivenReleasesWithFossologyStatus a_new_release() {
        release = mock(Release.class);
        releaseList.add(release);
        Mockito.when(release.getClearingState()).thenReturn(ClearingState.NEW_CLEARING);
        Mockito.when(release.getClearingTeamToFossologyStatus()).thenReturn(new HashMap<String, FossologyStatus>());
        return self();
    }

    public GivenReleasesWithFossologyStatus a_release_with_clearing_status(@TEnumToString ClearingState clearingState) {
        a_new_release();
        Mockito.when(release.getClearingState()).thenReturn(clearingState);
        return self();
    }
}
