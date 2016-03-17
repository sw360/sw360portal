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
package com.siemens.sw360.portal.portlets.moderation;

import com.liferay.portal.kernel.servlet.SessionMessages;
import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.common.SW360Constants;
import com.siemens.sw360.datahandler.common.SW360Utils;
import com.siemens.sw360.datahandler.thrift.ModerationState;
import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.attachments.Attachment;
import com.siemens.sw360.datahandler.thrift.components.Component;
import com.siemens.sw360.datahandler.thrift.components.ComponentService;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.components.ReleaseLink;
import com.siemens.sw360.datahandler.thrift.licenses.License;
import com.siemens.sw360.datahandler.thrift.licenses.LicenseService;
import com.siemens.sw360.datahandler.thrift.licenses.Obligation;
import com.siemens.sw360.datahandler.thrift.moderation.ModerationRequest;
import com.siemens.sw360.datahandler.thrift.moderation.ModerationService;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.projects.ProjectLink;
import com.siemens.sw360.datahandler.thrift.projects.ProjectRelationship;
import com.siemens.sw360.datahandler.thrift.projects.ProjectService;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.thrift.users.UserService;
import com.siemens.sw360.portal.common.PortalConstants;
import com.siemens.sw360.portal.portlets.FossologyAwarePortlet;
import com.siemens.sw360.portal.users.UserCacheHolder;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.jetbrains.annotations.NotNull;

import javax.portlet.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.siemens.sw360.datahandler.common.CommonUtils.nullToEmptySet;
import static com.siemens.sw360.portal.common.PortalConstants.*;

/**
 * Moderation portlet implementation
 *
 * @author daniele.fognini@tngtech.com
 * @author johannes.najjar@tngtech.com
 */
public class ModerationPortlet extends FossologyAwarePortlet {

    private static final Logger log = Logger.getLogger(ModerationPortlet.class);

    @Override
    public void serveResource(ResourceRequest request, ResourceResponse response) throws IOException, PortletException {
        String action = request.getParameter(PortalConstants.ACTION);
        if (isGenericAction(action)) {
            dealWithGenericAction(request, response, action);
        }
    }

    @Override
    public void doView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        if (PAGENAME_EDIT.equals(request.getParameter(PAGENAME))) {
            renderEditView(request, response);
        } else if (PAGENAME_ACTION.equals(request.getParameter(PAGENAME))) {
            renderActionView(request, response);
        } else {
            renderStandardView(request, response);
        }
    }

    private void renderActionView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        final User user = UserCacheHolder.getUserFromRequest(request);
        final String id = request.getParameter(MODERATION_ID);
        String sessionMessage;
        if (id != null) {
            try {
                ModerationService.Iface client = thriftClients.makeModerationClient();
                ModerationRequest moderationRequest = client.getModerationRequestById(id);
                if (ACTION_CANCEL.equals(request.getParameter(ACTION))) {
                    client.cancelInProgress(id);

                    sessionMessage = "You have cancelled working on the previous moderation request.";
                } else if (ACTION_DECLINE.equals(request.getParameter(ACTION))) {
                    client.refuseRequest(id);

                    sessionMessage = "You have declined the previous moderation request";
                } else if (ACTION_ACCEPT.equals(request.getParameter(ACTION))) {
                    String requestingUserEmail = moderationRequest.getRequestingUser();
                    User requestingUser = UserCacheHolder.getUserFromEmail(requestingUserEmail);
                    acceptModerationRequest(user, requestingUser, moderationRequest);

                    moderationRequest.setModerationState(ModerationState.APPROVED);
                    moderationRequest.setReviewer(user.getEmail());
                    client.updateModerationRequest(moderationRequest);

                    sessionMessage = "You have accepted the previous moderation request.";
                } else if (ACTION_POSTPONE.equals(request.getParameter(ACTION))) {
                    // keep me assigned but do it later... so nothing to be done here
                    sessionMessage = "You have postponed the previous moderation request.";
                } else if (ACTION_REMOVEME.equals(request.getParameter(ACTION))) {
                    client.removeUserFromAssignees(id, user);
                    sessionMessage = "You have removed yourself from the moderators of the previous moderation request.";

                } else {
                    throw new PortletException("Unknown action");
                }

                //! Actions are processed now we go and render the next one
                renderNextModeration(request, response, user, sessionMessage, client, moderationRequest);
            } catch (TException e) {
                log.error("Error in Moderation ", e);
            }
        }
    }

    private void acceptModerationRequest(User user, User requestingUser, ModerationRequest moderationRequest) throws TException {
        switch (moderationRequest.getDocumentType()) {
            case COMPONENT: {
                ComponentService.Iface componentClient = thriftClients.makeComponentClient();
                if (moderationRequest.isRequestDocumentDelete()) {
                    componentClient.deleteComponent(moderationRequest.getDocumentId(), user);
                } else {
                    componentClient.updateComponent(moderationRequest.getComponent(), user);
                }
            }
            break;
            case RELEASE: {
                ComponentService.Iface componentClient = thriftClients.makeComponentClient();
                if (moderationRequest.isRequestDocumentDelete()) {
                    componentClient.deleteRelease(moderationRequest.getDocumentId(), user);
                } else {
                    componentClient.updateRelease(moderationRequest.getRelease(), user);
                }
            }
            break;
            case PROJECT: {
                ProjectService.Iface projectClient = thriftClients.makeProjectClient();
                if (moderationRequest.isRequestDocumentDelete()) {
                    projectClient.deleteProject(moderationRequest.getDocumentId(), user);
                } else {
                    projectClient.updateProject(moderationRequest.getProject(), user);
                }
            }
            break;
            case LICENSE: {
                LicenseService.Iface licenseClient = thriftClients.makeLicenseClient();
                    licenseClient.updateLicense(moderationRequest.getLicense(), user, requestingUser);
            }
        }
        UserService.Iface userClient = thriftClients.makeUserClient();
        userClient.sendMailForAcceptedModerationRequest(moderationRequest.getRequestingUser());
    }

    private void renderNextModeration(RenderRequest request, RenderResponse response, final User user, String sessionMessage, ModerationService.Iface client, ModerationRequest moderationRequest) throws IOException, PortletException, TException {
        if (ACTION_CANCEL.equals(request.getParameter(ACTION))) {
            SessionMessages.add(request, "request_processed", sessionMessage);
            renderStandardView(request, response);
            return;
        }

        List<ModerationRequest> requestsByModerator = client.getRequestsByModerator(user);
        List<ModerationRequest> openModerationRequests = requestsByModerator
                .stream()
                .filter(input-> ModerationState.PENDING.equals(input.getModerationState()))
                .collect(Collectors.toList());

        Collections.sort(openModerationRequests, compareByTimeStamp());

        int nextIndex = openModerationRequests.indexOf(moderationRequest) + 1;
        if (nextIndex < openModerationRequests.size()) {
            renderEditViewForId(request, response, openModerationRequests.get(nextIndex).getId());
        } else {
            List<ModerationRequest> requestsInProgressAndAssignedToMe = requestsByModerator
                    .stream()
                    .filter(input-> ModerationState.INPROGRESS.equals(input.getModerationState()) && user.getEmail().equals(input.getReviewer()))
                    .collect(Collectors.toList());

            if (requestsInProgressAndAssignedToMe.size()>0) {
                sessionMessage += " You have returned to your first open request.";
                SessionMessages.add(request, "request_processed", sessionMessage);
                renderEditViewForId(request, response, Collections.min(requestsInProgressAndAssignedToMe, compareByTimeStamp()).getId());
            } else {
                sessionMessage += " You have no open Requests.";
                SessionMessages.add(request, "request_processed", sessionMessage);
                renderStandardView(request, response);
            }
        }
    }

    @NotNull
    private Comparator<ModerationRequest> compareByTimeStamp() {
        return new Comparator<ModerationRequest>() {
            @Override
            public int compare(ModerationRequest o1, ModerationRequest o2) {
                return Long.compare(o1.getTimestamp(), o2.getTimestamp());
            }
        };
    }

    private void addModerationBreadcrumb(RenderRequest request, RenderResponse response, ModerationRequest moderationRequest) {
        PortletURL baseUrl = response.createRenderURL();
        baseUrl.setParameter(PAGENAME, PAGENAME_EDIT);
        baseUrl.setParameter(MODERATION_ID, moderationRequest.getId());

        addBreadcrumbEntry(request, moderationRequest.getDocumentName(), baseUrl);
    }

    public void renderStandardView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        List<ModerationRequest> moderationRequests = null;
        try {
            ModerationService.Iface client = thriftClients.makeModerationClient();
            User user = UserCacheHolder.getUserFromRequest(request);
            moderationRequests = client.getRequestsByModerator(user);
        } catch (TException e) {
            log.error("Could not fetch license summary from backend!", e);
        }

        request.setAttribute(MODERATION_REQUESTS, CommonUtils.nullToEmptyList(moderationRequests));
        super.doView(request, response);
    }

    public void renderEditView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        String id = request.getParameter(MODERATION_ID);
        try {
            renderEditViewForId(request, response, id);
        } catch (TException e) {
            log.error("Thrift error", e);
        }
    }

    private void renderEditViewForId(RenderRequest request, RenderResponse response, String id) throws IOException, PortletException, TException {
        if (id != null) {
            ModerationRequest moderationRequest = null;
            User user = UserCacheHolder.getUserFromRequest(request);
            try {

                ModerationService.Iface client = thriftClients.makeModerationClient();
                moderationRequest = client.getModerationRequestById(id);
                if(moderationRequest.getModerationState().equals(ModerationState.PENDING) || moderationRequest.getModerationState().equals(ModerationState.INPROGRESS)) {
                    SessionMessages.add(request, "request_processed", "You have assigned yourself to this moderation request.");
                    client.setInProgress(id, user);
                }
                request.setAttribute(MODERATION_REQUEST, moderationRequest);
                addModerationBreadcrumb(request, response, moderationRequest);

            } catch (TException e) {
                log.error("Error fetching moderation  details from backend", e);
            }

            if (moderationRequest != null) {
                switch (moderationRequest.getDocumentType()) {
                    case COMPONENT:
                        renderComponentModeration(request, response, moderationRequest, user);
                        break;
                    case RELEASE:
                        renderReleaseModeration(request, response, moderationRequest, user);
                        break;
                    case PROJECT:
                        renderProjectModeration(request, response, moderationRequest, user);
                        break;
                    case LICENSE:
                        renderLicenseModeration(request, response, moderationRequest, user);
                        break;
                }
                request.setAttribute(PortalConstants.MODERATION_REQUEST, moderationRequest);
            }
        }
    }

    public void renderComponentModeration(RenderRequest request, RenderResponse response, ModerationRequest moderationRequest, User user) throws IOException, PortletException, TException {

        final boolean requestDocumentDelete = moderationRequest.isRequestDocumentDelete();
        Boolean is_used = false;

        Component actual_component = null;

        try {
            ComponentService.Iface client = thriftClients.makeComponentClient();
            actual_component = client.getComponentById(moderationRequest.getDocumentId(), user);
            is_used = client.componentIsUsed(actual_component.getId());
        } catch (TException e) {
            log.error("Could not retrieve component", e);
        }

        if (actual_component == null) {
            renderNextModeration(request, response, user, "Ignored unretrievable target", thriftClients.makeModerationClient(), moderationRequest);
            return;
        }

        if (refuseToDeleteUsedDocument(request, response, moderationRequest, user, requestDocumentDelete, is_used))
            return;

        prepareComponent(request, user, actual_component, moderationRequest);
        request.setAttribute(PortalConstants.ACTUAL_COMPONENT, actual_component);
        if (moderationRequest.isRequestDocumentDelete()) {
            include("/html/moderation/components/delete.jsp", request, response);
        } else {
            include("/html/moderation/components/merge.jsp", request, response);
        }
    }

    private void prepareComponent(RenderRequest request, User user, Component actualComponent, ModerationRequest moderationRequest) {
        List<Release> releases;

        releases = CommonUtils.nullToEmptyList(actualComponent.getReleases());
        Set<String> releaseIds = SW360Utils.getReleaseIds(releases);

        final Component moderatedComponent = moderationRequest.getComponent();
        setLicenseNames(user, actualComponent, moderatedComponent);

        Set<Project> usingProjects = null;

        if (releaseIds != null && releaseIds.size() > 0) {
            try {
                ProjectService.Iface projectClient = thriftClients.makeProjectClient();
                usingProjects = projectClient.searchByReleaseIds(releaseIds, user);
            } catch (TException e) {
                log.error("Could not retrieve using projects", e);
            }
        }
        request.setAttribute(DOCUMENT_TYPE, SW360Constants.TYPE_COMPONENT);
        setAttachmentsInRequest(request, actualComponent.getAttachments());
        request.setAttribute(USING_PROJECTS, nullToEmptySet(usingProjects));
    }

    public void renderReleaseModeration(RenderRequest request, RenderResponse response, ModerationRequest moderationRequest, User user) throws IOException, PortletException, TException {
        Release actual_release = null;

        final boolean requestDocumentDelete = moderationRequest.isRequestDocumentDelete();
        Boolean is_used = false;

        try {
            ComponentService.Iface client = thriftClients.makeComponentClient();
            actual_release = client.getReleaseById(moderationRequest.getDocumentId(), user);
            is_used = client.releaseIsUsed(actual_release.getId());
        } catch (TException e) {
            log.error("Could not retrieve release", e);
        }

        if (actual_release == null) {
            renderNextModeration(request, response, user, "Ignored unretrievable target", thriftClients.makeModerationClient(), moderationRequest);
            return;
        }

        if (refuseToDeleteUsedDocument(request, response, moderationRequest, user, requestDocumentDelete, is_used))
            return;

        prepareRelease(request, user, actual_release, moderationRequest);
        request.setAttribute(PortalConstants.ACTUAL_RELEASE, actual_release);
        if (requestDocumentDelete) {
            include("/html/moderation/releases/delete.jsp", request, response);
        } else {
            include("/html/moderation/releases/merge.jsp", request, response);
        }
    }

    private boolean refuseToDeleteUsedDocument(RenderRequest request, RenderResponse response, ModerationRequest moderationRequest, User user, boolean requestDocumentDelete, Boolean is_used) throws TException, IOException, PortletException {
        if (requestDocumentDelete && is_used) {
            ModerationService.Iface client = thriftClients.makeModerationClient();
            client.refuseRequest(moderationRequest.getId());
            renderNextModeration(request, response, user, "Ignored delete of used target", client, moderationRequest);
            return true;
        }
        return false;
    }

    private void prepareRelease(RenderRequest request, User user, Release actualRelease, ModerationRequest moderationRequest) {

        final Release moderatedRelease = moderationRequest.getRelease();

        setLicenseNames(user, actualRelease, moderatedRelease);

        String actualReleaseId = actualRelease.getId();
        request.setAttribute(DOCUMENT_ID, actualReleaseId);
        request.setAttribute(DOCUMENT_TYPE, SW360Constants.TYPE_RELEASE);
        setAttachmentsInRequest(request, actualRelease.getAttachments());
        try {
            ProjectService.Iface projectClient = thriftClients.makeProjectClient();
            Set<Project> usingProjects = projectClient.searchByReleaseId(actualReleaseId, user);
            request.setAttribute(USING_PROJECTS, nullToEmptySet(usingProjects));
            putLinkedReleaseRelationsInRequest(request, actualRelease.getReleaseIdToRelationship());
        } catch (TException e) {
            log.error("Could not retrieve using projects", e);
        }

        try {
            request.setAttribute(COMPONENT, thriftClients.makeComponentClient().getComponentById(actualRelease.getComponentId(), user));
        } catch (TException e) {
            log.error("Could not fetch component from Backend ", e);
        }

    }

    public void renderProjectModeration(RenderRequest request, RenderResponse response, ModerationRequest moderationRequest, User user) throws IOException, PortletException, TException {
        final boolean requestDocumentDelete = moderationRequest.isRequestDocumentDelete();
        Boolean is_used = false;
        Project actual_project = null;
        try {
            ProjectService.Iface client = thriftClients.makeProjectClient();
            actual_project = client.getProjectById(moderationRequest.getDocumentId(), user);
            is_used = client.projectIsUsed(actual_project.getId());
            request.setAttribute(PortalConstants.ACTUAL_PROJECT, actual_project);
        } catch (TException e) {
            log.error("Could not retrieve project", e);
        }

        if (actual_project == null) {
            renderNextModeration(request, response, user, "Ignored unretrievable target", thriftClients.makeModerationClient(), moderationRequest);
            return;
        }

        if (refuseToDeleteUsedDocument(request, response, moderationRequest, user, requestDocumentDelete, is_used))
            return;

        prepareProject(request, user, actual_project);
        if (moderationRequest.isRequestDocumentDelete()) {
            include("/html/moderation/projects/delete.jsp", request, response);
        } else {
            include("/html/moderation/projects/merge.jsp", request, response);
        }
    }

    private void prepareProject(RenderRequest request, User user, Project actual_project) {
        try {
            ProjectService.Iface client = thriftClients.makeProjectClient();
            request.setAttribute(PortalConstants.PROJECT_LIST, getLinkedProjects(actual_project.getLinkedProjects()));
            putLinkedReleasesInRequest(request, actual_project.getReleaseIdToUsage());
            Set<Project> usingProjects = client.searchLinkingProjects(actual_project.getId(), user);
            request.setAttribute(USING_PROJECTS, usingProjects);

            Map<Release, String> releaseStringMap = getReleaseStringMap(actual_project.getId(), user);

            request.setAttribute(PortalConstants.RELEASES_AND_PROJECTS, releaseStringMap);
            request.setAttribute(DOCUMENT_TYPE, SW360Constants.TYPE_PROJECT);
            setAttachmentsInRequest(request, actual_project.getAttachments());
        } catch (TException e) {
            log.error("Error fetching project from backend!", e);
        }
    }

    public void renderLicenseModeration(RenderRequest request, RenderResponse response, ModerationRequest moderationRequest, User user) throws IOException, PortletException, TException {
        License actual_license = null;
        User requestingUser = UserCacheHolder.getUserFromEmail(moderationRequest.getRequestingUser());
        try {
            LicenseService.Iface client = thriftClients.makeLicenseClient();
            actual_license = client.getByID(moderationRequest.getDocumentId(),requestingUser.getDepartment());
            request.setAttribute(PortalConstants.ACTUAL_LICENSE, actual_license);
            List<Obligation> obligations = client.getObligations();
            request.setAttribute(KEY_OBLIGATION_LIST, obligations);
            request.setAttribute(KEY_LICENSE_DETAIL, actual_license);
        } catch (TException e) {
            log.error("Could not retrieve license", e);
        }

        if (actual_license == null) {
            renderNextModeration(request, response, user, "Ignored unretrievable target", thriftClients.makeModerationClient(), moderationRequest);
            return;
        }

        include("/html/moderation/licenses/merge.jsp", request, response);
    }


    private Map<Integer, Collection<ReleaseLink>> getLinkedReleases(Map<String, String> releaseIdToUsage) {
        return SW360Utils.getLinkedReleases(releaseIdToUsage, thriftClients, log);
    }

    private List<ProjectLink> getLinkedProjects(Map<String, ProjectRelationship> linkedProjects) {
        return SW360Utils.getLinkedProjects(linkedProjects, thriftClients, log);
    }

    private UnsupportedOperationException unsupportedActionException() {
        throw new UnsupportedOperationException("cannot call this action on the moderation portlet");
    }

    private static void setLicenseNames(User user, Component actualComponent, Component moderatedComponent) {
        Set<String> licenseIds = new HashSet<>();
        if (actualComponent.isSetMainLicenseIds()) {
            licenseIds.addAll(actualComponent.getMainLicenseIds());
        }
        if (moderatedComponent.isSetMainLicenseIds()) {
            licenseIds.addAll(moderatedComponent.getMainLicenseIds());
        }

        Map<String, License> idToLicense = SW360Utils.getStringLicenseMap(user, licenseIds);
        SW360Utils.setLicenseNames(actualComponent, idToLicense);
        SW360Utils.setLicenseNames(moderatedComponent, idToLicense);
    }

    private static void setLicenseNames(User user, Release actualRelease, Release moderatedRelease) {
        Set<String> licenseIds = new HashSet<>();
        if (actualRelease.isSetMainLicenseIds()) {
            licenseIds.addAll(actualRelease.getMainLicenseIds());
        }
        if (moderatedRelease.isSetMainLicenseIds()) {
            licenseIds.addAll(moderatedRelease.getMainLicenseIds());
        }

        Map<String, License> idToLicense = SW360Utils.getStringLicenseMap(user, licenseIds);
        SW360Utils.setLicenseNames(actualRelease, idToLicense);
        SW360Utils.setLicenseNames(moderatedRelease, idToLicense);
    }

    @Override
    protected void dealWithFossologyAction(ResourceRequest request, ResourceResponse response, String action) throws IOException, PortletException {
        if (PortalConstants.FOSSOLOGY_GET_STATUS.equals(action)) {
            serveFossologyStatus(request, response);
        } else {
            throw unsupportedActionException();
        }
    }

    @Override
    protected Attachment linkAttachment(String documentId, String documentType, User user, String attachmentId) {
        throw unsupportedActionException();
    }

    @Override
    protected RequestStatus deleteAttachment(String documentId, String documentType, User user, String attachmentId) {
        throw unsupportedActionException();
    }

    @Override
    protected Set<Attachment> getAttachments(String documentId, String documentType, User user) {
        throw unsupportedActionException();
    }
}
