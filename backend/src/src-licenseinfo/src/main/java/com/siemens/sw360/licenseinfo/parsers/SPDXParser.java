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
import com.siemens.sw360.datahandler.thrift.licenseinfo.LicenseInfoParsingResult;
import com.siemens.sw360.datahandler.thrift.licenseinfo.LicenseInfoRequestStatus;

import java.util.function.Function;

/**
 * @author: alex.borodin@evosoft.com
 */
public class SPDXParser extends LicenseInfoParser {
    private static final String FILETYPE_SPDX = "SPDX";

    public SPDXParser(AttachmentConnector attachmentConnector, Function<Attachment, AttachmentContent> attachmentContentProvider) {
        super(attachmentConnector, attachmentContentProvider);
    }

    @Override
    public boolean isApplicableTo(Attachment attachment) {
        return false;
    }

    @Override
    public LicenseInfoParsingResult getLicenseInfo(Attachment attachment) {
        return new LicenseInfoParsingResult().setStatus(LicenseInfoRequestStatus.NO_APPLICABLE_SOURCE);
    }
}
