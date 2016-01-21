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

import com.google.common.base.Strings;
import com.siemens.sw360.datahandler.thrift.ThriftClients;
import com.siemens.sw360.datahandler.thrift.vendors.Vendor;
import com.siemens.sw360.datahandler.thrift.vendors.VendorService;
import org.apache.thrift.TException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * This displays a user in Edit mode
 *
 * @author Johannes.Najjar@tngtech.com
 */
public class DisplayVendorEdit extends NameSpaceAwareTag {

    private String id;
    private String vendorId="";
    private Vendor vendor=null;
    private String onclick = "";
    private String namespace;
    private Boolean displayLabel=true;
    public void setId(String id) {
        this.id = id;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }
    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public void setOnclick(String onclick) {
        this.onclick = onclick;
    }

    public int doStartTag() throws JspException {

        JspWriter jspWriter = pageContext.getOut();

        namespace = getNamespace();
        StringBuilder display = new StringBuilder();
        try {

            if (vendor==null && !Strings.isNullOrEmpty(vendorId)) {
                VendorService.Iface client;
                try {
                    client = new ThriftClients().makeVendorClient();
                    vendor = client.getByID(vendorId);
                } catch (TException ignored) {
                }
            }

            if (vendor != null) {
                printFullVendor(display, vendor);
            } else {
                printEmptyVendor(display);
            }

            jspWriter.print(display.toString());
        } catch (Exception e) {
            throw new JspException(e);
        }
        return SKIP_BODY;
    }

    private void printEmptyVendor(StringBuilder display) {
        printLabel(display);
        display.append(String.format("<input type=\"hidden\" readonly=\"\" value=\"\"  id=\"%s\" name=\"%s%s\"/>", id, namespace, id))
                .append(String.format("<input type=\"text\" readonly=\"\" class=\"clickable\" placeholder=\"Click to set vendor\" id=\"%sDisplay\" onclick=\"%s\" />", id, onclick));
    }

    private void printLabel(StringBuilder display) {
        if(displayLabel) {
            display.append(String.format("<label class=\"textlabel stackedLabel\" for=\"%sDisplay\">Vendor</label>", id));
        }
    }

    private void printFullVendor(StringBuilder display, Vendor vendor) {
        printLabel(display);
        display.append(String.format("<input type=\"hidden\" readonly=\"\" value=\"%s\"  id=\"%s\" name=\"%s%s\"/>", vendor.getId(), id, namespace, id))
                .append(String.format("<input type=\"text\" readonly=\"\" class=\"clickable\" value=\"%s\" id=\"%sDisplay\" onclick=\"%s\" />", vendor.getFullname(), id, onclick));
    }

    public void setDisplayLabel(Boolean displayLabel) {
        this.displayLabel = displayLabel;
    }
}
