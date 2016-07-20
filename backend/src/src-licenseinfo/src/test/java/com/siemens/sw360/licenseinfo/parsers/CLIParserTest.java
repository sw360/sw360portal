/*
 * Copyright Siemens AG, 2016. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.licenseinfo.parsers;

import com.siemens.sw360.datahandler.couchdb.AttachmentConnector;
import com.siemens.sw360.datahandler.thrift.attachments.Attachment;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentContent;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentType;
import com.siemens.sw360.datahandler.thrift.licenseinfo.LicenseInfoParsingResult;
import com.siemens.sw360.datahandler.thrift.licenseinfo.LicenseInfoRequestStatus;
import org.apache.commons.io.input.ReaderInputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.StringReader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

/**
 * @author: alex.borodin@evosoft.com
 */
@RunWith(MockitoJUnitRunner.class)
public class CLIParserTest {
    private static final String CLI_TESTFILE = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n" +
            "<ComponentLicenseInformation component=\"Clearing_Report_jquery-1_12_1\" creator=\"ite40294\" date=\"30/06/2016\"  baseDoc=\"Clearing_Report_jquery-1_12_1.doc\" toolUsed=\"ReadMe Generator V0.86\" componentID=\"-1\" >\n" +
            "<License type=\"global\" name=\"MIT License\" spdxidentifier=\"n/a\" > \n" +
            "<Content><![CDATA[jQuery projects are released under the terms of the MIT license.\n" +
            "]]></Content>\n" +
            "<Files><![CDATA[Found in:\n" +
            "\n" +
            "https://jquery.org/license/ \n" +
            "]]></Files>\n" +
            "</License>\n" +
            "<Copyright>\n" +
            "<Content><![CDATA[Copyrights\n" +
            "]]></Content>\n" +
            "<Files><![CDATA[Found in:\n" +
            "]]></Files>\n" +
            "</Copyright>\n" +
            "<Copyright>\n" +
            "<Content><![CDATA[(c) jQuery Foundation, Inc. | jquery.org\n" +
            "]]></Content>\n" +
            "<Files><![CDATA[\\jquery-1.12.1.min.js\n" +
            "]]></Files>\n" +
            "</Copyright>\n" +
            "</ComponentLicenseInformation>";

    @Mock
    private AttachmentConnector connector;
    private CLIParser parser;
    private AttachmentContent content;
    private Attachment attachment;

    @Before
    public void setUp() throws Exception {
        attachment = new Attachment("A1", "a.xml").setAttachmentType(AttachmentType.COMPONENT_LICENSE_INFO_XML);
        content = new AttachmentContent().setId("A1").setFilename("a.xml").setContentType("application/xml");
        parser = new CLIParser(connector, attachment -> content);
    }

    @Test
    public void testIsApplicableTo() throws Exception {
        when(connector.getAttachmentStream(content)).thenReturn(new ReaderInputStream(new StringReader(CLI_TESTFILE)));
        assertTrue(parser.isApplicableTo(attachment));
    }

    @Test
    public void testIsApplicableToFailsOnIncorrectRootElement() throws Exception {
        AttachmentContent content = new AttachmentContent().setId("A1").setFilename("a.xml").setContentType("application/xml");
        when(connector.getAttachmentStream(content)).thenReturn(new ReaderInputStream(new StringReader("<wrong-root/>")));
        assertFalse(parser.isApplicableTo(attachment));
    }

    @Test
    public void testIsApplicableToFailsOnMalformedXML() throws Exception {
        AttachmentContent content = new AttachmentContent().setId("A1").setFilename("a.xml").setContentType("application/xml");
        when(connector.getAttachmentStream(content)).thenReturn(new ReaderInputStream(new StringReader("this is not an xml file")));
        assertFalse(parser.isApplicableTo(attachment));
    }

    @Test
    public void testGetCLI() throws Exception {
        Attachment cliAttachment = new Attachment("A1", "a.xml");
        when(connector.getAttachmentStream(anyObject())).thenReturn(new ReaderInputStream(new StringReader(CLI_TESTFILE)));
        LicenseInfoParsingResult res = parser.getLicenseInfo(cliAttachment);
        assertThat(res.getStatus(), is(LicenseInfoRequestStatus.SUCCESS));
        assertThat(res.getLicenseInfo(), notNullValue());
        assertThat(res.getLicenseInfo().getFilenames(), contains("a.xml"));
        assertThat(res.getLicenseInfo().getFiletype(), is(CLIParser.FILETYPE_CLI));
        assertThat(res.getLicenseInfo().getLicenseTexts().size(), is(1));
        assertThat(res.getLicenseInfo().getLicenseTexts(), containsInAnyOrder("jQuery projects are released under the terms of the MIT license."));
        assertThat(res.getLicenseInfo().getCopyrights().size(), is(2));
        assertThat(res.getLicenseInfo().getCopyrights(), containsInAnyOrder("Copyrights", "(c) jQuery Foundation, Inc. | jquery.org"));

    }

    @Test
    public void testGetCLIFailsOnMalformedXML() throws Exception {
        Attachment cliAttachment = new Attachment("A1", "a.xml");
        when(connector.getAttachmentStream(anyObject())).thenReturn(new ReaderInputStream(new StringReader(CLI_TESTFILE.replaceAll("</Content>", "</Broken>"))));
        LicenseInfoParsingResult res = parser.getLicenseInfo(cliAttachment);
        assertThat(res.getStatus(), is(LicenseInfoRequestStatus.FAILURE));
        assertThat(res.getLicenseInfo(), notNullValue());
        assertThat(res.getLicenseInfo().getFilenames(), contains("a.xml"));
        assertThat(res.getLicenseInfo().getFiletype(), is(CLIParser.FILETYPE_CLI));

    }

}