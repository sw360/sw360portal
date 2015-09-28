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
package com.siemens.sw360.portal.tags.links;

import com.siemens.sw360.portal.common.PortalConstants;
import com.siemens.sw360.portal.tags.ContextAwareTag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import static com.siemens.sw360.portal.tags.urlutils.UrlWriterImpl.resourceUrl;
import static java.lang.String.format;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

/**
 * This displays a Download link for an attachment given its contentdId
 *
 * @author daniele.fognini@tngtech.com
 */
public class DisplayDownloadAttachment extends ContextAwareTag {
    private String id;
    private String name = "";

    @Override
    public int doStartTag() throws JspException {
        try {
            JspWriter jspWriter = pageContext.getOut();
            jspWriter.write("<a href='");

            resourceUrl(pageContext)
                    .withParam(PortalConstants.ACTION, PortalConstants.ATTACHMENT_DOWNLOAD)
                    .withParam(PortalConstants.ATTACHMENT_ID, id)
                    .writeUrlToJspWriter();

            jspWriter.write(format(
                    "'><img src='%s/images/downloadEnable.jpg' alt='Download%s' title='Download%s'/>",
                    getContext(), name, name
            ));
            jspWriter.write("</a>");
        } catch (Exception e) {
            throw new JspException(e);
        }

        return SKIP_BODY;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = escapeHtml(" " + name);
    }
}
