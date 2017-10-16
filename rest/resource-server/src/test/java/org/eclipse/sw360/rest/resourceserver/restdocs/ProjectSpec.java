/*
 * Copyright Siemens AG, 2017. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.sw360.rest.resourceserver.restdocs;

import org.eclipse.sw360.datahandler.thrift.projects.Project;
import org.eclipse.sw360.datahandler.thrift.projects.ProjectType;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.rest.resourceserver.TestHelper;
import org.eclipse.sw360.rest.resourceserver.project.Sw360ProjectService;
import org.eclipse.sw360.rest.resourceserver.user.Sw360UserService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;

import java.util.*;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProjectSpec  extends RestDocsSpecBase {

    @MockBean
    private Sw360UserService userServiceMock;

    @MockBean
    private Sw360ProjectService projectServiceMock;

    private Project project;

    @Before
    public void before() {
        List<Project> projectList = new ArrayList<>();
        project = new Project();
        project.setId("376576");
        project.setName("Emerald Web");
        project.setType("project");
        project.setProjectType(ProjectType.PRODUCT);
        project.setVersion("1.0.2");
        project.setDescription("Emerald Web provides a suite of components for Critical Infrastructures.");
        project.setCreatedOn("2016-12-15");
        project.setCreatedBy("admin@sw360.org");
        project.setModerators(new HashSet<>(Arrays.asList("admin@sw360.org", "jane@sw360.org")));
        project.setBusinessUnit("sw360 AR");
        project.setExternalIds(Collections.singletonMap("mainline-id-project", "515432"));
        projectList.add(project);

        Project project2 = new Project();
        project2.setId("376576");
        project2.setName("Orange Web");
        project2.setVersion("2.0.1");
        project2.setType("project");
        project2.setProjectType(ProjectType.PRODUCT);
        project2.setDescription("Orange Web provides a suite of components for documentation.");
        project2.setCreatedOn("2016-12-17");
        project2.setCreatedBy("john@sw360.org");
        project2.setBusinessUnit("sw360 EX DF");
        project2.setExternalIds(Collections.singletonMap("mainline-id-project", "7657"));
        projectList.add(project2);

        given(this.projectServiceMock.getProjectsForUser(anyObject())).willReturn(projectList);
        given(this.projectServiceMock.getProjectForUserById(eq(project.getId()), anyObject())).willReturn(project);

        User user = new User();
        user.setId("admin@sw360.org");
        user.setEmail("admin@sw360.org");
        user.setFullname("John Doe");

        given(this.userServiceMock.getUserByEmail("admin@sw360.org")).willReturn(user);
    }

    @Test
    public void should_document_get_projects()  throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, "admin@sw360.org", "sw360-password");
        mockMvc.perform(get("/api/projects")
                .header("Authorization", "Bearer " + accessToken)
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                        links(
                                linkWithRel("curies").description("Curies are used for online documentation")
                        ),
                        responseFields(
                                fieldWithPath("_embedded.sw360:projects").description("An array of <<resources-projects, Projects resources>>"),
                                fieldWithPath("_links").description("<<resources-index-links,Links>> to other resources")
                        )));
    }

    @Test
    public void should_document_get_project() throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, "admin@sw360.org", "sw360-password");
        mockMvc.perform(get("/api/projects/" + project.getId())
                .header("Authorization", "Bearer " + accessToken)
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                        links(
                                linkWithRel("self").description("The <<resources-projects,Projects resource>>")
                        ),
                        responseFields(
                                fieldWithPath("name").description("The name of the project"),
                                fieldWithPath("description").description("The project description"),
                                fieldWithPath("version").description("The project version"),
                                fieldWithPath("createdOn").description("The date the project was created"),
                                fieldWithPath("type").description("is always 'project'"),
                                fieldWithPath("projectType").description("The project type, possible values are: " + Arrays.asList(ProjectType.values())),
                                fieldWithPath("businessUnit").description("The business unit this project belongs to"),
                                fieldWithPath("externalIds").description("When projects are imported from other tools, the external ids can be stored here"),
                                fieldWithPath("_links").description("<<resources-index-links,Links>> to other resources"),
                                fieldWithPath("_embedded.createdBy").description("The user who created this project"),
                                fieldWithPath("_embedded.moderators").description("An array of all project moderators with email and link to their <<resources-user-get,User resource>>")
                        )));
    }
}
