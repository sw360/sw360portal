/*
 * Copyright Siemens AG, 2015. Part of the SW360 Portal Project.
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
package com.siemens.sw360.portal.tags.links;

import com.siemens.sw360.datahandler.thrift.licenses.License;
import com.siemens.sw360.portal.common.PortalConstants;
import com.siemens.sw360.portal.common.page.PortletDefaultPage;
import com.siemens.sw360.portal.portlets.PortletProperties;

import javax.servlet.jsp.JspException;

import static com.siemens.sw360.portal.tags.urlutils.UrlWriterImpl.renderUrl;

/**
 * @author daniele.fognini@tngtech.com
 */
public class DisplayLinkToLicense extends DisplayLinkAbstract {
    private License license;
    private PortletDefaultPage page = PortletDefaultPage.DETAIL;
    private Boolean showName = true;

    public void setLicense(License license) {
        this.license = license;
    }

    public void setPage(PortletDefaultPage page) {
        this.page = page;
    }

    public void setShowName(Boolean showName) {
        this.showName = showName;
    }

    @Override
    protected String getTextDisplay() {
        return showName ? license.getShortname() : null;
    }

    @Override
    protected void writeUrl() throws JspException {
        renderUrl(pageContext)
                .toPortlet(PortletProperties.LICENSES)
                .toPage(page)
                .withParam(PortalConstants.LICENSE_ID, license.getId())
                .writeUrlToJspWriter();
    }
}
