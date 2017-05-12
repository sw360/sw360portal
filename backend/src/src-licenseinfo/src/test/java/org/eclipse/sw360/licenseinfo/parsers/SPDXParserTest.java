/*
 * Copyright Bosch Software Innovations GmbH, 2016-2017.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.licenseinfo.parsers;

import org.eclipse.sw360.datahandler.common.SW360Constants;
import org.eclipse.sw360.datahandler.common.UncheckedSW360Exception;
import org.eclipse.sw360.datahandler.couchdb.AttachmentConnector;
import org.eclipse.sw360.datahandler.thrift.attachments.Attachment;
import org.eclipse.sw360.datahandler.thrift.attachments.AttachmentContent;
import org.eclipse.sw360.datahandler.thrift.attachments.AttachmentType;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.LicenseInfo;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.LicenseInfoParsingResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.rdfparser.SPDXDocumentFactory;

import java.io.InputStream;
import java.util.Arrays;

import static org.eclipse.sw360.licenseinfo.TestHelper.*;
import static org.eclipse.sw360.licenseinfo.parsers.SPDXParser.FILETYPE_SPDX_INTERNAL;
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

        attachmentContentStore.put(spdxExampleFile);
    }

    private void assertIsResultOfExample(LicenseInfo result){
        assertLicenseInfo(result);

        assertThat(result.getFilenames().size(), is(1));
        assertThat(result.getFilenames().get(0), is(spdxExampleFile));

        assertThat(result.getLicenseNamesWithTextsSize(), is(6));
        assertThat(result.getLicenseNamesWithTexts().stream()
                .map(lt -> lt.getLicenseText())
                .anyMatch(t -> t.contains("Free Software Foundation")),
                is(true));

        assertThat(result.getCopyrightsSize(), is(4));
        assertThat(result.getCopyrights().stream()
                        .anyMatch(c -> c.contains("Copyright 2008-2010 John Smith")),
                is(true));
    }

    @Test
    public void testAddSPDXContentToCLI() throws Exception {
        AttachmentContent attachmentContent = new AttachmentContent()
                .setFilename(spdxExampleFile);

        InputStream input = makeAttachmentContentStream(spdxExampleFile);
        SpdxDocument spdxDocument = SPDXDocumentFactory.createSpdxDocument(input,
                parser.getUriOfAttachment(attachmentContentStore.get(spdxExampleFile)),
                FILETYPE_SPDX_INTERNAL);

        LicenseInfoParsingResult result = SPDXParserTools.getLicenseInfoFromSpdx(attachmentContent, spdxDocument);
        assertIsResultOfExample(result.getLicenseInfo());
    }

    @Test
    public void testGetLicenseInfo() throws Exception {

        Attachment attachment = makeAttachment(spdxExampleFile,
                Arrays.stream(AttachmentType.values())
                        .filter(at -> SW360Constants.LICENSE_INFO_ATTACHMENT_TYPES.contains(at))
                        .findAny()
                        .get());

        LicenseInfoParsingResult result = parser.getLicenseInfos(attachment).stream().findFirst().orElseThrow(()->new RuntimeException("Parser returned empty LisenceInfoParsingResult list"));

        assertLicenseInfoParsingResult(result);
        assertIsResultOfExample(result.getLicenseInfo());
    }
}