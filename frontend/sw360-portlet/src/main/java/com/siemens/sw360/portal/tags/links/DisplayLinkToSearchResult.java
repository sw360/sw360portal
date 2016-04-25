/*
 * Copyright Siemens AG, 2015. Part of the SW360 Portal Project.
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

import com.siemens.sw360.datahandler.common.SW360Constants;
import com.siemens.sw360.datahandler.thrift.search.SearchResult;
import com.siemens.sw360.portal.common.PortalConstants;
import com.siemens.sw360.portal.common.page.PortletReleasePage;
import com.siemens.sw360.portal.portlets.LinkToPortletConfiguration;
import com.siemens.sw360.portal.tags.urlutils.UrlWriter;

import javax.servlet.jsp.JspException;

import static com.siemens.sw360.portal.tags.urlutils.UrlWriterImpl.renderUrl;

/**
 * @author daniele.fognini@tngtech.com
 */
public class DisplayLinkToSearchResult extends DisplayLinkAbstract {
    private SearchResult searchResult;

    public void setSearchResult(SearchResult searchResult) {
        this.searchResult = searchResult;
    }

    @Override
    protected String getTextDisplay() {
        return searchResult.getName();
    }

    @Override
    protected void writeUrl() throws JspException {
        String searchResultType = searchResult.getType();
        String searchResultId = searchResult.getId();
        UrlWriter writer;
        switch (searchResultType) {
            case SW360Constants.TYPE_RELEASE:
                writer = renderUrl(pageContext)
                        .toPortlet(LinkToPortletConfiguration.COMPONENTS, scopeGroupId)
                        .withParam(PortalConstants.RELEASE_ID, searchResultId);
                break;
            case SW360Constants.TYPE_PROJECT:
                writer =renderUrl(pageContext)
                    .toPortlet(LinkToPortletConfiguration.PROJECTS, scopeGroupId)
                    .withParam(PortalConstants.PROJECT_ID, searchResultId);
                break;
            case SW360Constants.TYPE_COMPONENT:
                writer =renderUrl(pageContext)
                    .toPortlet(LinkToPortletConfiguration.COMPONENTS, scopeGroupId)
                    .withParam(PortalConstants.COMPONENT_ID, searchResultId);
                break;
            case SW360Constants.TYPE_LICENSE:
                writer =renderUrl(pageContext)
                    .toPortlet(LinkToPortletConfiguration.LICENSES, scopeGroupId)
                    .withParam(PortalConstants.LICENSE_ID, searchResultId);
                break;
            default:
                throw new IllegalArgumentException("Unexpected searchResultType " + searchResultType);
        }
        writer.toPage(PortletReleasePage.DETAIL)
                .writeUrlToJspWriter();
    }
}
