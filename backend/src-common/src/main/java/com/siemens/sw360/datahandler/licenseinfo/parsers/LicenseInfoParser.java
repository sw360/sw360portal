/*
 * Copyright Siemens AG, 2016. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.datahandler.licenseinfo.parsers;

import com.siemens.sw360.datahandler.couchdb.AttachmentConnector;
import com.siemens.sw360.datahandler.thrift.attachments.Attachment;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentContent;
import com.siemens.sw360.datahandler.thrift.licenseinfo.LicenseInfoParsingResult;

import java.util.function.Function;

/**
 * @author: alex.borodin@evosoft.com
 */
public abstract class LicenseInfoParser {
    protected final AttachmentConnector attachmentConnector;
    protected Function<Attachment, AttachmentContent> attachmentContentProvider;

    protected LicenseInfoParser(AttachmentConnector attachmentConnector, Function<Attachment, AttachmentContent> attachmentContentProvider){
        this.attachmentConnector = attachmentConnector;
        this.attachmentContentProvider = attachmentContentProvider;
    }

    public abstract boolean isApplicableTo(Attachment attachmentContent);
    public abstract LicenseInfoParsingResult getLicenseInfo(Attachment attachment);
}
