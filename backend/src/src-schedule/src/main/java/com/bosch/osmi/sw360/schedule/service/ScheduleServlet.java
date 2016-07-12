/*
 * Copyright (c) Bosch Software Innovations GmbH 2016.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.bosch.osmi.sw360.schedule.service;

import com.siemens.sw360.datahandler.thrift.schedule.ScheduleService;
import com.siemens.sw360.projects.Sw360ThriftServlet;
import org.apache.thrift.protocol.TCompactProtocol;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;

public class ScheduleServlet extends Sw360ThriftServlet {
    public ScheduleServlet() throws MalformedURLException, FileNotFoundException {
        // Create a service processor using the provided handler
        super(new ScheduleService.Processor<>(new ScheduleHandler()), new TCompactProtocol.Factory());
    }
}
