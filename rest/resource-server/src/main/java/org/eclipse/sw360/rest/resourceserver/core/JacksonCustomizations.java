/*
 * Copyright Siemens AG, 2017. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.sw360.rest.resourceserver.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.eclipse.sw360.datahandler.thrift.attachments.Attachment;
import org.eclipse.sw360.datahandler.thrift.components.Component;
import org.eclipse.sw360.datahandler.thrift.components.Release;
import org.eclipse.sw360.datahandler.thrift.licenses.License;
import org.eclipse.sw360.datahandler.thrift.projects.Project;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.datahandler.thrift.vendors.Vendor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
class JacksonCustomizations {

    @Bean
    public Module sw360Module() {
        return new Sw360Module();
    }

    @SuppressWarnings("serial")
    static class Sw360Module extends SimpleModule {

        public Sw360Module() {
            setMixInAnnotation(Project.class, Sw360Module.ProjectMixin.class);
            setMixInAnnotation(User.class, Sw360Module.UserMixin.class);
            setMixInAnnotation(Component.class, Sw360Module.ComponentMixin.class);
            setMixInAnnotation(Release.class, Sw360Module.ReleaseMixin.class);
            setMixInAnnotation(Attachment.class, Sw360Module.AttachmentMixin.class);
            setMixInAnnotation(Vendor.class, Sw360Module.VendorMixin.class);
            setMixInAnnotation(License.class, Sw360Module.LicenseMixin.class);
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonIgnoreProperties({
                "id",
                "revision",
                "attachments",
                "createdBy",
                "state",
                "tag",
                "projectResponsible",
                "leadArchitect",
                "moderators",
                "contributors",
                "visbility",
                "linkedProjects",
                "clearingTeam",
                "preevaluationDeadline",
                "systemTestStart",
                "systemTestEnd",
                "deliveryStart",
                "phaseOutSince",
                "homepage",
                "wiki",
                "documentState",
                "releaseClearingStateSummary",
                "permissions",
                "attachmentsIterator",
                "moderatorsIterator",
                "contributorsIterator",
                "releaseIdsIterator",
                "setId",
                "setRevision",
                "setType",
                "setName",
                "setDescription",
                "setVersion",
                "setExternalIds",
                "setAttachments",
                "setCreatedOn",
                "setState",
                "setProjectType",
                "setTag",
                "setCreatedBy",
                "setModerators",
                "setVisbility",
                "setHomepage",
                "externalIdsSize",
                "attachmentsSize",
                "setBusinessUnit",
                "setProjectResponsible",
                "setLeadArchitect",
                "moderatorsSize",
                "contributorsSize",
                "setContributors",
                "linkedProjectsSize",
                "setLinkedProjects",
                "releaseIdToUsageSize",
                "setReleaseIdToUsage",
                "setClearingTeam",
                "setPreevaluationDeadline",
                "setSystemTestStart",
                "setSystemTestEnd",
                "setDeliveryStart",
                "setPhaseOutSince",
                "setDocumentState",
                "releaseIdsSize",
                "setReleaseClearingStateSummary",
                "permissionsSize",
                "setWiki",
                "setReleaseIds",
                "setPermissions",
                "setClearingState",
                "securityResponsibles",
                "securityResponsiblesSize",
                "securityResponsiblesIterator",
                "setSecurityResponsibles",
                "setOwnerGroup",
                "rolesSize",
                "setRoles",
                "setOwnerAccountingUnit",
                "setProjectOwner",
                "enableSvm",
                "setEnableSvm"
                })
        static abstract class ProjectMixin {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonIgnoreProperties({
                "id",
                "revision",
                "externalid",
                "wantsMailNotification",
                "setWantsMailNotification",
                "setId",
                "setRevision",
                "setType",
                "setEmail",
                "setUserGroup",
                "setExternalid",
                "setFullname",
                "setGivenname",
                "setLastname",
                "setDepartment",
                "notificationPreferencesSize",
                "setNotificationPreferences",
                "setCommentMadeDuringModerationRequest"
        })
        static abstract class UserMixin extends User {
            @Override
            @JsonProperty("fullName")
            abstract public String getFullname();

            @Override
            @JsonProperty("givenName")
            abstract public String getGivenname();

            @Override
            @JsonProperty("lastName")
            abstract public String getLastname();
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonIgnoreProperties({
                "id",
                "revision",
                "attachments",
                "createdBy",
                "subscribers",
                "moderators",
                "releases",
                "mainLicenseIds",
                "categories",
                "languages",
                "softwarePlatforms",
                "operatingSystems",
                "homepage",
                "mailinglist",
                "wiki",
                "blog",
                "wikipedia",
                "openHub",
                "documentState",
                "permissions",
                "setId",
                "setRevision",
                "setType",
                "setName",
                "setDescription",
                "setAttachments",
                "setCreatedOn",
                "setCreatedBy",
                "setSubscribers",
                "setModerators",
                "releasesSize",
                "setReleases",
                "setReleaseIds",
                "setCategories",
                "languagesSize",
                "setLanguages",
                "setVendorNames",
                "setHomepage",
                "setMailinglist",
                "setWiki",
                "setBlog",
                "setWikipedia",
                "setOpenHub",
                "setPermissions",
                "attachmentsSize",
                "attachmentsIterator",
                "setComponentType",
                "subscribersSize",
                "subscribersIterator",
                "moderatorsSize",
                "moderatorsIterator",
                "releasesIterator",
                "releaseIdsSize",
                "releaseIdsIterator",
                "mainLicenseIdsSize",
                "mainLicenseIdsIterator",
                "setMainLicenseIds",
                "categoriesSize",
                "categoriesIterator",
                "languagesIterator",
                "softwarePlatformsSize",
                "softwarePlatformsIterator",
                "setSoftwarePlatforms",
                "operatingSystemsSize",
                "operatingSystemsIterator",
                "setOperatingSystems",
                "vendorNamesSize",
                "vendorNamesIterator",
                "setDocumentState",
                "permissionsSize",
                "setComponentOwner",
                "setOwnerAccountingUnit",
                "setOwnerGroup",
                "rolesSize",
                "setRoles"
                })
        static abstract class ComponentMixin extends Component {
            @Override
            @JsonProperty("vendors")
            abstract public Set<String> getVendorNames();
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonIgnoreProperties({
                "id",
                "revision",
                "permissions",
                "createdBy",
                "moderators",
                "clearingInformation",
                "mainlineState",
                "downloadurl",
                "setAttachments",
                "setCreatedOn",
                "setRepository",
                "setFossologyId",
                "setCreatedBy",
                "setModerators",
                "setSubscribers",
                "setVendor",
                "setVendorId",
                "languagesSize",
                "setLanguages",
                "setCotsDetails",
                "setDownloadurl",
                "setPermissions",
                "externalIdsSize",
                "attachmentsSize",
                "setMainlineState",
                "setClearingState",
                "setAttachmentInFossology",
                "contributorsSize",
                "setContributors",
                "moderatorsSize",
                "moderatorsIterator",
                "subscribersSize",
                "setClearingInformation",
                "operatingSystemsSize",
                "setOperatingSystems",
                "mainLicenseIdsSize",
                "setMainLicenseIds",
                "releaseIdToRelationshipSize",
                "setReleaseIdToRelationship",
                "setDocumentState",
                "permissionsSize",
                "setId",
                "setRevision",
                "setType",
                "setCpeid",
                "setName",
                "setVersion",
                "setComponentId",
                "setReleaseDate",
                "setExternalIds",
                "clearingTeamToFossologyStatusSize",
                "setClearingTeamToFossologyStatus",
                "setEccInformation",
                "eccInformation",
                "languages",
                "operatingSystems",
                "languagesIterator",
                "operatingSystemsIterator",
                "cotsDetails",
                "releaseIdToRelationship",
                "documentState",
                "contributorsIterator",
                "rolesSize",
                "setRoles",
                "setCreatorDepartment"
        })
        static abstract class ReleaseMixin extends Release {
            @Override
            @JsonProperty("cpeId")
            abstract public String getCpeid();
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonIgnoreProperties({
                "attachmentContentId",
                "createdBy",
                "setAttachmentContentId",
                "setAttachmentType",
                "setCreatedComment",
                "setCheckedComment",
                "uploadHistory",
                "uploadHistorySize",
                "uploadHistoryIterator",
                "setUploadHistory",
                "setFilename",
                "setSha1",
                "setCreatedBy",
                "setCreatedTeam",
                "setCreatedOn",
                "setCheckedBy",
                "setCheckedTeam",
                "setCheckedOn",
                "setCheckStatus"
        })
        static abstract class AttachmentMixin {
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonIgnoreProperties({
                "id",
                "revision",
                "permissionsSize",
                "setId",
                "setRevision",
                "setType",
                "setPermissions",
                "setFullname",
                "setShortname",
                "setUrl"
        })
        static abstract class VendorMixin extends Vendor {
            @Override
            @JsonProperty("fullName")
            abstract public String getFullname();

            @Override
            @JsonProperty("shortName")
            abstract public String getShortname();
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonIgnoreProperties({
                "id",
                "revision",
                "licenseType",
                "licenseTypeDatabaseId",
                "externalLicenseLink",
                "GPLv2Compat",
                "GPLv3Compat",
                "reviewdate",
                "todos",
                "todoDatabaseIds",
                "risks",
                "riskDatabaseIds",
                "documentState",
                "permissions",
                "setId",
                "setRevision",
                "setType",
                "setExternalIds",
                "externalIdsSize",
                "setDocumentState",
                "permissionsSize",
                "setLicenseTypeDatabaseId",
                "setExternalLicenseLink",
                "todoDatabaseIdsSize",
                "todoDatabaseIdsIterator",
                "setTodoDatabaseIds",
                "riskDatabaseIdsSize",
                "riskDatabaseIdsIterator",
                "setRiskDatabaseIds",
                "setPermissions",
                "setFullname",
                "setShortname",
                "setLicenseType",
                "gplv2Compat",
                "setGPLv2Compat",
                "gplv3Compat",
                "setGPLv3Compat",
                "setReviewdate",
                "todosSize",
                "todosIterator",
                "setTodos",
                "risksSize",
                "risksIterator",
                "setRisks",
                "setText",
                "mainLicenseIdsIterator"
                })
        static abstract class LicenseMixin extends License {
            @Override
            @JsonProperty("fullName")
            abstract public String getFullname();

            @Override
            @JsonProperty("shortName")
            abstract public String getShortname();
        }

    }
}
