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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.siemens.sw360.datahandler.thrift.ThriftClients;
import com.siemens.sw360.datahandler.thrift.licenses.License;
import com.siemens.sw360.datahandler.thrift.licenses.LicenseService;
import com.siemens.sw360.portal.users.UserCacheHolder;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.collect.Collections2.transform;
import static com.siemens.sw360.datahandler.thrift.ThriftUtils.extractField;
import static com.siemens.sw360.datahandler.thrift.ThriftUtils.extractId;

/**
 * This displays a user in Edit mode
 *
 * @author Johannes.Najjar@tngtech.com
 * @author daniele.fognini@tngtech.com
 */
public class DisplayLicensesEdit extends NameSpaceAwareTag {

    private String id;
    private Set<String> licenseIds = new HashSet<>();
    private String userEmail = null;
    private String namespace;
    private String onclick = "";

    public void setId(String id) {
        this.id = id;
        onclick = String.format("showSetLicensesDialog('%s')", id);
    }

    public void setLicenseIds(Set<String> licenseIds) {
        this.licenseIds = licenseIds;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public int doStartTag() throws JspException {
        JspWriter jspWriter = pageContext.getOut();

        namespace = getNamespace();
        StringBuilder display = new StringBuilder();
        try {

            if (licenseIds != null && !licenseIds.isEmpty()) {
                LicenseService.Iface licenseClient = new ThriftClients().makeLicenseClient();
                String department = UserCacheHolder.getUserFromEmail(userEmail).getDepartment();

                printLicenses(display, licenseClient.getByIds(licenseIds,department));

            } else {
                printEmptyLicenses(display);
            }

            jspWriter.print(display.toString());
        } catch (Exception e) {
            throw new JspException(e);
        }
        return SKIP_BODY;
    }

    private void printEmptyLicenses(StringBuilder display) {
        display.append(String.format("<label class=\"textlabel stackedLabel\" for=\"%sDisplay\">Licenses</label>", id))
                .append(String.format("<input type=\"hidden\" readonly=\"\" value=\"\"  id=\"%s\" name=\"%s%s\"/>", id, namespace, id))
                .append(String.format("<input class=\"clickable\" type=\"text\" readonly=\"\" placeholder=\"Click to set Licenses\" id=\"%sDisplay\" onclick=\"%s\" />", id, onclick));
    }

    private void printLicenses(StringBuilder display, Collection<License> licenses) {
        Joiner commaJoiner = Joiner.on(", ");
        Function<License, String> nameExtract = extractField(License._Fields.FULLNAME, String.class);

        String licenseIdsStr = commaJoiner.join(transform(licenses, extractId()));
        String licenseNamesStr = commaJoiner.join(transform(licenses, nameExtract));

        display.append(String.format("<label class=\"textlabel stackedLabel\" for=\"%sDisplay\">Licenses</label>", id))
                .append(String.format("<input type=\"hidden\" readonly=\"\" value=\"%s\" id=\"%s\" name=\"%s%s\"/>", licenseIdsStr, id, namespace, id))
                .append(String.format("<input class=\"clickable\" type=\"text\" readonly=\"\" value=\"%s\" id=\"%sDisplay\" onclick=\"%s\" />", licenseNamesStr, id, onclick));
    }
}
