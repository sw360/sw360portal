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

import com.siemens.sw360.datahandler.couchdb.DatabaseConnector;
import com.siemens.sw360.datahandler.thrift.vendors.Vendor;
import com.siemens.sw360.datahandler.thrift.vendors.VendorService;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

/**
 * Small client for testing a service
 *
 * @author cedric.bodet@tngtech.com
 */
public class TestVendorClient {

    @SuppressWarnings("unused")
    public static void InitDatabase() throws MalformedURLException {
        DatabaseConnector databaseConnector = new DatabaseConnector("http://localhost:5984", "sw360db");

        databaseConnector.add(new Vendor().setShortname("Microsoft").setFullname("Microsoft Corporation").setUrl("http://www.microsoft.com"));
        databaseConnector.add(new Vendor().setShortname("Apache").setFullname("The Apache Software Foundation").setUrl("http://www.apache.org"));
        databaseConnector.add(new Vendor().setShortname("Oracle").setFullname("Oracle Corporation Inc").setUrl("http://www.oracle.com"));
    }

    public static void main(String[] args) throws TException, IOException {
        THttpClient thriftClient = new THttpClient("http://127.0.0.1:8080/vendorservice/thrift");
        TProtocol protocol = new TCompactProtocol(thriftClient);
        VendorService.Iface client = new VendorService.Client(protocol);

        List<Vendor> vendors = client.getAllVendors();

        reportFindings(vendors);

        System.out.println("Now looking for matches starting with 'm' from vendor service");

        reportFindings(client.searchVendors("m"));
    }

    private static void reportFindings(List<Vendor> vendors) {
        System.out.println("Fetched " + vendors.size() + " from vendor service");
        for (Vendor vendor : vendors) {
            System.out.println(vendor.getId() + ": " + vendor.getFullname() + " (" + vendor.getShortname() + ")");
        }
    }

}
