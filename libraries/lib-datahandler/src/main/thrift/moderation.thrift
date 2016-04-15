/*
 * Copyright Siemens AG, 2014-2015. Part of the SW360 Portal Project.
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
include "components.thrift"
include "projects.thrift"
include "users.thrift"
include "licenses.thrift"

namespace java com.siemens.sw360.datahandler.thrift.moderation
namespace php sw360.thrift.moderation

typedef sw360.RequestStatus RequestStatus
typedef sw360.ModerationState ModerationState
typedef components.Component Component
typedef components.Release Release
typedef projects.Project Project
typedef users.User User
typedef licenses.License License
typedef licenses.Todo Todo

enum DocumentType {
    COMPONENT = 1,
    RELEASE = 2,
    PROJECT = 3,
    LICENSE = 4,
    USER = 5,
}

struct ModerationRequest {

    // Basic information
    1: optional string id,
    2: optional string revision,
    3: optional string type = "moderation",

    // Moderation request
    10: required i64 timestamp,
    11: required string documentId,
    12: required DocumentType documentType,
    13: optional string requestingUser,
    14: optional set<string> moderators,
    15: optional string documentName,
    16: required ModerationState moderationState,
    17: optional string reviewer,
    18: required bool requestDocumentDelete,

    // Underlying objects
    20: optional Component component,
    21: optional Release Release,
    22: optional Project project,
    23: optional License license,
    24: optional User user,
}

service ModerationService {

    // Create a new moderation request
    oneway void createComponentRequest(1: Component component, 2: User user);
    oneway void createReleaseRequest(1: Release release, 2: User user);
    oneway void createProjectRequest(1: Project project, 2: User user);
    oneway void createLicenseRequest(1: License license, 2: User user);
    oneway void createUserRequest(1: User user);

    // Create a new delete request
    oneway void createComponentDeleteRequest(1: Component component, 2: User user);
    oneway void createReleaseDeleteRequest(1: Release release, 2: User user);
    oneway void createProjectDeleteRequest(1: Project project, 2: User user);

    // Get the moderation of a given document
    list<ModerationRequest> getModerationRequestByDocumentId(1: string documentId);

    RequestStatus updateModerationRequest(1: ModerationRequest moderationRequest);
    ModerationRequest getModerationRequestById(1: string id);

    // Accept/refuse a moderation request
    oneway void acceptRequest(1: string requestId);
    oneway void refuseRequest(1: string requestId);

    // Refuse to work on moderation request, but keep it open for other moderators
    oneway void removeUserFromAssignees(1: string requestId, 2:User user);
    oneway void cancelInProgress(1: string requestId);
    oneway void setInProgress(1: string requestId, 2:User user);

    // Delete request when project was updated otherwise
    oneway void deleteRequestsOnDocument(1: string documentId);

    // Get moderation requests relevant to a given user
    list<ModerationRequest> getRequestsByModerator(1: User user);
    list<ModerationRequest> getRequestsByRequestingUser(1: User user);

    RequestStatus deleteModerationRequest(1: string id, 2: User user);
}
