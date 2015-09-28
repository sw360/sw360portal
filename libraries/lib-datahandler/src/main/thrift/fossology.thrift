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
include "attachments.thrift"
include "components.thrift"
include "users.thrift"

namespace java com.siemens.sw360.datahandler.thrift.fossology
namespace php sw360.thrift.fossology

typedef attachments.Attachment Attachment
typedef components.Release Release
typedef users.User User
typedef sw360.RequestStatus RequestStatus

struct FossologyHostFingerPrint {
    1: optional string id,
    2: optional string revision
    3: optional string type = "fossologyHostFingerPrint",
    4: bool trusted;
    5: string fingerPrint;
}

service FossologyService {

    RequestStatus sendToFossology(1: string releaseId, 2:User user, 3: string clearingTeam );
    RequestStatus sendReleasesToFossology(1: list< string > releaseIds, 2:User user, 3: string clearingTeam );
    Release getStatusInFossology(1: string releaseId, 2:User user, 3: string clearingTeam );

    list<FossologyHostFingerPrint> getFingerPrints();
    RequestStatus setFingerPrints(1: list<FossologyHostFingerPrint> fingerPrints);

    RequestStatus deployScripts();
    RequestStatus checkConnection();

    string getPublicKey();
}