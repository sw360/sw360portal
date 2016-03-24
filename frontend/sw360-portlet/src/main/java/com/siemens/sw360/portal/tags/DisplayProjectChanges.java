/*
 * Copyright Siemens AG, 2016. Part of the SW360 Portal Project.
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
import com.siemens.sw360.datahandler.thrift.projects.ProjectLink;
import com.siemens.sw360.datahandler.thrift.projects.ProjectRelationship;
import com.siemens.sw360.datahandler.thrift.projects.ProjectService;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.thrift.users.UserService;
import com.siemens.sw360.portal.tags.urlutils.LinkedReleaseRenderer;
import org.apache.thrift.TException;
import org.apache.thrift.meta_data.FieldMetaData;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.siemens.sw360.portal.tags.TagUtils.*;

/**
 * Display the fields that have changed in the project
 *
 * @author birgit.heydenreich@tngtech.com
 */
public class DisplayProjectChanges extends NameSpaceAwareTag {
    private Project actual;
    private Project additions;
    private Project deletions;
    private String tableClasses = "";
    private String idPrefix = "";

    public void setActual(Project actual) {
        this.actual = actual;
    }

    public void setAdditions(Project additions) {
        this.additions = additions;
    }

    public void setDeletions(Project deletions) {
        this.deletions = deletions;
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

        if (additions == null || deletions == null) {
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
                    case DOCUMENT_STATE:
                        //Taken care of externally
                    case ATTACHMENTS:

                        //Done in extra tables
                    case LINKED_PROJECTS:
                    case RELEASE_ID_TO_USAGE:
                        break;

                    default:
                        FieldMetaData fieldMetaData = Project.metaDataMap.get(field);
                        displaySimpleFieldOrSet(display, actual, additions, deletions, field, fieldMetaData, "");
                }
            }

            String renderString = display.toString();

            if (Strings.isNullOrEmpty(renderString)) {
                renderString = "<h4> No changes in basic fields </h4>";
            } else {
                renderString = String.format("<table class=\"%s\" id=\"%schanges\" >", tableClasses, idPrefix)
                        + "<thead><tr><th colspan=\"4\"> Changes for Basic fields</th></tr>"
                        + String.format("<tr><th>%s</th><th>%s</th><th>%s</th><th>%s</th></tr></thead><tbody>",
                        FIELD_NAME, CURRENT_VAL, DELETED_VAL, SUGGESTED_VAL)
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

            Set<String> changedProjectIds = Sets.intersection(additions.getLinkedProjects().keySet(),
                                                              deletions.getLinkedProjects().keySet());
           //remove projects already deleted in database;
            changedProjectIds = Sets.intersection(changedProjectIds, actual.getLinkedProjects().keySet());

            Set<String> removedProjectIds = Sets.difference(deletions.getLinkedProjects().keySet(), changedProjectIds);
            removedProjectIds = Sets.intersection(removedProjectIds, actual.getLinkedProjects().keySet());

            Set<String> addedProjectIds = Sets.difference(additions.getLinkedProjects().keySet(), changedProjectIds);

            renderProjectLinkList(display, deletions.getLinkedProjects(), removedProjectIds, "Removed Project Links");
            renderProjectLinkList(display, additions.getLinkedProjects(), addedProjectIds, "Added Project Links");
            renderProjectLinkListCompare(
                    display,
                    actual.getLinkedProjects(),
                    deletions.getLinkedProjects(),
                    additions.getLinkedProjects(),
                    changedProjectIds);
        }
    }

    private boolean ensureSomethingTodoAndNoNullLinkedProjects() {
        if (!deletions.isSetLinkedProjects() && !additions.isSetLinkedProjects()) {
            return false;
        }
        if(!deletions.isSetLinkedProjects()){
            deletions.setLinkedProjects(new HashMap<>());
        }
        if(!additions.isSetLinkedProjects()){
            additions.setLinkedProjects(new HashMap<>());
        }
        return true;
    }

    private void renderProjectLinkList(StringBuilder display,
                                       Map<String, ProjectRelationship> projectRelationshipMap,
                                       Set<String> projectIds,
                                       String msg) {
        if (projectIds.isEmpty()) return;

        Map<String, ProjectRelationship> filteredMap = new HashMap<>();
        for(String id : projectIds){
            filteredMap.put(id, projectRelationshipMap.get(id));
        }
        StringBuilder candidate = new StringBuilder();
        try {
            UserService.Iface userClient = new ThriftClients().makeUserClient();
            User user = userClient.getByEmail(actual.getCreatedBy());
            ProjectService.Iface client = new ThriftClients().makeProjectClient();
            //use getLinkedProjects, as it does not check permissions
            for (ProjectLink projectLink : client.getLinkedProjects(filteredMap)) {
                candidate.append(
                        String.format("<tr><td>%s</td><td>%s</td></tr>", projectLink.getName(), projectLink.getRelation()));
            }
        } catch (TException ignored) {
        }
        String tableContent = candidate.toString();
        if (!tableContent.isEmpty()) {

            display.append(String.format("<table class=\"%s\" id=\"%s%s\" >", tableClasses, idPrefix, msg));
            display.append(String.format("<thead><tr><th colspan=\"2\">%s</th></tr>" +
                    "<tr><th>Project Name</th><th>Project Relationship</th></tr></thead><tbody>", msg));
            display.append(tableContent);
            display.append("</tbody></table>");
        }
    }


    private void renderProjectLinkListCompare(StringBuilder display,
                                              Map<String, ProjectRelationship> oldProjectRelationshipMap,
                                              Map<String, ProjectRelationship> deleteProjectRelationshipMap,
                                              Map<String, ProjectRelationship> updateProjectRelationshipMap,
                                              Set<String> projectIds) {
        if (projectIds.isEmpty()) return;

        StringBuilder candidate = new StringBuilder();
        try {
            UserService.Iface userClient = new ThriftClients().makeUserClient();
            User user = userClient.getByEmail(actual.getCreatedBy());
            ProjectService.Iface client = new ThriftClients().makeProjectClient();

            Map<String, ProjectRelationship> changeMap= new HashMap<>();

            for (String projectId : projectIds) {
                ProjectRelationship updateProjectRelationship = updateProjectRelationshipMap.get(projectId);
                ProjectRelationship oldProjectRelationship = oldProjectRelationshipMap.get(projectId);

                if (!updateProjectRelationship.equals(oldProjectRelationship)) {
                    changeMap.put(projectId, oldProjectRelationshipMap.get(projectId));
                }
            }
            //! This code doubling is done to reduce the database queries. I.e. one big query instead of multiple small ones
            for (ProjectLink projectLink : client.getLinkedProjects(changeMap)) {
                ProjectRelationship updateProjectRelationship = updateProjectRelationshipMap.get(projectLink.getId());
                ProjectRelationship deleteProjectRelationship = deleteProjectRelationshipMap.get(projectLink.getId());
                ProjectRelationship oldProjectRelationship = oldProjectRelationshipMap.get(projectLink.getId());
                candidate.append(String.format("<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>",
                        projectLink.getName(),
                        oldProjectRelationship,
                        deleteProjectRelationship,
                        updateProjectRelationship));
            }

        } catch (TException ignored) {
        }

        String tableContent = candidate.toString();
        if (!tableContent.isEmpty()) {
            display.append(String.format("<table class=\"%s\" id=\"%sUpdated\" >", tableClasses, idPrefix));
            display.append("<thead><tr><th colspan=\"4\">Updated Project Links</th></tr>" +
                    "<tr><th>Project Name</th>" +
                    "<th>Current Project Relationship</th>" +
                    "<th>Deleted Project Relationship</th>" +
                    "<th>Suggested Project Relationship</th></tr>" +
                    "</thead><tbody>");
            display.append(tableContent);
            display.append("</tbody></table>");
        }
    }

    private void renderReleaseIdToUsage(StringBuilder display) {

       if (ensureSomethingTodoAndNoNullReleaseIdUsage()) {

           Set<String> changedReleaseIds = Sets.intersection(
                   additions.getReleaseIdToUsage().keySet(),
                   deletions.getReleaseIdToUsage().keySet());
           changedReleaseIds = Sets.intersection(
                   changedReleaseIds,
                   actual.getReleaseIdToUsage().keySet());//remove projects already deleted in database
           Set<String> removedReleaseIds = Sets.difference(
                   deletions.getReleaseIdToUsage().keySet(),
                   changedReleaseIds);
           removedReleaseIds = Sets.intersection(
                   removedReleaseIds,
                   actual.getReleaseIdToUsage().keySet());
           Set<String> addedReleaseIds = Sets.difference(
                   additions.getReleaseIdToUsage().keySet(),
                   changedReleaseIds);

           LinkedReleaseRenderer renderer = new LinkedReleaseRenderer(display, tableClasses, idPrefix, actual.getCreatedBy());
           renderer.renderReleaseLinkList(display, deletions.getReleaseIdToUsage(), removedReleaseIds, "Removed Release Links");
           renderer.renderReleaseLinkList(display, additions.getReleaseIdToUsage(), addedReleaseIds, "Added Release Links");
           renderer.renderReleaseLinkListCompare(display,
                   actual.getReleaseIdToUsage(),
                   deletions.getReleaseIdToUsage(),
                   additions.getReleaseIdToUsage(), changedReleaseIds);
        }
    }

    private boolean ensureSomethingTodoAndNoNullReleaseIdUsage() {
        if (!deletions.isSetReleaseIdToUsage() && !additions.isSetReleaseIdToUsage()) {
            return false;
        }
        if(!deletions.isSetReleaseIdToUsage()){
            deletions.setReleaseIdToUsage(new HashMap<>());
        }
        if(!additions.isSetReleaseIdToUsage()){
            additions.setReleaseIdToUsage(new HashMap<>());
        }
        return true;
    }
}
