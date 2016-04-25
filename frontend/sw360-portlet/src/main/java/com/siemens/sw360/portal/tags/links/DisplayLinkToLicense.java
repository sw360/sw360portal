/*
 * Copyright Siemens AG, 2015-2016. Part of the SW360 Portal Project.
 * With contributions by Bosch Software Innovations GmbH, 2016.
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

import com.siemens.sw360.portal.common.PortalConstants;
import com.siemens.sw360.portal.common.page.PortletDefaultPage;
import com.siemens.sw360.portal.portlets.LinkToPortletConfiguration;

import javax.servlet.jsp.JspException;

import static com.siemens.sw360.portal.tags.urlutils.UrlWriterImpl.renderUrl;

/**
 * @author daniele.fognini@tngtech.com
 */
public class DisplayLinkToLicense extends DisplayLinkAbstract {
    private String licenseId;
    private PortletDefaultPage page = PortletDefaultPage.DETAIL;
    private Boolean showName = true;

    public void setLicenseId(String licenseId) {
        this.licenseId = licenseId;
    }
    public void setScopeGroupId(Long scopeGroupId) {
        if(scopeGroupId != null && scopeGroupId.longValue() != 0) {
            this.scopeGroupId = scopeGroupId;
        }
    }

    @Override
    protected String getTextDisplay() {
        return licenseId;
    }

    @Override
    protected void writeUrl() throws JspException {
        renderUrl(pageContext)
                .toPortlet(LinkToPortletConfiguration.LICENSES, scopeGroupId)
                .toPage(page)
                .withParam(PortalConstants.LICENSE_ID, licenseId)
                .writeUrlToJspWriter();
    }
}
