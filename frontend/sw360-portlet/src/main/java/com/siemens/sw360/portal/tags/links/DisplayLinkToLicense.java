/*
 * Copyright Siemens AG, 2015-2016. Part of the SW360 Portal Project.
 * With contributions by Bosch Software Innovations GmbH, 2016.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.portal.tags.links;

import org.eclipse.sw360.portal.common.PortalConstants;
import org.eclipse.sw360.portal.common.page.PortletDefaultPage;
import org.eclipse.sw360.portal.portlets.LinkToPortletConfiguration;

import javax.servlet.jsp.JspException;

import static org.eclipse.sw360.portal.tags.urlutils.UrlWriterImpl.renderUrl;

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
