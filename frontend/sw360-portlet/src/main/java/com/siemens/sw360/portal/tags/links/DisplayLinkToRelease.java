/*
 * Copyright Siemens AG, 2015. Part of the SW360 Portal Project.
 * With contributions by Bosch Software Innovations GmbH, 2016.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.portal.tags.links;

import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.portal.common.PortalConstants;
import com.siemens.sw360.portal.common.page.PortletReleasePage;
import com.siemens.sw360.portal.portlets.LinkToPortletConfiguration;

import javax.servlet.jsp.JspException;

import static com.siemens.sw360.datahandler.common.SW360Utils.printName;
import static com.siemens.sw360.portal.tags.urlutils.UrlWriterImpl.renderUrl;

/**
 * @author daniele.fognini@tngtech.com
 */
public class DisplayLinkToRelease extends DisplayLinkAbstract {
    private Release release;
    private PortletReleasePage page = PortletReleasePage.DETAIL;
    private Boolean showName = true;
    private String releaseId;

    public void setRelease(Release release) {
        this.release = release;
        releaseId=release.getId();
    }

    public void setPage(PortletReleasePage page) {
        this.page = page;
    }
    public void setReleaseId(String releaseId) {
        this.releaseId = releaseId;
        showName=false;
    }
    public void setShowName(Boolean showName) {
        this.showName = showName;
    }

    @Override
    protected String getTextDisplay() {
        return showName ? printName(release) : null;
    }

    @Override
    protected void writeUrl() throws JspException {
        renderUrl(pageContext)
                .toPortlet(LinkToPortletConfiguration.COMPONENTS, scopeGroupId)
                .toPage(page)
                .withParam(PortalConstants.RELEASE_ID, releaseId)
                .writeUrlToJspWriter();
    }
}
