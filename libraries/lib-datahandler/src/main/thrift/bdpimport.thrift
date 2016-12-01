/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

/*
     @author Maximilian.Huber@tngtech.com
     @author Andreas.Reichel@tngtech.com
*/

include "projects.thrift"
include "sw360.thrift"
include "users.thrift"
include "importstatus.thrift"

namespace java org.eclipse.sw360.datahandler.thrift.bdpimport
namespace php sw360.thrift.bdpimport

typedef projects.Project Project
typedef users.User User
typedef importstatus.ImportStatus ImportStatus

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
   ImportStatus importDatasources(1: list<string> bdpProjectIds, 2: User user, 3: RemoteCredentials reCred);

   string getIdName();
}

