/*
 * Copyright (c) Bosch Software Innovations GmbH 2016.
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


include "sw360.thrift"

namespace java com.siemens.sw360.datahandler.thrift.cvesearch
namespace php sw360.thrift.cvesearch

typedef sw360.RequestStatus RequestStatus

enum UpdateType {
    NEW = 0,
    UPDATED = 1,
    OLD = 2,
    FAILED = 3,
}

struct VulnerabilityUpdateStatus {
    1: map<UpdateType, list<string>> statusToVulnerabilityIds;
    2: RequestStatus requestStatus;
}

service CveSearchService {
    VulnerabilityUpdateStatus updateForRelease(1: string ReleaseId);
    VulnerabilityUpdateStatus updateForComponent(1: string ComponentId);
    VulnerabilityUpdateStatus updateForProject(1: string ProjectId);
    VulnerabilityUpdateStatus fullUpdate();

    set<string> findCpes(1: string vendor, 2: string product, 3:string version);
}
