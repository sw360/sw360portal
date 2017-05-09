/*
 * Copyright Siemens AG, 2013-2017. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.portal.portlets;

import com.google.common.collect.SetMultimap;
import junit.framework.TestCase;
import org.eclipse.sw360.datahandler.thrift.MainlineState;
import org.eclipse.sw360.datahandler.thrift.ProjectReleaseRelationship;
import org.eclipse.sw360.datahandler.thrift.ReleaseRelationship;
import org.eclipse.sw360.datahandler.thrift.ThriftClients;
import org.eclipse.sw360.datahandler.thrift.attachments.Attachment;
import org.eclipse.sw360.datahandler.thrift.components.ComponentService;
import org.eclipse.sw360.datahandler.thrift.components.Release;
import org.eclipse.sw360.datahandler.thrift.projects.Project;
import org.eclipse.sw360.datahandler.thrift.projects.ProjectRelationship;
import org.eclipse.sw360.datahandler.thrift.projects.ProjectService;
import org.eclipse.sw360.datahandler.thrift.projects.ProjectWithReleaseRelationTuple;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import javax.portlet.PortletException;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import java.io.IOException;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class FossologyAwarePortletTest extends TestCase {

    @Mock
    private ComponentService.Iface componentClient;

    @Mock
    private ProjectService.Iface projectClient;

    @Mock
    private ThriftClients thriftClients;

    @Mock
    private User user;

    private final Map<String, Release> releaseMap  = new HashMap<>();
    private final Map<String, Project> projectMap = new HashMap<>();

    Project p1;
    Project p2;
    Project p3;

    Release r1;
    Release r2;
    Release r3;
    Release r4;

    FossologyAwarePortlet fossologyAwarePortlet;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {


        r1 = new Release().setId("r1").setName("r1");
        r2 = new Release().setId("r2").setName("r2");

        r3 = new Release().setId("r3").setName("r3");
        r4 = new Release().setId("r4").setName("r4");

        releaseMap.put(r1.getId(),r1);
        releaseMap.put(r2.getId(),r2);
        releaseMap.put(r3.getId(),r3);
        releaseMap.put(r4.getId(),r4);


        Map<String, ProjectReleaseRelationship> releaseUsageMap1 = new HashMap<>();
        releaseUsageMap1.put(r1.getId(), newDefaultProjectReleaseRelationship());
        releaseUsageMap1.put(r2.getId(), newDefaultProjectReleaseRelationship());

        Map<String, ProjectReleaseRelationship> releaseUsageMap2 = new HashMap<>();
        releaseUsageMap2.put(r1.getId(), newDefaultProjectReleaseRelationship());
        releaseUsageMap2.put( r3.getId(), newDefaultProjectReleaseRelationship());

        Map<String, ProjectReleaseRelationship> releaseUsageMap3 = new HashMap<>();
        releaseUsageMap3.put(r2.getId(), newDefaultProjectReleaseRelationship());
        releaseUsageMap3.put(r4.getId(), newDefaultProjectReleaseRelationship());



        p1 = new Project().setReleaseIdToUsage(releaseUsageMap1).setId("p1");
        p2 = new Project().setReleaseIdToUsage(releaseUsageMap2).setId("p2");
        p3 = new Project().setReleaseIdToUsage(releaseUsageMap3).setId("p3");


        projectMap.put(p1.getId(),p1);
        projectMap.put(p2.getId(),p2);
        projectMap.put(p3.getId(),p3);


        Map<String, ProjectRelationship> projectRelationshipMap1  = new HashMap<>();
        projectRelationshipMap1.put(p2.getId(), ProjectRelationship.CONTAINED);
        projectRelationshipMap1.put(p3.getId(), ProjectRelationship.DUPLICATE);


        Map<String, ProjectRelationship> projectRelationshipMap2  = new HashMap<>();
        projectRelationshipMap2.put(p1.getId(), ProjectRelationship.DUPLICATE);
        projectRelationshipMap2.put(p3.getId(), ProjectRelationship.REFERRED);


        Map<String, ProjectRelationship> projectRelationshipMap3  = new HashMap<>();
        projectRelationshipMap3.put(p1.getId(), ProjectRelationship.DUPLICATE);
        projectRelationshipMap3.put(p2.getId(), ProjectRelationship.REFERRED);


        p1.setLinkedProjects(projectRelationshipMap1);
        p2.setLinkedProjects(projectRelationshipMap2);
        p3.setLinkedProjects(projectRelationshipMap3);


        when(thriftClients.makeComponentClient() ).thenReturn(componentClient);
        when(thriftClients.makeProjectClient() ).thenReturn(projectClient);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                Object[] arguments = invocation.getArguments();
                String id = (String) arguments[0];

                return releaseMap.get(id);

            }
        }).when(componentClient).getReleaseById(anyString(), any(User.class));



        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                Object[] arguments = invocation.getArguments();
                Set<String> ids = (Set) arguments[0];
                User user1 =  (User) arguments[1];

                List<Release> releaseList =  new ArrayList<>();

                for (String id : ids) {
                    releaseList.add(componentClient.getReleaseById(id, user1));
                }


                return releaseList;

            }
        }).when(componentClient).getReleasesById(any(Set.class), any(User.class));


        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {

                Object[] arguments = invocation.getArguments();
                String id = (String) arguments[0];

                return projectMap.get(id);

            }
        }).when(projectClient).getProjectById(anyString(), any(User.class));

        fossologyAwarePortlet = new FossologyAwarePortlet(thriftClients) {

            @Override
            protected Set<Attachment> getAttachments(String documentId, String documentType, User user) {
                return null;
            }

            @Override
            protected void dealWithFossologyAction(ResourceRequest request, ResourceResponse response, String action) throws IOException, PortletException {

            }
        };
    }

    private ProjectReleaseRelationship newDefaultProjectReleaseRelationship() {
        return new ProjectReleaseRelationship(ReleaseRelationship.UNKNOWN, MainlineState.MAINLINE);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testReleaseIdToProjects() throws Exception {

        SetMultimap<String, ProjectWithReleaseRelationTuple> releaseIdToProjects = fossologyAwarePortlet.releaseIdToProjects(p1, user);

        Set<String> releaseIds = releaseIdToProjects.keySet();

        assertThat(releaseIds, containsInAnyOrder("r1", "r2","r3","r4"));
        assertThat(releaseIdToProjects.get("r1"), containsInAnyOrder(createTuple(p1),createTuple(p2)));
        assertThat(releaseIdToProjects.get("r2"), containsInAnyOrder(createTuple(p1),createTuple(p3)));
        assertThat(releaseIdToProjects.get("r3"), containsInAnyOrder(createTuple(p2)));
        assertThat(releaseIdToProjects.get("r4"), containsInAnyOrder(createTuple(p3)));

    }

    private ProjectWithReleaseRelationTuple createTuple(Project p) {
        return new ProjectWithReleaseRelationTuple(p, newDefaultProjectReleaseRelationship());
    }


    @Test
    public void testReleaseIdToEmptyProjects() throws Exception {
        SetMultimap<String, ProjectWithReleaseRelationTuple> releaseIdToProjects = fossologyAwarePortlet.releaseIdToProjects(new Project().setId("p4"), user);
        Set<String> releaseIds = releaseIdToProjects.keySet();
        assertTrue("Release IDs size", releaseIds.size() == 0);
    }

}