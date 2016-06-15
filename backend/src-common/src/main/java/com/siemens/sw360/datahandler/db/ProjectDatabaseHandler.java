/*
 * Copyright Siemens AG, 2013-2016. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.datahandler.db;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.common.Duration;
import com.siemens.sw360.datahandler.common.SW360Constants;
import com.siemens.sw360.datahandler.common.SW360Utils;
import com.siemens.sw360.datahandler.couchdb.AttachmentConnector;
import com.siemens.sw360.datahandler.couchdb.DatabaseConnector;
import com.siemens.sw360.datahandler.entitlement.ProjectModerator;
import com.siemens.sw360.datahandler.thrift.*;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.components.ReleaseLink;
import com.siemens.sw360.datahandler.thrift.moderation.ModerationRequest;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.projects.ProjectLink;
import com.siemens.sw360.datahandler.thrift.projects.ProjectRelationship;
import com.siemens.sw360.datahandler.thrift.users.RequestedAction;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.thrift.vulnerabilities.ProjectVulnerabilityLink;
import org.apache.log4j.Logger;

import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.siemens.sw360.datahandler.common.CommonUtils.*;
import static com.siemens.sw360.datahandler.common.SW360Assert.assertNotNull;
import static com.siemens.sw360.datahandler.common.SW360Assert.fail;
import static com.siemens.sw360.datahandler.common.SW360Utils.*;
import static com.siemens.sw360.datahandler.permissions.PermissionUtils.makePermission;

/**
 * Class for accessing the CouchDB database
 *
 * @author cedric.bodet@tngtech.com
 * @author daniele.fognini@tngtech.com
 * @author alex.borodin@evosoft.com
 */
public class ProjectDatabaseHandler {

    private static final Logger log = Logger.getLogger(ProjectDatabaseHandler.class);

    private final ProjectRepository repository;
    private final ProjectVulnerabilityLinkRepository pvrRepository;
    private final ProjectModerator moderator;
    private final AttachmentConnector attachmentConnector;
    private final ComponentDatabaseHandler componentDatabaseHandler;

    public ProjectDatabaseHandler(String url, String dbName, String attachmentDbName) throws MalformedURLException {
        this(url, dbName, attachmentDbName, new ProjectModerator(), new ComponentDatabaseHandler(url,dbName,attachmentDbName));
    }

    @VisibleForTesting
    public ProjectDatabaseHandler(String url, String dbName, String attachmentDbName, ProjectModerator moderator, ComponentDatabaseHandler componentDatabaseHandler) throws MalformedURLException {
        DatabaseConnector db = new DatabaseConnector(url, dbName);

        // Create the repositories
        repository = new ProjectRepository(db);
        pvrRepository = new ProjectVulnerabilityLinkRepository(db);

        // Create the moderator
        this.moderator = moderator;

        // Create the attachment connector
        attachmentConnector = new AttachmentConnector(url, attachmentDbName, Duration.durationOf(30, TimeUnit.SECONDS));

        this.componentDatabaseHandler = componentDatabaseHandler;
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

            //clean up attachments in database
            attachmentConnector.deleteAttachmentDifference(actual.getAttachments(), project.getAttachments());
            return RequestStatus.SUCCESS;
        } else {
            return moderator.updateProject(project, user);
        }
    }

    private void prepareProject(Project project) throws SW360Exception {
        // Prepare project for database
        ThriftValidate.prepareProject(project);

        //add sha1 to attachments if necessary
        if(project.isSetAttachments()) {
            attachmentConnector.setSha1ForAttachments(project.getAttachments());
        }
    }

    public RequestStatus updateProjectFromAdditionsAndDeletions(Project projectAdditions, Project projectDeletions, User user){

        try {
            Project project = getProjectById(projectAdditions.getId(), user);
            project = moderator.updateProjectFromModerationRequest(project, projectAdditions, projectDeletions);
            return updateProject(project, user);
        } catch (SW360Exception e) {
            log.error("Could not get original project when updating from moderation request.");
            return RequestStatus.FAILURE;
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

        if (checkIfInUse(id)) {
            return RequestStatus.IN_USE;
        }

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
        attachmentConnector.deleteAttachments(project.getAttachments());
        repository.remove(project);
        moderator.notifyModeratorOnDelete(project.getId());
    }

    //////////////////////
    // HELPER FUNCTIONS //
    //////////////////////

    public List<ProjectLink> getLinkedProjects(Map<String, ProjectRelationship> relations) {
        List<ProjectLink> out;
        final List<Project> projects = repository.getAll();
        final Map<String, Project> projectMap = ThriftUtils.getIdMap(projects);

        Set<String> visitedIds = new HashSet<>();

        out = iterateProjectRelationShips(relations, projectMap, visitedIds, null);

        return out;
    }


    private List<ProjectLink> iterateProjectRelationShips(Map<String, ProjectRelationship> relations, Map<String, Project> projectMap, Set<String> visitedIds, String parentId) {
        List<ProjectLink> out = new ArrayList<>();
        for (Map.Entry<String, ProjectRelationship> entry : relations.entrySet()) {
            String id = entry.getKey();
            Optional<ProjectLink> projectLinkOptional = createProjectLink(projectMap, visitedIds, id, entry.getValue(), parentId);
            if (projectLinkOptional.isPresent()) {
                out.add(projectLinkOptional.get());
            }
        }
        return out;
    }

    private Optional<ProjectLink> createProjectLink(Map<String, Project> projectMap, Set<String> visitedIds, String id, ProjectRelationship relationship, String parentId) {
        if (visitedIds.add(id)) {
            Project project = projectMap.get(id);
            if (project != null) {
                final ProjectLink projectLink = new ProjectLink(id, project.name);
                if (project.isSetReleaseIdToUsage()){
                    List<ReleaseLink> linkedReleases = componentDatabaseHandler.getLinkedReleases(project.getReleaseIdToUsage());
                    projectLink.setLinkedReleases(nullToEmptyList(linkedReleases));
                }

                projectLink.setParentId(parentId);
                projectLink.setRelation(relationship);
                projectLink.setVersion(project.getVersion());
                if (project.isSetLinkedProjects()) {
                    List<ProjectLink> subprojectLinks = iterateProjectRelationShips(project.getLinkedProjects(), projectMap, visitedIds, id);
                    projectLink.setSubprojects(subprojectLinks);
                }
                return Optional.of(projectLink);
            } else {
                log.error("Broken ProjectLink in project with id: " + id + ", received null from DB");
            }
        }
        return Optional.empty();
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

    public Project getProjectForEdit(String id, User user) throws SW360Exception {

        List<ModerationRequest> moderationRequestsForDocumentId = moderator.getModerationRequestsForDocumentId(id);

        Project project = getProjectById(id,user);
        DocumentState documentState;
        if (moderationRequestsForDocumentId.isEmpty()) {
            documentState = CommonUtils.getOriginalDocumentState();
        } else {
            final String email = user.getEmail();
            Optional<ModerationRequest> moderationRequestOptional = CommonUtils.getFirstModerationRequestOfUser(moderationRequestsForDocumentId, email);
            if (moderationRequestOptional.isPresent()
                    && isInProgressOrPending(moderationRequestOptional.get())){
                ModerationRequest moderationRequest = moderationRequestOptional.get();
                project = moderator.updateProjectFromModerationRequest(project,
                        moderationRequest.getProjectAdditions(),
                        moderationRequest.getProjectDeletions());
                documentState = CommonUtils.getModeratedDocumentState(moderationRequest);
            } else {
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

    public List<ProjectVulnerabilityLink> getProjectVulnerabilityLinkByProjectId(String projectId){
        return pvrRepository.getProjectVulnerabilityLink(projectId);
    }

    public RequestStatus updateProjectVulnerabilityLink(ProjectVulnerabilityLink link) {
        if( ! link.isSetId()){
            link.setId(SW360Constants.PROJECT_VULNERABILITY_RATING_ID_PREFIX + link.getProjectId());
            pvrRepository.add(link);
        } else {
            pvrRepository.update(link);
        }
        return RequestStatus.SUCCESS;
    }
}
