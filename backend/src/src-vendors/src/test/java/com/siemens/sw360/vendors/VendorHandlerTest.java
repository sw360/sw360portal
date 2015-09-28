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

import com.siemens.sw360.datahandler.TestUtils;
import com.siemens.sw360.datahandler.common.DatabaseSettings;
import com.siemens.sw360.datahandler.couchdb.DatabaseConnector;
import com.siemens.sw360.datahandler.couchdb.DatabaseInstance;
import com.siemens.sw360.datahandler.thrift.vendors.Vendor;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class VendorHandlerTest {

    private static final String url = DatabaseSettings.COUCH_DB_URL;
    private static final String dbName = DatabaseSettings.COUCH_DB_DATABASE;

    private VendorHandler vendorHandler;
    private List<Vendor> vendorList;

    @Before
    public void setUp() throws Exception {

        // Create the database
        TestUtils.createDatabase(url, dbName);

        // Prepare the database
        DatabaseConnector databaseConnector = new DatabaseConnector(url, dbName);
        vendorList = new ArrayList<>();
        vendorList.add(new Vendor().setShortname("Microsoft").setFullname("Microsoft Corporation").setUrl("http://www.microsoft.com"));
        vendorList.add(new Vendor().setShortname("Apache").setFullname("The Apache Software Foundation").setUrl("http://www.apache.org"));


        for (Vendor vendor : vendorList) {
            databaseConnector.add(vendor);
        }

        vendorHandler = new VendorHandler();
    }

    @After
    public void tearDown() throws Exception {
        // Delete the database
        TestUtils.deleteDatabase(url, dbName);
    }


    @Test
    public void testGetByID() throws Exception {
        for (Vendor vendor : vendorList) {
            String id = vendor.getId();

            Vendor actualVendor = vendorHandler.getByID(id);
            assertVendorEquals(vendor, actualVendor);
        }
    }

    @Test
    public void testGetAllVendors() throws Exception {
        List<Vendor> actualList = vendorHandler.getAllVendors();
        assertEquals(vendorList.size(), actualList.size());
    }

    @Test
    public void testAddVendor() throws Exception {
        Vendor oracle = new Vendor().setShortname("Oracle").setFullname("Oracle Corporation Inc").setUrl("http://www.oracle.com");
        String id = vendorHandler.addVendor(oracle);
        assertNotNull(id);
        assertEquals(vendorList.size() + 1, vendorHandler.getAllVendors().size());

        Vendor actual = vendorHandler.getByID(id);

        assertVendorEquals(oracle, actual);
    }

    private static void assertVendorEquals(Vendor vendor, Vendor actualVendor) {
        assertEquals(vendor.getShortname(), actualVendor.getShortname());
        assertEquals(vendor.getFullname(), actualVendor.getFullname());
        assertEquals(vendor.getUrl(), actualVendor.getUrl());
        assertEquals(vendor.getId(), actualVendor.getId());
    }

    @Test
    @Ignore
    public void testSearchVendors1() throws Exception {
        List<Vendor> vendors = vendorHandler.searchVendors("foundation");
        assertEquals(1, vendors.size());
        assertEquals(vendorList.get(1), vendors.get(0));
    }

    @Test
    @Ignore
    public void testSearchVendors2() throws Exception {
        List<Vendor> vendors = vendorHandler.searchVendors("http");
        assertEquals(0, vendors.size());
    }

    @Test
    @Ignore
    public void testSearchVendors3() throws Exception {
        List<Vendor> vendors = vendorHandler.searchVendors("soft");
        assertEquals(1, vendors.size()); // Software but not Microsoft
    }

    @Test
    @Ignore
    public void testSearchVendors4() throws Exception {
        List<Vendor> vendors = vendorHandler.searchVendors("m");
        assertEquals(1, vendors.size());
    }

}