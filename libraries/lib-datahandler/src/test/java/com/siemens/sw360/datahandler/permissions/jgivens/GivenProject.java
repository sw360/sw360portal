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
    public List<Project> projectList = newArrayList();

    private Project project;

    public GivenProject a_new_project() {
        project = mock(Project.class);
        projectList.add(project);
        Mockito.when(project.getVisbility()).thenReturn(Visibility.EVERYONE);
        return self();
    }

    public GivenProject a_project_created_by_$( @Quoted String m1) {
        a_new_project();
        Mockito.when(project.isSetCreatedBy()).thenReturn(true);
        Mockito.when(project.getCreatedBy()).thenReturn(m1);
        return self();
    }

    public GivenProject a_project_with_lead_architect_$( @Quoted String m1) {
        a_new_project();
        Mockito.when(project.isSetLeadArchitect()).thenReturn(true);
        Mockito.when(project.getLeadArchitect()).thenReturn(m1);
        return self();
    }

    public GivenProject a_project_with_moderator_$( @Quoted String m1) {
        a_new_project();
        Mockito.when(project.isSetModerators()).thenReturn(true);
        Mockito.when(project.getModerators()).thenReturn(ImmutableSet.of(m1));
        return self();
    }

    public GivenProject a_project_with_comoderator_$( @Quoted String m1) {
        a_new_project();
        Mockito.when(project.isSetComoderators()).thenReturn(true);
        Mockito.when(project.getComoderators()).thenReturn(ImmutableSet.of(m1));
        return self();
    }

    public GivenProject a_project_with_contributor_$( @Quoted String m1) {
        a_new_project();
        Mockito.when(project.isSetContributors()).thenReturn(true);
        Mockito.when(project.getContributors()).thenReturn(ImmutableSet.of(m1));
        return self();
    }

    public GivenProject a_project_with_project_responsible_$( @Quoted String m1) {
        a_new_project();
        Mockito.when(project.isSetProjectResponsible()).thenReturn(true);
        Mockito.when(project.getProjectResponsible()).thenReturn(m1);
        return self();
    }
}
