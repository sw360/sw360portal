/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.importer;

import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.siemens.sw360.datahandler.common.ImportCSV;
import com.siemens.sw360.datahandler.thrift.ThriftClients;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentService;
import com.siemens.sw360.datahandler.thrift.components.ComponentService;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.thrift.vendors.VendorService;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import java.io.IOException;
import java.util.List;

import static com.siemens.sw360.importer.ComponentImportUtils.*;

/**
 * Main class of the component importer
 *
 * @author daniele.fognini@tngtech.com
 * @author johannes.najjar@tngtech.com
 */
public class ReleaseImporter {
    private static final Logger log = Logger.getLogger(ReleaseImporter.class);

    public static void main(String[] args) throws IOException, TException {

        List<CSVRecord> releaseRecords = ImportCSV.readAsCSVRecords(getURL("components3.csv").openStream());
        FluentIterable<ComponentCSVRecord> compCSVRecords = convertCSVRecordsToCompCSVRecords(releaseRecords);
        log.trace("read records <" + Joiner.on("\n").join(compCSVRecords) + ">");

        final ComponentService.Iface componentClient;
        final VendorService.Iface vendorClient;
        final AttachmentService.Iface attachmentClient;
        final User byEmail;

        try {
            ThriftClients thriftClients = new ThriftClients();
            componentClient = thriftClients.makeComponentClient();
            vendorClient = thriftClients.makeVendorClient();
            attachmentClient = thriftClients.makeAttachmentClient();
            byEmail = thriftClients.makeUserClient().getByEmail("admin@siemens.com");
        } catch (TException e) {
            log.error("cannot communicate with backend", e);
            return;
        }

        writeToDatabase(compCSVRecords, componentClient, vendorClient, attachmentClient, byEmail);

        log.debug("Import done.");
    }


}
