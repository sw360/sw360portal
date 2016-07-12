/*
 * Copyright Siemens AG, 2016. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.cliservice;

import com.siemens.sw360.datahandler.thrift.cliservice.CopyrightLicenseInfoService;
import com.siemens.sw360.projects.Sw360ThriftServlet;
import org.apache.thrift.protocol.TCompactProtocol;

import java.net.MalformedURLException;

/**
 * Thrift Servlet instantiation
 *
 * @author alex.borodin@evosoft.com
 */
public class CLIServlet extends Sw360ThriftServlet {

    public CLIServlet() throws MalformedURLException {
        // Create a service processor using the provided handler
        super(new CopyrightLicenseInfoService.Processor<>(new CLIHandler()), new TCompactProtocol.Factory());
    }

}
