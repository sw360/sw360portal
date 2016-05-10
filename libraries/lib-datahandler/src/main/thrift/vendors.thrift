/*
 * Copyright Siemens AG, 2014-2015. Part of the SW360 Portal Project.
 * With contributions by Bosch Software Innovations GmbH, 2016.
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

    /**
     * return vendor specified by id
     **/
    Vendor getByID(1: string id);

    /**
     * return list of all vendors in database
     **/
    list<Vendor> getAllVendors();

    /**
     * return set of names of all vendors in database, no duplicates
     **/
    set<string> getAllVendorNames();

    /**
     * get lists of vendors whose fullname or shortname starts with searchText
     **/
    list<Vendor> searchVendors(1: string searchText);

    /**
     * get set of vendorIds whose fullname or shortname starts with searchText
     **/
    set<string> searchVendorIds(1: string searchText);

    /**
     * write vendor to database and return id
     **/
    string addVendor(1: Vendor vendor);

    /**
     * vendor specified by id is deleted from database if user has sufficient permissions, otherwise FAILURE is returned
     **/
    RequestStatus deleteVendor(1: string id, 2: User user);

    /**
     * vendor specified by id is updated in database if user has sufficient permissions, otherwise FAILURE is returned
     **/
    RequestStatus updateVendor(1: Vendor vendor, 2: User user);
}
