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

    /**
     * send unique source attachment of release with releaseId to Fossology or update existing upload,
     *
     * clearingTeam is addressee in Fossology,
     *
     * release's fossology status is set to SENT and release is updated if user has permissions, otherwise moderation request,
     * is created,
     *
     * FAILURE if source attachment is not present or not unique or if its ID is different from attachment in existing,
     * fossology upload
     **/
    RequestStatus sendToFossology(1: string releaseId, 2:User user, 3: string clearingTeam );

     /**
       * send unique source attachment of each release with id in releaseIds to Fossology or update existing upload,
       *
       * clearingTeam is addressee in Fossology,
       *
       * release's fossology statuses are set to SENT and releases are updated if user has permissions, otherwise moderation requests
       * are created
       *
       * FAILURE if source attachment is not present or not unique or if its ID is different from attachment in existing
       * fossology upload for one of the releases
       **/
    RequestStatus sendReleasesToFossology(1: list< string > releaseIds, 2:User user, 3: string clearingTeam );

    /**
     * for release specified by releaseId update status of attachment sent to fossology, i.e.
     * update fossology status for clearing teams already in release.clearingTeamToFossologyStatus,
     * add clearingTeam and corresponding fossology status to release.clearingTeamToFossologyStatus,
     * return resulting release,
     * user is necessary to get release from database
     **/
    Release getStatusInFossology(1: string releaseId, 2:User user, 3: string clearingTeam );

    /**
     * get finger prints from FossologyFingerPrintRepository
     **/
    list<FossologyHostFingerPrint> getFingerPrints();

    /**
     * set finger prints in FossologyFingerPrintRepository
     **/
    RequestStatus setFingerPrints(1: list<FossologyHostFingerPrint> fingerPrints);

    /**
     * deploy SW360 scripts to fossology server for later use for uploads etc.
     **/
    RequestStatus deployScripts();

    /**
     * check connection with fossology, if connection works, SUCCESS is returned
     **/
    RequestStatus checkConnection();

    /**
     * returns the public key, used for the ssh connection
     **/
    string getPublicKey();
}
