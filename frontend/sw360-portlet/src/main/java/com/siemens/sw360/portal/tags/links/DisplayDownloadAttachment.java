/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.portal.tags.links;

import com.siemens.sw360.portal.tags.ContextAwareTag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import static com.siemens.sw360.portal.tags.TagUtils.addDownloadLink;
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
            addDownloadLink(pageContext, jspWriter, name, id);
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
