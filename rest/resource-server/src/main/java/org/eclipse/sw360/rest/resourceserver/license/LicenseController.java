/*
 * Copyright Siemens AG, 2017. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.sw360.rest.resourceserver.license;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.sw360.datahandler.thrift.licenses.License;
import org.eclipse.sw360.datahandler.thrift.users.User;
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
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@BasePathAwareController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LicenseController implements ResourceProcessor<RepositoryLinksResource> {
    public static final String LICENSES_URL = "/licenses";

    @NonNull
    private final Sw360LicenseService licenseService;

    @NonNull
    private final RestControllerHelper restControllerHelper;

    @RequestMapping(value = LICENSES_URL)
    public ResponseEntity<Resources<Resource<License>>> getLicenses(OAuth2Authentication oAuth2Authentication) {
        List<License> licenses = licenseService.getLicenses();

        List<Resource<License>> licenseResources = new ArrayList<>();
        for (License license : licenses) {
            // TODO Kai Tödter 2017-01-04
            // Find better way to decrease details in list resources,
            // e.g. apply projections or Jackson Mixins
            license.setText(null);
            license.setType(null);
            license.setShortname(null);
            Resource<License> licenseResource = new Resource<>(license);
            licenseResources.add(licenseResource);
        }
        Resources<Resource<License>> resources = new Resources<>(licenseResources);

        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    @RequestMapping(LICENSES_URL + "/{id}")
    public ResponseEntity<Resource<License>> getLicense(
            @PathVariable("id") String id, OAuth2Authentication oAuth2Authentication) {
        License sw360License = licenseService.getLicenseById(id);
        HalResource<License> licenseHalResource = createHalLicense(sw360License);
        return new ResponseEntity<>(licenseHalResource, HttpStatus.OK);
    }

    @RequestMapping(value = LICENSES_URL, method = RequestMethod.POST)
    public ResponseEntity<Resource<License>> createLicense(
            OAuth2Authentication oAuth2Authentication,
            @RequestBody License license) throws URISyntaxException {
        User sw360User = restControllerHelper.getSw360UserFromAuthentication(oAuth2Authentication);
        license = licenseService.createLicense(license, sw360User);
        HalResource<License> halResource = createHalLicense(license);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(license.getId()).toUri();

        return ResponseEntity.created(location).body(halResource);
    }

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        resource.add(linkTo(LicenseController.class).slash("api/licenses").withRel("licenses"));
        return resource;
    }

    private HalResource<License> createHalLicense(License sw360License) {
        HalResource<License> halLicense = new HalResource<>(sw360License);
        return halLicense;
    }
}
