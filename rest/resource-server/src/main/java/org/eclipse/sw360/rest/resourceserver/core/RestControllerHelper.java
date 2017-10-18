/*
 * Copyright Siemens AG, 2017. Part of the SW360 Portal Component.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.sw360.rest.resourceserver.core;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.sw360.datahandler.thrift.attachments.Attachment;
import org.eclipse.sw360.datahandler.thrift.components.Component;
import org.eclipse.sw360.datahandler.thrift.components.Release;
import org.eclipse.sw360.datahandler.thrift.licenses.License;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.datahandler.thrift.vendors.Vendor;
import org.eclipse.sw360.rest.resourceserver.attachment.AttachmentController;
import org.eclipse.sw360.rest.resourceserver.component.ComponentController;
import org.eclipse.sw360.rest.resourceserver.license.LicenseController;
import org.eclipse.sw360.rest.resourceserver.license.Sw360LicenseService;
import org.eclipse.sw360.rest.resourceserver.release.ReleaseController;
import org.eclipse.sw360.rest.resourceserver.release.Sw360ReleaseService;
import org.eclipse.sw360.rest.resourceserver.user.Sw360UserService;
import org.eclipse.sw360.rest.resourceserver.user.UserController;
import org.eclipse.sw360.rest.resourceserver.vendor.Sw360VendorService;
import org.eclipse.sw360.rest.resourceserver.vendor.VendorController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.Set;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RestControllerHelper {
    @NonNull
    private final Sw360UserService userService;

    @NonNull
    private final Sw360VendorService vendorService;

    @NonNull
    private final Sw360LicenseService licenseService;

    public User getSw360UserFromAuthentication(OAuth2Authentication oAuth2Authentication) {
        String userId = oAuth2Authentication.getName();
        return userService.getUserByEmail(userId);
    }

    public void addEmbeddedModerators(HalResource halResource, Set<String> moderators) {
        for (String moderatorEmail : moderators) {
            User user = new User();
            user.setEmail(moderatorEmail);
            addEmbeddedUser(halResource, user, "moderators");
        }
    }

    public void addEmbeddedReleases(
            HalResource halResource,
            Set<String> releases,
            Sw360ReleaseService sw360ReleaseService,
            User user,
            String linkRelation) {
        for (String releaseId : releases) {
            final Release release = sw360ReleaseService.getReleaseForUserById(releaseId, user);
            addEmbeddedRelease(halResource, release, linkRelation);
        }
    }

    public void addEmbeddedReleases(
            HalResource halResource,
            List<Release> releases) {
        for (Release release : releases) {
            addEmbeddedRelease(halResource, release, "releases");
        }
    }

    public void addEmbeddedUser(HalResource halResource, User user, String relation) {
        User embeddedUser = new User();
        Resource<User> embeddedUserResource = new Resource<>(embeddedUser);
        try {
            embeddedUser.setEmail(user.getEmail());
            embeddedUser.setType(null);
            String userUUID = Base64.getEncoder().encodeToString(user.getEmail().getBytes("utf-8"));
            Link userLink = linkTo(UserController.class).slash("api/users/" + userUUID).withSelfRel();
            embeddedUserResource.add(userLink);
        } catch (Exception e) {
            log.error("cannot create embedded user with email: " + user.getEmail());
        }

        halResource.addEmbeddedResource(relation, embeddedUserResource);
    }


    public void addEmbeddedVendors(HalResource<Component> halComponent, Set<String> vendors) {
        for (String vendorFullName : vendors) {
            HalResource<Vendor> vendorHalResource = addEmbeddedVendor(vendorFullName);
            halComponent.addEmbeddedResource("vendors", vendorHalResource);
        }

    }

    private HalResource<Vendor> addEmbeddedVendor(String vendorFullName) {
        Vendor vendor = new Vendor();
        HalResource<Vendor> halVendor = new HalResource<>(vendor);
        vendor.setFullname(vendorFullName);
        vendor.setType(null);
        try {
            Vendor vendorByFullName = vendorService.getVendorByFullName(vendorFullName);
            Link vendorSelfLink = linkTo(UserController.class)
                    .slash("api" + VendorController.VENDORS_URL + "/" + vendorByFullName.getId()).withSelfRel();
            halVendor.add(vendorSelfLink);
            return halVendor;
        } catch (Exception e) {
            log.error("cannot create self link for vendor with full name: " + vendorFullName);
        }
        return null;
    }

    public void addEmbeddedLicenses(HalResource<Release> halComponent, Set<String> licenseIds) {
        for (String licenseId : licenseIds) {
            HalResource<License> licenseHalResource = addEmbeddedLicense(licenseId);
            halComponent.addEmbeddedResource("licenses", licenseHalResource);
        }
    }

    private HalResource<License> addEmbeddedLicense(String licenseId) {
        License license = new License();
        HalResource<License> halLicense = new HalResource<>(license);
        license.setId(licenseId);
        license.setType(null);
        try {
            License licenseById = licenseService.getLicenseById(licenseId);
            license.setFullname(licenseById.getFullname());
            Link licenseSelfLink = linkTo(UserController.class)
                    .slash("api" + LicenseController.LICENSES_URL + "/" + licenseById.getId()).withSelfRel();
            halLicense.add(licenseSelfLink);
            return halLicense;
        } catch (Exception e) {
            log.error("cannot create self link for license with id: " + licenseId);
        }
        return null;
    }

    public HalResource<Release> createHalReleaseResource(Release release, boolean verbose) {
        HalResource<Release> halRelease = new HalResource<>(release);

        Link componentLink = linkTo(ReleaseController.class)
                .slash("api" + ComponentController.COMPONENTS_URL + "/" + release.getComponentId()).withRel("component");
        halRelease.add(componentLink);
        release.setComponentId(null);

        if (verbose) {
            if (release.getModerators() != null) {
                Set<String> moderators = release.getModerators();
                this.addEmbeddedModerators(halRelease, moderators);
                release.setModerators(null);
            }
            if (release.getAttachments() != null) {
                Set<Attachment> attachments = release.getAttachments();
                this.addEmbeddedAttachments(halRelease, attachments);
                release.setAttachments(null);
            }
            if (release.getVendor() != null) {
                Vendor vendor = release.getVendor();
                HalResource<Vendor> vendorHalResource = this.addEmbeddedVendor(vendor.getFullname());
                halRelease.addEmbeddedResource("vendor", vendorHalResource);
                release.setVendor(null);
            }
            if (release.getMainLicenseIds() != null) {
                this.addEmbeddedLicenses(halRelease, release.getMainLicenseIds());
                release.setMainLicenseIds(null);
            }
        }
        return halRelease;
    }

    public void addEmbeddedRelease(HalResource halResource, Release release, String linkRelation) {
        release.setType(null);
        release.setComponentId(null);
        release.setCreatedOn(null);
        release.setAttachments(null);
        release.setReleaseDate(null);
        release.setMainlineState(null);
        release.setCpeid(null);
        release.setExternalIds(null);
        release.setClearingInformation(null);
        release.setDownloadurl(null);
        release.setAttachments(null);
        release.setVendor(null);
        release.setEccInformation(null);
        release.setOperatingSystems(null);
        release.setMainLicenseIds(null);
        release.setOperatingSystems(null);
        release.setLanguages(null);
        HalResource<Release> halRelease = new HalResource<>(release);
        try {
            Link releaseLink = linkTo(ReleaseController.class).slash("api/releases/" + release.getId()).withSelfRel();
            halRelease.add(releaseLink);
        } catch (Exception e) {
            log.error("cannot create embedded release with id: " + release.getId());
        }

        halResource.addEmbeddedResource(linkRelation, halRelease);
    }

    private void addEmbeddedAttachments(
            HalResource halResource,
            Set<Attachment> attachments) {
        for (Attachment attachment : attachments) {
            attachment.setCreatedTeam(null);
            attachment.setCreatedComment(null);
            attachment.setCreatedOn(null);
            attachment.setCheckedBy(null);
            attachment.setCheckedOn(null);
            attachment.setCheckedTeam(null);
            attachment.setCheckedComment(null);
            attachment.setCheckStatus(null);

            HalResource<Attachment> halAttachmentResource = new HalResource<>(attachment);
            try {
                Link attachmentLink = linkTo(AttachmentController.class)
                        .slash("api/attachments/" + attachment.getAttachmentContentId()).withSelfRel();
                halAttachmentResource.add(attachmentLink);
            } catch (Exception e) {
                log.error("cannot create embedded attachment with content id: " + attachment.getAttachmentContentId());
            }

            halResource.addEmbeddedResource("attachments", halAttachmentResource);
        }
    }
}
