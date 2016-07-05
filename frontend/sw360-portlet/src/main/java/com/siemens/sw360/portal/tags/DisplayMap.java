/*
 * Copyright (c) Bosch Software Innovations GmbH 2016.
 * Part of the SW360 Portal Project.
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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.util.Map;

/**
 * This displays a map
 *
 * @author birgit.heydenreich@tngtech.com
 */
public class DisplayMap extends SimpleTagSupport {

    private Map<String, String> value;
    private Map<String, String> autoFillValue;

    public void setValue(Map<String, String> value) {
        this.value = value;
    }

    public void setAutoFillValue(Map<String, String> autoFillValue) {
        this.autoFillValue = autoFillValue;
    }

    public void doTag() throws JspException, IOException {
        Map<String, String> fullValue;

        if (value == null) {
            fullValue = autoFillValue;
        } else {
            fullValue = value;
        }

        if (null != fullValue && !fullValue.isEmpty()) {
            String result = getMapAsString(fullValue);
            getJspContext().getOut().print(result);
        }
    }

    public static String getMapAsString(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("<ul style=\"list-style-type: none; padding:0; margin:0;\">");
        map.entrySet().stream().forEach(e -> sb.append(
                "<li><b>" + e.getKey() + "</b> " + e.getValue() + "</li>"
        ));
        sb.append("</ul>");
        return sb.toString();
    }
}
