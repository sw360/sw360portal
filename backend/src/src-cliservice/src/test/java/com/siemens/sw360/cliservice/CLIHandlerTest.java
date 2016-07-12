/*
 * Copyright Siemens AG, 2016. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.siemens.sw360.cliservice;

import com.siemens.sw360.attachments.db.AttachmentDatabaseHandler;
import com.siemens.sw360.cliservice.parsers.CLIParser;
import com.siemens.sw360.datahandler.TestUtils;
import com.siemens.sw360.datahandler.common.DatabaseSettings;
import com.siemens.sw360.datahandler.couchdb.AttachmentConnector;
import com.siemens.sw360.datahandler.couchdb.DatabaseConnector;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentContent;
import com.siemens.sw360.datahandler.thrift.cliservice.CopyrightLicenseInfo;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.InputStream;
import java.io.StringReader;

import static com.siemens.sw360.datahandler.TestUtils.assertTestDbNames;
import static com.siemens.sw360.datahandler.TestUtils.deleteAllDatabases;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CLIHandlerTest {

    private CLIHandler handler;

    @Mock
    private AttachmentDatabaseHandler attachmentDatabaseHandler;

    @Mock
    private AttachmentConnector connector;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        when(attachmentDatabaseHandler.getAttachmentConnector()).thenReturn(connector);
        handler = new CLIHandler(attachmentDatabaseHandler);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testCallsCLIParser() throws TException {
        //// TODO: write some code here
    }

    @Test
    public void testCallsSPDXParser() throws TException {
        //// TODO: write some code here
    }
}