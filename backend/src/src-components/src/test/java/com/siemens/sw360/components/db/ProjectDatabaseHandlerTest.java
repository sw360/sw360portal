/*
 * Copyright Siemens AG, 2016. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.siemens.sw360.components.db;

import com.google.common.collect.ImmutableMap;
import com.siemens.sw360.datahandler.TestUtils;
import com.siemens.sw360.datahandler.common.DatabaseSettings;
import com.siemens.sw360.datahandler.couchdb.DatabaseConnector;
import com.siemens.sw360.datahandler.db.ComponentDatabaseHandler;
import com.siemens.sw360.datahandler.db.ProjectDatabaseHandler;
import com.siemens.sw360.datahandler.entitlement.ProjectModerator;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.components.ReleaseLink;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.projects.ProjectLink;
import com.siemens.sw360.datahandler.thrift.projects.ProjectRelationship;
import com.siemens.sw360.datahandler.thrift.vendors.Vendor;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.crypto.Data;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.siemens.sw360.datahandler.TestUtils.assertTestString;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ProjectDatabaseHandlerTest {

    private static final String url = DatabaseSettings.COUCH_DB_URL;
    private static final String dbName = DatabaseSettings.COUCH_DB_DATABASE;
    private static final String attachmentsDbName = DatabaseSettings.COUCH_DB_ATTACHMENTS;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private List<Project> projects;
    private List<Vendor> vendors;
    private List<Release> releases;
    private ProjectDatabaseHandler handler;

    @Mock
    private ProjectModerator moderator;

    @Before
    public void setUp() throws Exception {
        assertTestString(dbName);
        assertTestString(attachmentsDbName);

        vendors = new ArrayList<>();
        vendors.add(new Vendor().setId("V1").setShortname("vendor").setFullname("vendor").setUrl("http://vendor.example.com"));

        releases = new ArrayList<>();
        Release release1a = new Release().setId("R1A").setComponentId("C1").setName("component1").setVersion("releaseA").setVendorId("V1");
        releases.add(release1a);
        Release release1b = new Release().setId("R1B").setComponentId("C1").setName("component1").setVersion("releaseB").setVendorId("V1");
        releases.add(release1b);
        Release release2a = new Release().setId("R2A").setComponentId("C2").setName("component2").setVersion("releaseA").setVendorId("V1");
        releases.add(release2a);
        Release release2b = new Release().setId("R2B").setComponentId("C2").setName("component2").setVersion("releaseB").setVendorId("V1");
        releases.add(release2b);

        projects = new ArrayList<>();
        Project project1 = new Project().setId("P1").setName("project1").setLinkedProjects(ImmutableMap.of("P2", ProjectRelationship.CONTAINED));
        projects.add(project1);
        Project project2 = new Project().setId("P2").setName("project2").setLinkedProjects(ImmutableMap.of("P3", ProjectRelationship.REFERRED, "P4", ProjectRelationship.CONTAINED)).setReleaseIdToUsage(ImmutableMap.of("R1A", "used", "R1B", "abused"));
        projects.add(project2);
        Project project3 = new Project().setId("P3").setName("project3").setLinkedProjects(ImmutableMap.of("P2", ProjectRelationship.UNKNOWN)).setReleaseIdToUsage(ImmutableMap.of("R2A", "used", "R2B", "considered for use"));
        projects.add(project3);
        Project project4 = new Project().setId("P4").setName("project4").setLinkedProjects(ImmutableMap.of("P1", ProjectRelationship.UNKNOWN));
        projects.add(project4);
        Project project5 = new Project().setId("P5").setName("project5").setLinkedProjects(ImmutableMap.of("P6", ProjectRelationship.CONTAINED, "P7", ProjectRelationship.CONTAINED));
        projects.add(project5);
        Project project6 = new Project().setId("P6").setName("project6").setLinkedProjects(ImmutableMap.of("P7", ProjectRelationship.CONTAINED));
        projects.add(project6);
        Project project7 = new Project().setId("P7").setName("project7");
        projects.add(project7);

        // Create the database
        TestUtils.createDatabase(DatabaseSettings.getConfiguredHttpClient(), dbName);

        // Prepare the database
        DatabaseConnector databaseConnector = new DatabaseConnector(DatabaseSettings.getConfiguredHttpClient(), dbName);

        for (Vendor vendor : vendors) {
            databaseConnector.add(vendor);
        }
        for (Release release : releases) {
            databaseConnector.add(release);
        }
        for (Project project : projects) {
            databaseConnector.add(project);
        }

        ComponentDatabaseHandler componentHandler = new ComponentDatabaseHandler(DatabaseSettings.getConfiguredHttpClient(), dbName, attachmentsDbName);
        handler = new ProjectDatabaseHandler(DatabaseSettings.getConfiguredHttpClient(), dbName, attachmentsDbName, moderator, componentHandler);
    }

    @After
    public void tearDown() throws Exception {
        TestUtils.deleteDatabase(DatabaseSettings.getConfiguredHttpClient(), dbName);
    }

    @Test
    public void testGetLinkedProjects() throws Exception {

        // we wrap the potentially infinite loop in an executor
        final ExecutorService service = Executors.newSingleThreadExecutor();

        final Future<List<ProjectLink>> completionFuture = service.submit(() -> handler.getLinkedProjects(ImmutableMap.of("P1", ProjectRelationship.UNKNOWN)));

        service.shutdown();
        service.awaitTermination(10, TimeUnit.SECONDS);

        final List<ProjectLink> linkedProjects = completionFuture.get();

        ReleaseLink releaseLinkR1A = new ReleaseLink("R1A", "vendor", "component1", "releaseA").setComment("used").setNodeId("R1A_1");
        ReleaseLink releaseLinkR1B = new ReleaseLink("R1B", "vendor", "component1", "releaseB").setComment("abused").setNodeId("R1B_1");
        ReleaseLink releaseLinkR2A = new ReleaseLink("R2A", "vendor", "component2", "releaseA").setComment("used").setNodeId("R2A_1");
        ReleaseLink releaseLinkR2B = new ReleaseLink("R2B", "vendor", "component2", "releaseB").setComment("considered for use").setNodeId("R2B_1");

        ProjectLink link3 = new ProjectLink("P3", "project3")
                .setRelation(ProjectRelationship.REFERRED)
                .setNodeId("P3_1")
                .setParentNodeId("P2_1")
                .setLinkedReleases(Arrays.asList(releaseLinkR2A, releaseLinkR2B))
                .setSubprojects(Collections.emptyList());
        ProjectLink link4 = new ProjectLink("P4", "project4")
                .setRelation(ProjectRelationship.CONTAINED)
                .setNodeId("P4_1")
                .setParentNodeId("P2_1")
                .setSubprojects(Collections.emptyList());
        ProjectLink link2 = new ProjectLink("P2", "project2")
                .setRelation(ProjectRelationship.CONTAINED)
                .setNodeId("P2_1")
                .setParentNodeId("P1_1")
                .setLinkedReleases(Arrays.asList(releaseLinkR1A, releaseLinkR1B))
                .setSubprojects(Arrays.asList(link3, link4));
        ProjectLink link1 = new ProjectLink("P1", "project1")
                .setRelation(ProjectRelationship.UNKNOWN)
                .setNodeId("P1_1")
                .setSubprojects(Arrays.asList(link2));

        assertThat(linkedProjects, contains(link1));
    }

    @Test
    public void testGetLinkedProjects2() throws Exception {

        // we wrap the potentially infinite loop in an executor
        final ExecutorService service = Executors.newSingleThreadExecutor();

        final Future<List<ProjectLink>> completionFuture = service.submit(() -> handler.getLinkedProjects(ImmutableMap.of("P5", ProjectRelationship.CONTAINED)));

        service.shutdown();
        service.awaitTermination(10, TimeUnit.SECONDS);

        final List<ProjectLink> linkedProjects = completionFuture.get();

        ProjectLink link7_5 = new ProjectLink("P7", "project7")
                .setRelation(ProjectRelationship.CONTAINED)
                .setNodeId("P7_2")
                .setParentNodeId("P5_1");
        ProjectLink link7_6 = new ProjectLink("P7", "project7")
                .setRelation(ProjectRelationship.CONTAINED)
                .setNodeId("P7_1")
                .setParentNodeId("P6_1");

        ProjectLink link6 = new ProjectLink("P6", "project6")
                .setRelation(ProjectRelationship.CONTAINED)
                .setNodeId("P6_1")
                .setParentNodeId("P5_1")
                .setSubprojects(Arrays.asList(link7_6));
        ProjectLink link5 = new ProjectLink("P5", "project5")
                .setRelation(ProjectRelationship.CONTAINED)
                .setNodeId("P5_1")
                .setSubprojects(Arrays.asList(link6, link7_5));

        assertThat(linkedProjects, contains(link5));
    }


}
