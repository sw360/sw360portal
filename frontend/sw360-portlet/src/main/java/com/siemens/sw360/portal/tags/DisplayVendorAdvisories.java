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

import com.siemens.sw360.datahandler.thrift.vulnerabilities.VendorAdvisory;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.util.Set;

/**
 * This displays a set of CVEReferences
 *
 * @author birgit.heydenreich@tngtech.com
 */
public class DisplayVendorAdvisories extends SimpleTagSupport {

    private Set<VendorAdvisory> value;
    private Set<VendorAdvisory> autoFillValue;

    public void setValue(Set<VendorAdvisory> value) {
        this.value = value;
    }
    public void setAutoFillValue(Set<VendorAdvisory> autoFillValue) {
        this.autoFillValue = autoFillValue;
    }

    public void doTag() throws JspException, IOException {
        Set<VendorAdvisory> fullValue;

        if (value == null)
            fullValue = autoFillValue;
        else {
            fullValue = value;
        }

        if (null != fullValue && !fullValue.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("<ul>");
            fullValue.stream().forEach(a -> sb.append("<li>"+toString(a)+"</li>"));
            sb.append("</ul>");
            getJspContext().getOut().print(sb.toString());
        }
    }

    private String toString(VendorAdvisory advisory){
        return "<b>vendor: </b>"+ advisory.getVendor()
                +", <b>name: </b>"+ advisory.getName()
                +", <b>url: </b>"+ advisory.getUrl()+"<br/>";
    }
}
