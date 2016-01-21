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
package com.siemens.sw360.licenses;

import com.google.common.collect.ImmutableList;
import com.siemens.sw360.datahandler.thrift.licenses.License;
import com.siemens.sw360.datahandler.thrift.licenses.LicenseService;
import com.siemens.sw360.datahandler.thrift.licenses.Obligation;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;

import java.io.IOException;
import java.util.List;

/**
 * Small client for testing a service
 *
 * @author cedric.bodet@tngtech.com
 */
public class TestLicenseClient {

    public static void main(String[] args) throws TException, IOException {
        THttpClient thriftClient = new THttpClient("http://127.0.0.1:8080/licenses/thrift");
        TProtocol protocol = new TCompactProtocol(thriftClient);
        LicenseService.Iface client = new LicenseService.Client(protocol);

        List<License> licenses = client.getLicenseSummary();
        List<Obligation> obligations = client.getAllObligations();

        System.out.println("Fetched " + licenses.size() + " licenses from license service");
        System.out.println("Fetched " + obligations.size() + " obligations from license service");

//        final List<License> licenseList = client.getDetailedLicenseSummaryForExport("");
        final List<License> licenseList = client.getDetailedLicenseSummary("", ImmutableList.of("AFL-2.1","Artistic-1.0"));
        System.out.println(licenseList.toString());

    }

}
