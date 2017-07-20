/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.vendors;

import org.eclipse.sw360.datahandler.common.DatabaseSettings;
import org.eclipse.sw360.datahandler.couchdb.DatabaseConnector;
import org.eclipse.sw360.datahandler.thrift.RequestStatus;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.datahandler.thrift.vendors.Vendor;
import org.eclipse.sw360.datahandler.thrift.vendors.VendorService;
import org.eclipse.sw360.datahandler.db.VendorRepository;
import org.eclipse.sw360.datahandler.db.VendorSearch;
import org.apache.thrift.TException;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.eclipse.sw360.datahandler.common.SW360Assert.*;
import static org.eclipse.sw360.datahandler.thrift.ThriftValidate.prepareVendor;

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
        DatabaseConnector databaseConnector = new DatabaseConnector(DatabaseSettings.getConfiguredHttpClient(), DatabaseSettings.COUCH_DB_DATABASE);
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
            vendorNames.add(vendor.getShortname());
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
