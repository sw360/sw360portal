/*
 * Copyright Siemens AG, 2016. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.licenseinfo;

import com.google.common.annotations.VisibleForTesting;
import com.siemens.sw360.datahandler.db.AttachmentDatabaseHandler;
import com.siemens.sw360.datahandler.common.DatabaseSettings;
import com.siemens.sw360.datahandler.licenseinfo.LicenseInfoBackendHandler;
import com.siemens.sw360.datahandler.licenseinfo.UncheckedTException;
import com.siemens.sw360.datahandler.thrift.attachments.Attachment;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentContent;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.licenseinfo.LicenseInfoParsingResult;
import com.siemens.sw360.datahandler.thrift.licenseinfo.LicenseInfoService;
import com.siemens.sw360.datahandler.licenseinfo.parsers.CLIParser;
import com.siemens.sw360.datahandler.licenseinfo.parsers.LicenseInfoParser;
import com.siemens.sw360.datahandler.licenseinfo.parsers.SPDXParser;
import org.apache.thrift.TException;

import java.net.MalformedURLException;
import java.util.function.Function;

import static com.siemens.sw360.datahandler.common.SW360Assert.assertNotNull;

/**
 * Implementation of the Thrift service
 *
 * @author alex.borodin@evosoft.com
 */
public class LicenseInfoHandler implements LicenseInfoService.Iface {

    private final AttachmentDatabaseHandler attachmentDatabaseHandler;

    private final LicenseInfoBackendHandler licenseInfoBackendHandler;

    public LicenseInfoHandler() throws MalformedURLException {
        this(new AttachmentDatabaseHandler(DatabaseSettings.COUCH_DB_URL, DatabaseSettings.COUCH_DB_DATABASE, DatabaseSettings.COUCH_DB_ATTACHMENTS));
    }

    @VisibleForTesting
    public LicenseInfoHandler(AttachmentDatabaseHandler handler) throws MalformedURLException {
        attachmentDatabaseHandler = handler;
        licenseInfoBackendHandler = new LicenseInfoBackendHandler(attachmentDatabaseHandler);
    }

    @Override
    public LicenseInfoParsingResult getLicenseInfoForAttachment(Attachment attachment) throws TException {
        assertNotNull(attachment);
        return licenseInfoBackendHandler.getLicenseInfoForAttachment(attachment);
    }

    @Override
    public LicenseInfoParsingResult getLicenseInfoForRelease(Release release) throws TException {
        assertNotNull(release);
        return licenseInfoBackendHandler.getLicenseInfoForRelease(release);
    }

}
