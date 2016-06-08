/*
 * Copyright Siemens AG, 2016. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
include "sw360.thrift"
include "users.thrift"

namespace java com.siemens.sw360.datahandler.thrift.vulnerabilities
namespace php sw360.thrift.vulnerabilities

typedef sw360.RequestSummary RequestSummary
typedef users.User User
typedef sw360.RequestStatus RequestStatus

struct ReleaseVulnerabilityRelation{
    // Basic information
    1: optional string id,
    2: optional string revision,
    3: optional string type = "releasevulnerabilityrelation",

    // Additional information
    10: required string releaseId,
    11: required string vulnerabilityId,
}

struct Vulnerability{
    // General information
    1: optional string id,
    2: optional string revision,
    3: optional string type = "vulnerability",
    4: optional string lastUpdateDate,

    // Additional information
    10: required string externalId,
    11: optional string title,
    12: optional string description,
    13: optional string publishDate,
    14: optional string lastExternalUpdate,
    15: optional string priority,
    16: optional string priorityText,
    17: optional string action,
    19: optional string impact,
    20: optional string legalNotice,
    21: optional set<string> assignedExtComponentIds,
    22: optional set<CVEReference> cveReferences,
    23: optional set<VendorAdvisory> vendorAdvisories,
    24: optional string extendedDescription,
    25: optional set<string> references,
}

struct VulnerabilityDTO{
    // WILL NOT BE SAVED IN DB, only for view
    // General information
    1: optional string id,
    2: optional string revision,
    3: optional string type = "vulnerabilitydto",

    // Additional information
    10: required string externalId,
    11: optional string title,
    12: optional string description,
    13: optional string publishDate,
    14: optional string lastExternalUpdate,
    15: optional string priority,
    16: optional string priorityToolTip,
    17: optional string action,
    19: optional string impact,
    20: optional string legalNotice,
    22: optional set<CVEReference> cveReferences,
    25: optional set<string> references,

    // additional DTO fields
    31: optional string intReleaseId
    32: optional string intReleaseName
    33: optional string intComponentId
    34: optional string intComponentName
}

struct CVEReference{
    // Basic information
    1: optional string id,
    2: optional string revision,
    3: optional string type = "cveReference",

    // Additional information
    10: required string year,
    11: required string number,
}

struct VendorAdvisory{
    // Basic information
    1: optional string id,
    2: optional string revision,
    3: optional string type = "vendoradvisory",

    // Additional information
    10: required string vendor,
    11: required string name,
    12: required string url
}

enum VulnerabilityRatingForProject {
    NOT_CHECKED = 0,
    IRRELEVANT = 1,
    RESOLVED = 2,
    APPLICABLE = 3,
}

struct VulnerabilityCheckStatus{
    1: required string checkedOn,
    2: required string checkedBy,
    3: optional string comment,
    4: required VulnerabilityRatingForProject vulnerabilityRating,
}

struct ProjectVulnerabilityLink{

    1: optional string id,
    2: optional string revision,
    3: optional string type = "projectvulnerabilitylink",

    4: required string projectId,
    //keys are the externalIds of the vulnerabilities
    5: required map<string, VulnerabilityCheckStatus> vulnerabilityIdToStatus,
}

service VulnerabilityService {
    // General information
    list<VulnerabilityDTO> getVulnerabilitiesByReleaseId(1: string releaseId, 2: User user);
    list<VulnerabilityDTO> getVulnerabilitiesByComponentId(1: string componentId, 2: User user);
    list<VulnerabilityDTO> getVulnerabilitiesByProjectId(1: string projectId, 2: User user);

    /**
     * returns list with one ProjectVulnerabilityLink for given projectId
     * returns emptyList if none is found
     **/
    list<ProjectVulnerabilityLink> getProjectVulnerabilityLinkByProjectId(1: string projectId, 2: User user);

    RequestStatus updateProjectVulnerabilityLink(1: ProjectVulnerabilityLink link, 2: User user);
}
