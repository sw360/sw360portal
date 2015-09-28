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
package com.siemens.sw360.portal.portlets;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.common.ThriftEnumUtils;
import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.ThriftClients;
import com.siemens.sw360.datahandler.thrift.attachments.Attachment;
import com.siemens.sw360.datahandler.thrift.components.ComponentService;
import com.siemens.sw360.datahandler.thrift.components.FossologyStatus;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.fossology.FossologyService;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.projects.ProjectRelationship;
import com.siemens.sw360.datahandler.thrift.projects.ProjectService;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.portal.common.PortalConstants;
import com.siemens.sw360.portal.common.datatables.DataTablesParser;
import com.siemens.sw360.portal.common.datatables.data.DataTablesParameters;
import com.siemens.sw360.portal.users.UserCacheHolder;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import java.io.IOException;
import java.util.*;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.siemens.sw360.datahandler.common.CommonUtils.nullToEmptyMap;
import static com.siemens.sw360.datahandler.common.SW360Utils.printName;
import static com.siemens.sw360.portal.common.PortalConstants.*;

/**
 * Fossology aware portlet implementation
 *
 * @author Daniele.Fognini@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 */
public abstract class FossologyAwarePortlet extends AttachmentAwarePortlet {

    private static final Logger log = Logger.getLogger(FossologyAwarePortlet.class);

    public FossologyAwarePortlet() {

    }

    public FossologyAwarePortlet(ThriftClients thriftClients) {
        super(thriftClients);
    }

    protected abstract void dealWithFossologyAction(ResourceRequest request, ResourceResponse response, String action) throws IOException, PortletException;

    protected void serveSendToFossology(ResourceRequest request, ResourceResponse response) throws PortletException {
        final RequestStatus requestStatus = sendToFossology(request);
        renderRequestStatus(request, response, requestStatus);
    }

    protected RequestStatus sendToFossology(ResourceRequest request) {
        final String releaseId = request.getParameter(RELEASE_ID);
        final String clearingTeam = request.getParameter(CLEARING_TEAM);

        try {
            FossologyService.Iface client = thriftClients.makeFossologyClient();
            return client.sendToFossology(releaseId, UserCacheHolder.getUserFromRequest(request), clearingTeam);
        } catch (TException e) {
            log.error("Could not send release to fossology", e);
        }
        return RequestStatus.FAILURE;
    }

    protected void serveFossologyStatus(ResourceRequest request, ResourceResponse response) throws IOException {
        DataTablesParameters parameters = DataTablesParser.parametersFrom(request);

        Release release = getReleaseForFossologyStatus(request);

        Map<String, FossologyStatus> fossologyStatus = getFossologyStatus(release);

        JSONObject jsonResponse = JSONFactoryUtil.createJSONObject();

        JSONArray data = JSONFactoryUtil.createJSONArray();

        for (Map.Entry<String, FossologyStatus> entry : fossologyStatus.entrySet()) {
            JSONObject row = JSONFactoryUtil.createJSONObject();
            row.put("0", entry.getKey());
            row.put("1", ThriftEnumUtils.enumToString(entry.getValue()));
            data.put(row);
        }

        jsonResponse.put("attachment", getFossologyUploadableAttachment(release));

        jsonResponse.put("data", data);
        jsonResponse.put("draw", parameters.getDraw());
        jsonResponse.put("recordsTotal", fossologyStatus.size());
        jsonResponse.put("recordsFiltered", fossologyStatus.size());

        writeJSON(request, response, jsonResponse);
    }

    private String getFossologyUploadableAttachment(Release release) {
        String sourceAttachment = null;
        if (release != null) {
            try {
                ComponentService.Iface fossologyClient = thriftClients.makeComponentClient();
                final Set<Attachment> sourceAttachments = fossologyClient.getSourceAttachments(release.getId());
                if (sourceAttachments.size() == 1) {
                    sourceAttachment = CommonUtils.getFirst(sourceAttachments).getFilename();
                }
            } catch (TException e) {
                log.error("cannot get name of the attachment of release", e);
            }
        }

        if (isNullOrEmpty(sourceAttachment)) {
            return "no unique source attachment found!";
        } else {
            return sourceAttachment;
        }
    }

    protected Map<String, FossologyStatus> getFossologyStatus(Release release) {
        if (release != null) {
            return nullToEmptyMap(release.getClearingTeamToFossologyStatus());
        } else {
            log.error("no response from backend!");
        }

        return Collections.emptyMap();
    }

    protected Release getReleaseForFossologyStatus(ResourceRequest request) {
        String releaseId = request.getParameter(RELEASE_ID);
        String clearingTeam = request.getParameter(CLEARING_TEAM);

        boolean cached = Boolean.parseBoolean(request.getParameter("cached"));

        if (!isNullOrEmpty(releaseId) && !isNullOrEmpty(clearingTeam)) {
            try {
                final Release release;
                if (!cached) {
                    FossologyService.Iface client = thriftClients.makeFossologyClient();
                    release = client.getStatusInFossology(releaseId, UserCacheHolder.getUserFromRequest(request), clearingTeam);
                } else {
                    ComponentService.Iface client = thriftClients.makeComponentClient();
                    release = client.getReleaseById(releaseId, UserCacheHolder.getUserFromRequest(request));
                }
                if (release != null) {
                    return release;
                } else {
                    log.error("no response from backend!");
                }
            } catch (TException e) {
                log.error("Could not release status in fossology", e);
            }
        }

        log.error("Could not get release from request");
        return null;
    }

    protected boolean isFossologyAwareAction(String action) {
        return action.startsWith(PortalConstants.FOSSOLOGY_PREFIX);
    }

    @Override
    protected boolean isGenericAction(String action) {
        return super.isGenericAction(action) || isFossologyAwareAction(action);
    }

    @Override
    protected void dealWithGenericAction(ResourceRequest request, ResourceResponse response, String action) throws IOException, PortletException {

        if (super.isGenericAction(action)) {
            super.dealWithGenericAction(request, response, action);
        } else if (isFossologyAwareAction(action)) {
            dealWithFossologyAction(request, response, action);
        }
    }

    protected void serveGetSendableReleases(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
        String projectId = request.getParameter(PROJECT_ID);

        User user = UserCacheHolder.getUserFromRequest(request);
        try {
            Map<Release, String> releaseStringMap = getReleaseStringMap(projectId, user);
            request.setAttribute(PortalConstants.RELEASES_AND_PROJECTS, releaseStringMap);
            include("/html/projects/ajax/sendableTable.jsp", request, response, PortletRequest.RESOURCE_PHASE);

        } catch (TException e) {
            log.error("Problem with project client", e);
            throw new PortletException(e);
        }
    }

    protected Map<Release, String> getReleaseStringMap(String projectId, User user) throws TException {
        ProjectService.Iface client = thriftClients.makeProjectClient();
        Project project = client.getProjectById(projectId, user);
        SetMultimap<String, Project> releaseIdsToProject = releaseIdToProjects(project, user);
        List<Release> releasesById = thriftClients.makeComponentClient().getFullReleasesById(releaseIdsToProject.keySet(), user);

        Map<Release, String> releaseStringMap = new HashMap<>();
        for (Release release : releasesById) {
            Set<String> projectNames = new HashSet<>();

            for (Project project1 : releaseIdsToProject.get(release.getId())) {
                projectNames.add(printName(project1));
                if (projectNames.size() > 3) {
                    projectNames.add("...");
                    break;
                }
            }

            String commaSeparated = Joiner.on(", ").join(projectNames);
            releaseStringMap.put(release, commaSeparated);
        }

        return releaseStringMap;
    }

    protected void serveProjectSendToFossology(ResourceRequest request, ResourceResponse response) {

        String projectId = request.getParameter(PROJECT_ID);
        String[] releaseIdArray = request.getParameterValues(RELEASE_ID);
        if (projectId == null || releaseIdArray == null) {
            renderRequestStatus(request, response, RequestStatus.FAILURE);
            return;
        }

        String clearingTeam = request.getParameter(CLEARING_TEAM);

        if (isNullOrEmpty(clearingTeam)) {
            try {
                User user = UserCacheHolder.getUserFromRequest(request);
                ProjectService.Iface client = thriftClients.makeProjectClient();
                Project project = client.getProjectById(projectId, user);
                clearingTeam = project.getClearingTeam();
            } catch (TException e) {
                log.error("Problem with project client", e);
            }
        }

        if (!isNullOrEmpty(clearingTeam)) {
            List<String> releaseIds = Arrays.asList(releaseIdArray);
            try {
                FossologyService.Iface fossologyClient = thriftClients.makeFossologyClient();

                renderRequestStatus(request, response, fossologyClient.sendReleasesToFossology(releaseIds, UserCacheHolder.getUserFromRequest(request), clearingTeam));

            } catch (TException e) {
                log.error("Problem with fossology client", e);
            }
        } else {
            log.error("Cannot decide on a clearing team for project " + projectId);
        }
    }

    protected SetMultimap<String, Project> releaseIdToProjects(Project project, User user) {
        Set<String> visitedProjectIds = new HashSet<>();
        SetMultimap<String, Project> releaseIdToProjects = HashMultimap.create();

        releaseIdToProjects(project, user, visitedProjectIds, releaseIdToProjects);
        return releaseIdToProjects;
    }

    protected void releaseIdToProjects(Project project, User user, Set<String> visitedProjectIds, Multimap<String, Project> releaseIdToProjects) {

        if (nothingTodo(project, visitedProjectIds)) return;

        final Set<String> releaseIds = nullToEmptyMap(project.getReleaseIdToUsage()).keySet();
        try {
            List<Release> releasesById = thriftClients.makeComponentClient().getReleasesById(releaseIds, user);
            for (Release release : releasesById) {
                releaseIdToProjects.put(release.getId(), project);
            }
        } catch (TException e) {
            log.error("Error with component client", e);
        }

        Map<String, ProjectRelationship> linkedProjects = project.getLinkedProjects();
        if (linkedProjects != null) {

            try {
                ProjectService.Iface projectClient = thriftClients.makeProjectClient();
                for (String projectId : linkedProjects.keySet()) {
                    if (visitedProjectIds.contains(projectId)) continue;

                    Project linkedProject = projectClient.getProjectById(projectId, user);
                    releaseIdToProjects(linkedProject, user, visitedProjectIds, releaseIdToProjects);
                }

            } catch (TException e) {
                log.error("Error with project client", e);
            }
        }
    }

    private boolean nothingTodo(Project project, Set<String> visitedProjectIds) {
        if (project == null) {
            return true;
        }
        return alreadyBeenHere(project.getId(), visitedProjectIds);
    }

    private boolean alreadyBeenHere(String id, Set<String> visitedProjectIds) {
        if (visitedProjectIds.contains(id)) {
            return true;
        }
        visitedProjectIds.add(id);
        return false;
    }
}
