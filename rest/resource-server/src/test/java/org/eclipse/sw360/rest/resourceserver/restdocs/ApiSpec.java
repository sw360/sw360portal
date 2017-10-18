/*
 * Copyright Siemens AG, 2017. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.sw360.rest.resourceserver.restdocs;

import org.apache.thrift.TException;
import org.eclipse.sw360.rest.resourceserver.TestHelper;
import org.eclipse.sw360.rest.resourceserver.project.Sw360ProjectService;
import org.eclipse.sw360.rest.resourceserver.user.Sw360UserService;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ApiSpec extends RestDocsSpecBase {

    @MockBean
    private Sw360UserService userServiceMock;

    @MockBean
    private Sw360ProjectService projectServiceMock;

    @Test
    public void should_document_headers() throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, "admin@sw360.org", "sw360-password");

        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/api")
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                        responseHeaders(
                                headerWithName("Content-Type").description("The Content-Type of the payload, e.g. `application/hal+json`"))));
    }

    @Test
    public void should_document_errors() throws Exception {
        given(this.projectServiceMock.getProjectForUserById(anyString(), anyObject())).willThrow(new RuntimeException(new TException("Internal error processing getProjectById")));

        String accessToken = TestHelper.getAccessToken(mockMvc, "admin@sw360.org", "sw360-password");
        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/api/projects/12321")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isInternalServerError())
                .andDo(this.documentationHandler.document(
                        responseFields(
                                fieldWithPath("httpStatus").description("The HTTP status code, e.g. 500"),
                                fieldWithPath("httpError").description("The HTTP error code, e.g. Internal Server Error"),
                                fieldWithPath("message").description("The error message, e.g. an exception message"),
                                fieldWithPath("timestamp").description("The timestamp when the error occurred")



                                )));
    }

    @Test
    public void should_document_index() throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, "admin@sw360.org", "sw360-password");
        this.mockMvc.perform(get("/api")
                .header("Authorization", "Bearer " + accessToken)
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                        links(
                                linkWithRel("sw360:users").description("The <<resources-users,Users resource>>"),
                                linkWithRel("sw360:projects").description("The <<resources-projects,Projects resource>>"),
                                linkWithRel("sw360:components").description("The <<resources-components,Components resource>>"),
                                linkWithRel("sw360:releases").description("The <<resources-releases,Releases resource>>"),
                                linkWithRel("sw360:attachments").description("The <<resources-attachments,Attachments resource>>"),
                                linkWithRel("sw360:vendors").description("The <<resources-vendors,Vendors resource>>"),
                                linkWithRel("sw360:licenses").description("The <<resources-licenses,Licenses resource>>"),
                                linkWithRel("curies").description("The Curies for documentation"),
                                linkWithRel("profile").description("The profiles of the REST resources")
                        ),
                        responseFields(
                                fieldWithPath("_links").description("<<resources-index-links,Links>> to other resources"))));
    }

}
