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

import com.siemens.sw360.datahandler.thrift.components.ReleaseClearingStateSummary;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author daniele.fognini@tngtech.com
 */
public class ThenReleaseClearingState extends Stage<ThenReleaseClearingState> {

    @ExpectedScenarioState
    ReleaseClearingStateSummary releaseClearingStateSummary;

    public ThenReleaseClearingState new_releases_should_be(int i) {
        assertThat(releaseClearingStateSummary.getNewRelease(), is(i));
        return self();
    }

    public ThenReleaseClearingState under_clearing_should_be(int i) {
        assertThat(releaseClearingStateSummary.getUnderClearing(), is(i));
        return self();
    }

    public ThenReleaseClearingState under_clearing_by_project_team_should_be(int i) {
        assertThat(releaseClearingStateSummary.getUnderClearingByProjectTeam(), is(i));
        return self();
    }

    public ThenReleaseClearingState report_available_should_be(int i) {
        assertThat(releaseClearingStateSummary.getReportAvailable(), is(i));
        return self();
    }
}
