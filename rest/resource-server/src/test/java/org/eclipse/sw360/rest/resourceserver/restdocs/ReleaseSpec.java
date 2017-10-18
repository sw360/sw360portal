/*
 * Copyright Siemens AG, 2017. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.sw360.rest.resourceserver.restdocs;

import org.eclipse.sw360.datahandler.thrift.components.ClearingState;
import org.eclipse.sw360.datahandler.thrift.components.Component;
import org.eclipse.sw360.datahandler.thrift.components.ComponentType;
import org.eclipse.sw360.datahandler.thrift.components.Release;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.rest.resourceserver.TestHelper;
import org.eclipse.sw360.rest.resourceserver.release.Sw360ReleaseService;
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

public class ReleaseSpec extends RestDocsSpecBase {

    @MockBean
    private Sw360UserService userServiceMock;

    @MockBean
    private Sw360ReleaseService releaseServiceMock;

    private Release release;

    @Before
    public void before() {
        Component component = new Component();
        component.setId("17653524");
        component.setName("Angular");
        component.setDescription("Angular is a development platform for building mobile and desktop web applications.");
        component.setCreatedOn("2016-12-15");
        component.setCreatedBy("admin@sw360.org");
        component.setComponentType(ComponentType.OSS);
        component.setVendorNames(new HashSet<>(Collections.singletonList("Google")));
        component.setModerators(new HashSet<>(Arrays.asList("admin@sw360.org", "john@sw360.org")));

        List<Release> releaseList = new ArrayList<>();
        release = new Release();
        release.setId("3765276512");
        release.setName("Angular 2.3.0");
        release.setCpeid("cpe:/a:Google:Angular:2.3.0:");
        release.setType("release");
        release.setReleaseDate("2016-12-07");
        release.setVersion("2.3.0");
        release.setCreatedOn("2016-12-18");
        release.setCreatedBy("admin@sw360.org");
        release.setModerators(new HashSet<>(Arrays.asList("admin@sw360.org", "jane@sw360.org")));
        release.setComponentId(component.getId());
        release.setClearingState(ClearingState.APPROVED);
        release.setExternalIds(Collections.singletonMap("mainline-id-component", "1432"));
        releaseList.add(release);

        Release release2 = new Release();
        release2.setId("3765276512");
        release2.setName("Angular 2.3.1");
        release2.setCpeid("cpe:/a:Google:Angular:2.3.1:");
        release2.setType("release");
        release2.setReleaseDate("2016-12-15");
        release2.setVersion("2.3.1");
        release2.setCreatedOn("2016-12-18");
        release2.setCreatedBy("admin@sw360.org");
        release2.setModerators(new HashSet<>(Arrays.asList("admin@sw360.org", "jane@sw360.org")));
        release2.setComponentId(component.getId());
        release2.setClearingState(ClearingState.APPROVED);
        release2.setExternalIds(Collections.singletonMap("mainline-id-component", "4876"));
        releaseList.add(release2);

        given(this.releaseServiceMock.getReleasesForUser(anyObject())).willReturn(releaseList);
        given(this.releaseServiceMock.getReleaseForUserById(eq(release.getId()), anyObject())).willReturn(release);

        User user = new User();
        user.setId("admin@sw360.org");
        user.setEmail("admin@sw360.org");
        user.setFullname("John Doe");

        given(this.userServiceMock.getUserByEmail("admin@sw360.org")).willReturn(user);
    }

    @Test
    public void should_document_get_releases()  throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, "admin@sw360.org", "sw360-password");
        mockMvc.perform(get("/api/releases")
                .header("Authorization", "Bearer " + accessToken)
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                        links(
                                linkWithRel("curies").description("Curies are used for online documentation")
                        ),
                        responseFields(
                                fieldWithPath("_embedded.sw360:releases").description("An array of <<resources-releases, Releases resources>>"),
                                fieldWithPath("_links").description("<<resources-index-links,Links>> to other resources")
                        )));
    }

    @Test
    public void should_document_get_release() throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, "admin@sw360.org", "sw360-password");
        mockMvc.perform(get("/api/releases/" + release.getId())
                .header("Authorization", "Bearer " + accessToken)
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                        links(
                                linkWithRel("self").description("The <<resources-releases,Releases resource>>"),
                                linkWithRel("sw360:component").description("The link to the corresponding component"),
                                linkWithRel("curies").description("The curies for documentation")
                        ),
                        responseFields(
                                fieldWithPath("version").description("The version of the release"),
                                fieldWithPath("name").description("The name of the release, optional"),
                                fieldWithPath("clearingState").description("The clearing of the release, possible values are " + Arrays.asList(ClearingState.values())),
                                fieldWithPath("cpeId").description("The CPE id"),
                                fieldWithPath("releaseDate").description("The date of this release"),
                                fieldWithPath("createdOn").description("The creation date of the internal sw360 release"),
                                fieldWithPath("type").description("is always 'release'"),
                                fieldWithPath("externalIds").description("When releases are imported from other tools, the external ids can be stored here"),
                                fieldWithPath("_embedded.moderators").description("An array of all release moderators with email and link to their <<resources-user-get,User resource>>"),
                                fieldWithPath("_links").description("<<resources-index-links,Links>> to other resources")
                        )));
    }
}
