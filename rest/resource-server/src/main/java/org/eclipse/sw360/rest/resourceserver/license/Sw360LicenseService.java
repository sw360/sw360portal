/*
 * Copyright Siemens AG, 2017. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.sw360.rest.resourceserver.license;

import lombok.RequiredArgsConstructor;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransportException;
import org.eclipse.sw360.datahandler.thrift.licenses.License;
import org.eclipse.sw360.datahandler.thrift.licenses.LicenseService;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Sw360LicenseService {
    @Value("${sw360.thrift-server-url:http://localhost:8080}")
    private String thriftServerUrl;

    public List<License> getLicenses() {
        try {
            LicenseService.Iface sw360LicenseClient = getThriftLicenseClient();
            return sw360LicenseClient.getLicenseSummary();
        } catch (TException e) {
            throw new RuntimeException(e);
        }
    }

    public License getLicenseById(String licenseId) {
        try {
            LicenseService.Iface sw360LicenseClient = getThriftLicenseClient();
            // TODO Kai Tödter 2017-01-26
            // What is the semantics of the second parameter (organization)?
            return sw360LicenseClient.getByID(licenseId, "?");
        } catch (TException e) {
            throw new RuntimeException(e);
        }
    }

    public License createLicense(License license, User sw360User) {
        try {
            LicenseService.Iface sw360LicenseClient = getThriftLicenseClient();
            license.setId(license.getShortname());
            List<License> licenses = sw360LicenseClient.addLicenses(Collections.singletonList(license), sw360User);
            for(License newlicense: licenses) {
                if(license.getFullname().equals(newlicense.getFullname())) {
                    return  newlicense;
                }
            }
        } catch (TException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private LicenseService.Iface getThriftLicenseClient() throws TTransportException {
        THttpClient thriftClient = new THttpClient(thriftServerUrl + "/licenses/thrift");
        TProtocol protocol = new TCompactProtocol(thriftClient);
        return new LicenseService.Client(protocol);
    }
}
