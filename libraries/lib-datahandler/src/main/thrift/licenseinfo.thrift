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

namespace java com.siemens.sw360.datahandler.thrift.licenseinfo
namespace php sw360.thrift.licenseinfo

typedef components.Release Release
typedef components.Attachment Attachment

enum LicenseInfoRequestStatus{
    SUCCESS = 0,
    NO_APPLICABLE_SOURCE = 1,
    FAILURE = 2,
}

struct LicenseInfo {
    10: optional list<string> filenames, // actual sources used
    11: required string filetype, // actual parser type used

    20: optional set<string> copyrights,
    21: optional set<string> licenseTexts,
}

struct LicenseInfoParsingResult {
    1: required LicenseInfoRequestStatus status,
    2: optional string message,
    3: optional LicenseInfo licenseInfo,
}

service LicenseInfoService {

    /**
     * Returns the CLI contained in the attachment, if any
     * */
    LicenseInfoParsingResult getLicenseInfoForAttachment(1: Attachment attachment);

    /**
     * Returns the CLI for the given release.
     * */
    LicenseInfoParsingResult getLicenseInfoForRelease(1: Release release);

}
