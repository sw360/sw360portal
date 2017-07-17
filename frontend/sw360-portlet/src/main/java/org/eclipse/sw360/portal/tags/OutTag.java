/*
 * Copyright Siemens AG, 2013-2016. Part of the SW360 Portal Project.
 *
 * SPDX-License-Identifier: EPL-1.0
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.portal.tags;

import org.apache.commons.lang.StringUtils;
import org.apache.taglibs.standard.tag.common.core.OutSupport;

import javax.servlet.jsp.JspException;

import java.io.IOException;

import static java.util.regex.Matcher.quoteReplacement;

/**
 * Util to display multiline strings also in javascript
 *
 * @author Daniele.Fognini@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 * @author alex.borodin@evosoft.com
 */
public class OutTag extends OutSupport {
    private String jsQuoting = null;
    private boolean stripNewlines = true;

    public OutTag() {
    }

    private Integer maxChar = -1;

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public int doStartTag() throws JspException {
        if (value instanceof String) {
            boolean abbreviated = false;
            String candidate = (String) this.value;
            String originalValue = candidate;
            if (maxChar > 4) {
                candidate = StringUtils.abbreviate(candidate, maxChar);
                if (!originalValue.equals(candidate)){
                    abbreviated = true;
                }
            }

            if (stripNewlines){
                candidate = candidate.replaceAll("[\r\n]+", " ");
            }
            if ("'".equals(jsQuoting)) {
                candidate = escapeInSingleQuote(candidate);
            } else if ("\"".equals(jsQuoting)) {
                candidate = escapeInDoubleQuote(candidate);
            }
            this.value = candidate;

            if (abbreviated) {
                try {
                    this.pageContext.getOut().write("<span title=\"" + escapeInDoubleQuote(originalValue) + "\">");
                    int i = super.doStartTag();
                    this.pageContext.getOut().write("</span>");
                    return i;
                } catch (IOException e) {
                    throw new JspException(e.toString(), e);
                }
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

    public void setStripNewlines(boolean stripNewlines) {
        this.stripNewlines = stripNewlines;
    }
}


