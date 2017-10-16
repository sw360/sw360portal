/*
 * Copyright Siemens AG, 2017. Part of the SW360 Portal Component.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.sw360.rest.resourceserver.component;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.sw360.datahandler.thrift.components.Component;
import org.eclipse.sw360.datahandler.thrift.components.Release;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.datahandler.thrift.vendors.Vendor;
import org.eclipse.sw360.rest.resourceserver.core.HalResource;
import org.eclipse.sw360.rest.resourceserver.core.RestControllerHelper;
import org.eclipse.sw360.rest.resourceserver.release.Sw360ReleaseService;
import org.eclipse.sw360.rest.resourceserver.vendor.Sw360VendorService;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@BasePathAwareController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ComponentController implements ResourceProcessor<RepositoryLinksResource> {
    public static final String COMPONENTS_URL = "/components";

    @NonNull
    private final Sw360ComponentService componentService;

    @NonNull
    private final Sw360ReleaseService releaseService;

    @NonNull
    private final Sw360VendorService vendorService;

    @NonNull
    private final RestControllerHelper restControllerHelper;

    @RequestMapping(value = COMPONENTS_URL)
    public ResponseEntity<Resources<Resource<Component>>> getComponents(OAuth2Authentication oAuth2Authentication) {
        User user = restControllerHelper.getSw360UserFromAuthentication(oAuth2Authentication);
        List<Component> components = componentService.getComponentsForUser(user);

        List<Resource<Component>> componentResources = new ArrayList<>();
        for (Component component : components) {
            // TODO Kai Tödter 2017-01-04
            // Find better way to decrease details in list resources,
            // e.g. apply projections or Jackson Mixins
            component.setDescription(null);
            component.setType(null);
            component.setCreatedOn(null);
            component.setVendorNames(null);
            component.setReleaseIds(null);

            Resource<Component> componentResource = new Resource<>(component);
            componentResources.add(componentResource);
        }
        Resources<Resource<Component>> resources = new Resources<>(componentResources);

        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    @RequestMapping(COMPONENTS_URL + "/{id}")
    public ResponseEntity<Resource<Component>> getComponent(
            @PathVariable("id") String id, OAuth2Authentication oAuth2Authentication) {
        User user = restControllerHelper.getSw360UserFromAuthentication(oAuth2Authentication);
        Component sw360Component = componentService.getComponentForUserById(id, user);
        HalResource<Component> userHalResource = createHalComponent(sw360Component, user);
        return new ResponseEntity<>(userHalResource, HttpStatus.OK);
    }

    @RequestMapping(value = COMPONENTS_URL, method = RequestMethod.POST)
    public ResponseEntity<Resource<Component>> createComponent(
            OAuth2Authentication oAuth2Authentication,
            @RequestBody Component component) throws URISyntaxException {

        User user = restControllerHelper.getSw360UserFromAuthentication(oAuth2Authentication);

        if(component.getVendorNames() != null) {
            Set<String> vendors = new HashSet<>();
            for (String vendorUriString : component.getVendorNames()) {
                URI vendorURI = new URI(vendorUriString);
                String path = vendorURI.getPath();
                String vendorId = path.substring(path.lastIndexOf('/') + 1);
                Vendor vendor = vendorService.getVendorById(vendorId);
                String vendorFullName = vendor.getFullname();
                vendors.add(vendorFullName);
            }
            component.setVendorNames(vendors);
        }

        Component sw360Component = componentService.createComponent(component, user);
        HalResource<Component> halResource = createHalComponent(sw360Component, user);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(sw360Component.getId()).toUri();

        return ResponseEntity.created(location).body(halResource);
    }

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        resource.add(linkTo(ComponentController.class).slash("api/components").withRel("components"));
        return resource;
    }

    private HalResource<Component> createHalComponent(Component sw360Component, User user) {
        HalResource<Component> halComponent = new HalResource<>(sw360Component);

        if (sw360Component.getReleaseIds() != null) {
            Set<String> releases = sw360Component.getReleaseIds();
            restControllerHelper.addEmbeddedReleases(halComponent, releases, releaseService, user, "releases");
        }

        if (sw360Component.getReleases() != null) {
            List<Release> releases = sw360Component.getReleases();
            restControllerHelper.addEmbeddedReleases(halComponent, releases);
        }

        if (sw360Component.getModerators() != null) {
            Set<String> moderators = sw360Component.getModerators();
            restControllerHelper.addEmbeddedModerators(halComponent, moderators);
        }

        if (sw360Component.getVendorNames() != null) {
            Set<String> vendors = sw360Component.getVendorNames();
            restControllerHelper.addEmbeddedVendors(halComponent, vendors);
            sw360Component.setVendorNames(null);
        }

        restControllerHelper.addEmbeddedUser(halComponent, user, "createdBy");

        return halComponent;
    }
}
