/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
