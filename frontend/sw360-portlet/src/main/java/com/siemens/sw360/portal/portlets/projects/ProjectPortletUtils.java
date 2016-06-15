/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.portal.portlets.projects;

import com.siemens.sw360.datahandler.common.SW360Utils;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.ResourceConstants;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.RoleConstants;
import com.liferay.portal.security.permission.ActionKeys;
import com.liferay.portal.service.ResourcePermissionLocalServiceUtil;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portlet.expando.model.ExpandoBridge;
import com.liferay.portlet.expando.model.ExpandoColumn;
import com.liferay.portlet.expando.model.ExpandoColumnConstants;
import com.liferay.portlet.expando.model.ExpandoTableConstants;
import com.liferay.portlet.expando.service.ExpandoColumnLocalServiceUtil;
import com.siemens.sw360.datahandler.thrift.components.ReleaseLink;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.projects.ProjectLink;
import com.siemens.sw360.datahandler.thrift.projects.ProjectRelationship;
import com.siemens.sw360.datahandler.thrift.vulnerabilities.ProjectVulnerabilityRating;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.thrift.vulnerabilities.VulnerabilityCheckStatus;
import com.siemens.sw360.datahandler.thrift.vulnerabilities.VulnerabilityRatingForProject;
import com.siemens.sw360.portal.common.PortalConstants;
import com.siemens.sw360.portal.common.PortletUtils;
import com.siemens.sw360.portal.users.UserCacheHolder;
import org.apache.log4j.Logger;

import javax.portlet.PortletRequest;
import javax.portlet.ResourceRequest;
import javax.portlet.RenderRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.siemens.sw360.portal.common.PortalConstants.CUSTOM_FIELD_PROJECT_GROUP_FILTER;

/**
 * Component portlet implementation
 *
 * @author cedric.bodet@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 * @author alex.borodin@evosoft.com
 */
public class ProjectPortletUtils {

    private static final Logger log = Logger.getLogger(ProjectPortletUtils.class);

    private ProjectPortletUtils() {
        // Utility class with only static functions
    }

    public static void updateProjectFromRequest(PortletRequest request, Project project) {
        for (Project._Fields field : Project._Fields.values()) {
            switch (field) {
                case LINKED_PROJECTS:
                    if (!project.isSetLinkedProjects()) project.setLinkedProjects(new HashMap<String, ProjectRelationship>());
                    updateLinkedProjectsFromRequest(request, project.linkedProjects);
                    break;
                case RELEASE_ID_TO_USAGE:
                    if (!project.isSetReleaseIdToUsage()) project.setReleaseIdToUsage(new HashMap<String,String>());
                    updateLinkedReleasesFromRequest(request, project.releaseIdToUsage);
                    break;

                case ATTACHMENTS:
                    project.setAttachments(PortletUtils.updateAttachmentsFromRequest(request, project.getAttachments()));
                    break;
                default:
                    setFieldValue(request, project, field);
            }
        }
    }

    private static void updateLinkedReleasesFromRequest(PortletRequest request, Map<String, String> releaseUsage) {
        releaseUsage.clear();
        String[] ids = request.getParameterValues(Project._Fields.RELEASE_ID_TO_USAGE.toString() + ReleaseLink._Fields.ID.toString());
        String[] relations = request.getParameterValues(Project._Fields.RELEASE_ID_TO_USAGE.toString() + ReleaseLink._Fields.COMMENT.toString());
        if (ids != null && relations != null && ids.length == relations.length)
            for (int k = 0; k < ids.length; ++k) {
                releaseUsage.put(ids[k], relations[k]);
            }
    }

    private static void updateLinkedProjectsFromRequest(PortletRequest request, Map<String, ProjectRelationship> linkedProjects) {
        linkedProjects.clear();
        String[] ids = request.getParameterValues(Project._Fields.LINKED_PROJECTS.toString() + ProjectLink._Fields.ID.toString());
        String[] relations = request.getParameterValues(Project._Fields.LINKED_PROJECTS.toString() + ProjectLink._Fields.RELATION.toString());
        if (ids != null && relations != null && ids.length == relations.length)
            for (int k = 0; k < ids.length; ++k) {
                linkedProjects.put(ids[k], ProjectRelationship.findByValue(Integer.parseInt(relations[k])));
            }
    }

    private static void setFieldValue(PortletRequest request, Project project, Project._Fields field) {
        PortletUtils.setFieldValue(request, project, field, Project.metaDataMap.get(field), "");
    }

    public static ProjectVulnerabilityRating updateProjectVulnerabilityRatingFromRequest(List<ProjectVulnerabilityRating> projectVulnerabilityRatings, ResourceRequest request){

        ProjectVulnerabilityRating projectVulnerabilityRating;
        if(projectVulnerabilityRatings.size() == 0){
            String projectId = request.getParameter(PortalConstants.PROJECT_ID);
            projectVulnerabilityRating = new ProjectVulnerabilityRating().setProjectId(projectId);
            projectVulnerabilityRating.setVulnerabilityIdToStatus(new HashMap<>());
        } else {
            projectVulnerabilityRating = projectVulnerabilityRatings.get(0);
        }
        VulnerabilityCheckStatus vulnerabilityCheckStatus = newVulnerabilityCheckStatusFromRequest(request);
        String vulnerabilityId = request.getParameter(PortalConstants.VULNERABILITY_ID);
        projectVulnerabilityRating.vulnerabilityIdToStatus.put(vulnerabilityId, vulnerabilityCheckStatus);
        return projectVulnerabilityRating;
    }

    private static VulnerabilityCheckStatus newVulnerabilityCheckStatusFromRequest(ResourceRequest request){

        VulnerabilityCheckStatus vulnerabilityCheckStatus = new VulnerabilityCheckStatus()
                .setCheckedBy(UserCacheHolder.getUserFromRequest(request).getEmail())
                .setCheckedOn(SW360Utils.getCreatedOn())
                .setComment(request.getParameter(PortalConstants.VULNERABILITY_RATING_COMMENT))
                .setVulnerabilityRating(VulnerabilityRatingForProject.valueOf(request.getParameter(PortalConstants.VULNERABILITY_RATING_VALUE)));
        return vulnerabilityCheckStatus;
    }

    private static com.liferay.portal.model.User getLiferayUser(RenderRequest request, User user) throws PortalException, SystemException {
        ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
        long companyId = themeDisplay.getCompanyId();
        return UserLocalServiceUtil.getUserByEmailAddress(companyId, user.email);
    }

    static void ensureUserCustomFieldExists(com.liferay.portal.model.User liferayUser, RenderRequest request) throws PortalException, SystemException {
        ExpandoBridge exp = liferayUser.getExpandoBridge();
        if (!exp.hasAttribute(CUSTOM_FIELD_PROJECT_GROUP_FILTER)){
            exp.addAttribute(CUSTOM_FIELD_PROJECT_GROUP_FILTER, ExpandoColumnConstants.STRING, false);
            long companyId = liferayUser.getCompanyId();

            ExpandoColumn column = ExpandoColumnLocalServiceUtil.getColumn(companyId, exp.getClassName(), ExpandoTableConstants.DEFAULT_TABLE_NAME, CUSTOM_FIELD_PROJECT_GROUP_FILTER);

            String[] roleNames = new String[]{RoleConstants.USER, RoleConstants.POWER_USER};
            for (String roleName: roleNames) {
                Role role = RoleLocalServiceUtil.getRole(companyId, roleName);
                if (role != null && column != null) {
                    ResourcePermissionLocalServiceUtil.setResourcePermissions(companyId,
                            ExpandoColumn.class.getName(),
                            ResourceConstants.SCOPE_INDIVIDUAL,
                            String.valueOf(column.getColumnId()),
                            role.getRoleId(),
                            new String[]{ActionKeys.VIEW, ActionKeys.UPDATE});
                }
            }
        }
    }

    static void saveStickyProjectGroup(RenderRequest request, User user, String groupFilterValue) {
        try {
            ExpandoBridge exp = getUserExpandoBridge(request, user);
            exp.setAttribute(CUSTOM_FIELD_PROJECT_GROUP_FILTER, groupFilterValue);
        } catch (PortalException | SystemException e) {
            log.warn("Could not save sticky project group to custom field", e);
        }
    }

    static String loadStickyProjectGroup(RenderRequest request, User user){
        try {
            ExpandoBridge exp = getUserExpandoBridge(request, user);
            return (String) exp.getAttribute(CUSTOM_FIELD_PROJECT_GROUP_FILTER);
        } catch (PortalException | SystemException e) {
            log.error("Could not load sticky project group from custom field", e);
            return null;
        }
    }

    private static ExpandoBridge getUserExpandoBridge(RenderRequest request, User user) throws PortalException, SystemException {
        com.liferay.portal.model.User liferayUser = getLiferayUser(request, user);
        ensureUserCustomFieldExists(liferayUser, request);
        return liferayUser.getExpandoBridge();
    }
}
