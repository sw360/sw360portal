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

import com.siemens.sw360.datahandler.thrift.vulnerabilities.CVEReference;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This displays a set of CVEReferences
 *
 * @author birgit.heydenreich@tngtech.com
 */
public class DisplayCVEReferences extends SimpleTagSupport {

    private Set<CVEReference> value;
    private Set<CVEReference> autoFillValue;

    public void setValue(Set<CVEReference> value) {
        this.value = value;
    }
    public void setAutoFillValue(Set<CVEReference> autoFillValue) {
        this.autoFillValue = autoFillValue;
    }

    public void doTag() throws JspException, IOException {
        Set<CVEReference> fullValue;

        if (value == null) {
            fullValue = autoFillValue;
        } else {
            fullValue = value;
        }

        if (null != fullValue && !fullValue.isEmpty()) {
            String result = String.join(",", fullValue.stream().map(this::toString).collect(Collectors.toList()));
            getJspContext().getOut().print(result);
        }
    }

    private String toString(CVEReference reference){
        return "CVE-" + reference.getYear() + "-" + reference.getNumber();
    }
}
