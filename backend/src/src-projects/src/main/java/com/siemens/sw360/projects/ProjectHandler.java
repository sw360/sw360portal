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
package com.siemens.sw360.projects;

import com.google.common.base.Optional;
import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.common.DatabaseSettings;
import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.attachments.Attachment;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.projects.ProjectLink;
import com.siemens.sw360.datahandler.thrift.projects.ProjectRelationship;
import com.siemens.sw360.datahandler.thrift.projects.ProjectService;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.db.ProjectDatabaseHandler;
import org.apache.thrift.TException;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.siemens.sw360.datahandler.common.SW360Assert.*;

/**
 * Implementation of the Thrift service
 *
 * @author cedric.bodet@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 */
public class ProjectHandler implements ProjectService.Iface {

    private final ProjectDatabaseHandler handler;

    ProjectHandler() throws MalformedURLException {
        handler = new ProjectDatabaseHandler(DatabaseSettings.COUCH_DB_URL, DatabaseSettings.COUCH_DB_DATABASE, DatabaseSettings.COUCH_DB_ATTACHMENTS);
    }

    /////////////////////
    // SUMMARY GETTERS //
    /////////////////////




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

        Project project = handler.getProjectByIdforEdit(id, user);
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
    public RequestStatus addAttachmentToProject(String projectId, User user, String attachmentContentId, String fileName) throws TException {
        Attachment attachment = CommonUtils.getNewAttachment(user, attachmentContentId, fileName);

        Project projectById = getProjectById(projectId, user);
        projectById.addToAttachments(attachment);
        return updateProject(projectById, user);
    }

    @Override
    public RequestStatus removeAttachmentFromProject(String projectId, User user, String attachmentContentId) throws TException {
        Project projectById = getProjectById(projectId, user);

        Set<Attachment> attachments = projectById.getAttachments();
        Optional<Attachment> attachmentOptional = CommonUtils.getAttachmentOptional(attachmentContentId, attachments);
        if (attachmentOptional.isPresent()) {
            attachments.remove(attachmentOptional.get());
            return updateProject(projectById, user);
        } else {
            return RequestStatus.SUCCESS;
        }
    }

    @Override
    public boolean projectIsUsed(String projectId) throws TException {
        return handler.checkIfInUse(projectId);
    }

}
