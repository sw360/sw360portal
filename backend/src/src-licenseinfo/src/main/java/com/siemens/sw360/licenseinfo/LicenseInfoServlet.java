/*
 * Copyright Siemens AG, 2016. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.licenseinfo;

import com.siemens.sw360.datahandler.thrift.licenseinfo.LicenseInfoService;
import com.siemens.sw360.projects.Sw360ThriftServlet;
import org.apache.thrift.protocol.TCompactProtocol;

import java.net.MalformedURLException;

/**
 * Thrift Servlet instantiation
 *
 * @author alex.borodin@evosoft.com
 */
public class LicenseInfoServlet extends Sw360ThriftServlet {

    public LicenseInfoServlet() throws MalformedURLException {
        // Create a service processor using the provided handler
        super(new LicenseInfoService.Processor<>(new LicenseInfoHandler()), new TCompactProtocol.Factory());
    }

}
