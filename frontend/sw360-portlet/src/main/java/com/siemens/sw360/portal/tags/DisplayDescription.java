
/*
 * Copyright Siemens AG, 2013-2016. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.portal.tags;

import org.apache.commons.lang.StringUtils;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

import static java.util.regex.Matcher.quoteReplacement;

/**
 * This prepares the description for display
 *
 * @author birgit.heydenreich@tngtech.com
 * @author alex.borodin@evosoft.com
 */

public class DisplayDescription extends SimpleTagSupport {

    private String description;
    private String jsQuoting ="";
    private int maxChar=140;

    public void setDescription(String description) {
        this.description = description;
    }
    public void setMaxChar(int maxChar) {
        this.maxChar = maxChar;
    }
    public void setJsQuoting(String jsQuoting) {
        this.jsQuoting = jsQuoting;
    }

    public void doTag() throws JspException, IOException {
        abbreviateDescription();
        if (!"".equals(jsQuoting)){
            sanitizeDescriptionForJavascriptStringLiteral();
        }
        getJspContext().getOut().print(description);

    }

    private void sanitizeDescriptionForJavascriptStringLiteral() {
        description = description.replaceAll("[\r\n]+", "");
        if ("'".equals(jsQuoting)) {
            this.description = escapeInSingleQuote(description);
        } else if ("\"".equals(jsQuoting)) {
            this.description = escapeInDoubleQuote(description);
        }
    }

    private void abbreviateDescription() {
        if (maxChar > 4) {
            description = StringUtils.abbreviate(description, maxChar);
        }
    }

    private String escapeInDoubleQuote(String value) {
        return value.replaceAll("\"", quoteReplacement("\\\""));
    }

    private String escapeInSingleQuote(String value) {
        return value.replaceAll("'", quoteReplacement("\\\'"));
    }
}

