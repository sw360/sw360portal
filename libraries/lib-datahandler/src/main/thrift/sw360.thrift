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
