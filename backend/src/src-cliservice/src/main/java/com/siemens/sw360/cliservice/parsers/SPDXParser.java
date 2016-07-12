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

import java.util.Arrays;

/**
 * @author: alex.borodin@evosoft.com
 */
public class SPDXParser extends CopyrightLicenseInfoParser {
    private static final String FILETYPE_SPDX = "SPDX";

    public SPDXParser(AttachmentConnector attachmentConnector) {
        super(attachmentConnector);
    }

    @Override
    public boolean isApplicableTo(AttachmentContent attachmentContent) {
        return false;
    }

    @Override
    public CopyrightLicenseInfo getCLI(AttachmentContent attachmentContent) {
        CopyrightLicenseInfo result = new CopyrightLicenseInfo().setFiletype(FILETYPE_PARSING_IMPOSSIBLE).setFilenames(Arrays.asList(attachmentContent.getFilename()));
        return result;
    }
}
