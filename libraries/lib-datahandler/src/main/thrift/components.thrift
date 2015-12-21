/*
 * Copyright Siemens AG, 2014-2016. Part of the SW360 Portal Project.
 * With contributions by Bosch Software Innovations GmbH, 2016.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License Version 2.0 as published by the
 * Free Software Foundation with classpath exception.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License version 2.0 for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (please see the COPYING file); if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */
include "sw360.thrift"
include "attachments.thrift"
include "users.thrift"
include "vendors.thrift"
include "licenses.thrift"

namespace java com.siemens.sw360.datahandler.thrift.components
namespace php sw360.thrift.components

typedef sw360.RequestStatus RequestStatus
typedef sw360.RequestSummary RequestSummary
typedef sw360.DocumentState DocumentState
typedef attachments.Attachment Attachment
typedef attachments.FilledAttachment FilledAttachment
typedef users.User User
typedef users.RequestedAction RequestedAction
typedef vendors.Vendor Vendor

enum RepositoryType {
    UNKNOWN = 0,
    GIT = 14,
    CLEARCASE = 7,
    SVN = 1,
    CVS = 2,
    MERCURIAL = 16,
    PERFORCE = 4,
    VISUAL_SOURCESAFE = 6,
    BAZAAR = 11,
    ALIENBRAIN = 3,
    TEAM_FOUNDATION_SERVER = 5,
    RATIONAL_SYNERGY = 8,
    PTC_INTEGRITY = 9,
    DTR = 10,
    DARCS = 12,
    FOSSIL = 13,
    GNU_ARCH = 15,
    MONOTONE = 17,
    BIT_KEEPER = 18,
    RATIONAL_TEAM_CONCERT = 19
    RCS = 20,
 }


struct Repository {
    1: required string url,
    2: optional RepositoryType repositorytype
}

enum FossologyStatus {
    CONNECTION_FAILED = 0,

    ERROR = 1,

    NON_EXISTENT = 2,
    NOT_SENT = 3,
    INACCESSIBLE = 4,

    SENT = 10,
    SCANNING = 11,

    OPEN = 20,
    IN_PROGRESS = 21,
    CLOSED = 22,
    REJECTED = 23,

    REPORT_AVAILABLE = 30
}

enum ClearingState {
    NEW_CLEARING = 0,
    SENT_TO_FOSSOLOGY = 1,
    UNDER_CLEARING = 2,
    REPORT_AVAILABLE = 3,
    APPROVED = 4,
}

enum MainlineState {
    OPEN = 0,
    MAINLINE = 1,
    SPECIFIC = 2,
    PHASEOUT = 3,
}


enum ReleaseRelationship {
    CONTAINED = 0,
    REFERRED = 1,
    UNKNOWN = 2,
}
struct COTSDetails{
    1: optional string usedLicense,
    2: optional string licenseClearingReportURL,
    3: optional bool containsOSS,
    4: optional bool ossContractSigned,
    5: optional string ossInformationURL,
    6: optional bool usageRightAvailable,
}
struct ClearingInformation {
    // supplier / ec info
    1: optional string AL, // German Ausfuhrliste
    2: optional string ECCN, // European control classification number
    3: optional string externalSupplierID, // foreign key fur SCM software TODO mcj move to component
    4: optional string assessorContactPerson, // email of ECC person
    5: optional string assessorDepartment, // department of ECC person
    6: optional string eccComment, // comments for ecc information
    7: optional string materialIndexNumber, // six digit material index number, string for convenience

    // clearing related metadata part 1: strings,
    12: optional string additionalRequestInfo, //
    13: optional string evaluated, // Date - YYYY-MM-dd
    14: optional string procStart, // Date - YYYY-MM-dd
    15: optional string requestID, // foreign key
    16: optional string clearingTeam, // who did the clearing in org
    17: optional string requestorPerson, // again email who requested the clearing TODO mcj should be set automtically

    // clearing related data: release level - just boolean (Yes / no)
    31: optional bool binariesOriginalFromCommunity,
    32: optional bool binariesSelfMade,
    33: optional bool componentLicenseInformation,
    34: optional bool sourceCodeDelivery,
    35: optional bool sourceCodeOriginalFromCommunity,
    36: optional bool sourceCodeToolMade,
    37: optional bool sourceCodeSelfMade,
    38: optional bool sourceCodeCotsAvailable,
    39: optional bool screenshotOfWebSite,

    40: optional bool finalizedLicenseScanReport,
    41: optional bool licenseScanReportResult,
    42: optional bool legalEvaluation,
    43: optional bool licenseAgreement,
    44: optional string scanned,
    45: optional bool componentClearingReport,
    46: optional string clearingStandard,   // which generation of tool used
    47: optional bool readmeOssAvailable,

    // more release base data from mainline
    50: optional string comment,
    52: optional i32 countOfSecurityVn, // count of security vulnerabilities
    53: optional string externalUrl, // URL pointing to another system, TODO should be map
}

struct Release {
    // Basic information
    1: optional string id,
    2: optional string revision,
    3: optional string type = "release",
    4: optional string cpeid, // Unique CPE id for the release object
    5: required string name, // Release name (e.g. thrift), often identical to Component name
    6: required string version, // version or release name (e.g. 0.9.1)
    7: required string componentId, // Id of the parent component
    8: optional string releaseDate,

    // Additional informations
    10: optional set<Attachment> attachments,
    11: optional string createdOn, // Creation date YYYY-MM-dd
    12: optional Repository repository, // Repository where the release is maintained
    16: optional MainlineState mainlineState, // enum: specific, open, mainline, phaseout
    17: optional ClearingState clearingState, // TODO we probably need to map by clearing team?

    // FOSSology Information
    20: optional string fossologyId,
    21: optional map<string, FossologyStatus> clearingTeamToFossologyStatus,
    22: optional string attachmentInFossology, // id of the attachment currently in fossology

    // string details
    30: optional string createdBy, // person who created the release
    32: optional set<string> contacts, // contacts linked to the release
    34: optional set<string> moderators, // people who can modify the data
    36: optional set<string> subscribers, // List of subscribers

    40: optional Vendor vendor,
    41: optional string vendorId,

    50: optional ClearingInformation clearingInformation,
    51: optional set<string> languages,
    53: optional set<string> operatingSystems,
    54: optional COTSDetails cotsDetails,

    65: optional set<string> mainLicenseIds,
    66: optional set<string> mainLicenseNames,

    // Urls for the project
    70: optional string downloadurl, // URL for download page for this release

    80: optional map<string, ReleaseRelationship> releaseIdToRelationship,    //id, comment

    // Information for ModerationRequests
    90: optional DocumentState documentState,

    200: optional map<RequestedAction, bool> permissions,
}

enum ComponentType {
    SIEMENS = 0,
    OSS = 1,      //open source software
    COTS = 2,     //commercial of the shelf
    FREESOFTWARE = 3,
}

struct Component {

    // General information
    1: optional string id,
    2: optional string revision,
    3: optional string type = "component",

    5: required string name, // Component name (e.g. thrift)
    6: optional string description, // Short description about the component

    // Additional informations
    10: optional set<Attachment> attachments,
    11: optional string createdOn, // Creation date YYYY-MM-dd
    12: optional ComponentType componentType,

    // string details
    20: optional string createdBy, // person who created the component in sw360
    24: optional set<string> subscribers, // List of subscriber information
    25: optional set<string> moderators, // people who can modify the data

    // Linked objects
    32: optional list<Release> releases,
    33: optional set<string> releaseIds,

    35: optional set<string> mainLicenseIds,        //Aggregate of release main licenses
    36: optional set<string> mainLicenseNames,

    // List of keywords
    40: optional set<string> categories,
    41: optional set<string> languages,             //Aggregate of release languages
    42: optional set<string> softwarePlatforms,
    43: optional set<string> operatingSystems,      //Aggregate of release operatingSystems
    44: optional set<string> vendorNames,           //Aggregate of release vendor Fullnames

    // Urls for the component, TODO should be map
    50: optional string homepage,
    51: optional string mailinglist,
    52: optional string wiki,
    53: optional string blog,
    54: optional string wikipedia,
    55: optional string openHub,

    // Information for ModerationRequests
    70: optional DocumentState documentState,

    200: optional map<RequestedAction, bool> permissions,
}

struct ReleaseClearingStateSummary {
    1: required i32 newRelease,
    2: required i32 underClearing,
    3: required i32 underClearingByProjectTeam,
    4: required i32 reportAvailable,
    5: required i32 approved,
}

struct ReleaseLink{
    1: required string id,
    2: required string vendor,
    5: required string name,
    10: required string version,
    15: optional string comment,
    16: optional ReleaseRelationship releaseRelationship,
    //This is to indicate the depth of the link (I link to a, a links to b, so b has depth 2)
    25: optional i32 depth,

    100: optional set<string> licenseIds,
    101: optional set<string> licenseNames
}

service ComponentService {

    // General information
    list<Component> getComponentSummary(1: User user);
    list<Release> getReleaseSummary(1: User user);

    // Refine search
    list<Component> refineSearch(1: string text, 2: map<string ,  set<string > > subQueryRestrictions);
    list<Release> searchReleaseByName(1: string name);

    // Information for home portlets
    list<Component> getMyComponents(1: User user);
    list<Component> getSubscribedComponents(1: User user);
    list<Release> getSubscribedReleases(1: User user);
    list<Component> getRecentComponents();
    list<Release> getRecentReleases();

    // Component CRUD support
    string addComponent(1: Component component, 2: User user);
    Component getComponentById(1: string id, 2: User user);
    Component getComponentByIdForEdit(1: string id, 2: User user);
    RequestStatus updateComponent(1: Component component, 2: User user);
    RequestSummary updateComponents(1: set<Component> components, 2: User user);
    RequestStatus deleteComponent(1: string id, 2: User user);
    RequestStatus updateComponentFromModerationRequest(1: Component additions, 2: Component deletions, 3: User user);

    // Release CRUD support
    string addRelease(1: Release release, 2: User user);
    Release getReleaseById(1: string id, 2: User user);
    Release getReleaseByIdForEdit(1: string id, 2: User user);
    list<Release> getReleasesByIdsForExport(1: set<string> ids);
    list<Release> getReleasesById(1: set<string> ids, 2: User user);
    list<Release> getFullReleasesById(1: set<string> ids, 2: User user);
    list<Release> getReleasesWithPermissions(1: set<string> ids, 2: User user);
    list<Release> getReleasesFromVendorId(1: string id, 2: User user);
    list<Release> getReleasesFromVendorIds(1: set<string> ids);
    RequestStatus updateRelease(1: Release release, 2: User user);
    RequestStatus updateReleaseFossology(1: Release release, 2: User user);
    RequestSummary updateReleases(1: set<Release> releases, 2: User user);
    RequestStatus deleteRelease(1: string id, 2: User user);
    RequestStatus updateReleaseFromModerationRequest(1: Release additions, 2: Release deletions, 3: User user);
    list<Release> getReleasesByComponentId(1: string id, 2: User user);

    set <Component> getUsingComponentsForRelease(1: string releaseId );
    set <Component> getUsingComponentsForComponent(1: set <string> releaseId );

    bool releaseIsUsed(1: string releaseId);
    bool componentIsUsed(1: string componentId);

    RequestStatus removeAttachmentFromComponent(1: string componentId, 2:User user, 3:string attachmentContentId);

    RequestStatus removeAttachmentFromRelease(1: string releaseId, 2:User user, 3:string attachmentContentId);

    // These two methods are needed because there is no rights management needed to subscribe
    RequestStatus subscribeComponent(1: string id, 2: User user);
    RequestStatus subscribeRelease(1: string id, 2: User user);
    RequestStatus unsubscribeComponent(1: string id, 2: User user);
    RequestStatus unsubscribeRelease(1: string id, 2: User user);
    // Get a summary of release status for a given set of IDs
    ReleaseClearingStateSummary getReleaseClearingStateSummary(1: set<string> ids, 2:string clearingTeam);

    // Make a list of components for Excel export
    list<Component> getComponentSummaryForExport();
    list<Component> getComponentDetailedSummaryForExport();
    list<Component> searchComponentForExport(1: string name);

    Component getComponentForReportFromFossologyUploadId(1: string uploadId );
    set<Attachment> getSourceAttachments(1:string releaseId);

    //Linked Releases
    list<ReleaseLink> getLinkedReleases(1: map<string, string> relations);
    list<ReleaseLink> getLinkedReleaseRelations(1: map<string, ReleaseRelationship> relations);

    set<string> getUsedAttachmentContentIds();

    //Methods to ensure uniqueness of Identifiers
    map <string, list<string>> getDuplicateComponents();
    map <string, list<string>> getDuplicateReleases();
    map <string, list<string>> getDuplicateReleaseSources();

}
