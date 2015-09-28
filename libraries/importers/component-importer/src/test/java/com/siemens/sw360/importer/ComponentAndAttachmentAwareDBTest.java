/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License Version 2.0 as published by the
 * Free Software Foundation with classpath exception.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License version 2.0 for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (please see the COPYING file); if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package com.siemens.sw360.importer;

import com.google.common.collect.FluentIterable;
import com.siemens.sw360.attachments.AttachmentHandler;
import com.siemens.sw360.attachments.db.AttachmentRepository;
import com.siemens.sw360.components.ComponentHandler;
import com.siemens.sw360.datahandler.common.DatabaseSettings;
import com.siemens.sw360.datahandler.common.ImportCSV;
import com.siemens.sw360.datahandler.couchdb.DatabaseConnector;
import com.siemens.sw360.datahandler.thrift.ThriftClients;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentService;
import com.siemens.sw360.datahandler.thrift.components.ComponentService;
import com.siemens.sw360.datahandler.thrift.moderation.ModerationService;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.thrift.vendors.VendorService;
import com.siemens.sw360.vendors.VendorHandler;
import org.apache.commons.csv.CSVRecord;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.List;

import static com.siemens.sw360.datahandler.TestUtils.*;
import static com.siemens.sw360.importer.ComponentImportUtils.convertCSVRecordsToCompCSVRecords;
import static com.siemens.sw360.importer.ComponentImportUtils.convertCSVRecordsToComponentAttachmentCSVRecords;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author daniele.fognini@tngtech.com
 */
public class ComponentAndAttachmentAwareDBTest {

    protected ComponentService.Iface componentClient;
    protected VendorService.Iface vendorClient;
    protected AttachmentService.Iface attachmentClient;
    protected AttachmentRepository attachmentRepository;
    protected User user;

    protected  static  DatabaseConnector getDBConnector(String couchDbDatabase) throws MalformedURLException {
        return new DatabaseConnector(DatabaseSettings.COUCH_DB_URL, couchDbDatabase);
    }


    protected static AttachmentRepository getAttachmentRepository() throws MalformedURLException {
        return new AttachmentRepository(getDBConnector(DatabaseSettings.COUCH_DB_ATTACHMENTS));
    }

    protected static FluentIterable<ComponentCSVRecord> getCompCSVRecordsFromTestFile(String fileName) throws IOException {
        InputStream testStream = spy(ComponentImportUtilsTest.class.getResourceAsStream(fileName));

        List<CSVRecord> testRecords = ImportCSV.readAsCSVRecords(testStream);
        verify(testStream).close();
        return convertCSVRecordsToCompCSVRecords(testRecords);
    }

    protected static FluentIterable<ComponentAttachmentCSVRecord> getCompAttachmentCSVRecordsFromTestFile(String fileName) throws IOException {
        InputStream testStream = spy(ComponentImportUtilsTest.class.getResourceAsStream(fileName));

        List<CSVRecord> testRecords = ImportCSV.readAsCSVRecords(testStream);
        verify(testStream).close();
        return convertCSVRecordsToComponentAttachmentCSVRecords(testRecords);
    }

    protected void deleteDatabases() throws MalformedURLException {
        deleteDatabase(DatabaseSettings.COUCH_DB_URL, DatabaseSettings.COUCH_DB_ATTACHMENTS);
        deleteDatabase(DatabaseSettings.COUCH_DB_URL, DatabaseSettings.COUCH_DB_DATABASE);
    }
    protected static ThriftClients getThriftClients() throws TException, IOException {
        assertTestDbNames();

        ThriftClients thriftClients = failingMock(ThriftClients.class);

        ComponentHandler componentHandler = new ComponentHandler(thriftClients);
        VendorHandler vendorHandler = new VendorHandler();
        AttachmentHandler attachmentHandler = new AttachmentHandler();

        ModerationService.Iface moderationService = failingMock(ModerationService.Iface.class);

        doNothing().when(moderationService).deleteRequestsOnDocument(anyString());

        doReturn(componentHandler).when(thriftClients).makeComponentClient();
        doReturn(vendorHandler).when(thriftClients).makeVendorClient();
        doReturn(attachmentHandler).when(thriftClients).makeAttachmentClient();
        doReturn(moderationService).when(thriftClients).makeModerationClient();

        return thriftClients;
    }
    @Before
    public void setUp() throws Exception {
        deleteDatabases();

        ThriftClients thriftClients = getThriftClients();

        componentClient = thriftClients.makeComponentClient();
        vendorClient = thriftClients.makeVendorClient();
        attachmentClient = thriftClients.makeAttachmentClient();
        attachmentRepository = getAttachmentRepository();
        user = getAdminUser(getClass());


    }

    @After
    public void tearDown() throws Exception {
        deleteDatabases();
    }
}
