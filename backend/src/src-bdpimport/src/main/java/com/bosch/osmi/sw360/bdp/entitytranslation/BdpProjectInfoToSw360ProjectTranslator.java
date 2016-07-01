/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 * Part of the SW360 Portal Project.
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
