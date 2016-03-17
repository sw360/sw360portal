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
import com.siemens.sw360.datahandler.thrift.components.*;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.thrift.users.UserService;
import com.siemens.sw360.datahandler.thrift.vendors.Vendor;
import org.apache.thrift.TException;
import org.apache.thrift.meta_data.FieldMetaData;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.siemens.sw360.portal.tags.TagUtils.*;
import static com.siemens.sw360.portal.tags.TagUtils.SUGGESTED_VAL;

/**
 * Display the fields that have changed from old to update
 *
 * @author Johannes.Najjar@tngtech.com
 */
public class CompareRelease extends NameSpaceAwareTag {
    private Release old;
    private Release update;
    private String tableClasses = "";
    private String idPrefix = "";

    public void setOld(Release old) {
        this.old = old;
    }

    public void setUpdate(Release update) {
        this.update = update;
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

        if(old==null || update==null ) {
            return SKIP_BODY;
        }


        try{

            for (Release._Fields field : Release._Fields.values()) {
                switch (field) {
                    //ignored Fields
                    case ID:
                    case REVISION:
                    case TYPE:
                    case ATTACHMENTS:
                    case CREATED_BY:
                    case CREATED_ON:
                    case SUBSCRIBERS:
                    case COMPONENT_ID:
                    case VENDOR_ID:
                    case PERMISSIONS:
                    case RELEASE_ID_TO_RELATIONSHIP:
                    case MAIN_LICENSE_IDS:
                    case FOSSOLOGY_ID:
                    case CLEARING_TEAM_TO_FOSSOLOGY_STATUS:
                    case DOCUMENT_STATE:
                        break;

                    case REPOSITORY:
                        renderRepository(display);
                        break;
                    case CLEARING_INFORMATION:
                        renderClearingInformation(display);
                        break;
                    case VENDOR:
                        renderVendor(display);
                        break;
                    default:
                        FieldMetaData fieldMetaData = Release.metaDataMap.get(field);
                        displaySimpleField(display, old, update, field, fieldMetaData, "");
                }
            }

            String renderString = display.toString();

            if(Strings.isNullOrEmpty(renderString)) {
                renderString="<h4> No changes in basic fields </h4>";
            } else {
                renderString = String.format("<table class=\"%s\" id=\"%schanges\" >", tableClasses, idPrefix)
                        + "<thead><tr><th colspan=\"3\"> Changes for Basic fields</th></tr>"
                        + String.format("<tr><th>%s</th><th>%s</th><th>%s</th></tr></thead><tbody>", FIELD_NAME, CURRENT_VAL, SUGGESTED_VAL)
                        + renderString + "</tbody></table>";
            }

            StringBuilder releaseUsageDisplay = new StringBuilder();
            renderReleaseIdToUsage(releaseUsageDisplay);

            jspWriter.print(renderString + releaseUsageDisplay.toString());

        } catch (Exception e) {
            throw new JspException(e);
        }
        return SKIP_BODY;
    }



    private void renderRepository(StringBuilder display) {
        if (!ensureSomethingTodoAndNoNullRepository()) {

            for (Repository._Fields repositoryField : Repository._Fields.values()) {
                FieldMetaData fieldMetaData = Repository.metaDataMap.get(repositoryField);
                displaySimpleField(display, old.getRepository(), update.getRepository(), repositoryField,
                        fieldMetaData, Release._Fields.REPOSITORY.getFieldName());
            }
        }
    }

    private boolean ensureSomethingTodoAndNoNullRepository() {
        if (!old.isSetRepository() && !update.isSetRepository()) {
            return true;
        }

        if (!old.isSetRepository()) {
            old.setRepository(new Repository());
        }

        if (!update.isSetRepository()) {
            update.setRepository(new Repository());
        }
        return false;
    }

    private void renderClearingInformation(StringBuilder display) {
        if (ensureSomethingTodoAndNoNullClearingInformation()) {

            for (ClearingInformation._Fields clearingInformationField : ClearingInformation._Fields.values()) {
                FieldMetaData fieldMetaData = ClearingInformation.metaDataMap.get(clearingInformationField);
                displaySimpleField(display, old.getClearingInformation(), update.getClearingInformation(),
                        clearingInformationField, fieldMetaData, Release._Fields.CLEARING_INFORMATION.getFieldName());
            }
        }
    }

    private boolean ensureSomethingTodoAndNoNullClearingInformation() {
        if (!old.isSetClearingInformation() && !update.isSetClearingInformation()) {
            return false;
        }

        if (!old.isSetClearingInformation()) {
            old.setClearingInformation(new ClearingInformation());
        }

        if (!update.isSetClearingInformation()) {
            update.setClearingInformation(new ClearingInformation());
        }
        return true;
    }

    private void renderVendor(StringBuilder display) {
        if (!ensureSomethingTodoAndNoNullVendor()) {
            // We only need the full name
            displaySimpleField(display, old.getVendor(), update.getVendor(),
                    Vendor._Fields.FULLNAME, Vendor.metaDataMap.get(Vendor._Fields.FULLNAME), Release._Fields.VENDOR.getFieldName());
        }
    }

    private boolean ensureSomethingTodoAndNoNullVendor() {
        if (!old.isSetVendor() && !update.isSetVendor()) {
            return true;
        }

        if (!old.isSetVendor()) {
            old.setVendor(new Vendor().setFullname(NOT_SET).setShortname(NOT_SET).setUrl(NOT_SET).setId(update.getVendorId()));
        }

        if (!update.isSetVendor()) {
            update.setVendor(new Vendor().setFullname(NOT_SET).setShortname(NOT_SET).setUrl(NOT_SET).setId(old.getVendorId()));
        }
        return false;
    }

    private void renderReleaseIdToUsage(StringBuilder display) {
        if (ensureSomethingTodoAndNoNullReleaseIdUsage()) {
            Map<String, ReleaseRelationship> oldReleaseIdToUsage = old.getReleaseIdToRelationship();
            Map<String, ReleaseRelationship> updateReleaseIdToUsage = update.getReleaseIdToRelationship();
            Sets.SetView<String> removedReleaseIds = Sets.difference(oldReleaseIdToUsage.keySet(), updateReleaseIdToUsage.keySet());
            Sets.SetView<String> addedReleaseIds = Sets.difference(updateReleaseIdToUsage.keySet(), oldReleaseIdToUsage.keySet());
            Sets.SetView<String> commonReleaseIds = Sets.intersection(updateReleaseIdToUsage.keySet(), oldReleaseIdToUsage.keySet());


            renderReleaseLinkList(display, oldReleaseIdToUsage, removedReleaseIds, "Removed Release links");
            renderReleaseLinkList(display, updateReleaseIdToUsage, addedReleaseIds, "Added Release links");
            renderReleaseLinkListCompare(display, oldReleaseIdToUsage, updateReleaseIdToUsage, commonReleaseIds);
        }
    }

    private boolean ensureSomethingTodoAndNoNullReleaseIdUsage() {

        if (!old.isSetReleaseIdToRelationship() && !update.isSetReleaseIdToRelationship()) {
            return false;
        }
        if (!old.isSetReleaseIdToRelationship()) {
            old.setReleaseIdToRelationship(Collections.<String, ReleaseRelationship>emptyMap());
        }
        if (!update.isSetReleaseIdToRelationship()) {
            update.setReleaseIdToRelationship(Collections.<String, ReleaseRelationship>emptyMap());
        }

        return true;
    }

    private void renderReleaseLinkList(StringBuilder display, Map<String, ReleaseRelationship> releaseRelationshipMap, Set<String> releaseIds, String msg) {
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
            display.append(String.format("<thead><tr><th colspan=\"2\">%s</th></tr><tr><th>Release name</th><th>Release relationship</th></tr></thead><tbody>", msg));
            display.append(tableContent);
            display.append("</tbody></table>");
        }
    }

    private void renderReleaseLinkListCompare(StringBuilder display, Map<String, ReleaseRelationship> oldReleaseRelationshipMap, Map<String, ReleaseRelationship> updateReleaseRelationshipMap, Set<String> releaseIds) {
        if (releaseIds.isEmpty()) return;


        StringBuilder candidate = new StringBuilder();
        try {
            UserService.Iface userClient = new ThriftClients().makeUserClient();
            User user = userClient.getByEmail(old.getCreatedBy());
            ComponentService.Iface componentClient = new ThriftClients().makeComponentClient();

            final HashSet<String> changedIds = new HashSet<>();

            for (String releaseId : releaseIds) {
                ReleaseRelationship oldReleaseRelationship = oldReleaseRelationshipMap.get(releaseId);
                ReleaseRelationship updateReleaseRelationship = updateReleaseRelationshipMap.get(releaseId);

                if (!oldReleaseRelationship.equals(updateReleaseRelationship)) {
                    changedIds.add(releaseId);
                }
            }

            for (Release release : componentClient.getReleasesById(changedIds, user)) {
                String releaseId = release.getId();
                ReleaseRelationship oldReleaseRelationship = oldReleaseRelationshipMap.get(releaseId);
                ReleaseRelationship updateReleaseRelationship = updateReleaseRelationshipMap.get(releaseId);
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
