/*
 * Copyright Siemens AG, 2014-2017. Part of the SW360 Portal Project.
 * With contributions by Bosch Software Innovations GmbH, 2016.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
include "users.thrift"
include "attachments.thrift"
include "vendors.thrift"
include "components.thrift"
include "sw360.thrift"

namespace java org.eclipse.sw360.datahandler.thrift.projects
namespace php sw360.thrift.projects

typedef sw360.RequestStatus RequestStatus
typedef sw360.DocumentState DocumentState
typedef sw360.Visibility Visibility
typedef components.Release Release
typedef components.ReleaseClearingStateSummary ReleaseClearingStateSummary
typedef users.User User
typedef users.RequestedAction RequestedAction
typedef attachments.Attachment Attachment
typedef components.ReleaseLink ReleaseLink
typedef sw360.AddDocumentRequestSummary AddDocumentRequestSummary

const string CLEARING_TEAM_UNKNOWN = "Unknown"

enum ProjectState {
    ACTIVE = 0,
    PHASE_OUT = 1,
    UNKNOWN = 2,
}

enum ProjectType {
    CUSTOMER = 0,
    INTERNAL = 1,
    PRODUCT = 2,
    SERVICE = 3,
}

enum ProjectRelationship {
    UNKNOWN = 0,
    REFERRED = 1,
    CONTAINED = 2,
    DUPLICATE = 3,
}

enum ProjectClearingState {
    OPEN = 0,
    IN_PROGRESS = 1,
    CLOSED = 2,
}

struct Project {

    // General information
    1: optional string id,
    2: optional string revision,
    3: optional string type = "project",
    4: required string name,
    5: optional string description,
    6: optional string version,

    // information from external data sources
    9: optional map<string, string> externalIds,

    // Additional informations
    10: optional set<Attachment> attachments,
    11: optional string createdOn, // Creation date YYYY-MM-dd
    12: optional string businessUnit,
    13: optional ProjectState state = ProjectState.ACTIVE,
    15: optional ProjectType projectType = ProjectType.CUSTOMER,
    16: optional string tag,// user defined tags
    17: optional ProjectClearingState clearingState,

    // User details
    21: optional string createdBy,
    22: optional string projectResponsible,
    23: optional string leadArchitect,
    25: optional set<string> moderators = [],
//    26: optional set<string> comoderators, //deleted
    27: optional set<string> contributors = [],
    28: optional Visibility visbility = sw360.Visibility.BUISNESSUNIT_AND_MODERATORS,
    129: optional set<string> securityResponsibles = [],
    130: optional string projectOwner,
    131: optional string ownerAccountingUnit,
    132: optional string ownerGroup,

    // Linked objects
    30: optional map<string, ProjectRelationship> linkedProjects,  //id, relationship
    31: optional map<string, string> releaseIdToUsage,    //id, comment

    // Admin data
    40: optional string clearingTeam = CLEARING_TEAM_UNKNOWN,
    41: optional string preevaluationDeadline,
    42: optional string systemTestStart,
    43: optional string systemTestEnd,
    44: optional string deliveryStart,
    45: optional string phaseOutSince,

    // Urls for the project
    50: optional string homepage,
    52: optional string wiki,

    // Information for ModerationRequests
    70: optional DocumentState documentState,

    // Optional fields for summaries!
    100: optional set<string> releaseIds,
    101: optional ReleaseClearingStateSummary releaseClearingStateSummary,

    200: optional map<RequestedAction, bool> permissions,
}

struct ProjectLink {
    1: required string id,
    2: required string name,
    3: optional ProjectRelationship relation,
    4: optional string version,
//    5: optional string parentId,
    6: optional string nodeId,
    7: optional string parentNodeId,
    8: optional ProjectType projectType,
    9: optional ProjectClearingState clearingState,
    10: optional list<ReleaseLink> linkedReleases,
    11: optional list<ProjectLink> subprojects,
    12: optional i32 treeLevel, //zero-based level in the ProjectLink tree, i.e. root has level 0
}

service ProjectService {

    // Summary getters
    /**
     * get projects for user by emailadress
     */
    list<Project> getMyProjects(1: string user);

    /**
     * get all projects as project summaries which are visible to user
     */
    list<Project> getAccessibleProjectsSummary(1: User user);

    /**
     * get all projects visible to user
     */
    set<Project> getAccessibleProjects(1: User user);

    // Search functions

    /**
     * returns a list of projects which match `text` and the
     * `subQueryRestrictions` and are visible to the `user`
     */
    list<Project> refineSearch(1: string text, 2: map<string,set<string>>  subQueryRestrictions, 3: User user);

    /**
     * list of projects which are visible to the `user` and match the `name`
     */
    list<Project> searchByName(1: string name, 2: User user);

    /**
     * list of short project summaries which are visible to the `user` and have `id` in releaseIdToUsage
     */
    set<Project> searchByReleaseId(1: string id, 2: User user);

    /**
     * list of short project summaries which are visible to the `user` and have one of the `ids` in releaseIdToUsage
     */
    set<Project> searchByReleaseIds(1: set<string> ids, 2: User user);

    /**
     * get short summaries of projects linked to the project with the id `id` which are visible
     * to the user
     */
    set<Project> searchLinkingProjects(1: string id, 2: User user);

    /**
     * add a project as a user to the db and get the id back
     * (part of project CRUD support)
     */
    AddDocumentRequestSummary addProject(1: Project project, 2: User user);

    /**
     * get a project by id, if it is visible for the user
     * (part of project CRUD support)
     */
    Project getProjectById(1: string id, 2: User user);

    /**
     * get multiple projects by id, if they are visible to the user
     * (part of project CRUD support)
     */
    list<Project> getProjectsById(1: list<string> id, 2: User user);

    /**
     * get project by id, with moderation requests of user applied
     */
    Project getProjectByIdForEdit(1: string id, 2: User user);

    /**
     * try to update a project as a user, if user has no permission, a moderation request is created
     * (part of project CRUD support)
     */
    RequestStatus updateProject(1: Project project, 2: User user);

    /**
     * try to delete a project as a user, if user has no permission, a moderation request is created
     * (part of project CRUD support)
     */
    RequestStatus deleteProject(1: string id, 2: User user);

    /**
     * updateproject in database if user has permissions, additions and deletions are the parts of the moderation request
     * that specify which properties to add to and which to delete from project
     **/
    RequestStatus updateProjectFromModerationRequest(1: Project additions, 2: Project deletions, 3: User user);

    /**
     * try to remove an attachment with the id `attachmentContentId` as `user`
     * from the project with projectId `projectId`, if user does not have permissions a moderation request is created
     */
    RequestStatus removeAttachmentFromProject(1: string projectId, 2:User user, 3:string attachmentContentId);

    //Linked Projects

    /**
     * check if a the project specified by projectId is linked to some other project
     */
    bool projectIsUsed(1: string projectId);

    /**
     * get a list of project links of the project that matches the id `id`
     */
    list<ProjectLink> getLinkedProjectsById(1: string id, 2: User user);

    /**
     * get a list of project links from keys of map `relations`
     */
    list<ProjectLink> getLinkedProjects(1:  map<string, ProjectRelationship> relations);

    /**
     * get a list of duplicated projects matched by `.printName()`
     * returned as map from pretty printed name to list of matching ids
     */
    map <string, list<string>> getDuplicateProjects();

    /**
    * get the same list of projects back, but with filled release clearing state summaries
     */
    list<Project> fillClearingStateSummary(list<Project> projects, User user);
}
