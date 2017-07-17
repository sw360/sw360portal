/*
 * Copyright Siemens AG, 2016. Part of the SW360 Portal Project.
 *
 * SPDX-License-Identifier: EPL-1.0
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.sw360.licenseinfo;

import org.eclipse.sw360.attachments.db.AttachmentDatabaseHandler;
import org.eclipse.sw360.datahandler.couchdb.AttachmentConnector;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LicenseInfoHandlerTest {

    private LicenseInfoHandler handler;

    @Mock
    private AttachmentDatabaseHandler attachmentDatabaseHandler;

    @Mock
    private AttachmentConnector connector;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        when(attachmentDatabaseHandler.getAttachmentConnector()).thenReturn(connector);
        handler = new LicenseInfoHandler(attachmentDatabaseHandler, null, null);
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