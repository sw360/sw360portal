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
package com.siemens.sw360.datahandler.db;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.common.Duration;
import com.siemens.sw360.datahandler.common.SW360Utils;
import com.siemens.sw360.datahandler.couchdb.AttachmentConnector;
import com.siemens.sw360.datahandler.couchdb.DatabaseConnector;
import com.siemens.sw360.datahandler.entitlement.ProjectModerator;
import com.siemens.sw360.datahandler.thrift.DocumentState;
import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.SW360Exception;
import com.siemens.sw360.datahandler.thrift.ThriftUtils;
import com.siemens.sw360.datahandler.thrift.moderation.ModerationRequest;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.projects.ProjectLink;
import com.siemens.sw360.datahandler.thrift.projects.ProjectRelationship;
import com.siemens.sw360.datahandler.thrift.users.RequestedAction;
import com.siemens.sw360.datahandler.thrift.users.User;
import org.apache.log4j.Logger;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.siemens.sw360.datahandler.common.SW360Assert.assertNotNull;
import static com.siemens.sw360.datahandler.common.SW360Assert.fail;
import static com.siemens.sw360.datahandler.common.SW360Utils.*;
import static com.siemens.sw360.datahandler.permissions.PermissionUtils.makePermission;
import static com.siemens.sw360.datahandler.thrift.ThriftValidate.prepareProject;

/**
 * Class for accessing the CouchDB database
 *
 * @author cedric.bodet@tngtech.com, daniele.fognini@tngtech.com
 */
public class ProjectDatabaseHandler {

    private static final Logger log = Logger.getLogger(ProjectDatabaseHandler.class);

    private final ProjectRepository repository;
    private final ProjectModerator moderator;
    private final AttachmentConnector attachments;

    public ProjectDatabaseHandler(String url, String dbName, String attachmentDbName) throws MalformedURLException {
        DatabaseConnector db = new DatabaseConnector(url, dbName);

        // Create the repository
        repository = new ProjectRepository(db);

        // Create the moderator
        moderator = new ProjectModerator();

        // Create the attachment connector
        attachments = new AttachmentConnector(url, attachmentDbName, Duration.durationOf(30, TimeUnit.SECONDS));
    }

    @VisibleForTesting
    public ProjectDatabaseHandler(String url, String dbName, String attachmentDbName, ProjectModerator moderator) throws MalformedURLException {
        DatabaseConnector db = new DatabaseConnector(url, dbName);

        // Create the repository
        repository = new ProjectRepository(db);

        this.moderator = moderator;

        // Create the attachment connector
        attachments = new AttachmentConnector(url, attachmentDbName, Duration.durationOf(30, TimeUnit.SECONDS));
    }

    /////////////////////
    // SUMMARY GETTERS //
    /////////////////////

    public List<Project> getMyProjectsSummary(String user) {
        return repository.getMyProjectsSummary(user);
    }

    public List<Project> getBUProjectsSummary(String organisation) {
        return repository.getBUProjectsSummary(organisation);
    }

    public List<Project> getAccessibleProjectsSummary(User user) {
        return repository.getAccessibleProjectsSummary(user);
    }

    public List<Project> searchByName(String name, User user) {
        return repository.searchByName(name, user);
    }

    public List<Project> searchByNameFortExport(String name, User user) {
        return repository.searchByNameForExport(name, user);
    }

    ////////////////////////////
    // GET INDIVIDUAL OBJECTS //
    ////////////////////////////

    public Project getProjectById(String id, User user) throws SW360Exception {
        Project project = repository.get(id);

        if (!makePermission(project, user).isActionAllowed(RequestedAction.READ)) {
            throw fail("User " + user +" is not allowed to view the requested project "+project+"!");
        }

        return project;
    }

    ////////////////////////////
    // ADD INDIVIDUAL OBJECTS //
    ////////////////////////////

    public String addProject(Project project, User user) throws SW360Exception {
        // Prepare project for database
        prepareProject(project);

        // Save creating user
        project.createdBy = user.getEmail();
        project.createdOn = getCreatedOn();
        project.businessUnit = getBUFromOrganisation(user.getDepartment());

        // Add project to database and return ID
        repository.add(project);

        return project.getId();
    }

    ///////////////////////////////
    // UPDATE INDIVIDUAL OBJECTS //
    ///////////////////////////////

    public RequestStatus updateProject(Project project, User user) throws SW360Exception {
        // Prepare project for database
        prepareProject(project);

        Project actual = repository.get(project.getId());

        if (makePermission(actual, user).isActionAllowed(RequestedAction.WRITE)) {
            copyImmutableFields(project,actual);
            repository.update(project);
            return RequestStatus.SUCCESS;
        } else {
            return moderator.updateProject(project, user);
        }
    }

    private void copyImmutableFields(Project destination, Project source) {
        ThriftUtils.copyField(source, destination, Project._Fields.CREATED_ON);
        ThriftUtils.copyField(source, destination, Project._Fields.CREATED_BY);
    }

    ///////////////////////////////
    // DELETE INDIVIDUAL OBJECTS //
    ///////////////////////////////

    public RequestStatus deleteProject(String id, User user) throws SW360Exception {
        Project project = repository.get(id);
        assertNotNull(project);

        if (checkIfInUse(id)) return RequestStatus.IN_USE;

        // Remove the project if the user is allowed to do it by himself
        if (makePermission(project, user).isActionAllowed(RequestedAction.DELETE)) {
            removeProjectAndCleanUp(project);
            return RequestStatus.SUCCESS;
        } else {
            return moderator.deleteProject(project, user);
        }
    }

    public boolean checkIfInUse(String projectId) {
        final Set<Project> usingProjects = repository.searchByLinkingProjectId(projectId);
       return !usingProjects.isEmpty();
    }

    private void removeProjectAndCleanUp(Project project) {
        attachments.deleteAttachments(project.getAttachments());
        repository.remove(project);
        moderator.notifyModeratorOnDelete(project.getId());
    }

    //////////////////////
    // HELPER FUNCTIONS //
    //////////////////////

    public List<ProjectLink> getLinkedProjects(Map<String, ProjectRelationship> relations) {
        List<ProjectLink> out = new ArrayList<>();
        for (Map.Entry<String, ProjectRelationship> entry : relations.entrySet()) {
            Project project = repository.get(entry.getKey());
            if (project != null) {
                out.add(new ProjectLink(project.id, project.name, entry.getValue()));
            } else {
                log.error("Broken ProjectLink in project with id: " + entry.getKey() + ", received null from DB");
            }
        }

        return out;
    }

    public Set<Project> searchByReleaseId(String id, User user) {
        return repository.searchByReleaseId(id, user);
    }

    public Set<Project> searchByReleaseId(Set<String> id, User user) {
        return repository.searchByReleaseId(id, user);
    }

    public Set<Project> searchLinkingProjects(String id, User user) {
        return repository.searchByLinkingProjectId(id, user);
    }

    public Project getProjectByIdforEdit(String id, User user) throws SW360Exception {

        List<ModerationRequest> moderationRequestsForDocumentId = moderator.getModerationRequestsForDocumentId(id);

        Project project;
        DocumentState documentState;
        if (moderationRequestsForDocumentId.isEmpty()) {
            project = getProjectById(id, user);

            documentState = CommonUtils.getOriginalDocumentState();
        } else {
            final String email = user.getEmail();
            Optional<ModerationRequest> moderationRequestOptional = CommonUtils.getFirstModerationRequestOfUser(moderationRequestsForDocumentId, email);
            if (moderationRequestOptional.isPresent()) {
                ModerationRequest moderationRequest = moderationRequestOptional.get();

                project = moderationRequest.getProject();

                documentState = CommonUtils.getModeratedDocumentState(moderationRequest);
            } else {
                project = getProjectById(id, user);

                documentState = new DocumentState().setIsOriginalDocument(true).setModerationState(moderationRequestsForDocumentId.get(0).getModerationState());
            }
        }
        project.setPermissions(makePermission(project, user).getPermissionMap());
        project.setDocumentState(documentState);
        return project;
    }

    public List<Project> getProjectsById(Set<String> id, User user) {

        List<Project> projects = repository.get(id);

        List<Project> output = new ArrayList<>();
        for (Project project : projects) {
            if (makePermission(project, user).isActionAllowed(RequestedAction.READ)) {
                output.add(project);
            } else {
                log.error("User " + user.getEmail() + " requested not accessible project " + printName(project));
            }
        }

        return output;
    }

    public Set<Project> getAccessibleProjects(User user) {
        return repository.getAccessibleProjects(user);
    }

    public Map<String, List<String>> getDuplicateProjects() {
        ListMultimap<String, String> projectIdentifierToReleaseId = ArrayListMultimap.create();

        for (Project project : repository.getAll()) {
            projectIdentifierToReleaseId.put(SW360Utils.printName(project), project.getId());
        }

        return CommonUtils.getIdentifierToListOfDuplicates(projectIdentifierToReleaseId);
    }
}
