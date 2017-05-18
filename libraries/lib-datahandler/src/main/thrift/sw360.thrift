/*
 * Copyright Siemens AG, 2014-2015. Part of the SW360 Portal Project.
 * With contributions by Bosch Software Innovations GmbH, 2016.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

namespace java org.eclipse.sw360.datahandler.thrift
namespace php sw360.thrift

enum Ternary {
    UNDEFINED = 0,
    NO = 1,
    YES = 2,
}

enum RequestStatus {
    SUCCESS = 0,
    SENT_TO_MODERATOR = 1,
    FAILURE = 2,
    IN_USE=3,
}

enum RemoveModeratorRequestStatus {
    SUCCESS = 0,
    LAST_MODERATOR = 1,
    FAILURE = 2,
}

enum AddDocumentRequestStatus {
    SUCCESS = 0,
    DUPLICATE = 1,
    FAILURE = 2
}

exception SW360Exception {
    1: required string why,
}

enum ModerationState {
    PENDING = 0,
    APPROVED = 1,
    REJECTED = 2,
    INPROGRESS =3,
}

enum Visibility {
    PRIVATE = 0,
    ME_AND_MODERATORS = 1,
    BUISNESSUNIT_AND_MODERATORS = 2,
    EVERYONE = 3
}

enum VerificationState {
    NOT_CHECKED = 0,
    CHECKED = 1,
    INCORRECT = 2,
}

struct VerificationStateInfo {
  1: required string checkedOn,
  2: required string checkedBy,
  3: optional string comment,
  4: required VerificationState verificationState,
}

struct DocumentState {
  1: required bool isOriginalDocument;
  2: optional ModerationState moderationState;
}

struct RequestSummary {
  1: required RequestStatus requestStatus;
  2: optional i32 totalAffectedElements;
  3: optional i32 totalElements;
  4: optional string message;
}

struct AddDocumentRequestSummary {
    1: optional AddDocumentRequestStatus requestStatus;
    2: optional string id;
}

struct CustomProperties {
    1: optional string id,
    2: optional string revision,
    3: optional string type = "customproperties",
    4: optional string documentType,
    5: map<string, set<string>> propertyToValues;
}

struct RequestStatusWithBoolean {
  1: required RequestStatus requestStatus;
  2: optional bool answerPositive;
}
