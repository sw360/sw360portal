/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.portal.tags;

import com.google.common.base.Strings;
import org.eclipse.sw360.datahandler.thrift.ThriftClients;
import org.eclipse.sw360.datahandler.thrift.vendors.Vendor;
import org.eclipse.sw360.datahandler.thrift.vendors.VendorService;
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
