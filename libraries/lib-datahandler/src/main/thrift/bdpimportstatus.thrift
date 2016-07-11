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
     @author Andreas.Reichel@tngtech.com
*/

include "sw360.thrift"

namespace java com.siemens.sw360.datahandler.thrift.bdpimportstatus
namespace php sw360.datahandler.thrift.bdpimportstatus

typedef sw360.RequestStatus RequestStatus


struct BdpImportStatus {
    1: list<string> successfulIds;
    2: list<string> failedIds;
    3: RequestStatus requestStatus;
}
