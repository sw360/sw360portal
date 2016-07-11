/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.bosch.osmi.sw360.bdp.service;

import com.siemens.sw360.datahandler.thrift.bdpimport.BdpImportService;
import com.siemens.sw360.projects.Sw360ThriftServlet;
import org.apache.thrift.protocol.TCompactProtocol;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;

/**
 * Created by maximilian.huber@tngtech.com on 12/3/15.
 *
 * @author maximilian.huber@tngtech.com
 */
public class BdpImportServlet extends Sw360ThriftServlet {
    public BdpImportServlet() throws MalformedURLException, FileNotFoundException {
        // Create a service processor using the provided handler
        super(new BdpImportService.Processor<>(new BdpImportHandler()), new TCompactProtocol.Factory());
    }
}
