/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.bosch.osmi.sw360.bdp.entitytranslation;

import com.bosch.osmi.bdp.access.api.model.ProjectInfo;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import java.util.HashMap;

public class BdpProjectInfoToSw360ProjectTranslator implements EntityTranslator<ProjectInfo, Project> {

    @Override
    public Project apply(com.bosch.osmi.bdp.access.api.model.ProjectInfo projectInfoBdp) {
        Project projectSW360 = new Project();

        projectSW360.setExternalIds(new HashMap<>());
        projectSW360.getExternalIds().put(TranslationConstants.BDP_ID, projectInfoBdp.getProjectId());

        projectSW360.setDescription("");

        com.bosch.osmi.bdp.access.api.model.Project projectBdp = projectInfoBdp.getProject();
        if (projectInfoBdp.getProjectName().equals(projectBdp.getName())) {
            projectSW360.setName(projectInfoBdp.getProjectName());
        } else {
            projectSW360.setName(projectInfoBdp.getProjectName() + " (" + projectBdp.getName() + ")");
        }

// Problem: Can not set mail address when no user with the corresponding mail address is registered
// projectSW360.setProjectResponsible(project.getCreatedBy());
// projectSW360.setProjectResponsibleIsSet(true);

        return projectSW360;
    }

}
