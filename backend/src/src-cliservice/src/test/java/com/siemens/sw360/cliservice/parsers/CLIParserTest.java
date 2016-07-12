/*
 * Copyright Siemens AG, 2016. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.cliservice.parsers;

import com.siemens.sw360.datahandler.couchdb.AttachmentConnector;
import com.siemens.sw360.datahandler.thrift.SW360Exception;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentContent;
import com.siemens.sw360.datahandler.thrift.cliservice.CopyrightLicenseInfo;
import org.apache.commons.io.input.ReaderInputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.InputStream;
import java.io.StringReader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.isNull;
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

    @Before
    public void setUp() throws Exception {
        parser = new CLIParser(connector);
    }

    @Test
    public void testIsApplicableTo() throws Exception {
        AttachmentContent content = new AttachmentContent().setId("A1").setFilename("a.xml").setContentType("application/xml");
        when(connector.getAttachmentStream(content)).thenReturn(new ReaderInputStream(new StringReader(CLI_TESTFILE)));
        assertTrue(parser.isApplicableTo(content));
    }

    @Test
    public void testIsApplicableToFailsOnIncorrectRootElement() throws Exception {
        AttachmentContent content = new AttachmentContent().setId("A1").setFilename("a.xml").setContentType("application/xml");
        when(connector.getAttachmentStream(content)).thenReturn(new ReaderInputStream(new StringReader("<wrong-root/>")));
        assertFalse(parser.isApplicableTo(content));
    }

    @Test
    public void testIsApplicableToFailsOnMalformedXML() throws Exception {
        AttachmentContent content = new AttachmentContent().setId("A1").setFilename("a.xml").setContentType("application/xml");
        when(connector.getAttachmentStream(content)).thenReturn(new ReaderInputStream(new StringReader("this is not an xml file")));
        assertFalse(parser.isApplicableTo(content));
    }

    @Test
    public void testGetCLI() throws Exception {
        AttachmentContent cliAttachment = new AttachmentContent().setId("A1").setFilename("a.xml").setContentType("application/xml");
        when(connector.getAttachmentStream(cliAttachment)).thenReturn(new ReaderInputStream(new StringReader(CLI_TESTFILE)));
        CopyrightLicenseInfo res = parser.getCLI(cliAttachment);
        assertThat(res.getFilenames(), contains("a.xml"));
        assertThat(res.getFiletype(), is(CLIParser.FILETYPE_CLI));
        assertThat(res.getLicenseTexts().size(), is(1));
        assertThat(res.getLicenseTexts(), containsInAnyOrder("jQuery projects are released under the terms of the MIT license."));
        assertThat(res.getCopyrights().size(), is(2));
        assertThat(res.getCopyrights(), containsInAnyOrder("Copyrights", "(c) jQuery Foundation, Inc. | jquery.org"));

    }

    @Test
    public void testGetCLIFailsOnMalformedXML() throws Exception {
        AttachmentContent cliAttachment = new AttachmentContent().setId("A1").setFilename("a.xml").setContentType("application/xml");
        when(connector.getAttachmentStream(cliAttachment)).thenReturn(new ReaderInputStream(new StringReader(CLI_TESTFILE.replaceAll("</Content>", "</Broken>"))));
        CopyrightLicenseInfo res = parser.getCLI(cliAttachment);
        assertThat(res.getFilenames(), contains("a.xml"));
        assertThat(res.getFiletype(), is(CLIParser.FILETYPE_PARSING_IMPOSSIBLE));
        assertThat(res.getLicenseTexts(), nullValue());
        assertThat(res.getCopyrights(), nullValue());

    }

}