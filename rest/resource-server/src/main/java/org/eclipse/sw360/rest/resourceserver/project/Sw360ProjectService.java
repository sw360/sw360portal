/*
 * Copyright Siemens AG, 2017. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.sw360.rest.resourceserver.project;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransportException;
import org.eclipse.sw360.datahandler.thrift.AddDocumentRequestStatus;
import org.eclipse.sw360.datahandler.thrift.AddDocumentRequestSummary;
import org.eclipse.sw360.datahandler.thrift.RequestStatus;
import org.eclipse.sw360.datahandler.thrift.projects.Project;
import org.eclipse.sw360.datahandler.thrift.projects.ProjectService;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Sw360ProjectService {
    @Value("${sw360.thrift-server-url:http://localhost:8080}")
    private String thriftServerUrl;

    public List<Project> getProjectsForUser(User sw360User) {
        try {
            ProjectService.Iface sw360ProjectClient = getThriftProjectClient();
            return sw360ProjectClient.getAccessibleProjectsSummary(sw360User);
        } catch (TException e) {
            throw new RuntimeException(e);
        }
    }

    public Project getProjectForUserById(String projectId, User sw360User) {
        try {
            ProjectService.Iface sw360ProjectClient = getThriftProjectClient();
            return sw360ProjectClient.getProjectById(projectId, sw360User);
        } catch (TException e) {
            throw new RuntimeException(e);
        }
    }

    public Project createProject(Project project, User sw360User) {
        try {
            ProjectService.Iface sw360ProjectClient = getThriftProjectClient();
            AddDocumentRequestSummary documentRequestSummary = sw360ProjectClient.addProject(project, sw360User);
            if (documentRequestSummary.getRequestStatus() == AddDocumentRequestStatus.SUCCESS) {
                project.setId(documentRequestSummary.getId());
                return project;
            } else if (documentRequestSummary.getRequestStatus() == AddDocumentRequestStatus.DUPLICATE) {
                throw new DataIntegrityViolationException("sw360 project with name '" + project.getName() + "' already exists.");
            }
        } catch (TException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void updateProject(Project project, User sw360User) {
        try {
            ProjectService.Iface sw360ProjectClient = getThriftProjectClient();
            RequestStatus requestStatus = sw360ProjectClient.updateProject(project, sw360User);
            if (requestStatus == RequestStatus.SUCCESS) {
                return;
            }
            throw new RuntimeException("sw360 project with name '" + project.getName() + " cannot be updated.");
        } catch (TException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteProject(Project project, User sw360User) {
        try {
            ProjectService.Iface sw360ProjectClient = getThriftProjectClient();
            RequestStatus requestStatus = sw360ProjectClient.deleteProject(project.getId(), sw360User);
            if (requestStatus == RequestStatus.SUCCESS) {
                return;
            }
            throw new RuntimeException("sw360 project with name '" + project.getName() + " cannot be deleted.");
        } catch (TException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteAllProjects(User sw360User) {
        try {
            ProjectService.Iface sw360ProjectClient = getThriftProjectClient();
            List<Project> projects = sw360ProjectClient.getAccessibleProjectsSummary(sw360User);
            for(Project project: projects) {
                sw360ProjectClient.deleteProject(project.getId(), sw360User);
            }
        } catch (TException e) {
            throw new RuntimeException(e);
        }
    }

    private ProjectService.Iface getThriftProjectClient() throws TTransportException {
        THttpClient thriftClient = new THttpClient(thriftServerUrl + "/projects/thrift");
        TProtocol protocol = new TCompactProtocol(thriftClient);
        return new ProjectService.Client(protocol);
    }
}
