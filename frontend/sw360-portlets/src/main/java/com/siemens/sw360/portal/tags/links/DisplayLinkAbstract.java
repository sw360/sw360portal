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
package com.siemens.sw360.portal.tags.links;

import com.liferay.portal.kernel.servlet.taglib.TagSupport;
import com.siemens.sw360.portal.tags.OutTag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;

/**
 * @author daniele.fognini@tngtech.com
 */
public abstract class DisplayLinkAbstract extends TagSupport {
    public Boolean bare = false;

    @Override
    public int doStartTag() throws JspException {
        try {
            JspWriter jspWriter = pageContext.getOut();
            if (!bare) jspWriter.write("<a href='");
            writeUrl();
            if (!bare) jspWriter.write("'>");

            String value = getTextDisplay();
            if (value != null) {
                OutTag outTag = new OutTag();
                outTag.setPageContext(pageContext);
                outTag.setValue(value);

                outTag.doStartTag();
                outTag.doEndTag();
            }
        } catch (IOException e) {
            throw new JspException("cannot write", e);
        }

        return EVAL_BODY_INCLUDE;
    }

    @Override
    public int doEndTag() throws JspException {
        try {
            if (!bare) {
                JspWriter jspWriter = pageContext.getOut();
                jspWriter.write("</a>");
            }
        } catch (IOException e) {
            throw new JspException("cannot write", e);
        }
        return super.doEndTag();
    }

    public void setBare(Boolean bare) {
        this.bare = bare;
    }

    protected abstract void writeUrl() throws JspException;

    protected abstract String getTextDisplay();
}
