/*
 * Copyright Bosch Software Innovations GmbH, 2016.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.licenseinfo.parsers;

import com.siemens.sw360.datahandler.couchdb.AttachmentConnector;
import com.siemens.sw360.datahandler.thrift.SW360Exception;
import com.siemens.sw360.datahandler.thrift.attachments.Attachment;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentContent;
import com.siemens.sw360.datahandler.thrift.licenseinfo.LicenseInfo;
import com.siemens.sw360.datahandler.thrift.licenseinfo.LicenseInfoParsingResult;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.rdfparser.SPDXDocumentFactory;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;

import static com.siemens.sw360.licenseinfo.TestHelper.*;
import static com.siemens.sw360.licenseinfo.parsers.SPDXParser.FILETYPE_SPDX_EXTERNAL;
import static com.siemens.sw360.licenseinfo.parsers.SPDXParser.FILETYPE_SPDX_INTERNAL;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author: maximilian.huber@tngtech.com
 */
@RunWith(MockitoJUnitRunner.class)
public class SPDXParserTest {

    private SPDXParser parser;

    private AttachmentContentStore attachmentContentStore;

    @Mock
    private AttachmentConnector connector;

    public static final String spdxExampleFile = "SPDXRdfExample-v2.0.rdf";
    private static String baseUri;

    @Before
    public void setUp() throws Exception {
        attachmentContentStore = new AttachmentContentStore(connector);

        parser = new SPDXParser(connector, attachmentContentStore.getAttachmentContentProvider());

        attachmentContentStore.put(spdxExampleFile, SPDXParser.ACCEPTABLE_ATTACHMENT_CONTENT_TYPES.get(0));
    }

    private void assertIsResultOfExample(LicenseInfo result){
        assertLicenseInfo(FILETYPE_SPDX_EXTERNAL, result);

        assertThat(result.getFilenames().size(), is(1));
        assertThat(result.getFilenames().get(0), is(spdxExampleFile));

        assertThat(result.getLicenseTextsSize(), is(5));
        assertThat(result.getLicenseTexts().stream()
                .map(l -> l.contains("\"THE BEER-WARE LICENSE\""))
                .filter(b -> b)
                .findAny()
                .get(),
                is(true));
        assertThat(result.getCopyrightsSize(), is(1));
        assertThat(result.getCopyrights(), hasItem("Copyright 2008-2010 John Smith"));
    }

    @Test
    public void testIsApplicableTo() throws Exception {
        try {
            SPDXParser.ACCEPTABLE_ATTACHMENT_CONTENT_TYPES.stream()
                    .forEach(contentType -> SPDXParser.ACCEPTABLE_ATTACHMENT_FILE_EXTENSIONS.stream()
                            .forEach(extension -> {
                                String filename = "filename." + extension;
                                try {
                                    attachmentContentStore.put(filename, contentType, "");
                                } catch (SW360Exception e) {
                                    throw new UncheckedSW360Exception(e);
                                }
                                Attachment attachment = makeAttachment(filename);
                                try {
                                    assertThat(parser.isApplicableTo(attachment), is(true));
                                } catch (TException e) {
                                    e.printStackTrace();
                                }
                            }));
        }catch (UncheckedSW360Exception se){
            throw se.getSW360ExceptionCause();
        }
    }

    @Test
    public void testAddSPDXContentToCLI() throws Exception {
        LicenseInfo emptyResult = new LicenseInfo().setFilenames(Arrays.asList(spdxExampleFile));

        InputStream input = makeAttachmentContentStream(spdxExampleFile);
        SpdxDocument spdxDocument = SPDXDocumentFactory.createSpdxDocument(input,
                parser.getUriOfAttachment(attachmentContentStore.get(spdxExampleFile)),
                FILETYPE_SPDX_INTERNAL);

        Optional<LicenseInfo> resultO = parser.addSpdxContentToCLI(emptyResult, spdxDocument);
        assertThat(resultO.isPresent(), is(true));

        assertIsResultOfExample(resultO.get());
    }

    @Test
    public void testGetLicenseInfo() throws Exception {

        AttachmentContent attachmentContent = attachmentContentStore.get(spdxExampleFile);
        Attachment attachment = new Attachment(spdxExampleFile, spdxExampleFile);

        LicenseInfoParsingResult result = parser.getLicenseInfo(attachment);

        assertLicenseInfoParsingResult(FILETYPE_SPDX_EXTERNAL, result);
        assertIsResultOfExample(result.getLicenseInfo());
    }

    class UncheckedSW360Exception extends RuntimeException{
        UncheckedSW360Exception(SW360Exception se) {
            super(se);
        }

        SW360Exception getSW360ExceptionCause(){
            return (SW360Exception) getCause();
        }
    }
}