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
package com.siemens.sw360.fossology;

import com.siemens.sw360.datahandler.thrift.fossology.FossologyService;
import org.apache.thrift.protocol.TCompactProtocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.MalformedURLException;

/**
 * Thrift Servlet instantiation
 *
 * @author daniele.fognini@tngtech.com
 */
@Controller
public class FossologyServlet extends SpringTServlet {

    @Autowired
    public FossologyServlet(FossologyHandler fossologyHandler) throws MalformedURLException {
        // Create a service processor using the provided handler
        super(new FossologyService.Processor<>(fossologyHandler), new TCompactProtocol.Factory());
    }

}
