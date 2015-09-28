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
package com.siemens.sw360.vendors;

import com.siemens.sw360.datahandler.common.DatabaseSettings;
import com.siemens.sw360.datahandler.couchdb.DatabaseConnector;
import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.thrift.vendors.Vendor;
import com.siemens.sw360.datahandler.thrift.vendors.VendorService;
import com.siemens.sw360.datahandler.db.VendorRepository;
import com.siemens.sw360.datahandler.db.VendorSearch;
import org.apache.thrift.TException;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.siemens.sw360.datahandler.common.SW360Assert.*;
import static com.siemens.sw360.datahandler.thrift.ThriftValidate.prepareVendor;

/**
 * Implementation of the Thrift service
 *
 * @author Cedric.Bodet@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 */
public class VendorHandler implements VendorService.Iface {

    private final VendorRepository vendorRepository;
    private final VendorSearch vendorSearch;

    public VendorHandler() throws IOException {
        DatabaseConnector databaseConnector = new DatabaseConnector(DatabaseSettings.COUCH_DB_URL, DatabaseSettings.COUCH_DB_DATABASE);
        vendorRepository = new VendorRepository(databaseConnector);
        vendorSearch = new VendorSearch(databaseConnector);     // Remove release id from component
    }

    @Override
    public Vendor getByID(String id) throws TException {
        assertNotEmpty(id);

        Vendor vendor = vendorRepository.get(id);
        assertNotNull(vendor);

        return vendor;
    }

    @Override
    public List<Vendor> getAllVendors() throws TException {
        return vendorRepository.getAll();
    }

    @Override
    public Set<String> getAllVendorNames() throws TException {

        HashSet<String> vendorNames = new HashSet<>();
        for (Vendor vendor : getAllVendors()) {
            vendorNames.add(vendor.getFullname());
        }
        return vendorNames;

    }

    @Override
    public List<Vendor> searchVendors(String searchText) throws TException {
        return vendorSearch.search(searchText);
    }

    @Override
    public Set<String> searchVendorIds(String searchText) throws TException {
        return vendorSearch.searchIds(searchText);
    }


    @Override
    public String addVendor(Vendor vendor) throws TException {
        assertNotNull(vendor);
        assertIdUnset(vendor.getId());

        prepareVendor(vendor);
        vendorRepository.add(vendor);

        return vendor.getId();
    }

    @Override
    public RequestStatus deleteVendor(String id, User user) throws TException {
        assertUser(user);
        assertId(id);

        return vendorRepository.deleteVendor(id, user);
    }

    @Override
    public RequestStatus updateVendor(Vendor vendor, User user) throws TException {
        assertUser(user);
        assertNotNull(vendor);

        return vendorRepository.updateVendor(vendor, user);
    }
}
