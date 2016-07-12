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
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentContent;
import com.siemens.sw360.datahandler.thrift.cliservice.CopyrightLicenseInfo;

/**
 * @author: alex.borodin@evosoft.com
 */
public abstract class CopyrightLicenseInfoParser {
    //    private static final Logger log = Logger.getLogger(CLIHandler.class);
    public static final String FILETYPE_PARSING_IMPOSSIBLE = "NONE";
    protected final AttachmentConnector attachmentConnector;

    protected CopyrightLicenseInfoParser(AttachmentConnector attachmentConnector){
        this.attachmentConnector = attachmentConnector;
    }

    public abstract boolean isApplicableTo(AttachmentContent attachmentContent);
    public abstract CopyrightLicenseInfo getCLI(AttachmentContent attachmentContent);
}
