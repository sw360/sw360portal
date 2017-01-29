/*
 * Copyright Siemens AG, 2013-2016. Part of the SW360 Portal Project.
 * With contributions by Bosch Software Innovations GmbH, 2016.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.portal.portlets.components;

import com.google.common.collect.ImmutableList;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.portlet.PortletResponseUtil;
import com.liferay.portal.kernel.servlet.SessionMessages;
import org.eclipse.sw360.datahandler.common.CommonUtils;
import org.eclipse.sw360.datahandler.common.SW360Constants;
import org.eclipse.sw360.datahandler.common.SW360Utils;
import org.eclipse.sw360.datahandler.common.ThriftEnumUtils;
import org.eclipse.sw360.datahandler.permissions.PermissionUtils;
import org.eclipse.sw360.datahandler.thrift.*;
import org.eclipse.sw360.datahandler.thrift.attachments.Attachment;
import org.eclipse.sw360.datahandler.thrift.components.*;
import org.eclipse.sw360.datahandler.thrift.cvesearch.CveSearchService;
import org.eclipse.sw360.datahandler.thrift.cvesearch.VulnerabilityUpdateStatus;
import org.eclipse.sw360.datahandler.thrift.projects.Project;
import org.eclipse.sw360.datahandler.thrift.projects.ProjectService;
import org.eclipse.sw360.datahandler.thrift.users.RequestedAction;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.datahandler.thrift.vendors.Vendor;
import org.eclipse.sw360.datahandler.thrift.vendors.VendorService;
import org.eclipse.sw360.datahandler.thrift.vulnerabilities.ReleaseVulnerabilityRelation;
import org.eclipse.sw360.datahandler.thrift.vulnerabilities.Vulnerability;
import org.eclipse.sw360.datahandler.thrift.vulnerabilities.VulnerabilityDTO;
import org.eclipse.sw360.datahandler.thrift.vulnerabilities.VulnerabilityService;
import org.eclipse.sw360.exporter.ComponentExporter;
import org.eclipse.sw360.portal.common.*;
import org.eclipse.sw360.portal.portlets.FossologyAwarePortlet;
import org.eclipse.sw360.portal.users.LifeRayUserSession;
import org.eclipse.sw360.portal.users.UserCacheHolder;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import javax.portlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;
import static org.eclipse.sw360.datahandler.common.CommonUtils.nullToEmptyList;
import static org.eclipse.sw360.datahandler.common.SW360Utils.printName;
import static org.eclipse.sw360.portal.common.PortalConstants.*;
import static org.eclipse.sw360.portal.common.PortletUtils.addToMatchedByHistogram;
import static org.eclipse.sw360.portal.common.PortletUtils.getVerificationState;

/**
 * Component portlet implementation
 *
 * @author cedric.bodet@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 * @author stefan.jaeger@evosoft.com
 * @author alex.borodin@evosoft.com
 */
public class ComponentPortlet extends FossologyAwarePortlet {

    private static final Logger log = Logger.getLogger(ComponentPortlet.class);

    private boolean typeIsComponent(String documentType) {
        return SW360Constants.TYPE_COMPONENT.equals(documentType);
    }

    @Override
    protected Set<Attachment> getAttachments(String documentId, String documentType, User user) {

        try {
            ComponentService.Iface client = thriftClients.makeComponentClient();
            if (typeIsComponent(documentType)) {
                Component component = client.getComponentById(documentId, user);
                return CommonUtils.nullToEmptySet(component.getAttachments());
            } else {
                Release release = client.getReleaseById(documentId, user);
                return CommonUtils.nullToEmptySet(release.getAttachments());
            }
        } catch (TException e) {
            log.error("Could not get " + documentType + " attachments for " + documentId, e);
        }
        return Collections.emptySet();
    }

    private static final ImmutableList<Component._Fields> componentFilteredFields = ImmutableList.of(
            Component._Fields.NAME,
            Component._Fields.CATEGORIES,
            Component._Fields.LANGUAGES,
            Component._Fields.SOFTWARE_PLATFORMS,
            Component._Fields.OPERATING_SYSTEMS,
            Component._Fields.VENDOR_NAMES,
            Component._Fields.COMPONENT_TYPE,
            Component._Fields.MAIN_LICENSE_IDS);

    //! Serve resource and helpers
    @Override
    public void serveResource(ResourceRequest request, ResourceResponse response) throws IOException, PortletException {
        String action = request.getParameter(PortalConstants.ACTION);

        if (VIEW_VENDOR.equals(action)) {
            serveViewVendor(request, response);
        } else if (ADD_VENDOR.equals(action)) {
            serveAddVendor(request, response);
        } else if (DELETE_COMPONENT.equals(action)) {
            serveDeleteComponent(request, response);
        } else if (DELETE_RELEASE.equals(action)) {
            serveDeleteRelease(request, response);
        } else if (SUBSCRIBE.equals(action)) {
            serveSubscribe(request, response);
        } else if (SUBSCRIBE_RELEASE.equals(action)) {
            serveSubscribeRelease(request, response);
        } else if (UNSUBSCRIBE.equals(action)) {
            serveUnsubscribe(request, response);
        } else if (UNSUBSCRIBE_RELEASE.equals(action)) {
            serveUnsubscribeRelease(request, response);
        } else if (PortalConstants.VIEW_LINKED_RELEASES.equals(action)) {
            serveLinkedReleases(request, response);
        } else if (PortalConstants.UPDATE_VULNERABILITIES_RELEASE.equals(action)){
            updateVulnerabilitiesRelease(request,response);
        } else if (PortalConstants.UPDATE_VULNERABILITIES_COMPONENT.equals(action)){
            updateVulnerabilitiesComponent(request,response);
        } else if (PortalConstants.UPDATE_ALL_VULNERABILITIES.equals(action)) {
            updateAllVulnerabilities(request, response);
        } else if (PortalConstants.UPDATE_VULNERABILITY_VERIFICATION.equals(action)){
                updateVulnerabilityVerification(request,response);
        } else if (PortalConstants.EXPORT_TO_EXCEL.equals(action)) {
            exportExcel(request, response);
        } else if (isGenericAction(action)) {
            dealWithGenericAction(request, response, action);
        }
    }

    @Override
    protected void dealWithFossologyAction(ResourceRequest request, ResourceResponse response, String action) throws IOException, PortletException {
        if (PortalConstants.FOSSOLOGY_SEND.equals(action)) {
            serveSendToFossology(request, response);
        } else if (PortalConstants.FOSSOLOGY_GET_STATUS.equals(action)) {
            serveFossologyStatus(request, response);
        }
    }

    private void serveViewVendor(ResourceRequest request, ResourceResponse response) throws IOException, PortletException {
        String what = request.getParameter(PortalConstants.WHAT);
        String where = request.getParameter(PortalConstants.WHERE);

        if ("vendor".equals(what)) {
            renderVendor(request, response, where);
        } else if ("vendorSearch".equals(what)) {
            renderVendorSearch(request, response, where);
        }
    }

    private void renderVendorSearch(ResourceRequest request, ResourceResponse response, String searchText) throws IOException, PortletException {
        List<Vendor> vendors = null;
        try {
            VendorService.Iface client = thriftClients.makeVendorClient();
            if (isNullOrEmpty(searchText)) {
                vendors = client.getAllVendors();
            } else {
                vendors = client.searchVendors(searchText);
            }
        } catch (TException e) {
            log.error("Error searching vendors", e);
        }

        request.setAttribute("vendorsSearch", nullToEmptyList(vendors));
        include("/html/components/ajax/vendorSearch.jsp", request, response, PortletRequest.RESOURCE_PHASE);
    }

    private void renderVendor(ResourceRequest request, ResourceResponse response, String vendorId) throws IOException, PortletException {
        Vendor vendor = null;

        if (vendorId != null && !vendorId.isEmpty()) {
            try {
                VendorService.Iface client = thriftClients.makeVendorClient();
                vendor = client.getByID(vendorId);
            } catch (TException e) {
                log.error("Error getting vendor from backend", e);
            }
        }

        if (vendor == null) {
            vendor = new Vendor();
            vendor.setFullname("This is a vendor");
            vendor.setShortname("It really is");
        }
        request.setAttribute("vendor", vendor);
        include("/html/components/ajax/vendorAjax.jsp", request, response, PortletRequest.RESOURCE_PHASE);
    }

    private void serveAddVendor(ResourceRequest request, ResourceResponse response) throws IOException, PortletException {
        final Vendor vendor = new Vendor();
        ComponentPortletUtils.updateVendorFromRequest(request, vendor);

        try {
            VendorService.Iface client = thriftClients.makeVendorClient();
            String vendorId = client.addVendor(vendor);
            JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
            jsonObject.put("id", vendorId);
            try {
                writeJSON(request, response, jsonObject);
            } catch (IOException e) {
                log.error("Problem rendering VendorId", e);
            }
        } catch (TException e) {
            log.error("Error adding vendor", e);
        }
    }

    private void serveDeleteComponent(ResourceRequest request, ResourceResponse response) throws IOException {
        RequestStatus requestStatus = ComponentPortletUtils.deleteComponent(request, log);
        serveRequestStatus(request, response, requestStatus, "Problem removing component", log);

    }

    private void serveDeleteRelease(PortletRequest request, ResourceResponse response) throws IOException {
        final RequestStatus requestStatus = ComponentPortletUtils.deleteRelease(request, log);
        serveRequestStatus(request, response, requestStatus, "Problem removing release", log);
    }

    private void exportExcel(ResourceRequest request, ResourceResponse response) {
        try {
            boolean extendedByReleases = Boolean.valueOf(request.getParameter(PortalConstants.EXTENDED_EXCEL_EXPORT));
            List<Component> components = getFilteredComponentList(request);
            ComponentExporter exporter = new ComponentExporter(thriftClients.makeComponentClient(), extendedByReleases);
            PortletResponseUtil.sendFile(request, response, "Components.xlsx", exporter.makeExcelExport(components),
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        } catch (IOException | SW360Exception e) {
            log.error("An error occurred while generating the Excel export", e);
        }
    }

    private void serveSubscribe(ResourceRequest request, ResourceResponse response) {
        final RequestStatus requestStatus = ComponentPortletUtils.subscribeComponent(request, log);
        serveRequestStatus(request, response, requestStatus, "Problem subscribing component", log);
    }

    private void serveSubscribeRelease(ResourceRequest request, ResourceResponse response) {
        final RequestStatus requestStatus = ComponentPortletUtils.subscribeRelease(request, log);
        serveRequestStatus(request, response, requestStatus, "Problem subscribing release", log);
    }

    private void serveUnsubscribe(ResourceRequest request, ResourceResponse response) {
        final RequestStatus requestStatus = ComponentPortletUtils.unsubscribeComponent(request, log);
        serveRequestStatus(request, response, requestStatus, "Problem unsubscribing component", log);
    }

    private void serveUnsubscribeRelease(ResourceRequest request, ResourceResponse response) {
        final RequestStatus requestStatus = ComponentPortletUtils.unsubscribeRelease(request, log);
        serveRequestStatus(request, response, requestStatus, "Problem unsubscribing release", log);
    }


    private void serveLinkedReleases(ResourceRequest request, ResourceResponse response) throws IOException, PortletException {
        String what = request.getParameter(PortalConstants.WHAT);

        String projectId = request.getParameter(RELEASE_ID);

        if (PortalConstants.LIST_NEW_LINKED_RELEASES.equals(what)) {
            String[] where = request.getParameterValues(PortalConstants.WHERE_ARRAY);
            serveNewTableRowLinkedRelease(request, response, where);
        } else if (PortalConstants.RELEASE_SEARCH.equals(what)) {
            String where = request.getParameter(PortalConstants.WHERE);
            serveReleaseSearchResults(request, response, where);
        } else if (PortalConstants.RELEASE_SEARCH_BY_VENDOR.equals(what)) {
            String where = request.getParameter(PortalConstants.WHERE);
            serveReleaseSearchResultsByVendor(request, response, where);
        }
    }

    private void serveNewTableRowLinkedRelease(ResourceRequest request, ResourceResponse response, String[] linkedIds) throws IOException, PortletException {
        final User user = UserCacheHolder.getUserFromRequest(request);

        List<ReleaseLink> linkedReleases = new ArrayList<>();
        try {
            ComponentService.Iface client = thriftClients.makeComponentClient();
            for (Release release : client.getReleasesById(new HashSet<>(Arrays.asList(linkedIds)), user)) {
                final Vendor vendor = release.getVendor();

                final String fullname = vendor != null ? vendor.getFullname() : "";
                ReleaseLink linkedRelease = new ReleaseLink(release.getId(), fullname, release.getName(), release.getVersion());
                linkedRelease.setReleaseRelationship(ReleaseRelationship.CONTAINED);
                linkedReleases.add(linkedRelease);
            }
        } catch (TException e) {
            log.error("Error getting releases!", e);
            throw new PortletException("cannot get releases " + Arrays.toString(linkedIds), e);
        }

        request.setAttribute(RELEASE_LIST, linkedReleases);

        include("/html/utils/ajax/linkedReleasesRelationAjax.jsp", request, response, PortletRequest.RESOURCE_PHASE);
    }

    private void serveReleaseSearchResults(ResourceRequest request, ResourceResponse response, String searchText) throws IOException, PortletException {
        serveReleaseSearch(request, response, searchText, false);
    }

    private void serveReleaseSearchResultsByVendor(ResourceRequest request, ResourceResponse response, String searchText) throws IOException, PortletException {
        serveReleaseSearch(request, response, searchText, true);
    }

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

    //! VIEW and helpers
    @Override
    public void doView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        String pageName = request.getParameter(PAGENAME);
        if (PAGENAME_DETAIL.equals(pageName)) {
            prepareDetailView(request, response);
            include("/html/components/detail.jsp", request, response);
        } else if (PAGENAME_RELEASE_DETAIL.equals(pageName)) {
            prepareReleaseDetailView(request, response);
            include("/html/components/detailRelease.jsp", request, response);
        } else if (PAGENAME_EDIT.equals(pageName)) {
            prepareComponentEdit(request);
            include("/html/components/edit.jsp", request, response);
        } else if (PAGENAME_EDIT_RELEASE.equals(pageName)) {
            prepareReleaseEdit(request, response);
            include("/html/components/editRelease.jsp", request, response);
        } else if (PAGENAME_DUPLICATE_RELEASE.equals(pageName)) {
            prepareReleaseDuplicate(request, response);
            include("/html/components/editRelease.jsp", request, response);
        } else {
            prepareStandardView(request);
            super.doView(request, response);
        }
    }

    private void prepareComponentEdit(RenderRequest request) {
        String id = request.getParameter(COMPONENT_ID);
        final User user = UserCacheHolder.getUserFromRequest(request);
        request.setAttribute(DOCUMENT_TYPE, SW360Constants.TYPE_COMPONENT);
        if (id != null) {
            try {
                ComponentService.Iface client = thriftClients.makeComponentClient();
                Component component = client.getComponentByIdForEdit(id, user);

                request.setAttribute(COMPONENT, component);
                request.setAttribute(DOCUMENT_ID, id);

                setAttachmentsInRequest(request, component.getAttachments());
                Map<RequestedAction, Boolean> permissions = component.getPermissions();
                DocumentState documentState = component.getDocumentState();

                addEditDocumentMessage(request, permissions, documentState);
                Set<String> releaseIds = SW360Utils.getReleaseIds(component.getReleases());
                setUsingDocs(request, user, client, releaseIds);
            } catch (TException e) {
                log.error("Error fetching component from backend!", e);
                setSW360SessionError(request, ErrorMessages.ERROR_GETTING_COMPONENT);
            }
        } else {
            if(request.getAttribute(COMPONENT) == null) {
                Component component = new Component();
                request.setAttribute(COMPONENT, component);
                setUsingDocs(request, user, null, component.getReleaseIds());
                setAttachmentsInRequest(request, component.getAttachments());
                SessionMessages.add(request, "request_processed", "New Component");
            }
        }
    }

    private void prepareReleaseEdit(RenderRequest request, RenderResponse response) throws PortletException {
        String id = request.getParameter(COMPONENT_ID);
        String releaseId = request.getParameter(RELEASE_ID);
        request.setAttribute(DOCUMENT_TYPE, SW360Constants.TYPE_RELEASE);
        final User user = UserCacheHolder.getUserFromRequest(request);

        if (isNullOrEmpty(id) && isNullOrEmpty(releaseId)) {
            throw new PortletException("Component and Release ID not set!");
        }

        try {
            ComponentService.Iface client = thriftClients.makeComponentClient();

            Release release;

            if (!isNullOrEmpty(releaseId)) {
                release = client.getReleaseByIdForEdit(releaseId, user);
                request.setAttribute(RELEASE, release);
                request.setAttribute(DOCUMENT_ID, releaseId);
                setAttachmentsInRequest(request, release.getAttachments());

                putDirectlyLinkedReleaseRelationsInRequest(request, release.getReleaseIdToRelationship());
                Map<RequestedAction, Boolean> permissions = release.getPermissions();
                DocumentState documentState = release.getDocumentState();
                setUsingDocs(request, releaseId, user, client);
                addEditDocumentMessage(request, permissions, documentState);

                if (isNullOrEmpty(id)) {
                    id = release.getComponentId();
                }

            } else {
                release = (Release) request.getAttribute(RELEASE);
                if(release == null) {
                    release = new Release();
                    release.setComponentId(id);
                    release.setClearingState(ClearingState.NEW_CLEARING);
                    request.setAttribute(RELEASE, release);
                    putDirectlyLinkedReleaseRelationsInRequest(request, release.getReleaseIdToRelationship());
                    setAttachmentsInRequest(request, release.getAttachments());
                    setUsingDocs(request, null, user, client);
                    SessionMessages.add(request, "request_processed", "New Release");
                }
            }

            Component component = client.getComponentById(id, user);
            addComponentBreadcrumb(request, response, component);
            if (!isNullOrEmpty(release.getId())) { //Otherwise the link is meaningless
                addReleaseBreadcrumb(request, response, release);
            }
            request.setAttribute(COMPONENT, component);

        } catch (TException e) {
            log.error("Error fetching release from backend!", e);
            setSW360SessionError(request, ErrorMessages.ERROR_GETTING_RELEASE);
        }
    }

    private void prepareReleaseDuplicate(RenderRequest request, RenderResponse response) throws PortletException {
        String id = request.getParameter(COMPONENT_ID);
        String releaseId = request.getParameter(RELEASE_ID);
        request.setAttribute(DOCUMENT_TYPE, SW360Constants.TYPE_RELEASE);
        final User user = UserCacheHolder.getUserFromRequest(request);

        if (isNullOrEmpty(releaseId)) {
            throw new PortletException("Release ID not set!");
        }

        try {
            ComponentService.Iface client = thriftClients.makeComponentClient();
            String emailFromRequest = LifeRayUserSession.getEmailFromRequest(request);

            Release release = PortletUtils.cloneRelease(emailFromRequest, client.getReleaseById(releaseId, user));

            if (isNullOrEmpty(id)) {
                id = release.getComponentId();
            }
            Component component = client.getComponentById(id, user);
            addComponentBreadcrumb(request, response, component);
            request.setAttribute(COMPONENT, component);
            request.setAttribute(RELEASE_LIST, Collections.emptyList());
            setUsingDocs(request, null, user, client);
            request.setAttribute(RELEASE, release);
            request.setAttribute(PortalConstants.ATTACHMENTS, Collections.emptySet());

        } catch (TException e) {
            log.error("Error fetching release from backend!", e);
        }
    }

    private void prepareDetailView(RenderRequest request, RenderResponse response) {
        String id = request.getParameter(COMPONENT_ID);
        final User user = UserCacheHolder.getUserFromRequest(request);

        if (!isNullOrEmpty(id)) {
            try {
                ComponentService.Iface client = thriftClients.makeComponentClient();
                Component component = client.getComponentById(id, user);

                request.setAttribute(COMPONENT, component);
                request.setAttribute(DOCUMENT_ID, id);
                request.setAttribute(DOCUMENT_TYPE, SW360Constants.TYPE_COMPONENT);
                setAttachmentsInRequest(request, component.getAttachments());
                Set<String> releaseIds = SW360Utils.getReleaseIds(component.getReleases());

                setUsingDocs(request, user, client, releaseIds);

                // get vulnerabilities
                putVulnerabilitiesInRequestComponent(request, id, user);
                request.setAttribute(VULNERABILITY_VERIFICATION_EDITABLE, PermissionUtils.isAdmin(user));

                addComponentBreadcrumb(request, response, component);
            } catch (TException e) {
                log.error("Error fetching component from backend!", e);
                setSW360SessionError(request, ErrorMessages.ERROR_GETTING_COMPONENT);
            }
        }
    }

    private void setUsingDocs(RenderRequest request, User user, ComponentService.Iface client, Set<String> releaseIds) {
        Set<Project> usingProjects = null;
        Set<Component> usingComponentsForComponent = null;
        if (releaseIds != null && releaseIds.size() > 0) {
            try {
                ProjectService.Iface projectClient = thriftClients.makeProjectClient();
                usingProjects = projectClient.searchByReleaseIds(releaseIds, user);
                usingComponentsForComponent = client.getUsingComponentsForComponent(releaseIds);
            } catch (TException e) {
                log.error("Problem filling using docs", e);
            }

        }

        request.setAttribute(USING_PROJECTS, CommonUtils.nullToEmptySet(usingProjects));
        request.setAttribute(USING_COMPONENTS, CommonUtils.nullToEmptySet(usingComponentsForComponent));
    }

    private void prepareReleaseDetailView(RenderRequest request, RenderResponse response) throws PortletException {
        String id = request.getParameter(COMPONENT_ID);
        String releaseId = request.getParameter(RELEASE_ID);
        final User user = UserCacheHolder.getUserFromRequest(request);

        if (isNullOrEmpty(id) && isNullOrEmpty(releaseId)) {
            throw new PortletException("Component and Release ID not set!");
        }

        try {
            ComponentService.Iface client = thriftClients.makeComponentClient();
            Component component;
            Release release = null;

            if (!isNullOrEmpty(releaseId)) {
                release = client.getReleaseById(releaseId, user);

                request.setAttribute(RELEASE_ID, releaseId);
                request.setAttribute(RELEASE, release);
                request.setAttribute(DOCUMENT_ID, releaseId);
                request.setAttribute(DOCUMENT_TYPE, SW360Constants.TYPE_RELEASE);
                setAttachmentsInRequest(request, release.getAttachments());

                setUsingDocs(request, releaseId, user, client);
                putLinkedReleaseRelationsInRequest(request, release.getReleaseIdToRelationship());

                if (isNullOrEmpty(id)) {
                    id = release.getComponentId();
                }

                putVulnerabilitiesInRequestRelease(request, releaseId, user);
                request.setAttribute(VULNERABILITY_VERIFICATION_EDITABLE, PermissionUtils.isAdmin(user));
            }

            component = client.getComponentById(id, user);
            request.setAttribute(COMPONENT, component);

            addComponentBreadcrumb(request, response, component);
            if (release != null) {
                addReleaseBreadcrumb(request, response, release);
            }

        } catch (TException e) {
            log.error("Error fetching release from backend!", e);
            setSW360SessionError(request, ErrorMessages.ERROR_GETTING_RELEASE);
        }

    }

    private String formatedMessageForVul(List<VerificationStateInfo> infoHistory){
        return CommonVulnerabilityPortletUtils.formatedMessageForVul(infoHistory,
                e -> e.getVerificationState().name(),
                e -> e.getCheckedOn(),
                e -> e.getCheckedBy(),
                e -> e.getComment());
    }

    private void putVulnerabilitiesInRequestRelease(RenderRequest request, String releaseId, User user) throws TException {
        VulnerabilityService.Iface vulClient = thriftClients.makeVulnerabilityClient();
        List<VulnerabilityDTO> vuls;
        if (PermissionUtils.isAdmin(user)) {
            vuls = vulClient.getVulnerabilitiesByReleaseId(releaseId, user);
        } else {
            vuls = vulClient.getVulnerabilitiesByReleaseIdWithoutIncorrect(releaseId, user);
        }
        request.setAttribute(VULNERABILITY_LIST,vuls);

        putVulnerabilityMetadatasInRequest(request, vuls);
    }

    private void putVulnerabilitiesInRequestComponent(RenderRequest request, String componentId, User user) throws TException{
        VulnerabilityService.Iface vulClient = thriftClients.makeVulnerabilityClient();
        List<VulnerabilityDTO> vuls;
        if (PermissionUtils.isAdmin(user)) {
            vuls = vulClient.getVulnerabilitiesByComponentId(componentId, user);
        } else {
            vuls = vulClient.getVulnerabilitiesByComponentIdWithoutIncorrect(componentId, user);
        }
        request.setAttribute(VULNERABILITY_LIST, vuls);

        putVulnerabilityMetadatasInRequest(request, vuls);

    }

    private void addToVulnerabilityVerifications(Map<String, Map<String, VerificationState>> vulnerabilityVerifications,
                                                 Map<String, Map<String, String>> vulnerabilityTooltips,
                                                 VulnerabilityDTO vulnerability){
        String vulnerabilityId = vulnerability.getExternalId();
        String releaseId = vulnerability.getIntReleaseId();
        if(! vulnerabilityVerifications.containsKey(vulnerabilityId)){
            vulnerabilityVerifications.put(vulnerabilityId, new HashMap<>());
        }
        if(! vulnerabilityTooltips.containsKey(vulnerabilityId)){
            vulnerabilityTooltips.put(vulnerabilityId, new HashMap<>());
        }
        ReleaseVulnerabilityRelation relation = vulnerability.getReleaseVulnerabilityRelation();

        if (! relation.isSetVerificationStateInfo()) {
            vulnerabilityVerifications.get(vulnerabilityId).put(releaseId, VerificationState.NOT_CHECKED);
            vulnerabilityTooltips.get(vulnerabilityId).put(releaseId, "Not checked yet.");
        } else {
            List<VerificationStateInfo> infoHistory = relation.getVerificationStateInfo();
            VerificationStateInfo info = infoHistory.get(infoHistory.size() - 1);
            vulnerabilityVerifications.get(vulnerabilityId).put(releaseId, info.getVerificationState());
            vulnerabilityTooltips.get(vulnerabilityId).put(releaseId, formatedMessageForVul(infoHistory));
        }
    }

    private void putVulnerabilityMetadatasInRequest(RenderRequest request, List<VulnerabilityDTO> vuls) {
        Map<String, Map<String, String>> vulnerabilityTooltips = new HashMap<>();
        Map<String, Map<String, VerificationState>> vulnerabilityVerifications = new HashMap<>();
        Map<String, Integer> matchedByHistogram = new HashMap<>();
        for (VulnerabilityDTO vulnerability : vuls) {
            addToVulnerabilityVerifications(vulnerabilityVerifications, vulnerabilityTooltips, vulnerability);
            addToMatchedByHistogram(matchedByHistogram, vulnerability);
        }
        if (PermissionUtils.isAdmin(UserCacheHolder.getUserFromRequest(request))) {
            long numberOfIncorrectVuls = vuls.stream()
                    .filter(v -> VerificationState.INCORRECT.equals(getVerificationState(v)))
                    .count();
            request.setAttribute(NUMBER_OF_INCORRECT_VULNERABILITIES, numberOfIncorrectVuls);
            request.setAttribute(NUMBER_OF_CHECKED_OR_UNCHECKED_VULNERABILITIES, vuls.size()- numberOfIncorrectVuls);
        } else {
            request.setAttribute(NUMBER_OF_CHECKED_OR_UNCHECKED_VULNERABILITIES, (long) vuls.size());
        }
        request.setAttribute(PortalConstants.VULNERABILITY_MATCHED_BY_HISTOGRAM, matchedByHistogram);
        request.setAttribute(PortalConstants.VULNERABILITY_VERIFICATIONS,vulnerabilityVerifications);
        request.setAttribute(PortalConstants.VULNERABILITY_VERIFICATION_TOOLTIPS,vulnerabilityTooltips);
    }


    private void setUsingDocs(RenderRequest request, String releaseId, User user, ComponentService.Iface client) throws TException {
        if (releaseId != null) {
            ProjectService.Iface projectClient = thriftClients.makeProjectClient();
            Set<Project> usingProjects = projectClient.searchByReleaseId(releaseId, user);
            request.setAttribute(USING_PROJECTS, CommonUtils.nullToEmptySet(usingProjects));

            final Set<Component> usingComponentsForRelease = client.getUsingComponentsForRelease(releaseId);
            request.setAttribute(USING_COMPONENTS, CommonUtils.nullToEmptySet(usingComponentsForRelease));
        } else {
            request.setAttribute(USING_PROJECTS, Collections.emptySet());
            request.setAttribute(USING_COMPONENTS, Collections.emptySet());
        }
    }

    private void addComponentBreadcrumb(RenderRequest request, RenderResponse response, Component component) {
        PortletURL componentUrl = response.createRenderURL();
        componentUrl.setParameter(PAGENAME, PAGENAME_DETAIL);
        componentUrl.setParameter(COMPONENT_ID, component.getId());

        addBreadcrumbEntry(request, printName(component), componentUrl);
    }

    private void addReleaseBreadcrumb(RenderRequest request, RenderResponse response, Release release) {
        PortletURL releaseURL = response.createRenderURL();
        releaseURL.setParameter(PAGENAME, PAGENAME_RELEASE_DETAIL);
        releaseURL.setParameter(RELEASE_ID, release.getId());

        addBreadcrumbEntry(request, printName(release), releaseURL);
    }

    private void prepareStandardView(RenderRequest request) throws IOException {
        List<Component> componentList = getFilteredComponentList(request);

        Set<String> vendorNames;

        try {
            vendorNames = thriftClients.makeVendorClient().getAllVendorNames();
        } catch (TException e) {
            log.error("Problem retrieving all the Vendor names", e);
            vendorNames = Collections.emptySet();
        }

        List<String> componentTypeNames = Arrays.asList(ComponentType.values())
                .stream()
                .map(ThriftEnumUtils::enumToString)
                .collect(Collectors.toList());

        request.setAttribute(VENDOR_LIST, new ThriftJsonSerializer().toJson(vendorNames));
        request.setAttribute(COMPONENT_LIST, componentList);
        request.setAttribute(COMPONENT_TYPE_LIST, new ThriftJsonSerializer().toJson(componentTypeNames));

    }

    private List<Component> getFilteredComponentList(PortletRequest request) throws IOException {
        List<Component> componentList;
        Map<String, Set<String>> filterMap = new HashMap<>();

        for (Component._Fields filteredField : componentFilteredFields) {
            String parameter = request.getParameter(filteredField.toString());
            if (!isNullOrEmpty(parameter) &&
                    !(filteredField.equals(Component._Fields.COMPONENT_TYPE) && parameter.equals(PortalConstants.NO_FILTER))) {
                Set<String> values = CommonUtils.splitToSet(parameter);
                if (filteredField.equals(Component._Fields.NAME)) {
                    values = values.stream().map(v -> v + "*").collect(Collectors.toSet());
                }
                filterMap.put(filteredField.getFieldName(), values);
            }
            request.setAttribute(filteredField.getFieldName(), nullToEmpty(parameter));
        }

        try {
            final User user = UserCacheHolder.getUserFromRequest(request);
            ComponentService.Iface componentClient = thriftClients.makeComponentClient();

            if (filterMap.isEmpty()) {
                componentList = componentClient.getComponentSummary(user);
            } else {
                componentList = componentClient.refineSearch(null, filterMap);
            }
        } catch (TException e) {
            log.error("Could not search components in backend ", e);
            componentList = Collections.emptyList();
        }
        return componentList;
    }

    //! Actions
    @UsedAsLiferayAction
    public void updateComponent(ActionRequest request, ActionResponse response) throws PortletException, IOException {
        String id = request.getParameter(COMPONENT_ID);
        final User user = UserCacheHolder.getUserFromRequest(request);

        try {
            ComponentService.Iface client = thriftClients.makeComponentClient();

            if (id != null) {
                Component component = client.getComponentByIdForEdit(id, user);
                ComponentPortletUtils.updateComponentFromRequest(request, component);
                RequestStatus requestStatus = client.updateComponent(component, user);
                setSessionMessage(request, requestStatus, "Component", "update", component.getName());
                cleanUploadHistory(user.getEmail(),id);
                response.setRenderParameter(PAGENAME, PAGENAME_DETAIL);
                response.setRenderParameter(COMPONENT_ID, request.getParameter(COMPONENT_ID));
            } else {
                Component component = new Component();
                ComponentPortletUtils.updateComponentFromRequest(request, component);
                AddDocumentRequestSummary summary = client.addComponent(component, user);

                AddDocumentRequestStatus status = summary.getRequestStatus();
                switch(status){
                    case SUCCESS:
                        String successMsg = "Component " + component.getName() + " added successfully";
                        SessionMessages.add(request, "request_processed", successMsg);
                        response.setRenderParameter(COMPONENT_ID, summary.getId());
                        response.setRenderParameter(PAGENAME, PAGENAME_EDIT);
                        break;
                    case DUPLICATE:
                        setSW360SessionError(request, ErrorMessages.COMPONENT_DUPLICATE);
                        response.setRenderParameter(PAGENAME, PAGENAME_EDIT);
                        prepareRequestForEditAfterDuplicateError(request, component);
                        break;
                    default:
                        setSW360SessionError(request, ErrorMessages.COMPONENT_NOT_ADDED);
                        response.setRenderParameter(PAGENAME, PAGENAME_VIEW);
                }
            }

        } catch (TException e) {
            log.error("Error fetching component from backend!", e);
        }
    }

    private void prepareRequestForEditAfterDuplicateError(ActionRequest request, Component component) throws TException {
        request.setAttribute(COMPONENT, component);
        setAttachmentsInRequest(request, component.getAttachments());
        request.setAttribute(USING_PROJECTS, Collections.emptySet());
        request.setAttribute(USING_COMPONENTS, Collections.emptySet());
    }

    @UsedAsLiferayAction
    public void updateRelease(ActionRequest request, ActionResponse response) throws PortletException, IOException {
        String id = request.getParameter(COMPONENT_ID);
        final User user = UserCacheHolder.getUserFromRequest(request);

        if (id != null) {
            try {
                ComponentService.Iface client = thriftClients.makeComponentClient();
                Component component = client.getComponentById(id, user);

                Release release;
                String releaseId = request.getParameter(RELEASE_ID);
                if (releaseId != null) {
                    release = client.getReleaseByIdForEdit(releaseId, user);
                    ComponentPortletUtils.updateReleaseFromRequest(request, release);

                    RequestStatus requestStatus = client.updateRelease(release, user);
                    setSessionMessage(request, requestStatus, "Release", "update", printName(release));
                    cleanUploadHistory(user.getEmail(),releaseId);

                    response.setRenderParameter(PAGENAME, PAGENAME_RELEASE_DETAIL);
                    response.setRenderParameter(COMPONENT_ID, request.getParameter(COMPONENT_ID));
                    response.setRenderParameter(RELEASE_ID, request.getParameter(RELEASE_ID));
                } else {
                    release = new Release();
                    release.setComponentId(component.getId());
                    release.setClearingState(ClearingState.NEW_CLEARING);
                    ComponentPortletUtils.updateReleaseFromRequest(request, release);
                    AddDocumentRequestSummary summary = client.addRelease(release, user);

                    AddDocumentRequestStatus status = summary.getRequestStatus();
                    switch(status){
                        case SUCCESS:
                            response.setRenderParameter(RELEASE_ID, summary.getId());
                            String successMsg = "Release " + printName(release) + " added successfully";
                            SessionMessages.add(request, "request_processed", successMsg);
                            response.setRenderParameter(PAGENAME, PAGENAME_EDIT_RELEASE);
                            break;
                        case DUPLICATE:
                            setSW360SessionError(request, ErrorMessages.RELEASE_DUPLICATE);
                            response.setRenderParameter(PAGENAME, PAGENAME_EDIT_RELEASE);
                            prepareRequestForReleaseEditAfterDuplicateError(request, release);
                            break;
                        default:
                            setSW360SessionError(request, ErrorMessages.RELEASE_NOT_ADDED);
                            response.setRenderParameter(PAGENAME, PAGENAME_DETAIL);
                    }

                    response.setRenderParameter(COMPONENT_ID, request.getParameter(COMPONENT_ID));
                }
            } catch (TException e) {
                log.error("Error fetching release from backend!", e);
            }
        }
    }

    private void prepareRequestForReleaseEditAfterDuplicateError(ActionRequest request, Release release) throws TException {
        fillVendor(release);
        request.setAttribute(RELEASE, release);
        setAttachmentsInRequest(request, release.getAttachments());
        putDirectlyLinkedReleaseRelationsInRequest(request, release.getReleaseIdToRelationship());
        request.setAttribute(USING_PROJECTS, Collections.emptySet());
        request.setAttribute(USING_COMPONENTS, Collections.emptySet());
    }

    private void fillVendor(Release release) throws TException {
        VendorService.Iface client = thriftClients.makeVendorClient();
        if(release.isSetVendorId()) {
            Vendor vendor = client.getByID(release.getVendorId());
            release.setVendor(vendor);
        }
    }

    @UsedAsLiferayAction
    public void deleteRelease(ActionRequest request, ActionResponse response) throws PortletException, IOException {
        RequestStatus requestStatus = ComponentPortletUtils.deleteRelease(request, log);

        String userEmail = UserCacheHolder.getUserFromRequest(request).getEmail();
        String releaseId = request.getParameter(PortalConstants.RELEASE_ID);
        deleteUnneededAttachments(userEmail, releaseId);
        setSessionMessage(request, requestStatus, "Release", "delete");

        response.setRenderParameter(PAGENAME, PAGENAME_DETAIL);
        response.setRenderParameter(COMPONENT_ID, request.getParameter(COMPONENT_ID));
    }

    @UsedAsLiferayAction
    public void deleteComponent(ActionRequest request, ActionResponse response) throws PortletException, IOException {
        RequestStatus requestStatus = ComponentPortletUtils.deleteComponent(request, log);

        String userEmail = UserCacheHolder.getUserFromRequest(request).getEmail();
        String id = request.getParameter(PortalConstants.COMPONENT_ID);
        deleteUnneededAttachments(userEmail, id);
        setSessionMessage(request, requestStatus, "Component", "delete");
    }

    @UsedAsLiferayAction
    public void applyFilters(ActionRequest request, ActionResponse response) throws PortletException, IOException {
        for (Component._Fields componentFilteredField : componentFilteredFields) {
            response.setRenderParameter(componentFilteredField.toString(), nullToEmpty(request.getParameter(componentFilteredField.toString())));
        }
    }

    private void updateVulnerabilitiesRelease(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
        String releaseId = request.getParameter(PortalConstants.RELEASE_ID);
        CveSearchService.Iface cveClient = thriftClients.makeCvesearchClient();
        try {
            VulnerabilityUpdateStatus importStatus = cveClient.updateForRelease(releaseId);
            JSONObject responseData = PortletUtils.importStatusToJSON(importStatus);
            PrintWriter writer = response.getWriter();
            writer.write(responseData.toString());
        } catch (TException e){
            log.error("Error updating CVEs for release in backend.", e);
        }
    }

    private void updateVulnerabilitiesComponent(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
        String componentId = request.getParameter(PortalConstants.COMPONENT_ID);
        CveSearchService.Iface cveClient = thriftClients.makeCvesearchClient();
        try {
            VulnerabilityUpdateStatus importStatus = cveClient.updateForComponent(componentId);
            JSONObject responseData = PortletUtils.importStatusToJSON(importStatus);
            PrintWriter writer = response.getWriter();
            writer.write(responseData.toString());
        } catch (TException e) {
            log.error("Error updating CVEs for component in backend.", e);
        }
    }

    private void updateAllVulnerabilities(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
        CveSearchService.Iface cveClient = thriftClients.makeCvesearchClient();
        try {
            VulnerabilityUpdateStatus importStatus = cveClient.fullUpdate();
            JSONObject responseData = PortletUtils.importStatusToJSON(importStatus);
            PrintWriter writer = response.getWriter();
            writer.write(responseData.toString());
        } catch (TException e) {
            log.error("Error occured with full update of CVEs in backend.", e);
        }
    }

    private void updateVulnerabilityVerification(ResourceRequest request, ResourceResponse response) throws IOException{
        String releaseId = request.getParameter(PortalConstants.RELEASE_ID);
        String vulnerabilityExternalId = request.getParameter(PortalConstants.VULNERABILITY_ID);
        User user = UserCacheHolder.getUserFromRequest(request);

        VulnerabilityService.Iface vulClient = thriftClients.makeVulnerabilityClient();

       try {
           Vulnerability dbVulnerability = vulClient.getVulnerabilityByExternalId(vulnerabilityExternalId, user);
           ReleaseVulnerabilityRelation dbRelation = vulClient.getRelationByIds(releaseId, dbVulnerability.getId(), user);
           ReleaseVulnerabilityRelation resultRelation = ComponentPortletUtils.updateReleaseVulnerabilityRelationFromRequest(dbRelation, request);
           RequestStatus requestStatus = vulClient.updateReleaseVulnerabilityRelation(resultRelation, user);

            JSONObject responseData = JSONFactoryUtil.createJSONObject();
            responseData.put(PortalConstants.REQUEST_STATUS, requestStatus.toString());
            responseData.put(PortalConstants.VULNERABILITY_ID, vulnerabilityExternalId);
            PrintWriter writer = response.getWriter();
            writer.write(responseData.toString());
        } catch (TException e) {
            log.error("Error updating vulnerability verification for release "+ releaseId +" in backend.", e);
        }
    }
}
