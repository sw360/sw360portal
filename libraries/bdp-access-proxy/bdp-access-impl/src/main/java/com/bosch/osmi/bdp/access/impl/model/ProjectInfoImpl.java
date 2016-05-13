/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
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

package com.bosch.osmi.bdp.access.impl.model;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.bosch.osmi.bdp.access.api.model.Project;
import com.bosch.osmi.bdp.access.api.model.ProjectInfo;
import com.bosch.osmi.bdp.access.impl.BdpApiAccessImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author johannes.kristan@bosch-si.com
 * @since 11/17/15.
 */
public class ProjectInfoImpl implements ProjectInfo {

    private static final Logger LOGGER = LogManager.getLogger(ProjectInfo.class);

    private BdpApiAccessImpl access;
    private com.blackducksoftware.sdk.protex.project.ProjectInfo projectInfo;

    public ProjectInfoImpl(com.blackducksoftware.sdk.protex.project.ProjectInfo projectInfo, BdpApiAccessImpl access){
        this.projectInfo= projectInfo;
        this.access = access;
    }


    @Override
    public String getProjectName() {
        return projectInfo.getName();
    }

    @Override
    public String getProjectId() {
        return projectInfo.getProjectId();
    }

    @Override
    public Project getProject() {
        try {
            com.blackducksoftware.sdk.protex.project.Project project =
                    access.getProjectApi().getProjectById(getProjectId());
            Project result = new ProjectImpl(project, access);
            return result;
        } catch (SdkFault sdkFault) {
            LOGGER.error("Unable to retrieve project data. Reason \n" + sdkFault.getMessage());
            LOGGER.debug("ProjectInfo: " + projectInfo.getName() + "ProjectId: " + projectInfo.getProjectId());
            throw new IllegalStateException(sdkFault.getCause());
        }

    }
}
