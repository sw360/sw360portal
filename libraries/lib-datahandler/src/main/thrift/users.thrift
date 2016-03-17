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
namespace java com.siemens.sw360.datahandler.thrift.users
namespace php sw360.thrift.users

typedef sw360.RequestStatus RequestStatus

enum UserGroup {
    USER = 0,
    ADMIN = 1,
    CLEARING_ADMIN = 2,
}

enum LocalGroup {
    BU = 0,
    CONTRIBUTOR = 1,
    MODERATOR = 2,
    OWNER = 3,
}

enum RequestedAction {
    READ = 1,
    WRITE = 2,
    DELETE = 3,
    USERS = 4,
    CLEARING = 5,
    ATTACHMENTS = 6,
 }

struct User {

    1: required string id,
    2: optional string revision,
    3: optional string type = "user",
    4: required string email,
    5: optional UserGroup userGroup,
    6: optional string externalid, 
    7: optional string fullname,
    8: optional string givenname, // firstname or given name of the person
    9: optional string lastname, // Lastname or Surname of the person
    10: required string department,
    11: optional bool wantsMailNotification,
}

service UserService {

    // Get user by emails ID
    User getByEmail(1:string emails);
    list<User> searchUsers(1:string name);
    list<User> getAllUsers();

    RequestStatus addUser(1: User user);
    RequestStatus updateUser(1: User user);

    RequestStatus sendMailForAcceptedModerationRequest(1: string userEmail);
}
