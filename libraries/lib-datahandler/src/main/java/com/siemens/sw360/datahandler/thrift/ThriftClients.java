/*
 * Copyright Siemens AG, 2014-2016. Part of the SW360 Portal Project.
 * With modifications by Bosch Software Innovations GmbH, 2016.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.datahandler.thrift;

import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentService;
import com.siemens.sw360.datahandler.thrift.components.ComponentService;
import com.siemens.sw360.datahandler.thrift.fossology.FossologyService;
import com.siemens.sw360.datahandler.thrift.licenseinfo.LicenseInfoService;
import com.siemens.sw360.datahandler.thrift.licenses.LicenseService;
import com.siemens.sw360.datahandler.thrift.moderation.ModerationService;
import com.siemens.sw360.datahandler.thrift.projects.ProjectService;
import com.siemens.sw360.datahandler.thrift.bdpimport.BdpImportService;
import com.siemens.sw360.datahandler.thrift.search.SearchService;
import com.siemens.sw360.datahandler.thrift.users.UserService;
import com.siemens.sw360.datahandler.thrift.vendors.VendorService;
import com.siemens.sw360.datahandler.thrift.vulnerabilities.VulnerabilityService;
import org.apache.log4j.Logger;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransportException;

import java.util.Properties;

import static org.apache.log4j.Logger.getLogger;

/**
 * Created by bodet on 11/02/15.
 *
 * @author cedric.bodet@tngtech.com
 * @author stefan.jaeger@evosoft.com
 */
public class ThriftClients {

    private final static Logger log = getLogger(ThriftClients.class);

    public static final String PROPERTIES_FILE_PATH = "/sw360.properties";

    public static final String BACKEND_URL;

    //! Service addresses
    public static final String ATTACHMENT_SERVICE_URL = "/attachments/thrift";
    public static final String COMPONENT_SERVICE_URL = "/components/thrift";
    public static final String FOSSOLOGY_SERVICE_URL = "/fossology/thrift";
    public static final String LICENSE_SERVICE_URL = "/licenses/thrift";
    public static final String MODERATION_SERVICE_URL = "/moderation/thrift";
    public static final String PROJECT_SERVICE_URL = "/projects/thrift";
    public static final String SEARCH_SERVICE_URL = "/search/thrift";
    public static final String USER_SERVICE_URL = "/users/thrift";
    public static final String VENDOR_SERVICE_URL = "/vendors/thrift";
    public static final String BDPIMPORT_SERVICE_URL = "/bdpimport/thrift";
    public static final String VULNERABILITY_SERVICE_URL = "/vulnerabilities/thrift";

    static {
        Properties props = CommonUtils.loadProperties(ThriftClients.class, PROPERTIES_FILE_PATH);

        BACKEND_URL = props.getProperty("backend.url", "http://localhost:8080");
    }

    public ThriftClients() {
    }

    /**
     * Creates a Thrift Compact Protocol object linked to the given address
     */
    private static TProtocol makeProtocol(String url, String service) {
        THttpClient thriftClient = null;
        final String destinationAddress = url + service;
        try {
            thriftClient = new THttpClient(destinationAddress);
        } catch (TTransportException e) {
            log.error("cannot connect to backend on " + destinationAddress, e);
        }
        return new TCompactProtocol(thriftClient);
    }

    public AttachmentService.Iface makeAttachmentClient() {
        return new AttachmentService.Client(makeProtocol(BACKEND_URL, ATTACHMENT_SERVICE_URL));
    }

    public ComponentService.Iface makeComponentClient() {
        return new ComponentService.Client(makeProtocol(BACKEND_URL, COMPONENT_SERVICE_URL));
    }

    public FossologyService.Iface makeFossologyClient() {
        return new FossologyService.Client(makeProtocol(BACKEND_URL, FOSSOLOGY_SERVICE_URL));
    }

    public LicenseService.Iface makeLicenseClient() {
        return new LicenseService.Client(makeProtocol(BACKEND_URL, LICENSE_SERVICE_URL));
    }

    public ModerationService.Iface makeModerationClient() {
        return new ModerationService.Client(makeProtocol(BACKEND_URL, MODERATION_SERVICE_URL));
    }

    public ProjectService.Iface makeProjectClient() {
        return new ProjectService.Client(makeProtocol(BACKEND_URL, PROJECT_SERVICE_URL));
    }

    public SearchService.Iface makeSearchClient() {
        return new SearchService.Client(makeProtocol(BACKEND_URL, SEARCH_SERVICE_URL));
    }

    public UserService.Iface makeUserClient() {
        return new UserService.Client(makeProtocol(BACKEND_URL, USER_SERVICE_URL));
    }

    public VendorService.Iface makeVendorClient() {
        return new VendorService.Client(makeProtocol(BACKEND_URL, VENDOR_SERVICE_URL));
    }

    public BdpImportService.Iface makeBdpImportClient() {
        return new BdpImportService.Client(makeProtocol(BACKEND_URL, BDPIMPORT_SERVICE_URL));
    }

    public VulnerabilityService.Iface makeVulnerabilityClient() {
        return new VulnerabilityService.Client(makeProtocol(BACKEND_URL, VULNERABILITY_SERVICE_URL));
    }

    public LicenseInfoService.Client makeLicenseInfoClient() {
        return new LicenseInfoService.Client(makeProtocol(BACKEND_URL, PROJECT_SERVICE_URL));
    }
}
