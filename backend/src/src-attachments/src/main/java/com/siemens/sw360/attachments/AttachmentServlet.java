/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.attachments;

import com.siemens.sw360.datahandler.thrift.attachments.AttachmentService;
import org.apache.thrift.protocol.TCompactProtocol;
import com.siemens.sw360.projects.Sw360ThriftServlet;

import java.net.MalformedURLException;

/**
 * Thrift Servlet instantiation
 *
 * @author cedric.bodet@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 * @author Andreas.Reichel@tngtech.com
 */
public class AttachmentServlet extends Sw360ThriftServlet {

    public AttachmentServlet() throws MalformedURLException {
        // Create a service processor using the provided handler
        super(new AttachmentService.Processor<>(new AttachmentHandler()), new TCompactProtocol.Factory());
    }

}
