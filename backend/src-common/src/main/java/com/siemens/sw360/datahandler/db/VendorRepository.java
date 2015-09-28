/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
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
package com.siemens.sw360.datahandler.db;

import com.siemens.sw360.datahandler.couchdb.DatabaseConnector;
import com.siemens.sw360.datahandler.couchdb.DatabaseRepository;
import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.SW360Exception;
import com.siemens.sw360.datahandler.thrift.users.RequestedAction;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.thrift.vendors.Vendor;
import org.ektorp.support.View;

import static com.siemens.sw360.datahandler.common.SW360Assert.assertNotNull;
import static com.siemens.sw360.datahandler.permissions.PermissionUtils.makePermission;

/**
 * CRUD access for the Vendor class
 *
 * @author cedric.bodet@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 */
@View(name = "all", map = "function(doc) { if (doc.type == 'vendor') emit(null, doc._id) }")
public class VendorRepository extends DatabaseRepository<Vendor> {

    public VendorRepository(DatabaseConnector db) {
        super(Vendor.class, db);

        initStandardDesignDocument();
    }



    public RequestStatus deleteVendor(String id, User user) throws SW360Exception {
        Vendor vendor = get(id);
        assertNotNull(vendor);

        if (makePermission(vendor, user).isActionAllowed(RequestedAction.DELETE)) {
            remove(id);
            return RequestStatus.SUCCESS;
        } else {
            log.error("User is not allowed to delete!");
            return RequestStatus.FAILURE;
        }


    }

    public RequestStatus updateVendor(Vendor vendor, User user) {
        if (makePermission(vendor, user).isActionAllowed(RequestedAction.WRITE)) {
            update(vendor);
            return RequestStatus.SUCCESS;
        } else {
            log.error("User is not allowed to delete!");
            return RequestStatus.FAILURE;
        }
    }
}
