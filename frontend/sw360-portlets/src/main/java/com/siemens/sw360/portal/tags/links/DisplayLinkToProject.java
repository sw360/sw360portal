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

import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.portal.common.PortalConstants;
import com.siemens.sw360.portal.common.page.PortletDefaultPage;
import com.siemens.sw360.portal.portlets.PortletProperties;

import javax.servlet.jsp.JspException;

import static com.siemens.sw360.datahandler.common.SW360Utils.printName;
import static com.siemens.sw360.portal.tags.urlutils.UrlWriterImpl.renderUrl;

/**
 * @author daniele.fognini@tngtech.com
 */
public class DisplayLinkToProject extends DisplayLinkAbstract {
    private Project project;
    private Boolean showName = true;
    private String projectId;
    public void setProject(Project project) {
        this.project = project;
        projectId=project.getId();
    }
    public void setProjectId(String projectId) {
        this.projectId = projectId;
        showName=false;
    }
    public void setShowName(Boolean showName) {
        this.showName = showName;
    }

    @Override
    protected String getTextDisplay() {
        return showName ? printName(project) : null;
    }

    @Override
    protected void writeUrl() throws JspException {
        renderUrl(pageContext)
                .toPortlet(PortletProperties.PROJECTS)
                .toPage(PortletDefaultPage.DETAIL)
                .withParam(PortalConstants.PROJECT_ID, projectId)
                .writeUrlToJspWriter();
    }
}
