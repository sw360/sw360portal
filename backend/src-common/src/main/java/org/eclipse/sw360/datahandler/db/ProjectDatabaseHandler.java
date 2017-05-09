/*
 * Copyright Siemens AG, 2013-2017. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.datahandler.db;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.eclipse.sw360.components.summary.SummaryType;
import org.eclipse.sw360.datahandler.businessrules.ReleaseClearingStateSummaryComputer;
import org.eclipse.sw360.datahandler.common.CommonUtils;
import org.eclipse.sw360.datahandler.common.Duration;
import org.eclipse.sw360.datahandler.common.SW360Constants;
import org.eclipse.sw360.datahandler.common.SW360Utils;
import org.eclipse.sw360.datahandler.couchdb.AttachmentConnector;
import org.eclipse.sw360.datahandler.couchdb.DatabaseConnector;
import org.eclipse.sw360.datahandler.entitlement.ProjectModerator;
import org.eclipse.sw360.datahandler.thrift.*;
import org.eclipse.sw360.datahandler.thrift.components.Release;
import org.eclipse.sw360.datahandler.thrift.components.ReleaseClearingStateSummary;
import org.eclipse.sw360.datahandler.thrift.components.ReleaseLink;
import org.eclipse.sw360.datahandler.thrift.moderation.ModerationRequest;
import org.eclipse.sw360.datahandler.thrift.projects.Project;
import org.eclipse.sw360.datahandler.thrift.projects.ProjectLink;
import org.eclipse.sw360.datahandler.thrift.projects.ProjectRelationship;
import org.eclipse.sw360.datahandler.thrift.users.RequestedAction;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.datahandler.thrift.vulnerabilities.ProjectVulnerabilityRating;
import org.ektorp.http.HttpClient;

import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.eclipse.sw360.datahandler.common.CommonUtils.isInProgressOrPending;
import static org.eclipse.sw360.datahandler.common.CommonUtils.nullToEmptyList;
import static org.eclipse.sw360.datahandler.common.SW360Assert.assertNotNull;
import static org.eclipse.sw360.datahandler.common.SW360Assert.fail;
import static org.eclipse.sw360.datahandler.common.SW360Utils.*;
import static org.eclipse.sw360.datahandler.permissions.PermissionUtils.makePermission;

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
    private final ProjectVulnerabilityRatingRepository pvrRepository;
    private final ProjectModerator moderator;
    private final AttachmentConnector attachmentConnector;
    private final ComponentDatabaseHandler componentDatabaseHandler;

    public ProjectDatabaseHandler(Supplier<HttpClient> httpClient, String dbName, String attachmentDbName) throws MalformedURLException {
        this(httpClient, dbName, attachmentDbName, new ProjectModerator(), new ComponentDatabaseHandler(httpClient,dbName,attachmentDbName));
    }

    @VisibleForTesting
    public ProjectDatabaseHandler(Supplier<HttpClient> httpClient, String dbName, String attachmentDbName, ProjectModerator moderator, ComponentDatabaseHandler componentDatabaseHandler) throws MalformedURLException {
        DatabaseConnector db = new DatabaseConnector(httpClient, dbName);

        // Create the repositories
        repository = new ProjectRepository(db);
        pvrRepository = new ProjectVulnerabilityRatingRepository(db);

        // Create the moderator
        this.moderator = moderator;

        // Create the attachment connector
        attachmentConnector = new AttachmentConnector(httpClient, attachmentDbName, Duration.durationOf(30, TimeUnit.SECONDS));

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

    ////////////////////////////
    // GET INDIVIDUAL OBJECTS //
    ////////////////////////////

    public Project getProjectById(String id, User user) throws SW360Exception {
        Project project = repository.get(id);
        assertNotNull(project);

        if(!makePermission(project, user).isActionAllowed(RequestedAction.READ)) {
            throw fail("User " + user + " is not allowed to view the requested project " + project + "!");
        }

        return project;
    }

    ////////////////////////////
    // ADD INDIVIDUAL OBJECTS //
    ////////////////////////////

    public AddDocumentRequestSummary addProject(Project project, User user) throws SW360Exception {
        // Prepare project for database
        prepareProject(project);
        if(isDuplicate(project)) {
            return new AddDocumentRequestSummary()
                    .setRequestStatus(AddDocumentRequestStatus.DUPLICATE);
        }

        // Save creating user
        project.createdBy = user.getEmail();
        project.createdOn = getCreatedOn();
        project.businessUnit = getBUFromOrganisation(user.getDepartment());

        // Add project to database and return ID
        repository.add(project);

        return new AddDocumentRequestSummary().setId(project.getId()).setRequestStatus(AddDocumentRequestStatus.SUCCESS);
    }

    private boolean isDuplicate(Project project){
        List<Project> duplicates = repository.searchByNameAndVersion(project.getName(), project.getVersion());
        return duplicates.size()>0;
    }
    ///////////////////////////////
    // UPDATE INDIVIDUAL OBJECTS //
    ///////////////////////////////

    public RequestStatus updateProject(Project project, User user) throws SW360Exception {
        // Prepare project for database
        prepareProject(project);

        Project actual = repository.get(project.getId());

        assertNotNull(project);

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
        final Map<String, Release> releaseMap = componentDatabaseHandler.getAllReleasesIdMap();


        CountingStack<String> visitedIds = new CountingStack<>();

        out = iterateProjectRelationShips(relations, null, visitedIds, projectMap, releaseMap);

        return out;
    }


    private List<ProjectLink> iterateProjectRelationShips(Map<String, ProjectRelationship> relations, String parentId, CountingStack<String> visitedIds, Map<String, Project> projectMap, Map<String, Release> releaseMap) {
        List<ProjectLink> out = new ArrayList<>();
        for (Map.Entry<String, ProjectRelationship> entry : relations.entrySet()) {
            Optional<ProjectLink> projectLinkOptional = createProjectLink(entry.getKey(), entry.getValue(), parentId, visitedIds, projectMap, releaseMap);
            if (projectLinkOptional.isPresent()) {
                out.add(projectLinkOptional.get());
            }
        }
        out.sort(Comparator.comparing(ProjectLink::getName).thenComparing(ProjectLink::getVersion));
        return out;
    }

    private Optional<ProjectLink> createProjectLink(String id, ProjectRelationship relationship, String parentId, CountingStack<String> visitedIds, Map<String, Project> projectMap, Map<String, Release> releaseMap) {
        ProjectLink projectLink = null;
        if (!visitedIds.contains(id)) {
            visitedIds.push(id);
            Project project = projectMap.get(id);
            if (project != null) {
                projectLink = new ProjectLink(id, project.name);
                if (project.isSetReleaseIdToUsage()){
                    List<ReleaseLink> linkedReleases = componentDatabaseHandler.getLinkedReleases(project.getReleaseIdToUsage(), releaseMap, visitedIds);
                    fillMainlineStates(linkedReleases, project.getReleaseIdToUsage());
                    projectLink.setLinkedReleases(nullToEmptyList(linkedReleases));
                }

                projectLink
                        .setNodeId(generateNodeId(id, visitedIds))
                        .setParentNodeId(generateNodeId(parentId, visitedIds))
                        .setRelation(relationship)
                        .setVersion(project.getVersion())
                        .setProjectType(project.getProjectType())
                        .setClearingState(project.getClearingState())
                        .setTreeLevel(visitedIds.size() - 1);
                if (project.isSetLinkedProjects()) {
                    List<ProjectLink> subprojectLinks = iterateProjectRelationShips(project.getLinkedProjects(), id, visitedIds, projectMap, releaseMap);
                    projectLink.setSubprojects(subprojectLinks);
                }
            } else {
                log.error("Broken ProjectLink in project with id: " + parentId + ". Linked project with id " + id + " was not in DB");
            }
            visitedIds.pop();
        }
        return Optional.ofNullable(projectLink);
    }

    private void fillMainlineStates(List<ReleaseLink> linkedReleases, Map<String, ProjectReleaseRelationship> releaseIdToUsage) {
        for (ReleaseLink releaseLink : linkedReleases) {
            releaseLink.setMainlineState(releaseIdToUsage.get(releaseLink.getId()).getMainlineState());
        }
    }

    private String generateNodeId(String id, CountingStack<String> visitedIds) {
        return id == null ? null : id + "_" + visitedIds.getCount(id);
    }

    public Set<Project> searchByReleaseId(String id, User user) {
        return repository.searchByReleaseId(id, user);
    }

    public Set<Project> searchByReleaseId(Set<String> ids, User user) {
        return repository.searchByReleaseId(ids, user);
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

    public List<Project> getProjectsById(List<String> id, User user) {

        List<Project> projects = repository.makeSummaryFromFullDocs(SummaryType.SUMMARY, repository.get(id));

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

    public List<ProjectVulnerabilityRating> getProjectVulnerabilityRatingByProjectId(String projectId){
        return pvrRepository.getProjectVulnerabilityRating(projectId);
    }

    public RequestStatus updateProjectVulnerabilityRating(ProjectVulnerabilityRating link) {
        if( ! link.isSetId()){
            link.setId(SW360Constants.PROJECT_VULNERABILITY_RATING_ID_PREFIX + link.getProjectId());
            pvrRepository.add(link);
        } else {
            pvrRepository.update(link);
        }
        return RequestStatus.SUCCESS;
    }

    public List<Project> fillClearingStateSummary(List<Project> projects, User user) {
        Function<Project, Set<String>> extractReleaseIds = project -> {
            if (project.isSetReleaseIds()) {
                return project.getReleaseIds();
            } else {
                return CommonUtils.nullToEmptyMap(project.getReleaseIdToUsage()).keySet();
            }
        };

        Set<String> allReleaseIds = projects.stream().map(extractReleaseIds).reduce(Sets.newHashSet(), Sets::union);
        if (!allReleaseIds.isEmpty()) {
            Map<String, Release> releasesById = ThriftUtils.getIdMap(componentDatabaseHandler.getReleasesForClearingStateSummary(allReleaseIds));
            for (Project project : projects) {
                final Set<String> releaseIds = extractReleaseIds.apply(project);
                List<Release> releases = releaseIds.stream().map(releasesById::get).collect(Collectors.toList());
                final ReleaseClearingStateSummary releaseClearingStateSummary = ReleaseClearingStateSummaryComputer.computeReleaseClearingStateSummary(releases, project
                        .getClearingTeam());
                project.setReleaseClearingStateSummary(releaseClearingStateSummary);
            }
        }
        return projects;
    }
}
