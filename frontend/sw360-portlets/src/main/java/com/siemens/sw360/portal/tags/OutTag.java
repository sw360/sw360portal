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
package com.siemens.sw360.portal.tags;

import org.apache.commons.lang.StringUtils;
import org.apache.taglibs.standard.tag.common.core.OutSupport;

import javax.servlet.jsp.JspException;

import static java.util.regex.Matcher.quoteReplacement;

/**
 * Util to display multiline strings also in javascript
 *
 * @author Daniele.Fognini@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 */
public class OutTag extends OutSupport {
    private String jsQuoting = null;

    public OutTag() {
    }

    private Integer maxChar = -1;

    public void setValue(Object value) {
        if (value instanceof String) {

            String candidate = ((String) value);

            if (maxChar > 4) {
                candidate = StringUtils.abbreviate(candidate, maxChar);
            }

            this.value = candidate.replaceAll("[\r\n]+", " ");
        } else {
            this.value = value;
        }
    }

    @Override
    public int doStartTag() throws JspException {
        if (value instanceof String) {
            String value = (String) this.value; //TODO remove uneeded...
            if ("'".equals(jsQuoting)) {
                this.value = escapeInSingleQuote(value);
            } else if ("\"".equals(jsQuoting)) {
                this.value = escapeInDoubleQuote(value);
            }
        }
        return super.doStartTag();
    }

    //TODO remove unneeded...
    private String escapeInDoubleQuote(String value) {
        return value.replaceAll("\"", quoteReplacement("\\\""));
    }

    //TODO remove unneeded...
    protected String escapeInSingleQuote(String value) {
        return value.replaceAll("'", quoteReplacement("\\\'"));
    }

    public void setDefault(String def) {
        this.def = def;
    }

    public void setEscapeXml(boolean escapeXml) {
        this.escapeXml = escapeXml;
    }

    public void setMaxChar(Integer maxChar) {
        this.maxChar = maxChar;
    }

    public void setJsQuoting(String jsQuoting) {
        this.jsQuoting = jsQuoting;
    }
}


