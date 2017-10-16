/*
 * Copyright Siemens AG, 2017. Part of the SW360 Portal Component.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.sw360.rest.resourceserver.component;

import lombok.RequiredArgsConstructor;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransportException;
import org.eclipse.sw360.datahandler.thrift.AddDocumentRequestStatus;
import org.eclipse.sw360.datahandler.thrift.AddDocumentRequestSummary;
import org.eclipse.sw360.datahandler.thrift.components.Component;
import org.eclipse.sw360.datahandler.thrift.components.ComponentService;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Sw360ComponentService {
    @Value("${sw360.thrift-server-url:http://localhost:8080}")
    private String thriftServerUrl;

    public List<Component> getComponentsForUser(User sw360User) {
        try {
            ComponentService.Iface sw360ComponentClient = getThriftComponentClient();
            return sw360ComponentClient.getComponentSummary(sw360User);
        } catch (TException e) {
            throw new RuntimeException(e);
        }
    }

    public Component getComponentForUserById(String componentId, User sw360User) {
        try {
            ComponentService.Iface sw360ComponentClient = getThriftComponentClient();
            return sw360ComponentClient.getComponentById(componentId, sw360User);
        } catch (TException e) {
            throw new RuntimeException(e);
        }
    }

    public Component createComponent(Component component, User sw360User) {
        try {
            ComponentService.Iface sw360ComponentClient = getThriftComponentClient();
            AddDocumentRequestSummary documentRequestSummary = sw360ComponentClient.addComponent(component, sw360User);
            if (documentRequestSummary.getRequestStatus() == AddDocumentRequestStatus.SUCCESS) {
                component.setId(documentRequestSummary.getId());
                return component;
            } else if (documentRequestSummary.getRequestStatus() == AddDocumentRequestStatus.DUPLICATE) {
                throw new DataIntegrityViolationException("sw360 component with name '" + component.getName() + "' already exists.");
            }
        } catch (TException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private ComponentService.Iface getThriftComponentClient() throws TTransportException {
        THttpClient thriftClient = new THttpClient(thriftServerUrl + "/components/thrift");
        TProtocol protocol = new TCompactProtocol(thriftClient);
        return new ComponentService.Client(protocol);
    }
}
