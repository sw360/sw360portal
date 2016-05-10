/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 * With contributions by Bosch Software Innovations GmbH, 2016.
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
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.model.Layout;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;
import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.common.SW360Utils;
import com.siemens.sw360.datahandler.thrift.*;
import com.siemens.sw360.datahandler.thrift.components.ReleaseLink;
import com.siemens.sw360.datahandler.thrift.components.ReleaseRelationship;
import com.siemens.sw360.datahandler.thrift.licenses.License;
import com.siemens.sw360.datahandler.thrift.licenses.LicenseService;
import com.siemens.sw360.datahandler.thrift.projects.ProjectLink;
import com.siemens.sw360.datahandler.thrift.projects.ProjectRelationship;
import com.siemens.sw360.datahandler.thrift.users.RequestedAction;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.thrift.users.UserService;
import com.siemens.sw360.portal.common.PortalConstants;
import com.siemens.sw360.portal.users.UserCacheHolder;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import javax.portlet.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.liferay.portal.kernel.util.WebKeys.THEME_DISPLAY;
import static com.siemens.sw360.portal.common.PortalConstants.RELEASE_DEPTH_MAP;
import static com.siemens.sw360.portal.common.PortalConstants.RELEASE_LIST;
import static com.siemens.sw360.portal.common.PortalConstants.PROJECT_DEPTH_MAP;
import static com.siemens.sw360.portal.common.PortalConstants.PROJECT_LIST;


abstract public class Sw360Portlet extends MVCPortlet {

    private static final Logger log = Logger.getLogger(Sw360Portlet.class);

    private static final Map<Class<? extends Sw360Portlet>, Long> repository = new ConcurrentHashMap<>();

    private boolean registered = false;
    protected final ThriftClients thriftClients;

    protected Sw360Portlet() {
        thriftClients = new ThriftClients();
    }

    public Sw360Portlet(ThriftClients thriftClients) {
        this.thriftClients = thriftClients;
    }

    static long getPlid(Class<? extends Sw360Portlet> portlet) {
        Long plid = repository.get(portlet);
        return plid != null ? plid : 0;
    }

    @Override
    public void doView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        if (!registered) {
            register(request);
        }

        super.doView(request, response);
    }


    private synchronized void register(PortletRequest request) {
        if (!registered) {
            ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(THEME_DISPLAY);
            Layout layout = themeDisplay.getLayout();
            long plid = layout.getPlid();

            repository.put(this.getClass(), plid);

            registered = true;
        }
    }

    protected void addBreadcrumbEntry(PortletRequest request, String name, PortletURL url) {
        PortalUtil.addPortletBreadcrumbEntry(PortalUtil.getOriginalServletRequest(PortalUtil.getHttpServletRequest(request)),
                name, url.toString());
    }

    protected void renderRequestSummary(PortletRequest request, ActionResponse response, RequestSummary requestSummary) {
        StringBuilder successMsg = new StringBuilder();
        successMsg.append(requestSummary.requestStatus.toString());

        if (requestSummary.isSetTotalAffectedElements() || requestSummary.isSetTotalElements() || requestSummary.isSetMessage())
            successMsg.append(": ");
        if (requestSummary.isSetTotalAffectedElements() && requestSummary.isSetTotalElements()) {
            successMsg.append(requestSummary.totalAffectedElements)
                    .append(" affected of ")
                    .append(requestSummary.totalElements)
                    .append(" total. ");
        } else if (requestSummary.isSetTotalElements()) {
            successMsg.append(requestSummary.totalElements)
                    .append(" total Elements. ");
        } else if (requestSummary.isSetTotalAffectedElements()) {
            successMsg.append(requestSummary.totalAffectedElements)
                    .append(" total affected elements. ");
        }

        if (requestSummary.isSetMessage())
            successMsg.append(requestSummary.getMessage());

        SessionMessages.add(request, "request_processed", successMsg.toString());
    }

    protected void renderRequestSummary(PortletRequest request, MimeResponse response, RequestSummary requestSummary) {
        JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
        jsonObject.put("result", requestSummary.requestStatus.toString());
        if (requestSummary.isSetTotalAffectedElements())
            jsonObject.put("totalAffectedObjects", requestSummary.totalAffectedElements);
        if (requestSummary.isSetTotalElements())
            jsonObject.put("totalObjects", requestSummary.totalElements);

        try {
            writeJSON(request, response, jsonObject);
        } catch (IOException e) {
            log.error("Problem rendering RequestStatus", e);
        }
    }

    protected void renderRequestStatus(PortletRequest request, MimeResponse response, RequestStatus requestStatus) {
        JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
        jsonObject.put("result", requestStatus.toString());
        try {
            writeJSON(request, response, jsonObject);
        } catch (IOException e) {
            log.error("Problem rendering RequestStatus", e);
        }
    }

    protected void renderRequestStatus(PortletRequest request, ActionResponse response, RequestStatus requestStatus) {
        SessionMessages.add(request, "request_processed", requestStatus.toString());
    }

    protected void serveRequestStatus(PortletRequest request, ResourceResponse response, RequestStatus requestStatus, String message, Logger log) {
        if (requestStatus != RequestStatus.FAILURE) {
            request.setAttribute(PortalConstants.REQUEST_STATUS, requestStatus.toString());
            // We want failures to call the error method of the AJAX call so we do not send it
            renderRequestStatus(request, response, requestStatus);
        } else {
            log.error(message);
        }
    }


    public static boolean isUserAction(String action) {
        return action.startsWith(PortalConstants.USER_PREFIX);
    }

    public void dealWithUserAction(ResourceRequest request, ResourceResponse response, String action) throws IOException, PortletException {
        if (PortalConstants.USER_SEARCH.equals(action)) {
            String searchText = request.getParameter(PortalConstants.WHAT);
            String how = request.getParameter(PortalConstants.HOW);

            Boolean multiUsers = false;
            if (!isNullOrEmpty(how)) {
                multiUsers = Boolean.parseBoolean(how);
            }

            try {
                List<User> users;
                UserService.Iface client = thriftClients.makeUserClient();
                if (isNullOrEmpty(searchText)) {
                    users = client.getAllUsers();
                } else {
                    users = client.searchUsers(searchText);
                }
                request.setAttribute(PortalConstants.USER_LIST, users);
                request.setAttribute(PortalConstants.HOW, multiUsers);
                include("/html/utils/ajax/userListAjax.jsp", request, response, PortletRequest.RESOURCE_PHASE);
            } catch (TException e) {
                log.error("Error getting users", e);
            }

        }
    }

    public void dealWithLicenseAction(ResourceRequest request, ResourceResponse response, String action) throws IOException, PortletException {
        if (PortalConstants.LICENSE_SEARCH.equals(action)) {
            final String searchText = request.getParameter(PortalConstants.WHAT);

            try {
                LicenseService.Iface client = thriftClients.makeLicenseClient();
                List<License> licenses = client.getLicenseSummary();

                if (!isNullOrEmpty(searchText)) {
                    licenses = FluentIterable.from(licenses).filter(new Predicate<License>() {
                        @Override
                        public boolean apply(License input) {
                            String fullname = input.getFullname();
                            return !isNullOrEmpty(fullname) && fullname.contains(searchText);
                        }
                    }).toList();
                }

                request.setAttribute(PortalConstants.LICENSE_LIST, licenses);
                include("/html/utils/ajax/licenseListAjax.jsp", request, response, PortletRequest.RESOURCE_PHASE);
            } catch (TException e) {
                log.error("Error getting licenses", e);
            }

        }
    }

    public static boolean isLicenseAction(String action) {
        return action.startsWith(PortalConstants.LICENSE_PREFIX);
    }

    protected boolean isGenericAction(String action) {
        return isUserAction(action) || isLicenseAction(action);
    }

    protected void dealWithGenericAction(ResourceRequest request, ResourceResponse response, String action) throws IOException, PortletException {
        if (isUserAction(action)) {
            dealWithUserAction(request, response, action);
        } else if (isLicenseAction(action)) {
            dealWithLicenseAction(request, response, action);
        } else {
            throw new IllegalStateException("Cannot deal with action " + action + " as generic");
        }
    }

    public void setSessionMessage(PortletRequest request, RequestStatus requestStatus, String type, String verb, String name) throws PortletException {
        String successMsg;
        if (isNullOrEmpty(name)) {
            name = "";
        }
        else {
            name = " " + name;
        }
        switch (requestStatus) {
            case SUCCESS:
                successMsg = type + name + " " + verb + "d successfully!";
                break;
            case SENT_TO_MODERATOR:
                successMsg = "Moderation request was sent to " + verb + " the " + type + name + "!";
                break;
            case FAILURE:
                successMsg = type + name + " could not be " + verb + "d successfully!";
                break;
            case IN_USE:
                if(type.equals("License")) {
                    successMsg = type + name + " could not be " + verb + "d successfully, as it is used by at least one Release!";
                } else {
                    successMsg = type + name + " could not be " + verb + "d successfully, as it is used by other Projects or Releases!";
                }
                break;
            default:
                throw new PortletException("Unknown request status");
        }
        SessionMessages.add(request, "request_processed", successMsg);
    }

    public void setSessionMessage(PortletRequest request, String successMsg) throws PortletException {
        SessionMessages.add(request, "request_processed", successMsg);
    }

    public void setSessionMessage(PortletRequest request, RequestStatus requestStatus, String type, String verb) throws PortletException {
        setSessionMessage(request, requestStatus, type, verb, null);
    }

    protected void addEditDocumentMessage(RenderRequest request, Map<RequestedAction, Boolean> permissions, DocumentState documentState) {

        List<String> msgs = new ArrayList<>();
        if (documentState.isSetModerationState()) {
            ModerationState moderationState = documentState.getModerationState();
            switch (moderationState) {
                case PENDING:
                    msgs.add("There is a pending Moderation request.");
                    break;
                case APPROVED:
                    break;
                case REJECTED:
                    break;
                case INPROGRESS:
                    msgs.add("There is a Moderation request in progress.");
                    break;
            }
        }

        if (!permissions.get(RequestedAction.WRITE)) {
            msgs.add("You will create a moderation request if you update.");
        } else if (documentState.isIsOriginalDocument()) {
            msgs.add("You are editing the original document.");
        }

        SessionMessages.add(request, "request_processed", Joiner.on(" ").join(msgs));
    }

    protected void putLinkedReleaseRelationsInRequest(RenderRequest request, Map<String, ReleaseRelationship> releaseIdToRelationship) throws TException {
        final Map<Integer, Collection<ReleaseLink>> depthMap = Maps.newHashMap(SW360Utils.getLinkedReleaseRelations(releaseIdToRelationship, thriftClients, log));
        putLinkedObjectsInRequest(request, depthMap, RELEASE_LIST, RELEASE_DEPTH_MAP);
    }

    protected void putLinkedReleasesInRequest(RenderRequest request, Map<String, String> releaseIdToRelationship) throws TException {
        User user = UserCacheHolder.getUserFromRequest(request);
        final Map<Integer, Collection<ReleaseLink>> depthMap = Maps.newHashMap(SW360Utils.getLinkedReleases(releaseIdToRelationship, thriftClients, log));
        Collection<ReleaseLink> ownReleases = CommonUtils.nullToEmptyCollection(depthMap.get(0));
        for (ReleaseLink rl: ownReleases) {
            rl.setLicenseNames(new HashSet<>(SW360Utils.getLicenseNamesFromIds(rl.getLicenseIds(), user.getDepartment())));
        }
        putLinkedObjectsInRequest(request, depthMap, RELEASE_LIST, RELEASE_DEPTH_MAP);
    }

    protected void putLinkedProjectsInRequest(RenderRequest request, Map<String, ProjectRelationship> projectIdToRelationship) throws TException {
        final Map<Integer, Collection<ProjectLink>> depthMap = Maps.newHashMap(SW360Utils.getLinkedProjects(projectIdToRelationship, thriftClients, log));
        putLinkedObjectsInRequest(request, depthMap, PROJECT_LIST, PROJECT_DEPTH_MAP);
    }

    private <T> void putLinkedObjectsInRequest(RenderRequest request, Map<Integer, Collection<T>> depthMap, String listBeanName, String depthMapBeanName) throws TException {
        if (depthMap.containsKey(0)) {
            request.setAttribute(listBeanName, Lists.newArrayList(depthMap.get(0)));
            depthMap.remove(0);
        } else {
            request.setAttribute(listBeanName, Collections.emptyList());
        }
        request.setAttribute(depthMapBeanName, depthMap);
    }
}
