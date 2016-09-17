/*
 * Copyright Bosch Software Innovations GmbH, 2016.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.licenseinfo;

import com.siemens.sw360.datahandler.couchdb.AttachmentConnector;
import com.siemens.sw360.datahandler.thrift.SW360Exception;
import com.siemens.sw360.datahandler.thrift.attachments.Attachment;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentContent;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentType;
import com.siemens.sw360.datahandler.thrift.licenseinfo.LicenseInfo;
import com.siemens.sw360.datahandler.thrift.licenseinfo.LicenseInfoParsingResult;
import com.siemens.sw360.datahandler.thrift.licenseinfo.LicenseInfoRequestStatus;
import com.siemens.sw360.licenseinfo.parsers.AttachmentContentProvider;
import org.apache.commons.io.input.ReaderInputStream;

import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.mockito.Mockito.when;

/**
 * @author: maximilian.huber@tngtech.com
 */
public class TestHelper {

    public static InputStream makeAttachmentContentStream(String filename){
        return ClassLoader.getSystemResourceAsStream(filename);
    }

    public static AttachmentContent makeAttachmentContent(String filename){
        return new AttachmentContent()
                .setId(filename)
                .setFilename(filename);
    }

    public static Attachment makeAttachment(String filename, AttachmentType attachmentType){
        return new Attachment()
                .setAttachmentContentId(filename)
                .setFilename(filename)
                .setAttachmentType(attachmentType);
    }

    //==================================================================================================================
    // AttachmentContentStore:
    public static class AttachmentContentStore{
        private Map<String,AttachmentContent> store;
        private AttachmentConnector connectorMock;

        public AttachmentContentStore(AttachmentConnector connectorMock){
            store = new HashMap<>();
            this.connectorMock = connectorMock;
        }

        public AttachmentContentProvider getAttachmentContentProvider(){
            return this::get;
        }

        public AttachmentContent get(Attachment attachment){
            return get(attachment.getAttachmentContentId());
        }

        public AttachmentContent get(String attachmentContentId){
            return store.get(attachmentContentId);
        }

        public AttachmentContentStore put(String filename, String fileContent) throws SW360Exception {
            AttachmentContent attachmentContent = makeAttachmentContent(filename);
            store.put(attachmentContent.getId(), attachmentContent);
            when(connectorMock.getAttachmentStream(attachmentContent)).thenReturn(new ReaderInputStream(new StringReader(fileContent)));
            return this;
        }

        public AttachmentContentStore put(String filename) throws SW360Exception {
            return put(makeAttachmentContent(filename));
        }

        public AttachmentContentStore put(AttachmentContent attachmentContent) throws SW360Exception {
            store.put(attachmentContent.getId(), attachmentContent);
            when(connectorMock.getAttachmentStream(attachmentContent)).thenReturn(makeAttachmentContentStream(attachmentContent.getFilename()));
            return this;
        }
    }

    //==================================================================================================================
    // Assertions:
    public static void assertLicenseInfo(String expectedFiletype, LicenseInfo info){
        assertLicenseInfo(expectedFiletype, info, true);
    }

    public static void assertLicenseInfo(String expectedFiletype, LicenseInfo info, boolean assertNonempty){
        assertThat(info.getFilenames(), notNullValue());
        assertThat(info.getFilenames().size(), greaterThan(0));
        assertThat(info.getFiletype(), is(expectedFiletype));

        if(assertNonempty){
            assertThat(info.getCopyrights(), notNullValue());
            assertThat(info.getLicenseNamesWithTexts(), notNullValue());
            assertThat(info.getLicenseNamesWithTexts().stream()
                    .filter(lt -> lt.isSetLicenseText())
                    .findAny()
                    .isPresent(), is(true));
        }
    }

    public static void assertLicenseInfoParsingResult(String expectedFiletype, LicenseInfoParsingResult result){
        assertLicenseInfoParsingResult(expectedFiletype, result, LicenseInfoRequestStatus.SUCCESS);
    }

    public static void assertLicenseInfoParsingResult(String expectedFiletype, LicenseInfoParsingResult result, LicenseInfoRequestStatus status){
        assertThat(result.getStatus(), is(status));
        assertThat(result.getLicenseInfo(), notNullValue());
        assertLicenseInfo(expectedFiletype, result.getLicenseInfo(), status == LicenseInfoRequestStatus.SUCCESS);
    }
}
