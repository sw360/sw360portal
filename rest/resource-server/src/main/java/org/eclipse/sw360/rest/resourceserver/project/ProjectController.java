/*
 * Copyright Siemens AG, 2017. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.sw360.rest.resourceserver.project;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.sw360.datahandler.thrift.MainlineState;
import org.eclipse.sw360.datahandler.thrift.ProjectReleaseRelationship;
import org.eclipse.sw360.datahandler.thrift.ReleaseRelationship;
import org.eclipse.sw360.datahandler.thrift.projects.Project;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.rest.resourceserver.core.HalResource;
import org.eclipse.sw360.rest.resourceserver.core.RestControllerHelper;
import org.eclipse.sw360.rest.resourceserver.release.Sw360ReleaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@BasePathAwareController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProjectController implements ResourceProcessor<RepositoryLinksResource> {
    static final String PROJECTS_URL = "/projects";

    @NonNull
    private final Sw360ProjectService projectService;

    @NonNull
    private final Sw360ReleaseService releaseService;

    @NonNull
    private final RestControllerHelper restControllerHelper;

    @RequestMapping(value = PROJECTS_URL, method = RequestMethod.GET)
    public ResponseEntity<Resources<Resource<Project>>> getProjectsForUser(OAuth2Authentication oAuth2Authentication) {
        User sw360User = restControllerHelper.getSw360UserFromAuthentication(oAuth2Authentication);
        List<Project> projects = projectService.getProjectsForUser(sw360User);

        List<Resource<Project>> projectResources = new ArrayList<>();
        for (Project project : projects) {
            // TODO Kai Tödter 2017-01-04
            // Find better way to decrease details in list resources,
            // e.g. apply projections or Jackson Mixins
            project.setDescription(null);
            project.setType(null);
            project.setCreatedOn(null);
            project.setReleaseIdToUsage(null);
            project.setExternalIds(null);
            project.setBusinessUnit(null);

            Resource<Project> projectResource = new Resource<>(project);
            projectResources.add(projectResource);
        }
        Resources<Resource<Project>> resources = new Resources<>(projectResources);

        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    @RequestMapping(PROJECTS_URL + "/{id}")
    public ResponseEntity<Resource<Project>> getProject(
            @PathVariable("id") String id, OAuth2Authentication oAuth2Authentication) {
        User sw360User = restControllerHelper.getSw360UserFromAuthentication(oAuth2Authentication);
        Project sw360Project = projectService.getProjectForUserById(id, sw360User);
        HalResource<Project> userHalResource = createHalProject(sw360Project, sw360User);
        return new ResponseEntity<>(userHalResource, HttpStatus.OK);
    }

    @RequestMapping(value = PROJECTS_URL, method = RequestMethod.POST)
    public ResponseEntity createProject(
            OAuth2Authentication oAuth2Authentication,
            @RequestBody Project project) throws URISyntaxException {
        if (project.getReleaseIdToUsage() != null) {

            Map<String, ProjectReleaseRelationship> releaseIdToUsage = new HashMap<>();
            Map<String, ProjectReleaseRelationship> oriReleaseIdToUsage = project.getReleaseIdToUsage();
            for (String releaseURIString : oriReleaseIdToUsage.keySet()) {
                URI releaseURI = new URI(releaseURIString);
                String path = releaseURI.getPath();
                String releaseId = path.substring(path.lastIndexOf('/') + 1);
                releaseIdToUsage.put(releaseId, oriReleaseIdToUsage.get(releaseURIString));
            }
            project.setReleaseIdToUsage(releaseIdToUsage);
        }

        User sw360User = restControllerHelper.getSw360UserFromAuthentication(oAuth2Authentication);
        project = projectService.createProject(project, sw360User);
        HalResource<Project> halResource = createHalProject(project, sw360User);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(project.getId()).toUri();

        return ResponseEntity.created(location).body(halResource);
    }

    @RequestMapping(value = PROJECTS_URL + "/{id}/Releases", method = RequestMethod.POST)
    public ResponseEntity createReleases(
            @PathVariable("id") String id,
            OAuth2Authentication oAuth2Authentication,
            @RequestBody List<String> releaseURIs) throws URISyntaxException {
        User sw360User = restControllerHelper.getSw360UserFromAuthentication(oAuth2Authentication);
        Project project = projectService.getProjectForUserById(id, sw360User);
        Map<String, ProjectReleaseRelationship> releaseIdToUsage = new HashMap<>();
        for (String releaseURIString : releaseURIs) {
            URI releaseURI = new URI(releaseURIString);
            String path = releaseURI.getPath();
            String releaseId = path.substring(path.lastIndexOf('/') + 1);
            releaseIdToUsage.put(releaseId,
                    new ProjectReleaseRelationship(ReleaseRelationship.CONTAINED, MainlineState.MAINLINE));
        }
        project.setReleaseIdToUsage(releaseIdToUsage);
        projectService.updateProject(project, sw360User);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        resource.add(linkTo(ProjectController.class).slash("api" + PROJECTS_URL).withRel("projects"));
        return resource;
    }

    private HalResource<Project> createHalProject(Project sw360Project, User sw360User) {

        HalResource<Project> halProject = new HalResource<>(sw360Project);

        restControllerHelper.addEmbeddedUser(halProject, sw360User, "createdBy");
        Set<String> releaseIds = new HashSet<>();
        if (sw360Project.getReleaseIdToUsage() != null) {
            Map<String, ProjectReleaseRelationship> releaseIdToUsage = sw360Project.getReleaseIdToUsage();
            for (String releaseId : releaseIdToUsage.keySet()) {
                if (releaseIdToUsage.get(releaseId).releaseRelation.equals(ReleaseRelationship.CONTAINED)) {
                    releaseIds.add(releaseId);
                }
            }
            restControllerHelper.addEmbeddedReleases(halProject, releaseIds, releaseService, sw360User, "containedReleases");
            sw360Project.setReleaseIdToUsage(null);
        }
        if (sw360Project.getModerators() != null) {
            Set<String> moderators = sw360Project.getModerators();
            restControllerHelper.addEmbeddedModerators(halProject, moderators);
        }

        return halProject;
    }
}
