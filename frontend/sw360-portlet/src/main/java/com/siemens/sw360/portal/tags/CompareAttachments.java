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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
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
 * Display the fields that have changed from old to update
 *
 * @author Daniele.Fognini@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 */
public class CompareAttachments extends NameSpaceAwareTag {
    public static final List<Attachment._Fields> RELEVANT_FIELDS = FluentIterable
            .from(copyOf(Attachment._Fields.values()))
            .filter(new Predicate<Attachment._Fields>() {
                @Override
                public boolean apply(Attachment._Fields input) {
                    return isFieldRelevant(input);
                }
            }).toList();

    private Set<Attachment> old;
    private Set<Attachment> update;
    private String tableClasses = "";
    private String idPrefix = "";

    public void setOld(Set<Attachment> old) {
        this.old = nullToEmptySet(old);
    }

    public void setUpdate(Set<Attachment> update) {
        this.update = nullToEmptySet(update);
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
            renderAttachments(display, old, update);

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

    private void renderAttachments(StringBuilder display, Set<Attachment> currentAttachments, Set<Attachment> updateAttachments) {

        Map<String, Attachment> currentAttachmentById = getAttachmentsById(currentAttachments);
        Map<String, Attachment> updateAttachmentById = getAttachmentsById(updateAttachments);

        Set<String> currentAttachmentIds = currentAttachmentById.keySet();
        Set<String> updateAttachmentIds = updateAttachmentById.keySet();

        Set<String> deletedAttachmentIds = Sets.difference(currentAttachmentIds, updateAttachmentIds);
        Set<String> addedAttachmentIds = Sets.difference(updateAttachmentIds, currentAttachmentIds);

        Set<String> commonAttachmentIds = Sets.intersection(currentAttachmentIds, updateAttachmentIds);

        renderAttachmentList(display, currentAttachmentById, deletedAttachmentIds, "Deleted");
        renderAttachmentList(display, updateAttachmentById, addedAttachmentIds, "Added");
        renderAttachmentComparison(display, currentAttachmentById, updateAttachmentById, commonAttachmentIds);


    }

    private void renderAttachmentList(StringBuilder display, Map<String, Attachment> allAttachments, Set<String> attachmentIds, String msg) {
        if (attachmentIds.isEmpty()) return;
        display.append(String.format("<table class=\"%s\" id=\"%s%s\" >", tableClasses, idPrefix, msg));

        renderAttachmentRowHeader(display, msg);
        for (String deletedAttachmentId : attachmentIds) {
            renderAttachmentRow(display, allAttachments.get(deletedAttachmentId));
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

    private void renderAttachmentComparison(StringBuilder display, Map<String, Attachment> currentAttachmentById, Map<String, Attachment> updateAttachmentById, Set<String> commonAttachmentIds) {
        if (commonAttachmentIds.isEmpty()) return;

        StringBuilder candidate = new StringBuilder();
        for (String commonAttachmentId : commonAttachmentIds) {
            renderCompareAttachment(candidate, currentAttachmentById.get(commonAttachmentId), updateAttachmentById.get(commonAttachmentId));
        }
        String changedAttachmentTable = candidate.toString();
        if (!changedAttachmentTable.isEmpty()) {
            display.append("<h4>Changed Attachments</h4>");
            display.append(changedAttachmentTable);
        }
    }

    private void renderCompareAttachment(StringBuilder display, Attachment old, Attachment update) {

        if (old.equals(update)) return;
        display.append(String.format("<table class=\"%s\" id=\"%schanges%s\" >", tableClasses, idPrefix, old.getAttachmentContentId()));
        display.append(String.format("<thead><tr><th colspan=\"3\"> Changes for Attachment %s </th></tr>", old.getFilename()));
        display.append(String.format("<tr><th>%s</th><th>%s</th><th>%s</th></tr></thead><tbody>", FIELD_NAME, CURRENT_VAL, SUGGESTED_VAL));

        for (Attachment._Fields field : RELEVANT_FIELDS) {

            FieldMetaData fieldMetaData = Attachment.metaDataMap.get(field);
            displaySimpleField(display, old, update, field,
                    fieldMetaData, Release._Fields.ATTACHMENTS.getFieldName());
        }
        display.append("</tbody></table>");
    }


    private static Map<String, Attachment> getAttachmentsById(Set<Attachment> currentAttachments) {
        return Maps.uniqueIndex(currentAttachments, new Function<Attachment, String>() {
            @Override
            public String apply(Attachment input) {
                return input.getAttachmentContentId();
            }
        });
    }

    private static boolean isFieldRelevant(Attachment._Fields field) {
        switch (field) {
            //ignored Fields
            case ATTACHMENT_CONTENT_ID:
                return false;
            default:
                return true;
        }
    }
}
