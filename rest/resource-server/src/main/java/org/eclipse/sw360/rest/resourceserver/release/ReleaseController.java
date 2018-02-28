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
package org.eclipse.sw360.rest.resourceserver.release;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.eclipse.sw360.datahandler.thrift.components.Release;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.rest.resourceserver.attachment.AttachmentInfo;
import org.eclipse.sw360.rest.resourceserver.attachment.Sw360AttachmentService;
import org.eclipse.sw360.rest.resourceserver.core.HalResource;
import org.eclipse.sw360.rest.resourceserver.core.RestControllerHelper;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@BasePathAwareController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReleaseController implements ResourceProcessor<RepositoryLinksResource> {
    public static final String RELEASES_URL = "/releases";

    @NonNull
    private Sw360ReleaseService releaseService;

    @NonNull
    private Sw360AttachmentService attachmentService;

    @NonNull
    private final RestControllerHelper restControllerHelper;

    @RequestMapping(value = RELEASES_URL, method = RequestMethod.GET)
    public ResponseEntity<Resources<Resource>> getReleasesForUser(
            @RequestParam(value = "sha1", required = false) String sha1,
            OAuth2Authentication oAuth2Authentication) throws TException {

        User sw360User = restControllerHelper.getSw360UserFromAuthentication(oAuth2Authentication);
        List<Release> sw360Releases = new ArrayList<>();

        if (sha1 != null && !sha1.isEmpty()) {
            sw360Releases.add(searchReleaseBySha1(sha1, sw360User));
        } else {
            sw360Releases.addAll(releaseService.getReleasesForUser(sw360User));
        }

        List<Resource> releaseResources = new ArrayList<>();
        for (Release sw360Release : sw360Releases) {
            Release embeddedRelease = restControllerHelper.convertToEmbeddedRelease(sw360Release);
            Resource<Release> releaseResource = new Resource<>(embeddedRelease);
            releaseResources.add(releaseResource);
        }
        Resources<Resource> resources = new Resources<>(releaseResources);
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    private Release searchReleaseBySha1(String sha1, User sw360User) throws TException {
        AttachmentInfo sw360AttachmentInfo = this.attachmentService.getAttachmentBySha1ForUser(sha1, sw360User);
        return sw360AttachmentInfo.getRelease();
    }

    @RequestMapping(value = RELEASES_URL + "/{id}", method = RequestMethod.GET)
    public ResponseEntity<Resource> getRelease(
            @PathVariable("id") String id, OAuth2Authentication oAuth2Authentication) throws TException {
        User sw360User = restControllerHelper.getSw360UserFromAuthentication(oAuth2Authentication);
        Release sw360Release = releaseService.getReleaseForUserById(id, sw360User);
        HalResource halRelease = restControllerHelper.createHalReleaseResource(sw360Release, true);
        return new ResponseEntity<>(halRelease, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('WRITE')")
    @RequestMapping(value = RELEASES_URL, method = RequestMethod.POST)
    public ResponseEntity<Resource<Release>> createRelease(
            OAuth2Authentication oAuth2Authentication,
            @RequestBody Release release) throws URISyntaxException, TException {
        User sw360User = restControllerHelper.getSw360UserFromAuthentication(oAuth2Authentication);

        if (release.isSetComponentId()) {
            URI componentURI = new URI(release.getComponentId());
            String path = componentURI.getPath();
            String componentId = path.substring(path.lastIndexOf('/') + 1);
            release.setComponentId(componentId);
        }
        if (release.isSetVendorId()) {
            URI vendorURI = new URI(release.getVendorId());
            String path = vendorURI.getPath();
            String vendorId = path.substring(path.lastIndexOf('/') + 1);
            release.setVendorId(vendorId);
        }

        if (release.getMainLicenseIds() != null) {
            Set<String> mainLicenseIds = new HashSet<>();
            Set<String> mainLicenseUris = release.getMainLicenseIds();
            for (String licenseURIString : mainLicenseUris.toArray(new String[mainLicenseUris.size()])) {
                URI licenseURI = new URI(licenseURIString);
                String path = licenseURI.getPath();
                String licenseId = path.substring(path.lastIndexOf('/') + 1);
                mainLicenseIds.add(licenseId);
            }
            release.setMainLicenseIds(mainLicenseIds);
        }

        Release sw360Release = releaseService.createRelease(release, sw360User);
        HalResource<Release> halResource = restControllerHelper.createHalReleaseResource(sw360Release, true);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(sw360Release.getId()).toUri();

        return ResponseEntity.created(location).body(halResource);
    }

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        resource.add(linkTo(ReleaseController.class).slash("api" + RELEASES_URL).withRel("releases"));
        return resource;
    }
}
