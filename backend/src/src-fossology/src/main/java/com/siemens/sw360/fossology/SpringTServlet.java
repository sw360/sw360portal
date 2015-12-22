/*
 * Copyright Siemens AG, 2015. Part of the SW360 Portal Project.
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

import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TProtocolFactory;
import com.siemens.sw360.projects.Sw360ThriftServlet;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequestMapping("/thrift")
public class SpringTServlet extends Sw360ThriftServlet {
    public SpringTServlet(TProcessor processor, TProtocolFactory inProtocolFactory, TProtocolFactory outProtocolFactory) {
        super(processor, inProtocolFactory, outProtocolFactory);
    }

    public SpringTServlet(TProcessor processor, TProtocolFactory protocolFactory) {
        super(processor, protocolFactory);
    }

    @RequestMapping(method = RequestMethod.POST)
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doPost(request, response);
    }

    @RequestMapping(method = RequestMethod.GET)
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doGet(request, response);
    }

}
