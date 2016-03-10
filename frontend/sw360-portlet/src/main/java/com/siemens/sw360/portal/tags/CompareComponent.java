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
import com.siemens.sw360.datahandler.thrift.components.Component;
import org.apache.thrift.meta_data.FieldMetaData;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import static com.siemens.sw360.portal.tags.TagUtils.*;

/**
 * Display the fields that have changed from old to update
 *
 * @author Johannes.Najjar@tngtech.com
 */
public class CompareComponent extends NameSpaceAwareTag {
    private Component old;
    private Component update;
    private String tableClasses = "";
    private String idPrefix = "";

    public void setUpdate(Component update) {
        this.update = update;
    }

    public void setOld(Component old) {
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

            for (Component._Fields field : Component._Fields.values()) {
                switch (field) {
                    //ignored Fields
                    case ID:
                    case REVISION:
                    case TYPE:
                    case ATTACHMENTS:
                    case SUBSCRIBERS:
                    case CREATED_BY:
                    case CREATED_ON:
                    case PERMISSIONS:
                    case RELEASES:
                    case RELEASE_IDS:
                    case MAIN_LICENSE_IDS:
                    case DOCUMENT_STATE:
                        break;

                    default:
                        FieldMetaData fieldMetaData = Component.metaDataMap.get(field);
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

            jspWriter.print(renderString);
        } catch (Exception e) {
            throw new JspException(e);
        }
        return SKIP_BODY;
    }
}
