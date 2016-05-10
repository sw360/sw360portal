/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 * Part of the SW360 Portal Project.
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

/*
     @author Maximilian.Huber@tngtech.com
     @author Andreas.Reichel@tngtech.com
*/

include "projects.thrift"
include "sw360.thrift"
include "users.thrift"
include "bdpimportstatus.thrift"

namespace java com.siemens.sw360.datahandler.thrift.bdpimport
namespace php sw360.thrift.bdpimport

typedef projects.Project Project
typedef users.User User
typedef bdpimportstatus.BdpImportStatus BdpImportStatus

struct RemoteCredentials {
1:  string username,
2:  string password,
3:  string serverUrl,
}

service BdpImportService {
   /**
    * check credentials with bdp API
    **/
   bool validateCredentials(1: RemoteCredentials credentials)

   /**
    * returns a list of projects that can be imported from bdp with `reCred` credentials
    **/
   list<Project> loadImportables(1: RemoteCredentials reCred)

   /**
    *  imports projects from bdp specified by `bdpProjectIds` with credentials `reCred` and set user as creating
    *  user in SW360
    **/
   BdpImportStatus importDatasources(1: list<string> bdpProjectIds, 2: User user, 3: RemoteCredentials reCred);
}

