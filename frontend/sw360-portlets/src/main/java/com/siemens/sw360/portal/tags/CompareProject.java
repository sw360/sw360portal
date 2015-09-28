/*
 * Copyright Siemens AG, 2015. Part of the SW360 Portal Project.
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
package com.siemens.sw360.portal.tags;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.siemens.sw360.datahandler.thrift.ThriftClients;
import com.siemens.sw360.datahandler.thrift.components.ComponentService;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.projects.ProjectRelationship;
import com.siemens.sw360.datahandler.thrift.projects.ProjectService;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.thrift.users.UserService;
import org.apache.thrift.TException;
import org.apache.thrift.meta_data.FieldMetaData;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.util.*;

import static com.siemens.sw360.portal.tags.TagUtils.*;

/**
 * Display the fields that have changed from old to update
 *
 * @author Johannes.Najjar@tngtech.com
 */
public class CompareProject extends NameSpaceAwareTag {
    private Project old;
    private Project update;
    private String tableClasses = "";
    private String idPrefix = "";

    public void setUpdate(Project update) {
        this.update = update;
    }

    public void setOld(Project old) {
        this.old = old;
    }

    public void setTableClasses(String tableClasses) {
        this.tableClasses = tableClasses;
    }

    public void setIdPrefix(String idPrefix) {
        this.idPrefix = idPrefix;
    }

    public int doStartTag() throws JspException {

        JspWriter jspWriter = pageContext.getOut();

        StringBuilder display = new StringBuilder();
        String namespace = getNamespace();

        if (old == null || update == null) {
            return SKIP_BODY;
        }

        try {
            for (Project._Fields field : Project._Fields.values()) {
                switch (field) {
                    //ignored Fields
                    case ID:
                    case REVISION:
                    case TYPE:
                    case CREATED_BY:
                    case CREATED_ON:
                    case PERMISSIONS:
                    case RELEASE_CLEARING_STATE_SUMMARY:
                    case RELEASE_IDS:
                        //Taken care of externally
                    case ATTACHMENTS:

                        //Done in extra tables
                    case LINKED_PROJECTS:
                    case RELEASE_ID_TO_USAGE:
                        break;

                    default:
                        FieldMetaData fieldMetaData = Project.metaDataMap.get(field);
                        displaySimpleField(display, old, update, field, fieldMetaData, "");
                }
            }

            String renderString = display.toString();

            if (Strings.isNullOrEmpty(renderString)) {
                renderString = "<h4> No changes in basic fields </h4>";
            } else {
                renderString = String.format("<table class=\"%s\" id=\"%schanges\" >", tableClasses, idPrefix)
                        + "<thead><tr><th colspan=\"3\"> Changes for Basic fields</th></tr>"
                        + String.format("<tr><th>%s</th><th>%s</th><th>%s</th></tr></thead><tbody>", FIELD_NAME, CURRENT_VAL, SUGGESTED_VAL)
                        + renderString + "</tbody></table>";
            }

            StringBuilder linkedProjectsDisplay = new StringBuilder();
            renderLinkedProjects(linkedProjectsDisplay);

            StringBuilder releaseUsageDisplay = new StringBuilder();
            renderReleaseIdToUsage(releaseUsageDisplay);

            jspWriter.print(renderString + linkedProjectsDisplay.toString() + releaseUsageDisplay.toString());
        } catch (Exception e) {
            throw new JspException(e);
        }
        return SKIP_BODY;
    }

    private void renderLinkedProjects(StringBuilder display) {
        if (ensureSomethingTodoAndNoNullLinkedProjects()) {
            Map<String, ProjectRelationship> oldLinkedProjects = old.getLinkedProjects();
            Map<String, ProjectRelationship> updateLinkedProjects = update.getLinkedProjects();
            Sets.SetView<String> removedProjectIds = Sets.difference(oldLinkedProjects.keySet(), updateLinkedProjects.keySet());
            Sets.SetView<String> addedProjectIds = Sets.difference(updateLinkedProjects.keySet(), oldLinkedProjects.keySet());
            Sets.SetView<String> commonProjectIds = Sets.intersection(updateLinkedProjects.keySet(), oldLinkedProjects.keySet());

            renderProjectLinkList(display, oldLinkedProjects, removedProjectIds, "Removed Project links");
            renderProjectLinkList(display, updateLinkedProjects, addedProjectIds, "Added Project links");
            renderProjectLinkListCompare(display, oldLinkedProjects, updateLinkedProjects, commonProjectIds);
        }
    }

    private boolean ensureSomethingTodoAndNoNullLinkedProjects() {

        if (!old.isSetLinkedProjects() && !update.isSetLinkedProjects()) {
            return false;
        }
        if (!old.isSetLinkedProjects()) {
            old.setLinkedProjects(Collections.<String, ProjectRelationship>emptyMap());
        }
        if (!update.isSetLinkedProjects()) {
            update.setLinkedProjects(Collections.<String, ProjectRelationship>emptyMap());
        }

        return true;
    }

    private void renderProjectLinkList(StringBuilder display, Map<String, ProjectRelationship> projectRelationshipMap, Set<String> projectIds, String msg) {
        if (projectIds.isEmpty()) return;


        StringBuilder candidate = new StringBuilder();
        try {
            UserService.Iface userClient = new ThriftClients().makeUserClient();
            User user = userClient.getByEmail(old.getCreatedBy());
            ProjectService.Iface client = new ThriftClients().makeProjectClient();

            for (Project project : client.getProjectsById(projectIds, user)) {
                candidate.append(String.format("<tr><td>%s</td><td>%s</td></tr>", project.getName(), projectRelationshipMap.get(project.getId())));
            }

        } catch (TException ignored) {
        }

        String tableContent = candidate.toString();
        if (!tableContent.isEmpty()) {

            display.append(String.format("<table class=\"%s\" id=\"%s%s\" >", tableClasses, idPrefix, msg));
            display.append(String.format("<thead><tr><th colspan=\"2\">%s</th></tr><tr><th>Project Name</th><th>Project Relationship</th></tr></thead><tbody>", msg));
            display.append(tableContent);
            display.append("</tbody></table>");
        }
    }


    private void renderProjectLinkListCompare(StringBuilder display, Map<String, ProjectRelationship> oldProjectRelationshipMap, Map<String, ProjectRelationship> updateProjectRelationshipMap, Set<String> projectIds) {
        if (projectIds.isEmpty()) return;


        StringBuilder candidate = new StringBuilder();
        try {
            UserService.Iface userClient = new ThriftClients().makeUserClient();
            User user = userClient.getByEmail(old.getCreatedBy());
            ProjectService.Iface client = new ThriftClients().makeProjectClient();

            Set<String> changedIds= new HashSet<>();

            for (String projectId : projectIds) {
                ProjectRelationship currentProjectRelationship = updateProjectRelationshipMap.get(projectId);
                ProjectRelationship oldProjectRelationship = oldProjectRelationshipMap.get(projectId);

                if (!currentProjectRelationship.equals(oldProjectRelationship)) {
                    changedIds.add(projectId);
                }
            }
            //! This code doubling is done to reduce the database queries. I.e. one big query instead of multiple small ones
            for (Project project : client.getProjectsById(changedIds, user)) {
                ProjectRelationship currentProjectRelationship = updateProjectRelationshipMap.get(project.getId());
                ProjectRelationship oldProjectRelationship = oldProjectRelationshipMap.get(project.getId());
                candidate.append(String.format("<tr><td>%s</td><td>%s</td><td>%s</td></tr>", project.getName(), oldProjectRelationship, currentProjectRelationship));
            }


        } catch (TException ignored) {
        }

        String tableContent = candidate.toString();
        if (!tableContent.isEmpty()) {
            display.append(String.format("<table class=\"%s\" id=\"%sUpdated\" >", tableClasses, idPrefix));
            display.append("<thead><tr><th colspan=\"3\">Updated Project Links</th></tr><tr><th>Project Name</th><th>Current Project Relationship</th><th>Suggested Project Relationship</th></tr></thead><tbody>");
            display.append(tableContent);
            display.append("</tbody></table>");
        }
    }

    private void renderReleaseIdToUsage(StringBuilder display) {
        if (ensureSomethingTodoAndNoNullReleaseIdUsage()) {
            Map<String, String> oldReleaseIdToUsage = old.getReleaseIdToUsage();
            Map<String, String> updateReleaseIdToUsage = update.getReleaseIdToUsage();
            Sets.SetView<String> removedReleaseIds = Sets.difference(oldReleaseIdToUsage.keySet(), updateReleaseIdToUsage.keySet());
            Sets.SetView<String> addedReleaseIds = Sets.difference(updateReleaseIdToUsage.keySet(), oldReleaseIdToUsage.keySet());
            Sets.SetView<String> commonReleaseIds = Sets.intersection(updateReleaseIdToUsage.keySet(), oldReleaseIdToUsage.keySet());


            renderReleaseLinkList(display, oldReleaseIdToUsage, removedReleaseIds, "Removed Release links");
            renderReleaseLinkList(display, updateReleaseIdToUsage, addedReleaseIds, "Added Release links");
            renderReleaseLinkListCompare(display, oldReleaseIdToUsage, updateReleaseIdToUsage, commonReleaseIds);
        }
    }

    private boolean ensureSomethingTodoAndNoNullReleaseIdUsage() {

        if (!old.isSetReleaseIdToUsage() && !update.isSetReleaseIdToUsage()) {
            return false;
        }
        if (!old.isSetReleaseIdToUsage()) {
            old.setReleaseIdToUsage(Collections.<String, String>emptyMap());
        }
        if (!update.isSetReleaseIdToUsage()) {
            update.setReleaseIdToUsage(Collections.<String, String>emptyMap());
        }

        return true;
    }

    private void renderReleaseLinkList(StringBuilder display, Map<String, String> releaseRelationshipMap, Set<String> releaseIds, String msg) {
        if (releaseIds.isEmpty()) return;


        StringBuilder candidate = new StringBuilder();
        try {
            UserService.Iface userClient = new ThriftClients().makeUserClient();
            User user = userClient.getByEmail(old.getCreatedBy());
            ComponentService.Iface componentClient = new ThriftClients().makeComponentClient();

            for (Release release : componentClient.getReleasesById(releaseIds, user)) {
                candidate.append(String.format("<tr><td>%s</td><td>%s</td></tr>", release.getName(), releaseRelationshipMap.get(release.getId())));
            }

        } catch (TException ignored) {
        }

        String tableContent = candidate.toString();
        if (!tableContent.isEmpty()) {

            display.append(String.format("<table class=\"%s\" id=\"%s%s\" >", tableClasses, idPrefix, msg));
            display.append(String.format("<thead><tr><th colspan=\"2\">%s</th></tr><tr><th>Release Name</th><th>Release Relationship</th></tr></thead><tbody>", msg));
            display.append(tableContent);
            display.append("</tbody></table>");
        }
    }

    private void renderReleaseLinkListCompare(StringBuilder display, Map<String, String> oldReleaseRelationshipMap, Map<String, String> updateReleaseRelationshipMap, Set<String> releaseIds) {
        if (releaseIds.isEmpty()) return;


        StringBuilder candidate = new StringBuilder();
        try {
            UserService.Iface userClient = new ThriftClients().makeUserClient();
            User user = userClient.getByEmail(old.getCreatedBy());
            ComponentService.Iface componentClient = new ThriftClients().makeComponentClient();

            final HashSet<String> changedIds = new HashSet<>();

            for (String releaseId : releaseIds) {
                String oldReleaseRelationship = oldReleaseRelationshipMap.get(releaseId);
                String updateReleaseRelationship = updateReleaseRelationshipMap.get(releaseId);

                if (!oldReleaseRelationship.equals(updateReleaseRelationship)) {
                changedIds.add(releaseId);
                }
            }

            for (Release release : componentClient.getReleasesById(changedIds, user)) {
                String releaseId = release.getId();
                String oldReleaseRelationship = oldReleaseRelationshipMap.get(releaseId);
                String updateReleaseRelationship = updateReleaseRelationshipMap.get(releaseId);
                candidate.append(String.format("<tr><td>%s</td><td>%s</td><td>%s</td></tr>", release.getName(), oldReleaseRelationship, updateReleaseRelationship));
            }

        } catch (TException ignored) {
        }

        String tableContent = candidate.toString();
        if (!tableContent.isEmpty()) {
            display.append(String.format("<table class=\"%s\" id=\"%sUpdated\" >", tableClasses, idPrefix));
            display.append("<thead><tr><th colspan=\"3\">Updated Release Links</th></tr><tr><th>Release Name</th><th>Current Release Relationship</th><th>Suggested Release Relationship</th></tr></thead><tbody>");
            display.append(tableContent);
            display.append("</tbody></table>");
        }
    }
}
