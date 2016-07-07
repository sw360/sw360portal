/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.siemens.sw360.datahandler.db;

import com.siemens.sw360.datahandler.TestUtils;
import com.siemens.sw360.datahandler.common.DatabaseSettings;
import com.siemens.sw360.datahandler.couchdb.DatabaseConnector;
import com.siemens.sw360.datahandler.entitlement.ProjectModerator;
import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.users.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.siemens.sw360.datahandler.common.SW360Utils.printName;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProjectDatabaseHandlerTest {

    private static final String url = DatabaseSettings.COUCH_DB_URL;
    private static final String dbName = DatabaseSettings.COUCH_DB_DATABASE;
    private static final String attachmentDbName = DatabaseSettings.COUCH_DB_ATTACHMENTS;

    private static final User user1 = new User().setEmail("user1").setDepartment("AB CD EF");
    private static final User user2 = new User().setEmail("user2").setDepartment("AB CD FE");
    private static final User user3 = new User().setEmail("user3").setDepartment("AB CD EF");


    ProjectModerator moderator = Mockito.mock(ProjectModerator.class);
    ProjectDatabaseHandler handler;
    @Before
    public void setUp() throws Exception {
        List<Project> projects = new ArrayList<>();

        projects.add(new Project().setId("P1").setName("Project1").setBusinessUnit("AB CD EF").setCreatedBy("user1"));
        projects.add(new Project().setId("P2").setName("Project2").setBusinessUnit("AB CD FE").setCreatedBy("user2"));
        projects.get(1).addToContributors("user1");
        projects.add(new Project().setId("P3").setName("Project3").setBusinessUnit("AB CD EF").setCreatedBy("user3"));

        // Create the database
        TestUtils.createDatabase(url, dbName);

        // Prepare the database
        DatabaseConnector databaseConnector = new DatabaseConnector(url, dbName);
        for (Project project : projects) {
            databaseConnector.add(project);
        }
        handler = new ProjectDatabaseHandler(url, dbName, attachmentDbName,moderator);
    }

    @After
    public void tearDown() throws Exception {
        // Delete the database
        TestUtils.deleteDatabase(url, dbName);
    }



    @Test
    public void testUpdateProject2_1() throws Exception {
        Project project2 = handler.getProjectById("P2", user1);
        project2.setName("Project2new");

        Mockito.doReturn(RequestStatus.SENT_TO_MODERATOR).when(moderator).updateProject(project2, user1);

        RequestStatus status = handler.updateProject(project2, user1);

        // Now contributors can also change the project
        assertEquals(RequestStatus.SUCCESS, status);
    }


    @Test
    public void testDeleteProject1_3() throws Exception {
        when(moderator.deleteProject(any(Project.class), eq(user3))).thenReturn(RequestStatus.SENT_TO_MODERATOR);
        RequestStatus status = handler.deleteProject("P1", user3);

        assertEquals(RequestStatus.SENT_TO_MODERATOR, status);

        assertEquals(2, handler.getMyProjectsSummary(user1.getEmail()).size());
        assertEquals(1, handler.getMyProjectsSummary(user2.getEmail()).size());
        assertEquals(1, handler.getMyProjectsSummary(user3.getEmail()).size());

        assertEquals(2, handler.getBUProjectsSummary(user1.getDepartment()).size());
        assertEquals(1, handler.getBUProjectsSummary(user2.getDepartment()).size());
        assertEquals(2, handler.getBUProjectsSummary(user3.getDepartment()).size());

        assertEquals(3, handler.getAccessibleProjectsSummary(user1).size());
        assertEquals(1, handler.getAccessibleProjectsSummary(user2).size());
        assertEquals(2, handler.getAccessibleProjectsSummary(user3).size());

        boolean deleted = (handler.getProjectById("P1", user1) == null);
        assertEquals(false, deleted);
    }

    @Test
    public void testDeleteProject2_1() throws Exception {
        when(moderator.deleteProject(any(Project.class), eq(user1))).thenReturn(RequestStatus.SENT_TO_MODERATOR);
        RequestStatus status = handler.deleteProject("P2", user1);

        assertEquals(RequestStatus.SENT_TO_MODERATOR, status);

        assertEquals(2, handler.getMyProjectsSummary(user1.getEmail()).size());
        assertEquals(1, handler.getMyProjectsSummary(user2.getEmail()).size());
        assertEquals(1, handler.getMyProjectsSummary(user3.getEmail()).size());

        assertEquals(2, handler.getBUProjectsSummary(user1.getDepartment()).size());
        assertEquals(1, handler.getBUProjectsSummary(user2.getDepartment()).size());
        assertEquals(2, handler.getBUProjectsSummary(user3.getDepartment()).size());

        assertEquals(3, handler.getAccessibleProjectsSummary(user1).size());
        assertEquals(1, handler.getAccessibleProjectsSummary(user2).size());
        assertEquals(2, handler.getAccessibleProjectsSummary(user3).size());

        boolean deleted = (handler.getProjectById("P2", user2) == null);
        assertEquals(false, deleted);
    }


    @Test
    public void testDeleteProject2_3() throws Exception {
        when(moderator.deleteProject(any(Project.class), eq(user3))).thenReturn(RequestStatus.SENT_TO_MODERATOR);
        RequestStatus status = handler.deleteProject("P2", user3);

        assertEquals(RequestStatus.SENT_TO_MODERATOR, status);

        assertEquals(2, handler.getMyProjectsSummary(user1.getEmail()).size());
        assertEquals(1, handler.getMyProjectsSummary(user2.getEmail()).size());
        assertEquals(1, handler.getMyProjectsSummary(user3.getEmail()).size());

        assertEquals(2, handler.getBUProjectsSummary(user1.getDepartment()).size());
        assertEquals(1, handler.getBUProjectsSummary(user2.getDepartment()).size());
        assertEquals(2, handler.getBUProjectsSummary(user3.getDepartment()).size());

        assertEquals(3, handler.getAccessibleProjectsSummary(user1).size());
        assertEquals(1, handler.getAccessibleProjectsSummary(user2).size());
        assertEquals(2, handler.getAccessibleProjectsSummary(user3).size());

        boolean deleted = (handler.getProjectById("P2", user2) == null);
        assertEquals(false, deleted);
    }

    @Test
    public void testDeleteProject3_1() throws Exception {
        when(moderator.deleteProject(any(Project.class), eq(user1))).thenReturn(RequestStatus.SENT_TO_MODERATOR);
        RequestStatus status = handler.deleteProject("P3", user1);

        assertEquals(RequestStatus.SENT_TO_MODERATOR, status);

        assertEquals(2, handler.getMyProjectsSummary(user1.getEmail()).size());
        assertEquals(1, handler.getMyProjectsSummary(user2.getEmail()).size());
        assertEquals(1, handler.getMyProjectsSummary(user3.getEmail()).size());

        assertEquals(2, handler.getBUProjectsSummary(user1.getDepartment()).size());
        assertEquals(1, handler.getBUProjectsSummary(user2.getDepartment()).size());
        assertEquals(2, handler.getBUProjectsSummary(user3.getDepartment()).size());

        assertEquals(3, handler.getAccessibleProjectsSummary(user1).size());
        assertEquals(1, handler.getAccessibleProjectsSummary(user2).size());
        assertEquals(2, handler.getAccessibleProjectsSummary(user3).size());

        boolean deleted = (handler.getProjectById("P3", user3) == null);
        assertEquals(false, deleted);
    }

    @Test
    public void testDeleteProject3_2() throws Exception {
        when(moderator.deleteProject(any(Project.class), eq(user2))).thenReturn(RequestStatus.SENT_TO_MODERATOR);
        RequestStatus status = handler.deleteProject("P3", user2);

        assertEquals(RequestStatus.SENT_TO_MODERATOR, status);

        assertEquals(2, handler.getMyProjectsSummary(user1.getEmail()).size());
        assertEquals(1, handler.getMyProjectsSummary(user2.getEmail()).size());
        assertEquals(1, handler.getMyProjectsSummary(user3.getEmail()).size());

        assertEquals(2, handler.getBUProjectsSummary(user1.getDepartment()).size());
        assertEquals(1, handler.getBUProjectsSummary(user2.getDepartment()).size());
        assertEquals(2, handler.getBUProjectsSummary(user3.getDepartment()).size());

        assertEquals(3, handler.getAccessibleProjectsSummary(user1).size());
        assertEquals(1, handler.getAccessibleProjectsSummary(user2).size());
        assertEquals(2, handler.getAccessibleProjectsSummary(user3).size());

        boolean deleted = (handler.getProjectById("P3", user3) == null);
        assertEquals(false, deleted);
    }


    @Test
    public void testDuplicateProjectIsFound() throws Exception {

        String originalProjectId = "P1";
        final Project tmp = handler.getProjectById(originalProjectId, user1);
        tmp.unsetId();
        tmp.unsetRevision();
        String newProjectId = handler.addProject(tmp, user1);

        final Map<String, List<String>> duplicateProjects = handler.getDuplicateProjects();

        assertThat(duplicateProjects.size(), is(1));
        assertThat(duplicateProjects.get(printName(tmp)), containsInAnyOrder(newProjectId,originalProjectId));
    }

}