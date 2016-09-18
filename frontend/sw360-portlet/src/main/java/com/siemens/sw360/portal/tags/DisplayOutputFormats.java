/*
 * Copyright Bosch Software Innovations GmbH, 2016.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.portal.tags;

import com.siemens.sw360.datahandler.thrift.licenseinfo.OutputFormatInfo;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.util.Collection;

/**
 * @author birgit.heydenreich@tngtech.com
 */
public class DisplayOutputFormats extends SimpleTagSupport {

    private Collection<OutputFormatInfo> options;
    private String selected;

    public void setOptions(Collection<OutputFormatInfo> options) throws JspException {
        this.options = options;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }

    public void doTag() throws JspException, IOException {
            writeOptions(options);
    }

    private void writeOptions(Collection<OutputFormatInfo> options) throws IOException {
        JspWriter jspWriter = getJspContext().getOut();
        for (OutputFormatInfo outputOption : options) {
            String formattedOutputOption = outputOption.getDescription();
            jspWriter.write(String.format(
                    "<option value=\"%s\" class=\"textlabel stackedLabel\" \" +\n" +
                            "                            (selected ? \"selected=\\\"selected\\\" \" : \"\") +\n" +
                            "                            \">%s</option>",
                    outputOption.getGeneratorClassName(), formattedOutputOption));
        }
    }
}
