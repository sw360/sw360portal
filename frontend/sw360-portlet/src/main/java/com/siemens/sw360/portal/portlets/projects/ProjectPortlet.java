/*
 * Copyright Siemens AG, 2013-2016. Part of the SW360 Portal Project.
 * With contributions by Bosch Software Innovations GmbH, 2016.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.portal.portlets.projects;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.portlet.PortletResponseUtil;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.model.Organization;
import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.common.SW360Constants;
import com.siemens.sw360.datahandler.common.ThriftEnumUtils;
import com.siemens.sw360.datahandler.thrift.DocumentState;
import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.attachments.Attachment;
import com.siemens.sw360.datahandler.thrift.components.ComponentService;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.components.ReleaseClearingStateSummary;
import com.siemens.sw360.datahandler.thrift.components.ReleaseLink;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.projects.ProjectLink;
import com.siemens.sw360.datahandler.thrift.projects.ProjectRelationship;
import com.siemens.sw360.datahandler.thrift.projects.ProjectService;
import com.siemens.sw360.datahandler.thrift.users.RequestedAction;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.thrift.vendors.Vendor;
import com.siemens.sw360.datahandler.thrift.vendors.VendorService;
import com.siemens.sw360.exporter.ProjectExporter;
import com.siemens.sw360.portal.common.*;
import com.siemens.sw360.portal.portlets.FossologyAwarePortlet;
import com.siemens.sw360.portal.users.LifeRayUserSession;
import com.siemens.sw360.portal.users.UserCacheHolder;
import com.siemens.sw360.portal.users.UserUtils;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import javax.portlet.*;
import java.io.IOException;
import java.util.*;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;
import static com.liferay.portal.kernel.json.JSONFactoryUtil.createJSONArray;
import static com.liferay.portal.kernel.json.JSONFactoryUtil.createJSONObject;
import static com.siemens.sw360.datahandler.common.SW360Utils.printName;
import static com.siemens.sw360.portal.common.PortalConstants.*;
import static org.apache.commons.lang.StringUtils.abbreviate;

/**
 * Component portlet implementation
 *
 * @author cedric.bodet@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 * @author alex.borodin@evosoft.com
 */
public class ProjectPortlet extends FossologyAwarePortlet {

    private static final Logger log = Logger.getLogger(ProjectPortlet.class);

    private static final ImmutableList<Project._Fields> projectFilteredFields = ImmutableList.of(
            Project._Fields.BUSINESS_UNIT,
            Project._Fields.PROJECT_TYPE,
            Project._Fields.PROJECT_RESPONSIBLE,
            Project._Fields.NAME,
            Project._Fields.STATE,
            Project._Fields.TAG);

    @Override
    protected Set<Attachment> getAttachments(String documentId, String documentType, User user) {

        try {
            ProjectService.Iface client = thriftClients.makeProjectClient();
            Project projectById = client.getProjectById(documentId, user);
            return CommonUtils.nullToEmptySet(projectById.getAttachments());
        } catch (TException e) {
            log.error("Could not get project", e);
        }
        return Collections.emptySet();
    }

    //Helper methods
    private void addProjectBreadcrumb(RenderRequest request, RenderResponse response, Project project) {
        PortletURL url = response.createRenderURL();
        url.setParameter(PAGENAME, PAGENAME_DETAIL);
        url.setParameter(PROJECT_ID, project.getId());

        addBreadcrumbEntry(request, printName(project), url);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void serveResource(ResourceRequest request, ResourceResponse response) throws IOException, PortletException {
        String action = request.getParameter(PortalConstants.ACTION);

        if (PortalConstants.PROJECT_LIST.equals(action)) {
            serveListProjects(request, response);
        } else if (PortalConstants.VIEW_LINKED_PROJECTS.equals(action)) {
            serveLinkedProjects(request, response);
        } else if (PortalConstants.REMOVE_PROJECT.equals(action)) {
            serveRemoveProject(request, response);
        } else if (PortalConstants.VIEW_LINKED_RELEASES.equals(action)) {
            serveLinkedReleases(request, response);
        } else if (PortalConstants.EXPORT_TO_EXCEL.equals(action)) {
            exportExcel(request, response);
        } else if (isGenericAction(action)) {
            dealWithGenericAction(request, response, action);
        }
    }

    private void serveListProjects(ResourceRequest request, ResourceResponse response) throws IOException {
        User user = UserCacheHolder.getUserFromRequest(request);
        Collection<Project> projects = setClearingStateSummary(getAccessibleProjects(user));

        JSONObject jsonResponse = createJSONObject();
        JSONArray data = createJSONArray();
        ThriftJsonSerializer thriftJsonSerializer = new ThriftJsonSerializer();

        for (Project project : projects) {
            try {
                JSONObject row = createJSONObject();
                row.put("id", project.getId());
                row.put("name", printName(project));
                String pDesc = abbreviate(project.getDescription(), 140);
                row.put("description", pDesc == null || pDesc.isEmpty() ? "N.A.": pDesc);
                row.put("state", ThriftEnumUtils.enumToString(project.getState()));
                row.put("clearing", JsonHelpers.toJson(project.getReleaseClearingStateSummary(), thriftJsonSerializer));
                row.put("responsible", JsonHelpers.getProjectResponsible(thriftJsonSerializer, project));

                data.put(row);
            } catch (JSONException e) {
                log.error("cannot serialize json", e);
            }
        }

        jsonResponse.put("data", data);

        writeJSON(request, response, jsonResponse);
    }

    @Override
    protected void dealWithFossologyAction(ResourceRequest request, ResourceResponse response, String action) throws IOException, PortletException {
        if (PortalConstants.FOSSOLOGY_SEND.equals(action)) {
            serveProjectSendToFossology(request, response);
        } else if (PortalConstants.FOSSOLOGY_GET_SENDABLE.equals(action)) {
            serveGetSendableReleases(request, response);
        } else if (PortalConstants.FOSSOLOGY_GET_STATUS.equals(action)) {
            serveFossologyStatus(request, response);
        }
    }

    private void serveRemoveProject(ResourceRequest request, ResourceResponse response) throws IOException {
        RequestStatus requestStatus = removeProject(request);
        serveRequestStatus(request, response, requestStatus, "Problem removing project", log);
    }

    private void exportExcel(ResourceRequest request, ResourceResponse response) {
        final User user = UserCacheHolder.getUserFromRequest(request);
        try {
            ProjectService.Iface client = thriftClients.makeProjectClient();
            String searchText = request.getParameter(PortalConstants.KEY_SEARCH_TEXT);
            List<Project> projects;
            if (isNullOrEmpty(searchText)) {
                projects = client.getAccessibleProjectsSummary(user);
            } else {
                projects = client.searchByName(searchText, user);
            }

            ProjectExporter exporter = new ProjectExporter(thriftClients.makeComponentClient());
            PortletResponseUtil.sendFile(request, response, "Projects.xlsx", exporter.makeExcelExport(projects), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        } catch (IOException | TException e) {
            log.error("An error occurred while generating the Excel export", e);
        }
    }

    private RequestStatus removeProject(PortletRequest request) {
        String projectId = request.getParameter(PortalConstants.PROJECT_ID);
        final User user = UserCacheHolder.getUserFromRequest(request);

        try {
            deleteUnneededAttachments(user.getEmail(),projectId);
            ProjectService.Iface client = thriftClients.makeProjectClient();
            return client.deleteProject(projectId, user);
        } catch (TException e) {
            log.error("Error deleting project from backend", e);
        }

        return RequestStatus.FAILURE;
    }

    private void serveLinkedProjects(ResourceRequest request, ResourceResponse response) throws IOException, PortletException {
        String what = request.getParameter(PortalConstants.WHAT);

        if (PortalConstants.LIST_NEW_LINKED_PROJECTS.equals(what)) {
            String[] where = request.getParameterValues(PortalConstants.WHERE_ARRAY);
            serveNewTableRowLinkedProjects(request, response, where);
        } else if (PortalConstants.PROJECT_SEARCH.equals(what)) {
            String where = request.getParameter(PortalConstants.WHERE);
            serveProjectSearchResults(request, response, where);
        }
    }

    private void serveLinkedReleases(ResourceRequest request, ResourceResponse response) throws IOException, PortletException {
        String what = request.getParameter(PortalConstants.WHAT);

        String projectId = request.getParameter(PROJECT_ID);

        if (PortalConstants.LIST_NEW_LINKED_RELEASES.equals(what)) {
            String[] where = request.getParameterValues(PortalConstants.WHERE_ARRAY);
            serveNewTableRowLinkedRelease(request, response, where);
        } else if (PortalConstants.RELEASE_SEARCH.equals(what)) {
            String where = request.getParameter(PortalConstants.WHERE);
            serveReleaseSearchResults(request, response, where);
        } else if (PortalConstants.RELEASE_SEARCH_BY_VENDOR.equals(what)) {
            String where = request.getParameter(PortalConstants.WHERE);
            serveReleaseSearchResultsByVendor(request, response, where);
        } else if (PortalConstants.RELEASE_LIST_FROM_LINKED_PROJECTS.equals(what)) {
            serveReleasesFromLinkedProjects(request, response, projectId);
        }
    }

    private void serveNewTableRowLinkedProjects(ResourceRequest request, ResourceResponse response, String[] linkedIds) throws IOException, PortletException {
        final User user = UserCacheHolder.getUserFromRequest(request);

        List<ProjectLink> linkedProjects = new ArrayList<>();
        try {
            ProjectService.Iface client = thriftClients.makeProjectClient();

            for (String linkedId : linkedIds) {
                Project project = client.getProjectById(linkedId, user);
                ProjectLink linkedProject = new ProjectLink(linkedId, project.getName());
                linkedProject.setRelation(ProjectRelationship.UNKNOWN);
                linkedProject.setVersion(project.getVersion());
                linkedProjects.add(linkedProject);
            }
        } catch (TException e) {
            log.error("Error getting projects!", e);
            throw new PortletException("cannot get projects " + Arrays.toString(linkedIds), e);
        }

        request.setAttribute(PROJECT_LIST, linkedProjects);

        include("/html/projects/ajax/linkedProjectsAjax.jsp", request, response, PortletRequest.RESOURCE_PHASE);
    }


    @SuppressWarnings("Duplicates")
    private void serveNewTableRowLinkedRelease(ResourceRequest request, ResourceResponse response, String[] linkedIds) throws IOException, PortletException {
        final User user = UserCacheHolder.getUserFromRequest(request);

        List<ReleaseLink> linkedReleases = new ArrayList<>();
        try {
            ComponentService.Iface client = thriftClients.makeComponentClient();
            for (Release release : client.getReleasesById(new HashSet<>(Arrays.asList(linkedIds)), user)) {
                final Vendor vendor = release.getVendor();
                final String fullname = vendor!=null?vendor.getFullname():"";
                ReleaseLink linkedRelease = new ReleaseLink(release.getId(),
                        fullname, release.getName(), release.getVersion());
                linkedReleases.add(linkedRelease);
            }
        } catch (TException e) {
            log.error("Error getting releases!", e);
            throw new PortletException("cannot get releases " + Arrays.toString(linkedIds), e);
        }
        request.setAttribute(RELEASE_LIST, linkedReleases);
        include("/html/utils/ajax/linkedReleasesAjax.jsp", request, response, PortletRequest.RESOURCE_PHASE);
    }


    private void serveProjectSearchResults(ResourceRequest request, ResourceResponse response, String searchText) throws IOException, PortletException {
        final User user = UserCacheHolder.getUserFromRequest(request);
        List<Project> searchResult;

        try {
            ProjectService.Iface client = thriftClients.makeProjectClient();
            if (isNullOrEmpty(searchText)) {
                searchResult = client.getAccessibleProjectsSummary(user);
            } else {
                searchResult = client.searchByName(searchText, user);
            }
        } catch (TException e) {
            log.error("Error searching projects", e);
            searchResult = Collections.emptyList();
        }

        request.setAttribute(PortalConstants.PROJECT_SEARCH, searchResult);

        include("/html/projects/ajax/searchProjectsAjax.jsp", request, response, PortletRequest.RESOURCE_PHASE);
    }

    private void serveReleaseSearchResults(ResourceRequest request, ResourceResponse response, String searchText) throws IOException, PortletException {
        serveReleaseSearch(request, response, searchText, false);
    }

    private void serveReleaseSearchResultsByVendor(ResourceRequest request, ResourceResponse response, String searchText) throws IOException, PortletException {
        serveReleaseSearch(request, response, searchText, true);
    }


    @SuppressWarnings("Duplicates")
    private void serveReleaseSearch(ResourceRequest request, ResourceResponse response, String searchText, boolean searchByVendor) throws IOException, PortletException {
        List<Release> searchResult;

        try {
            ComponentService.Iface componentClient = thriftClients.makeComponentClient();
            if (searchByVendor) {
                final VendorService.Iface vendorClient = thriftClients.makeVendorClient();
                final Set<String> vendorIds = vendorClient.searchVendorIds(searchText);
                if (vendorIds != null && vendorIds.size() > 0) {
                    searchResult = componentClient.getReleasesFromVendorIds(vendorIds);
                } else {
                    searchResult = Collections.emptyList();
                }
            } else {
                searchResult = componentClient.searchReleaseByName(searchText);
            }
        } catch (TException e) {
            log.error("Error searching projects", e);
            searchResult = Collections.emptyList();
        }

        request.setAttribute(PortalConstants.RELEASE_SEARCH, searchResult);

        include("/html/utils/ajax/searchReleasesAjax.jsp", request, response, PortletRequest.RESOURCE_PHASE);
    }


    private void serveReleasesFromLinkedProjects(ResourceRequest request, ResourceResponse response, String projectId) throws IOException, PortletException {
        List<Release> searchResult;

        Set<String> releaseIdsFromLinkedProjects = new HashSet<>();

        User user = UserCacheHolder.getUserFromRequest(request);

        try {
            ComponentService.Iface componentClient = thriftClients.makeComponentClient();
            ProjectService.Iface projectClient = thriftClients.makeProjectClient();

            Project project = projectClient.getProjectById(projectId, user);

            Map<String, ProjectRelationship> linkedProjects = CommonUtils.nullToEmptyMap(project.getLinkedProjects());
            for (String linkedProjectId : linkedProjects.keySet()) {
                Project linkedProject = projectClient.getProjectById(linkedProjectId, user);

                if (linkedProject != null) {
                    Map<String, String> releaseIdToUsage = CommonUtils.nullToEmptyMap(linkedProject.getReleaseIdToUsage());
                    releaseIdsFromLinkedProjects.addAll(releaseIdToUsage.keySet());
                }
            }

            if (releaseIdsFromLinkedProjects.size() > 0) {
                searchResult = componentClient.getReleasesById(releaseIdsFromLinkedProjects, user);
            } else {
                searchResult = Collections.emptyList();
            }


        } catch (TException e) {
            log.error("Error searching projects", e);
            searchResult = Collections.emptyList();
        }

        request.setAttribute(PortalConstants.RELEASE_SEARCH, searchResult);

        include("/html/utils/ajax/searchReleasesAjax.jsp", request, response, PortletRequest.RESOURCE_PHASE);
    }


    @Override
    public void doView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        String pageName = request.getParameter(PAGENAME);
        if (PAGENAME_DETAIL.equals(pageName)) {
            prepareDetailView(request, response);
            include("/html/projects/detail.jsp", request, response);
        } else if (PAGENAME_EDIT.equals(pageName)) {
            prepareProjectEdit(request);
            include("/html/projects/edit.jsp", request, response);
        } else if (PAGENAME_DUPLICATE.equals(pageName)) {
            prepareProjectDuplicate(request);
            include("/html/projects/edit.jsp", request, response);
        } else {
            prepareStandardView(request);
            super.doView(request, response);
        }
    }

    private void prepareStandardView(RenderRequest request) throws IOException {

        String searchtext = request.getParameter(KEY_SEARCH_TEXT);

        String searchfilter = request.getParameter(KEY_SEARCH_FILTER_TEXT);

        Map<String, Set<String>> filterMap = new HashMap<>();
        for (Project._Fields filteredField : projectFilteredFields) {
            String parameter = request.getParameter(filteredField.toString());
            if (!isNullOrEmpty(parameter)) {
                filterMap.put(filteredField.getFieldName(), CommonUtils.splitToSet(parameter));
            }
            request.setAttribute(filteredField.getFieldName(), nullToEmpty(parameter));
        }

        List<Project> projectList;

        try {
            final User user = UserCacheHolder.getUserFromRequest(request);
            ProjectService.Iface projectClient = thriftClients.makeProjectClient();

            String groupFilterValue = request.getParameter(Project._Fields.BUSINESS_UNIT.toString());
            if (null == groupFilterValue) {
                addStickyProjectGroupToFilters(request, user, filterMap);
            } else {
                ProjectPortletUtils.saveStickyProjectGroup(request, user, groupFilterValue);
            }

            if (isNullOrEmpty(searchtext) && filterMap.isEmpty()) {
                projectList = projectClient.getAccessibleProjectsSummary(user);
            } else {
                projectList = projectClient.refineSearch(searchtext, filterMap, user);
            }
            for(Project project:projectList){
                setClearingStateSummary(project);
            }

        } catch (TException e) {
            log.error("Could not search projects in backend ", e);
            projectList = Collections.emptyList();
        }

        request.setAttribute(PROJECT_LIST, projectList);
        request.setAttribute(KEY_SEARCH_TEXT, searchtext);
        request.setAttribute(KEY_SEARCH_FILTER_TEXT, searchfilter);
        List<Organization> organizations = UserUtils.getOrganizations(request);
        request.setAttribute(PortalConstants.ORGANIZATIONS, organizations);

    }

    private void addStickyProjectGroupToFilters(RenderRequest request, User user, Map<String, Set<String>> filterMap){
        String stickyGroupFilter = ProjectPortletUtils.loadStickyProjectGroup(request, user);
        if (!isNullOrEmpty(stickyGroupFilter)){
            String groupFieldName = Project._Fields.BUSINESS_UNIT.getFieldName();
            filterMap.put(groupFieldName, Sets.newHashSet(stickyGroupFilter));
            request.setAttribute(groupFieldName, stickyGroupFilter);
        }
    }

    private void prepareDetailView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        User user = UserCacheHolder.getUserFromRequest(request);
        String id = request.getParameter(PROJECT_ID);
        request.setAttribute(DOCUMENT_TYPE, SW360Constants.TYPE_PROJECT);
        if (id != null) {
            try {
                ProjectService.Iface client = thriftClients.makeProjectClient();
                Project project = client.getProjectById(id, user);
                setClearingStateSummary(project);
                request.setAttribute(PROJECT, project);
                request.setAttribute(DOCUMENT_ID, id);
                setAttachmentsInRequest(request, project.getAttachments());
                putLinkedProjectsInRequest(request, project.getLinkedProjects());
                putLinkedReleasesInRequest(request, project.getReleaseIdToUsage());
                Set<Project> usingProjects = client.searchLinkingProjects(id, user);
                request.setAttribute(USING_PROJECTS, usingProjects);
                Map<Release, String> releaseStringMap = getReleaseStringMap(id, user);
                request.setAttribute(PortalConstants.RELEASES_AND_PROJECTS, releaseStringMap);

                addProjectBreadcrumb(request, response, project);
            } catch (TException e) {
                log.error("Error fetching project from backend!", e);
            }
        }
    }

    private void setClearingStateSummary(Project project) {
        ComponentService.Iface componentClient = thriftClients.makeComponentClient();

        setClearingStateSummary(componentClient, project);

    }

    private Collection<Project> setClearingStateSummary(Collection<Project> projects) {
        ComponentService.Iface componentClient = thriftClients.makeComponentClient();

        for (Project project : projects) {
            setClearingStateSummary(componentClient, project);
        }
        return projects;
    }

    private void setClearingStateSummary(ComponentService.Iface componentClient, Project project) {
        try {
            final Set<String> releaseIds;
            if(project.isSetReleaseIds()){
                releaseIds = project.getReleaseIds();
            } else {
                releaseIds = CommonUtils.nullToEmptyMap(project.getReleaseIdToUsage()).keySet();
            }
            final ReleaseClearingStateSummary releaseClearingStateSummary =
                    componentClient.getReleaseClearingStateSummary(releaseIds, project.getClearingTeam());
            project.setReleaseClearingStateSummary(releaseClearingStateSummary);
        } catch (TException e) {
            log.error("Could not summary of release status for project id " + project.getId() + "!", e);
        }
    }


    private void prepareProjectEdit(RenderRequest request) {
        User user = UserCacheHolder.getUserFromRequest(request);
        String id = request.getParameter(PROJECT_ID);
        request.setAttribute(DOCUMENT_TYPE, SW360Constants.TYPE_PROJECT);
        Project project;
        Set<Project> usingProjects;

        if (id != null) {

            try {
                ProjectService.Iface client = thriftClients.makeProjectClient();
                project = client.getProjectByIdForEdit(id, user);
                usingProjects = client.searchLinkingProjects(id, user);
            } catch (TException e) {
                log.error("Something went wrong with fetching the project", e);
                return;
            }

            request.setAttribute(PROJECT, project);
            request.setAttribute(DOCUMENT_ID, id);

            setAttachmentsInRequest(request, project.getAttachments());
            try {
                putLinkedProjectsInRequest(request, project.getLinkedProjects());
                putLinkedReleasesInRequest(request, project.getReleaseIdToUsage());
            } catch (TException e) {
                log.error("Could not fetch linked projects or linked releases in projects view.", e);
                return;
            }

            request.setAttribute(USING_PROJECTS, usingProjects);
            Map<RequestedAction, Boolean> permissions = project.getPermissions();
            DocumentState documentState = project.getDocumentState();

            addEditDocumentMessage(request, permissions, documentState);
        } else {
            project = new Project();
            project.setBusinessUnit(user.getDepartment());
            request.setAttribute(PROJECT, project);
            setAttachmentsInRequest(request, project.getAttachments());
            try {
                putLinkedProjectsInRequest(request, Collections.emptyMap());
                putLinkedReleasesInRequest(request, Collections.emptyMap());
            } catch (TException e) {
                log.error("Could not put empty linked projects or linked releases in projects view.", e);
            }
            request.setAttribute(USING_PROJECTS, Collections.emptySet());

            SessionMessages.add(request, "request_processed", "New Project");
        }

    }

    private void prepareProjectDuplicate(RenderRequest request) {
        User user = UserCacheHolder.getUserFromRequest(request);
        String id = request.getParameter(PROJECT_ID);
        request.setAttribute(DOCUMENT_TYPE, SW360Constants.TYPE_PROJECT);

        try {
            if (id != null) {
                ProjectService.Iface client = thriftClients.makeProjectClient();
                String emailFromRequest = LifeRayUserSession.getEmailFromRequest(request);
                String department = user.getDepartment();

                Project newProject = PortletUtils.cloneProject(emailFromRequest, department, client.getProjectById(id, user));
                setAttachmentsInRequest(request, newProject.getAttachments());
                request.setAttribute(PROJECT, newProject);
                putLinkedProjectsInRequest(request, newProject.getLinkedProjects());
                putLinkedReleasesInRequest(request, newProject.getReleaseIdToUsage());
                request.setAttribute(USING_PROJECTS, Collections.emptySet());
            } else {
                Project project = new Project();
                project.setBusinessUnit(user.getDepartment());
                setAttachmentsInRequest(request, project.getAttachments());

                request.setAttribute(PROJECT, project);
                putLinkedProjectsInRequest(request, Collections.emptyMap());
                putLinkedReleasesInRequest(request, Collections.emptyMap());

                request.setAttribute(USING_PROJECTS, Collections.emptySet());
            }
        } catch (TException e) {
            log.error("Error fetching project from backend!", e);
        }

    }

    private Set<Project> getAccessibleProjects(User user) {
        Set<Project> projects;
        try {
            ProjectService.Iface client = thriftClients.makeProjectClient();
            projects = client.getAccessibleProjects(user);
        } catch (TException e) {
            log.error("Could not fetch project summary from backend!", e);
            projects = Collections.emptySet();
        }
        return projects;
    }

    //! Actions
    @UsedAsLiferayAction
    public void delete(ActionRequest request, ActionResponse response) throws PortletException, IOException {
        RequestStatus requestStatus = removeProject(request);
        setSessionMessage(request, requestStatus, "Project", "remove");
    }

    @UsedAsLiferayAction
    public void update(ActionRequest request, ActionResponse response) throws PortletException, IOException {
        String id = request.getParameter(PROJECT_ID);
        User user = UserCacheHolder.getUserFromRequest(request);
        RequestStatus requestStatus;
        try {
            ProjectService.Iface client = thriftClients.makeProjectClient();
            if (id != null) {
                Project project = client.getProjectByIdForEdit(id, user);
                ProjectPortletUtils.updateProjectFromRequest(request, project);
                requestStatus = client.updateProject(project, user);
                setSessionMessage(request, requestStatus, "Project", "update", printName(project));
                cleanUploadHistory(user.getEmail(),id);
                response.setRenderParameter(PAGENAME, PAGENAME_DETAIL);
                response.setRenderParameter(PROJECT_ID, request.getParameter(PROJECT_ID));
            } else {
                // Add project
                Project project = new Project();
                ProjectPortletUtils.updateProjectFromRequest(request, project);
                id = client.addProject(project, user);

                if (id != null) {
                    String successMsg = "Project " + printName(project) + " added successfully";
                    SessionMessages.add(request, "request_processed", successMsg);
                    response.setRenderParameter(PROJECT_ID, id);
                } else {
                    String successMsg = "Project was not added successfully";
                    SessionMessages.add(request, "request_processed", successMsg);
                }
                response.setRenderParameter(PAGENAME, PAGENAME_EDIT);
            }

        } catch (TException e) {
            log.error("Error updating project in backend!", e);
        }
    }
    @UsedAsLiferayAction
    public void applyFilters(ActionRequest request, ActionResponse response) throws PortletException, IOException {
        response.setRenderParameter(KEY_SEARCH_TEXT, nullToEmpty(request.getParameter(KEY_SEARCH_TEXT)));
        response.setRenderParameter(KEY_SEARCH_FILTER_TEXT, nullToEmpty(request.getParameter(KEY_SEARCH_FILTER_TEXT)));
        for (Project._Fields projectFilteredField : projectFilteredFields) {
            response.setRenderParameter(projectFilteredField.toString(), nullToEmpty(request.getParameter(projectFilteredField.toString())));
        }
    }

}
