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

import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.portal.common.PortalConstants;
import com.siemens.sw360.portal.common.page.PortletReleasePage;
import com.siemens.sw360.portal.portlets.PortletProperties;

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
                .toPortlet(PortletProperties.COMPONENTS)
                .toPage(page)
                .withParam(PortalConstants.RELEASE_ID, releaseId)
                .writeUrlToJspWriter();
    }
}
