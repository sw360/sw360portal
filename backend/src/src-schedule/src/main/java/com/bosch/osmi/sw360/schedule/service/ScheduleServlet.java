/*
 * Copyright (c) Bosch Software Innovations GmbH 2016.
 * Part of the SW360 Portal Project.
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
