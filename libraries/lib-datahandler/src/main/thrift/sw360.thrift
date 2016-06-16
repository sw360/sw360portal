/*
 * Copyright Siemens AG, 2014-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

namespace java com.siemens.sw360.datahandler.thrift
namespace php sw360.thrift

enum RequestStatus {
    SUCCESS = 0,
    SENT_TO_MODERATOR = 1,
    FAILURE = 2,
    IN_USE=3,
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
