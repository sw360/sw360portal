/*
 * Copyright Siemens AG, 2017. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.sw360.rest.resourceserver.restdocs;

import org.eclipse.sw360.datahandler.thrift.attachments.Attachment;
import org.eclipse.sw360.datahandler.thrift.attachments.AttachmentType;
import org.eclipse.sw360.datahandler.thrift.attachments.CheckStatus;
import org.eclipse.sw360.datahandler.thrift.components.ClearingState;
import org.eclipse.sw360.datahandler.thrift.components.Release;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.rest.resourceserver.TestHelper;
import org.eclipse.sw360.rest.resourceserver.attachment.AttachmentInfo;
import org.eclipse.sw360.rest.resourceserver.attachment.Sw360AttachmentService;
import org.eclipse.sw360.rest.resourceserver.user.Sw360UserService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AttachmentSpec extends RestDocsSpecBase {

    @MockBean
    private Sw360UserService userServiceMock;

    @MockBean
    private Sw360AttachmentService attachmentServiceMock;

    private Attachment attachment;

    @Before
    public void before() {
        List<Attachment> attachmentList = new ArrayList<>();
        attachment = new Attachment();

        attachment.setAttachmentContentId("76537653");
        attachment.setFilename("spring-core-4.3.4.RELEASE.jar");
        attachment.setSha1("da373e491d3863477568896089ee9457bc316783");
        attachment.setAttachmentType(AttachmentType.BINARY_SELF);
        attachment.setCreatedTeam("Clearing Team 1");
        attachment.setCreatedComment("please check before Christmas :)");
        attachment.setCreatedOn("2016-12-18");
        attachment.setCreatedBy("admin@sw360.org");
        attachment.setCheckedTeam("Clearing Team 2");
        attachment.setCheckedComment("everything looks good");
        attachment.setCheckedOn("2016-12-18");
        attachment.setCheckStatus(CheckStatus.ACCEPTED);

        attachmentList.add(attachment);

        Release release = new Release();
        release.setId("874687");
        release.setName("Spring Core 4.3.4");
        release.setCpeid("cpe:/a:pivotal:spring-core:4.3.4:");
        release.setType("release");
        release.setReleaseDate("2016-12-07");
        release.setVersion("4.3.4");
        release.setCreatedOn("2016-12-18");
        release.setCreatedBy("admin@sw360.org");
        release.setModerators(new HashSet<>(Arrays.asList("admin@sw360.org", "jane@sw360.org")));
        release.setComponentId("678dstzd8");
        release.setClearingState(ClearingState.APPROVED);

        AttachmentInfo attachmentInfo = new AttachmentInfo(attachment, release);

        given(this.attachmentServiceMock.getAttachmentByIdForUser(eq(attachment.getAttachmentContentId()), anyObject())).willReturn(attachmentInfo);

        User user = new User();
        user.setId("admin@sw360.org");
        user.setEmail("admin@sw360.org");
        user.setFullname("John Doe");

        given(this.userServiceMock.getUserByEmail("admin@sw360.org")).willReturn(user);
    }

    @Test
    public void should_document_get_attachment() throws Exception {
        String accessToken = TestHelper.getAccessToken(mockMvc, "admin@sw360.org", "sw360-password");
        mockMvc.perform(get("/api/attachments/" + attachment.getAttachmentContentId())
                .header("Authorization", "Bearer " + accessToken)
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andDo(this.documentationHandler.document(
                        links(
                                linkWithRel("self").description("The <<resources-projects,Projects resource>>"),
                                linkWithRel("sw360:release").description("The release this attachment belongs to"),
                                linkWithRel("curies").description("Curies are used for online documentation")
                        ),
                        responseFields(
                                fieldWithPath("filename").description("The filename of the attachment"),
                                fieldWithPath("sha1").description("The attachment's file contents sha1 hash"),
                                fieldWithPath("attachmentType").description("The attachment type, possible values are " + Arrays.asList(AttachmentType.values())),
                                fieldWithPath("createdTeam").description("The team who created this attachment"),
                                fieldWithPath("createdComment").description("Comment of the creating team"),
                                fieldWithPath("createdOn").description("The date the attachment was created"),
                                fieldWithPath("checkedTeam").description("The team who checked this attachment"),
                                fieldWithPath("checkedComment").description("Comment of the checking team"),
                                fieldWithPath("checkedOn").description("The date the attachment was checked"),
                                fieldWithPath("checkStatus").description("The checking status. possible values are " + Arrays.asList(CheckStatus.values())),
                                fieldWithPath("_embedded.createdBy").description("The user who created this attachment"),
                                fieldWithPath("_embedded.release").description("The release this attachment belongs to"),
                                fieldWithPath("_links").description("<<resources-index-links,Links>> to other resources")
                        )));
    }
}
