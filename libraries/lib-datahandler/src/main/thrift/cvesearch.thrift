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

service CveSearchService{
    RequestStatus updateForCPE(1: string cpe);
    RequestStatus updateForRelease(1: string ReleaseId);
    RequestStatus updateForComponent(1: string ComponentId);
    RequestStatus updateForVendor(1: string vendorId);

    RequestStatus fullUpdate();

    RequestStatus fullUpdateLastMonth();
    RequestStatus fullUpdateLastWeek();
    RequestStatus fullUpdateLastDay();

    set<string> findCpes(1: string vendor, 2: string product, 3:string version);
}
