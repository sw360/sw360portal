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

import com.google.common.collect.ImmutableSet;
import com.siemens.sw360.datahandler.TEnumToString;
import com.siemens.sw360.datahandler.thrift.Visibility;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.Quoted;
import com.tngtech.jgiven.annotation.ScenarioState;
import org.mockito.Mockito;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.mock;

/**
 * @author johannes.najjar@tngtech.com
 */
public class GivenProject extends Stage<GivenProject> {
    @ScenarioState
    private Project project;

    public enum ProjectRole {
        CREATED_BY,
        LEAD_ARCHITECT,
        MODERATOR,
        CO_MODERATOR,
        CONTRIBUTOR,
        PROJECT_RESPONSIBLE
    }

    public GivenProject a_new_project() {
        project = mock(Project.class);
        Mockito.when(project.getVisbility()).thenReturn(Visibility.EVERYONE);
        return self();
    }

    public GivenProject a_project_with_$_$(ProjectRole role, @Quoted String m1){
        a_new_project();

        switch (role) {
            case CREATED_BY:
                Mockito.when(project.isSetCreatedBy()).thenReturn(true);
                Mockito.when(project.getCreatedBy()).thenReturn(m1);
                break;
            case LEAD_ARCHITECT:
                Mockito.when(project.isSetLeadArchitect()).thenReturn(true);
                Mockito.when(project.getLeadArchitect()).thenReturn(m1);
                break;
            case MODERATOR:
                Mockito.when(project.isSetModerators()).thenReturn(true);
                Mockito.when(project.getModerators()).thenReturn(ImmutableSet.of(m1));
                break;
            case CO_MODERATOR:
                Mockito.when(project.isSetComoderators()).thenReturn(true);
                Mockito.when(project.getComoderators()).thenReturn(ImmutableSet.of(m1));
                break;
            case CONTRIBUTOR:
                Mockito.when(project.isSetContributors()).thenReturn(true);
                Mockito.when(project.getContributors()).thenReturn(ImmutableSet.of(m1));
                break;
            case PROJECT_RESPONSIBLE:
                Mockito.when(project.isSetProjectResponsible()).thenReturn(true);
                Mockito.when(project.getProjectResponsible()).thenReturn(m1);
                break;
        }

        return self();
    }

    public GivenProject with_visibility_$_and_business_unit_$(@TEnumToString Visibility v1, @Quoted String b1) {
        Mockito.when(project.getVisbility()).thenReturn(v1);
        Mockito.when(project.getBusinessUnit()).thenReturn(b1);
        return self();
    }
}
