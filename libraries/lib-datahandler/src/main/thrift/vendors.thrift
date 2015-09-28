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
include "users.thrift"

namespace java com.siemens.sw360.datahandler.thrift.vendors
namespace php sw360.thrift.vendors

typedef sw360.RequestStatus RequestStatus
typedef users.User User
typedef users.RequestedAction RequestedAction

struct Vendor {
    1: optional string id,
    2: optional string revision,
    3: optional string type = "vendor",
    4: required string shortname,
    5: required string fullname,
    6: required string url

    200: optional map<RequestedAction, bool> permissions,
}

service VendorService {

    Vendor getByID(1: string id);

    list<Vendor> getAllVendors();
    set<string> getAllVendorNames();
    list<Vendor> searchVendors(1: string searchText);
    set<string> searchVendorIds(1: string searchText);

    string addVendor(1: Vendor vendor);

    RequestStatus deleteVendor(1: string id, 2: User user);
    RequestStatus updateVendor(1: Vendor vendor, 2: User user);
}