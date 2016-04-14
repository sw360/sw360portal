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
package com.siemens.sw360.portal.portlets.projects;

import com.siemens.sw360.datahandler.thrift.attachments.Attachment;
import com.siemens.sw360.datahandler.thrift.components.ReleaseLink;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.projects.ProjectLink;
import com.siemens.sw360.datahandler.thrift.projects.ProjectRelationship;
import com.siemens.sw360.portal.common.PortletUtils;

import javax.portlet.PortletRequest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Component portlet implementation
 *
 * @author cedric.bodet@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 */
public class ProjectPortletUtils {

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
                    if (!project.isSetAttachments()) project.setAttachments(new HashSet<Attachment>());
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

}
