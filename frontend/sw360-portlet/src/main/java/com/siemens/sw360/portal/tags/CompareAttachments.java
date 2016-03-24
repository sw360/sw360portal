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
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.siemens.sw360.datahandler.thrift.attachments.Attachment;
import com.siemens.sw360.datahandler.thrift.components.Release;
import org.apache.thrift.meta_data.FieldMetaData;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.siemens.sw360.datahandler.common.CommonUtils.nullToEmptySet;
import static com.siemens.sw360.portal.tags.TagUtils.*;

/**
 * Display the fields that have changed as described by additions and deletions
 *
 * @author Daniele.Fognini@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 */
public class CompareAttachments extends NameSpaceAwareTag {
    public static final List<Attachment._Fields> RELEVANT_FIELDS = FluentIterable
            .from(copyOf(Attachment._Fields.values()))
            .filter(CompareAttachments::isFieldRelevant)
            .toList();

    private Set<Attachment> actual;
    private Set<Attachment> additions;
    private Set<Attachment> deletions;
    private String tableClasses = "";
    private String idPrefix = "";

    public void setActual(Set<Attachment> actual) {
        this.actual = nullToEmptySet(actual);
    }

    public void setAdditions(Set<Attachment> additions) {
        this.additions = nullToEmptySet(additions);
    }

    public void setDeletions(Set<Attachment> deletions) {
        this.deletions = nullToEmptySet(deletions);
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

        try {
            renderAttachments(display, actual, additions, deletions);

            String renderString = display.toString();

            if (Strings.isNullOrEmpty(renderString)) {
                renderString = "<h4> No changes in attachments </h4>";
            }

            jspWriter.print(renderString);
        } catch (Exception e) {
            throw new JspException(e);
        }
        return SKIP_BODY;
    }

    private void renderAttachments(StringBuilder display, Set<Attachment> currentAttachments, Set<Attachment> addedAttachments , Set<Attachment> deletedAttachments) {

        Map<String, Attachment> currentAttachmentsById = getAttachmentsById(currentAttachments);
        Map<String, Attachment> addedAttachmentsById = getAttachmentsById(addedAttachments);
        Map<String, Attachment> deletedAttachmentsById = getAttachmentsById(deletedAttachments);

        Set<String> currentAttachmentIds = currentAttachmentsById.keySet();
        Set<String> addedAttachmentIds = addedAttachmentsById.keySet();
        Set<String> deletedAttachmentIds = deletedAttachmentsById.keySet();
        Set<String> commonAttachmentIds = Sets.intersection(deletedAttachmentIds, addedAttachmentIds);

        addedAttachmentIds = Sets.difference(addedAttachmentIds, commonAttachmentIds);
        deletedAttachmentIds = Sets.difference(deletedAttachmentIds, commonAttachmentIds);
        deletedAttachmentIds = Sets.intersection(deletedAttachmentIds, currentAttachmentIds);//remove what was deleted already in the database

        renderAttachmentList(display, currentAttachmentsById, deletedAttachmentIds, "Deleted");
        renderAttachmentList(display, addedAttachmentsById, addedAttachmentIds, "Added");
        renderAttachmentComparison(display, currentAttachmentsById, deletedAttachmentsById, addedAttachmentsById, commonAttachmentIds);
    }

    private void renderAttachmentList(StringBuilder display, Map<String, Attachment> allAttachments, Set<String> attachmentIds, String msg) {
        if (attachmentIds.isEmpty()) return;
        display.append(String.format("<table class=\"%s\" id=\"%s%s\" >", tableClasses, idPrefix, msg));

        renderAttachmentRowHeader(display, msg);
        for (String attachmentId : attachmentIds) {
            renderAttachmentRow(display, allAttachments.get(attachmentId));
        }

        display.append("</table>");
    }

    private static void renderAttachmentRowHeader(StringBuilder display, String msg) {

        display.append(String.format("<thead><tr><th colspan=\"%d\"> %s Attachments: </th></tr><tr>", RELEVANT_FIELDS.size(), msg));
        for (Attachment._Fields field : RELEVANT_FIELDS) {
            display.append(String.format("<th>%s</th>", field.getFieldName()));
        }
        display.append("</tr></thead>");
    }

    private static void renderAttachmentRow(StringBuilder display, Attachment attachment) {
        display.append("<tr>");
        for (Attachment._Fields field : RELEVANT_FIELDS) {

            FieldMetaData fieldMetaData = Attachment.metaDataMap.get(field);
            Object fieldValue = attachment.getFieldValue(field);
            display.append(String.format("<td>%s</td>", getDisplayString(fieldMetaData, fieldValue)));

        }
        display.append("</tr>");
    }

    private void renderAttachmentComparison(StringBuilder display, Map<String, Attachment> currentAttachmentsById, Map<String, Attachment> deletedAttachmentsById, Map<String, Attachment> addedAttachmentsById, Set<String> commonAttachmentIds) {
        if (commonAttachmentIds.isEmpty()) return;

        StringBuilder candidate = new StringBuilder();
        for (String commonAttachmentId : commonAttachmentIds) {
            renderCompareAttachment(candidate,
                    currentAttachmentsById.get(commonAttachmentId),
                    deletedAttachmentsById.get(commonAttachmentId),
                    addedAttachmentsById.get(commonAttachmentId));
        }
        String changedAttachmentTable = candidate.toString();
        if (!changedAttachmentTable.isEmpty()) {
            display.append("<h4>Changed Attachments</h4>");
            display.append(changedAttachmentTable);
        }
    }

    private void renderCompareAttachment(StringBuilder display, Attachment old, Attachment deleted, Attachment added) {

        if (old.equals(added)) return;
        display.append(String.format("<table class=\"%s\" id=\"%schanges%s\" >", tableClasses, idPrefix, old.getAttachmentContentId()));
        display.append(String.format("<thead><tr><th colspan=\"4\"> Changes for Attachment %s </th></tr>", old.getFilename()));
        display.append(String.format("<tr><th>%s</th><th>%s</th><th>%s</th><th>%s</th></tr></thead><tbody>", FIELD_NAME, CURRENT_VAL, DELETED_VAL, SUGGESTED_VAL));

        for (Attachment._Fields field : RELEVANT_FIELDS) {

            FieldMetaData fieldMetaData = Attachment.metaDataMap.get(field);
            displaySimpleFieldOrSet(display, old, added, deleted, field,
                    fieldMetaData, Release._Fields.ATTACHMENTS.getFieldName());
        }
        display.append("</tbody></table>");
    }


    private static Map<String, Attachment> getAttachmentsById(Set<Attachment> currentAttachments) {
        return Maps.uniqueIndex(currentAttachments, Attachment::getAttachmentContentId);
    }

    private static boolean isFieldRelevant(Attachment._Fields field) {
        switch (field) {
            //ignored Fields
            case ATTACHMENT_CONTENT_ID:
                return false;
            case UPLOAD_HISTORY:
                return false;
            default:
                return true;
        }
    }
}
