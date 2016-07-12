/*
 * Copyright Siemens AG, 2016. Part of the SW360 Portal Project.
 * With contributions by Bosch Software Innovations GmbH, 2016.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
include "components.thrift"

namespace java com.siemens.sw360.datahandler.thrift.cliservice
namespace php sw360.thrift.cliservice

typedef components.Release Release

struct CopyrightLicenseInfo {
    10: optional list<string> filenames, // actual sources used
    11: optional string filetype, // actual parser type used

    20: optional set<string> copyrights,
    21: optional set<string> licenseTexts,
}

service CopyrightLicenseInfoService {

    /**
     * Returns the CLI contained in the attachment with given attachmentContentId, if any
     * If there exists no parser for the given attachment, returned CopyrightLicenseInfo has filetype "NONE"
     * */
    CopyrightLicenseInfo getComponentLicenseInfoForAttachment(1: string attachmentContentId);

    /**
     * Returns the CLI for the given release.
     * If no attachments with CLI information are found, returned CopyrightLicenseInfo has filetype "NONE"
     * */
    CopyrightLicenseInfo getComponentLicenseInfoForRelease(1: Release release);

}
