/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.projects;

import com.siemens.sw360.attachments.AttachmentHandler;
import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.common.DatabaseSettings;
import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.SW360Exception;
import com.siemens.sw360.datahandler.thrift.attachments.Attachment;
import com.siemens.sw360.datahandler.thrift.licenseinfo.LicenseInfoParsingResult;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.projects.ProjectLink;
import com.siemens.sw360.datahandler.thrift.projects.ProjectRelationship;
import com.siemens.sw360.datahandler.thrift.projects.ProjectService;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.db.ProjectDatabaseHandler;
import com.siemens.sw360.datahandler.db.ProjectSearchHandler;
import org.apache.thrift.TException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.util.*;

import static com.siemens.sw360.datahandler.common.SW360Assert.*;

/**
 * Implementation of the Thrift service
 *
 * @author cedric.bodet@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 */
public class ProjectHandler implements ProjectService.Iface {

    private final ProjectDatabaseHandler handler;
    private final ProjectSearchHandler searchHandler;
    private final AttachmentHandler attachmentHandler;

    ProjectHandler() throws MalformedURLException, IOException {
        handler = new ProjectDatabaseHandler(DatabaseSettings.COUCH_DB_URL, DatabaseSettings.COUCH_DB_DATABASE, DatabaseSettings.COUCH_DB_ATTACHMENTS);
        searchHandler = new ProjectSearchHandler(DatabaseSettings.COUCH_DB_URL,DatabaseSettings.COUCH_DB_DATABASE);
        attachmentHandler = new AttachmentHandler();
    }

    /////////////////////
    // SUMMARY GETTERS //
    /////////////////////


    @Override
    public List<Project> refineSearch(String text, Map<String, Set<String>> subQueryRestrictions,User user) throws TException {
        return searchHandler.search(text, subQueryRestrictions,user);
    }

    @Override
    public List<Project> getMyProjects(String user) throws TException {
        assertNotEmpty(user);

        return handler.getMyProjectsSummary(user);
    }

    @Override
    public List<Project> getAccessibleProjectsSummary(User user) throws TException {
        assertUser(user);

        return handler.getAccessibleProjectsSummary(user);
    }

    @Override
    public Set<Project> getAccessibleProjects(User user) throws TException {
        assertUser(user);

        return handler.getAccessibleProjects(user);
    }

    @Override
    public List<Project> searchByName(String name, User user) throws TException {
        assertNotEmpty(name);
        assertUser(user);

        return handler.searchByName(name, user);
    }

    @Override
    public List<Project> searchByNameForExport(String name, User user) throws TException {
        assertNotEmpty(name);
        assertUser(user);

        return handler.searchByNameFortExport(name, user);
    }

    @Override
    public Set<Project> searchByReleaseId(String id, User user) throws TException {
        return handler.searchByReleaseId(id, user);
    }

    @Override
    public Set<Project> searchByReleaseIds(Set<String> ids, User user) throws TException {
        assertNotEmpty(ids);
        return handler.searchByReleaseId(ids, user);
    }

    @Override
    public Set<Project> searchLinkingProjects(String id, User user) throws TException {
        assertId(id);
        return handler.searchLinkingProjects(id, user);
    }

    ////////////////////////////
    // GET INDIVIDUAL OBJECTS //
    ////////////////////////////

    @Override
    public Project getProjectById(String id, User user) throws TException {
        assertUser(user);
        assertId(id);

        Project project = handler.getProjectById(id, user);
        assertNotNull(project);

        return project;
    }

    @Override
    public List<Project> getProjectsById(Set<String> id, User user) throws TException {
        assertUser(user);
        assertNotNull(id);
        return handler.getProjectsById(id,user);
    }

    @Override
    public Project getProjectByIdForEdit(String id, User user) throws TException {
        assertUser(user);
        assertId(id);

        Project project = handler.getProjectForEdit(id, user);
        assertNotNull(project);

        return project;
    }

    ////////////////////////////
    // ADD INDIVIDUAL OBJECTS //
    ////////////////////////////

    @Override
    public String addProject(Project project, User user) throws TException {
        assertNotNull(project);
        assertIdUnset(project.getId());
        assertUser(user);

        return handler.addProject(project, user);
    }

    ///////////////////////////////
    // UPDATE INDIVIDUAL OBJECTS //
    ///////////////////////////////

    @Override
    public RequestStatus updateProject(Project project, User user) throws TException {
        assertNotNull(project);
        assertId(project.getId());
        assertUser(user);

        return handler.updateProject(project, user);
    }

    public RequestStatus updateProjectFromModerationRequest(Project projectAdditions, Project projectDeletions, User user){
        return handler.updateProjectFromAdditionsAndDeletions(projectAdditions, projectDeletions, user);
    }

    ///////////////////////////////
    // DELETE INDIVIDUAL OBJECTS //
    ///////////////////////////////

    @Override
    public RequestStatus deleteProject(String id, User user) throws TException {
        assertId(id);
        assertUser(user);

        return handler.deleteProject(id, user);
    }


    //////////////////////
    // HELPER FUNCTIONS //
    //////////////////////

    @Override
    public List<ProjectLink> getLinkedProjectsById(String id, User user) throws TException {
        assertId(id);

        Project project = getProjectById(id, user);
        return handler.getLinkedProjects(project.getLinkedProjects());
    }

    @Override
    public List<ProjectLink> getLinkedProjects(Map<String, ProjectRelationship> relations) throws TException {
        assertNotNull(relations);

        return handler.getLinkedProjects(relations);
    }

    @Override
    public Map<String, List<String>> getDuplicateProjects() throws TException {
        return handler.getDuplicateProjects();
    }

    @Override
    public RequestStatus removeAttachmentFromProject(String projectId, User user, String attachmentContentId) throws TException {
        Project projectByIdForEdit = getProjectByIdForEdit(projectId, user);

        Set<Attachment> attachments = projectByIdForEdit.getAttachments();

        Optional<Attachment> attachmentOptional = CommonUtils.getAttachmentOptional(attachmentContentId, attachments);
        if (attachmentOptional.isPresent()) {
            attachments.remove(attachmentOptional.get());
            return updateProject(projectByIdForEdit, user);
        } else {
            return RequestStatus.SUCCESS;
        }
    }

    @Override
    public boolean projectIsUsed(String projectId) throws TException {
        return handler.checkIfInUse(projectId);
    }

}
