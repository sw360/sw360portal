/*
 * Copyright Siemens AG, 2017-2018.
 * Copyright Bosch Software Innovations GmbH, 2017.
 * Part of the SW360 Portal Project.
 *
 * SPDX-License-Identifier: EPL-1.0
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
import org.apache.thrift.TException;
import org.eclipse.sw360.datahandler.thrift.MainlineState;
import org.eclipse.sw360.datahandler.thrift.ProjectReleaseRelationship;
import org.eclipse.sw360.datahandler.thrift.ReleaseRelationship;
import org.eclipse.sw360.datahandler.thrift.components.Release;
import org.eclipse.sw360.datahandler.thrift.licenses.License;
import org.eclipse.sw360.datahandler.thrift.projects.Project;
import org.eclipse.sw360.datahandler.thrift.projects.ProjectRelationship;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.datahandler.thrift.vulnerabilities.VulnerabilityDTO;
import org.eclipse.sw360.rest.resourceserver.core.HalResource;
import org.eclipse.sw360.rest.resourceserver.core.RestControllerHelper;
import org.eclipse.sw360.rest.resourceserver.license.Sw360LicenseService;
import org.eclipse.sw360.rest.resourceserver.release.Sw360ReleaseService;
import org.eclipse.sw360.rest.resourceserver.vulnerability.Sw360VulnerabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@BasePathAwareController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProjectController implements ResourceProcessor<RepositoryLinksResource> {
    public static final String PROJECTS_URL = "/projects";

    @NonNull
    private final Sw360ProjectService projectService;

    @NonNull
    private final Sw360ReleaseService releaseService;

    @NonNull
    private final Sw360LicenseService licenseService;

    @NonNull
    private final Sw360VulnerabilityService vulnerabilityService;

    @NonNull
    private final RestControllerHelper restControllerHelper;

    @RequestMapping(value = PROJECTS_URL, method = RequestMethod.GET)
    public ResponseEntity<Resources<Resource<Project>>> getProjectsForUser(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "type", required = false) String projectType,
            OAuth2Authentication oAuth2Authentication) throws TException {
        User sw360User = restControllerHelper.getSw360UserFromAuthentication(oAuth2Authentication);
        List<Project> sw360Projects = new ArrayList<>();
        if (name != null && !name.isEmpty()) {
            sw360Projects.addAll(projectService.searchProjectByName(name, sw360User));
        } else {
            sw360Projects.addAll(projectService.getProjectsForUser(sw360User));
        }

        List<Resource<Project>> projectResources = new ArrayList<>();
        sw360Projects.stream()
                .filter(project -> projectType == null || projectType.equals(project.projectType.name()))
                .forEach(p -> {
                    Project embeddedProject = restControllerHelper.convertToEmbeddedProject(p);
                    projectResources.add(new Resource<>(embeddedProject));
                });

        Resources<Resource<Project>> resources = new Resources<>(projectResources);
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    @RequestMapping(value = PROJECTS_URL + "/{id}", method = RequestMethod.GET)
    public ResponseEntity<Resource<Project>> getProject(
            @PathVariable("id") String id, OAuth2Authentication oAuth2Authentication) throws TException {
        User sw360User = restControllerHelper.getSw360UserFromAuthentication(oAuth2Authentication);
        Project sw360Project = projectService.getProjectForUserById(id, sw360User);
        HalResource<Project> userHalResource = createHalProject(sw360Project, sw360User);
        return new ResponseEntity<>(userHalResource, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('WRITE')")
    @RequestMapping(value = PROJECTS_URL, method = RequestMethod.POST)
    public ResponseEntity createProject(
            OAuth2Authentication oAuth2Authentication,
            @RequestBody Project project) throws URISyntaxException, TException {
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

    @PreAuthorize("hasAuthority('WRITE')")
    @RequestMapping(value = PROJECTS_URL + "/{id}/releases", method = RequestMethod.POST)
    public ResponseEntity createReleases(
            @PathVariable("id") String id,
            OAuth2Authentication oAuth2Authentication,
            @RequestBody List<String> releaseURIs) throws URISyntaxException, TException {
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

    @RequestMapping(value = PROJECTS_URL + "/{id}/releases", method = RequestMethod.GET)
    public ResponseEntity<Resources<Resource<Release>>> getProjectReleases(
            @PathVariable("id") String id,
            @RequestParam(value = "transitive", required = false) String transitive,
            OAuth2Authentication oAuth2Authentication) throws TException {

        final User sw360User = restControllerHelper.getSw360UserFromAuthentication(oAuth2Authentication);
        final Set<String> releaseIds = projectService.getReleaseIds(id, sw360User, transitive);

        final List<Resource<Release>> releaseResources = new ArrayList<>();
        for (final String releaseId : releaseIds) {
            final Release sw360Release = releaseService.getReleaseForUserById(releaseId, sw360User);
            final Release embeddedRelease = restControllerHelper.convertToEmbeddedRelease(sw360Release);
            final Resource<Release> releaseResource = new Resource<>(embeddedRelease);
            releaseResources.add(releaseResource);
        }

        final Resources<Resource<Release>> resources = new Resources<>(releaseResources);
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    @RequestMapping(value = PROJECTS_URL + "/{id}/releases/ecc", method = RequestMethod.GET)
    public ResponseEntity<Resources<Resource<Release>>> getECCsOfReleases(
            @PathVariable("id") String id,
            @RequestParam(value = "transitive", required = false) String transitive,
            OAuth2Authentication oAuth2Authentication) throws TException {

        final User sw360User = restControllerHelper.getSw360UserFromAuthentication(oAuth2Authentication);
        final Set<String> releaseIds = projectService.getReleaseIds(id, sw360User, transitive);

        final List<Resource<Release>> releaseResources = new ArrayList<>();
        for (final String releaseId : releaseIds) {
            final Release sw360Release = releaseService.getReleaseForUserById(releaseId, sw360User);
            Release embeddedRelease = restControllerHelper.convertToEmbeddedRelease(sw360Release);
            embeddedRelease.setEccInformation(sw360Release.getEccInformation());
            final Resource<Release> releaseResource = new Resource<>(embeddedRelease);
            releaseResources.add(releaseResource);
        }

        final Resources<Resource<Release>> resources = new Resources<>(releaseResources);
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    @RequestMapping(value = PROJECTS_URL + "/{id}/vulnerabilities", method = RequestMethod.GET)
    public ResponseEntity<Resources<Resource<VulnerabilityDTO>>> getVulnerabilitiesOfReleases(@PathVariable("id") String id, OAuth2Authentication oAuth2Authentication) {
        final User sw360User = restControllerHelper.getSw360UserFromAuthentication(oAuth2Authentication);
        final List<VulnerabilityDTO> allVulnerabilityDTOs = vulnerabilityService.getVulnerabilitiesByProjectId(id, sw360User);

        final List<Resource<VulnerabilityDTO>> vulnerabilityResources = new ArrayList<>();
        for (final VulnerabilityDTO vulnerabilityDTO : allVulnerabilityDTOs) {
            final Resource<VulnerabilityDTO> vulnerabilityDTOResource = new Resource<>(vulnerabilityDTO);
            vulnerabilityResources.add(vulnerabilityDTOResource);
        }

        final Resources<Resource<VulnerabilityDTO>> resources = new Resources<>(vulnerabilityResources);
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    @RequestMapping(value = PROJECTS_URL + "/{id}/licenses", method = RequestMethod.GET)
    public ResponseEntity<Resources<Resource<License>>> getLicensesOfReleases(@PathVariable("id") String id, OAuth2Authentication oAuth2Authentication) throws TException {
        final User sw360User = restControllerHelper.getSw360UserFromAuthentication(oAuth2Authentication);
        final Project project = projectService.getProjectForUserById(id, sw360User);
        final List<Resource<License>> licenseResources = new ArrayList<>();
        final Set<String> allLicenseIds = new HashSet<>();

        final Set<String> releaseIdToUsage = project.getReleaseIdToUsage().keySet();
        for (final String releaseId : releaseIdToUsage) {
            final Release sw360Release = releaseService.getReleaseForUserById(releaseId, sw360User);
            final Set<String> licenseIds = sw360Release.getMainLicenseIds();
            if (licenseIds != null && !licenseIds.isEmpty()) {
                allLicenseIds.addAll(licenseIds);
            }
        }
        for (final String licenseId : allLicenseIds) {
            final License sw360License = licenseService.getLicenseById(licenseId);
            final License embeddedLicense = restControllerHelper.convertToEmbeddedLicense(sw360License);
            final Resource<License> licenseResource = new Resource<>(embeddedLicense);
            licenseResources.add(licenseResource);
        }

        final Resources<Resource<License>> resources = new Resources<>(licenseResources);
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        resource.add(linkTo(ProjectController.class).slash("api" + PROJECTS_URL).withRel("projects"));
        return resource;
    }

    private HalResource<Project> createHalProject(Project sw360Project, User sw360User) throws TException {
        HalResource<Project> halProject = new HalResource<>(sw360Project);
        restControllerHelper.addEmbeddedUser(halProject, sw360User, "createdBy");

        Map<String, ProjectReleaseRelationship> releaseIdToUsage = sw360Project.getReleaseIdToUsage();
        if (releaseIdToUsage != null) {
            restControllerHelper.addEmbeddedReleases(halProject, releaseIdToUsage.keySet(), releaseService, sw360User);
        }

        Map<String, ProjectRelationship> linkedProjects = sw360Project.getLinkedProjects();
        if (linkedProjects != null) {
            restControllerHelper.addEmbeddedProject(halProject, linkedProjects.keySet(), projectService, sw360User);
        }

        if (sw360Project.getModerators() != null) {
            Set<String> moderators = sw360Project.getModerators();
            restControllerHelper.addEmbeddedModerators(halProject, moderators);
        }

        return halProject;
    }
}
