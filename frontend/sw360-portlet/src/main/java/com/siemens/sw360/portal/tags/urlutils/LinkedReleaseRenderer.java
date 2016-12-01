/*
 * Copyright Siemens AG, 2013-2016. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.sw360.portal.tags.urlutils;

import org.eclipse.sw360.datahandler.thrift.ThriftClients;
import org.eclipse.sw360.datahandler.thrift.components.ComponentService;
import org.eclipse.sw360.datahandler.thrift.components.Release;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.datahandler.thrift.users.UserService;
import org.apache.thrift.TException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Helper class to render linked Releases
 *
 * @author birgit.heydenreich@tngtech.com
 */
public class LinkedReleaseRenderer {

    private StringBuilder display;
    private String tableClasses;
    private String idPrefix;
    private String userEmail;

    public LinkedReleaseRenderer(StringBuilder display, String tableClasses, String idPrefix, String userEmail) {
        this.display = display;
        this.tableClasses = tableClasses;
        this.idPrefix = idPrefix;
        this.userEmail = userEmail;
    }


    public <T> void renderReleaseLinkList(StringBuilder display, Map<String, T> releaseRelationshipMap, Set<String> releaseIds, String msg) {
        if (releaseIds.isEmpty()) return;


        StringBuilder candidate = new StringBuilder();
        try {
            UserService.Iface userClient = new ThriftClients().makeUserClient();
            User user = userClient.getByEmail(userEmail);
            ComponentService.Iface componentClient = new ThriftClients().makeComponentClient();

            for (Release release : componentClient.getReleasesById(releaseIds, user)) {
                candidate.append(String.format("<tr><td>%s</td><td>%s</td></tr>", release.getName(), releaseRelationshipMap.get(release.getId())));
            }

        } catch (TException ignored) {
        }

        String tableContent = candidate.toString();
        if (!tableContent.isEmpty()) {

            display.append(String.format("<table class=\"%s\" id=\"%s%s\" >", tableClasses, idPrefix, msg));
            display.append(String.format("<thead><tr><th colspan=\"2\">%s</th></tr><tr><th>Release name</th><th>Release relationship</th></tr></thead><tbody>", msg));
            display.append(tableContent);
            display.append("</tbody></table>");
        }
    }

    public <T> void renderReleaseLinkListCompare(StringBuilder display, Map<String,T> oldReleaseRelationshipMap, Map<String, T> deleteReleaseRelationshipMap, Map<String, T> updateReleaseRelationshipMap, Set<String> releaseIds) {
        if (releaseIds.isEmpty()) return;


        StringBuilder candidate = new StringBuilder();
        try {
            UserService.Iface userClient = new ThriftClients().makeUserClient();
            User user = userClient.getByEmail(userEmail);
            ComponentService.Iface componentClient = new ThriftClients().makeComponentClient();

            final HashSet<String> changedIds = new HashSet<>();

            for (String releaseId : releaseIds) {
                T oldReleaseRelationship = oldReleaseRelationshipMap.get(releaseId);
                T updateReleaseRelationship = updateReleaseRelationshipMap.get(releaseId);

                if (!oldReleaseRelationship.equals(updateReleaseRelationship)) {
                    changedIds.add(releaseId);
                }
            }

            for (Release release : componentClient.getReleasesById(changedIds, user)) {
                String releaseId = release.getId();
                T oldReleaseRelationship = oldReleaseRelationshipMap.get(releaseId);
                T deleteReleaseRelationship = deleteReleaseRelationshipMap.get(releaseId);
                T updateReleaseRelationship = updateReleaseRelationshipMap.get(releaseId);
                candidate.append(String.format("<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>", release.getName(), oldReleaseRelationship, deleteReleaseRelationship, updateReleaseRelationship));
            }

        } catch (TException ignored) {
        }

        String tableContent = candidate.toString();
        if (!tableContent.isEmpty()) {
            display.append(String.format("<table class=\"%s\" id=\"%sUpdated\" >", tableClasses, idPrefix));
            display.append("<thead><tr><th colspan=\"4\">Updated Release Links</th></tr><tr><th>Release name</th><th>Current Release relationship</th><th>Deleted Release relationship</th><th>Suggested release relationship</th></tr></thead><tbody>");
            display.append(tableContent);
            display.append("</tbody></table>");
        }
    }
}
