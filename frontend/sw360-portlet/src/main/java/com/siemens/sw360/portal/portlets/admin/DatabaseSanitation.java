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
package com.siemens.sw360.portal.portlets.admin;

import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.components.ComponentService;
import com.siemens.sw360.datahandler.thrift.projects.ProjectService;
import com.siemens.sw360.portal.common.PortalConstants;
import com.siemens.sw360.portal.portlets.Sw360Portlet;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import javax.portlet.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.siemens.sw360.datahandler.common.CommonUtils.allAreEmptyOrNull;
import static com.siemens.sw360.datahandler.common.CommonUtils.oneIsNull;

/**
 * This portlet looks for duplicates in entry-identifiers that should be unique in the database
 * @author johannes.najjar@tngtech.com
 */
public class DatabaseSanitation extends Sw360Portlet {

    private static final Logger log = Logger.getLogger(DatabaseSanitation.class);

    @Override
    public void doView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        // Proceed with page rendering
        super.doView(request, response);
    }

    @Override
    public void serveResource(ResourceRequest request, ResourceResponse response) throws IOException, PortletException {
        String action = request.getParameter(PortalConstants.ACTION);
        if (PortalConstants.DUPLICATES.equals(action)) {
                 serveDuplicates(request,response);
        }
    }

    private void serveDuplicates(ResourceRequest request, ResourceResponse response) throws IOException, PortletException {

        Map<String, List<String>> duplicateComponents=null;
        Map<String, List<String>> duplicateReleases=null;
        Map<String, List<String>> duplicateReleaseSources=null;
        Map<String, List<String>> duplicateProjects=null;
        try {
            final ComponentService.Iface componentClient = thriftClients.makeComponentClient();
            duplicateComponents = componentClient.getDuplicateComponents();
            duplicateReleases = componentClient.getDuplicateReleases();
            duplicateReleaseSources = componentClient.getDuplicateReleaseSources();
            final ProjectService.Iface projectClient = thriftClients.makeProjectClient();
            duplicateProjects =  projectClient.getDuplicateProjects();
        } catch (TException e) {
            log.error("Error in component client", e);
        }

        if(oneIsNull(duplicateComponents,duplicateReleases,duplicateProjects,duplicateReleaseSources)) {
            renderRequestStatus(request,response, RequestStatus.FAILURE);
        } else if(allAreEmptyOrNull(duplicateComponents, duplicateReleases, duplicateProjects, duplicateReleaseSources)) {
            renderRequestStatus(request,response, RequestStatus.SUCCESS);
        } else {
            request.setAttribute(PortalConstants.DUPLICATE_RELEASES, duplicateReleases);
            request.setAttribute(PortalConstants.DUPLICATE_RELEASE_SOURCES, duplicateReleaseSources);
            request.setAttribute(PortalConstants.DUPLICATE_COMPONENTS, duplicateComponents);
            request.setAttribute(PortalConstants.DUPLICATE_PROJECTS, duplicateProjects);
            include("/html/admin/databaseSanitation/duplicatesAjax.jsp", request, response, PortletRequest.RESOURCE_PHASE);
        }
    }
}
