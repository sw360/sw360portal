/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * SPDX-License-Identifier: EPL-1.0
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.sw360.attachments;

import com.google.common.collect.ImmutableSet;
import org.eclipse.sw360.datahandler.TestUtils;
import org.eclipse.sw360.datahandler.common.DatabaseSettings;
import org.eclipse.sw360.datahandler.couchdb.DatabaseConnector;
import org.eclipse.sw360.datahandler.couchdb.DatabaseInstance;
import org.eclipse.sw360.datahandler.thrift.RequestStatus;
import org.eclipse.sw360.datahandler.thrift.RequestSummary;
import org.eclipse.sw360.datahandler.thrift.SW360Exception;
import org.eclipse.sw360.datahandler.thrift.attachments.AttachmentContent;
import org.eclipse.sw360.datahandler.thrift.attachments.DatabaseAddress;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.datahandler.thrift.users.UserGroup;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;


public class AttachmentHandlerTest {

    private static final String url = DatabaseSettings.COUCH_DB_URL;
    private static final String dbName = DatabaseSettings.COUCH_DB_ATTACHMENTS;

    private AttachmentHandler handler;


    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        // Create the database
        TestUtils.createDatabase(DatabaseSettings.getConfiguredHttpClient(), dbName);

        DatabaseConnector databaseConnector = new DatabaseConnector(DatabaseSettings.getConfiguredHttpClient(), dbName);
//        databaseConnector.add(new Attachment().setId("A1").setCreatedBy("user1@test.com").setCreatedOn("2012-07-30").setFilename("a.txt").setContentType("text"));
//        databaseConnector.add(new Attachment().setId("A2").setCreatedBy("user2@test.com").setCreatedOn("2012-05-22").setFilename("b.jpg").setContentType("image"));
        databaseConnector.add(new AttachmentContent().setId("A1").setFilename("a.txt").setContentType("text"));
        databaseConnector.add(new AttachmentContent().setId("A2").setFilename("b.jpg").setContentType("image"));

        handler = new AttachmentHandler();
    }

    @After
    public void tearDown() throws Exception {
        // Delete the database
        TestUtils.deleteDatabase(DatabaseSettings.getConfiguredHttpClient(), dbName);
    }

    @Test
    public void testGetAttachmentContent() throws Exception {
        AttachmentContent attachment = handler.getAttachmentContent("A1");
        assertEquals("A1", attachment.id);
        assertEquals("a.txt", attachment.filename);
        assertEquals("text", attachment.contentType);
    }

    @Test
    public void testVacuum_OnlyAdminCanRun() throws Exception {
        final RequestSummary requestSummary = handler.vacuumAttachmentDB(new User("a", "a", "a").setUserGroup(UserGroup.USER), ImmutableSet.of("A1", "A2"));
        assertThat(requestSummary.requestStatus, is(RequestStatus.FAILURE));
    }

    @Test
    public void testVacuum_AllIdsUsedIsNoop() throws Exception {
        final RequestSummary requestSummary = handler.vacuumAttachmentDB(new User("a", "a", "a").setUserGroup(UserGroup.ADMIN), ImmutableSet.of("A1", "A2"));
        assertThat(requestSummary.requestStatus, is(RequestStatus.SUCCESS));
        assertThat(requestSummary.totalElements, is(2));
        assertThat(requestSummary.totalAffectedElements, is(0));

        final AttachmentContent a1 = handler.getAttachmentContent("A1");
        assertNotNull(a1);
        final AttachmentContent a2 = handler.getAttachmentContent("A2");
        assertNotNull(a2);
    }


    @Test
    public void testVacuum_UnusedIdIsDeleted() throws Exception {
        final RequestSummary requestSummary = handler.vacuumAttachmentDB(new User("a", "a", "a").setUserGroup(UserGroup.ADMIN), ImmutableSet.of("A1"));
        assertThat(requestSummary.requestStatus, is(RequestStatus.SUCCESS));
        assertThat(requestSummary.totalElements, is(2));
        assertThat(requestSummary.totalAffectedElements, is(1));

        final AttachmentContent a1 = handler.getAttachmentContent("A1");
        assertNotNull(a1);
        exception.expect(SW360Exception.class);
        final AttachmentContent a2 = handler.getAttachmentContent("A2");
        assert(a2==null);

    }
}