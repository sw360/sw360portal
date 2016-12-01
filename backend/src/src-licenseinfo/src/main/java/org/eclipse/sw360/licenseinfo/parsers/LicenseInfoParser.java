/*
 * Copyright Siemens AG, 2016. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.licenseinfo.parsers;

import org.eclipse.sw360.datahandler.couchdb.AttachmentConnector;
import org.eclipse.sw360.datahandler.thrift.attachments.Attachment;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.LicenseInfoParsingResult;
import org.apache.thrift.TException;

/**
 * @author: alex.borodin@evosoft.com
 */
public abstract class LicenseInfoParser {
    protected final AttachmentConnector attachmentConnector;
    protected AttachmentContentProvider attachmentContentProvider;

    protected LicenseInfoParser(AttachmentConnector attachmentConnector, AttachmentContentProvider attachmentContentProvider){
        this.attachmentConnector = attachmentConnector;
        this.attachmentContentProvider = attachmentContentProvider;
    }

    public abstract boolean isApplicableTo(Attachment attachmentContent) throws TException;
    public abstract LicenseInfoParsingResult getLicenseInfo(Attachment attachment) throws TException;
}
