/*
 * Copyright Siemens AG, 2013-2016. Part of the SW360 Portal Project.
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
package com.siemens.sw360.datahandler.entitlement;

import com.siemens.sw360.datahandler.common.Moderator;
import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.ThriftClients;
import com.siemens.sw360.datahandler.thrift.moderation.ModerationService;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.projects.ProjectRelationship;
import com.siemens.sw360.datahandler.thrift.users.User;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import java.util.HashMap;
import java.util.Map;

/**
 * Moderation for the project service
 *
 * @author cedric.bodet@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 * @author birgit.heydenreich@tngtech.com
 */
public class ProjectModerator extends Moderator<Project._Fields, Project> {

    private static final Logger log = Logger.getLogger(ProjectModerator.class);


    public ProjectModerator(ThriftClients thriftClients) {
        super(thriftClients);
    }

    public ProjectModerator(){
        super(new ThriftClients());
    }

    public RequestStatus updateProject(Project project, User user) {

        try {
            ModerationService.Iface client = thriftClients.makeModerationClient();
            client.createProjectRequest(project, user);
            return RequestStatus.SENT_TO_MODERATOR;
        } catch (TException e) {
            log.error("Could not moderate project " + project.getId() + " for User " + user.getEmail(), e);
            return  RequestStatus.FAILURE;
        }
    }

    public RequestStatus deleteProject(Project project, User user) {
        try {
            ModerationService.Iface client = thriftClients.makeModerationClient();
            client.createProjectDeleteRequest(project, user);
            return RequestStatus.SENT_TO_MODERATOR;
        } catch (TException e) {
            log.error("Could not moderate delete project " + project.getId() + " for User " + user.getEmail(), e);
            return  RequestStatus.FAILURE;
        }
    }

    public Project updateProjectFromModerationRequest(Project project, Project projectAdditions, Project projectDeletions){

        for (Project._Fields field : Project._Fields.values()) {
            if(projectAdditions.getFieldValue(field) == null && projectDeletions.getFieldValue(field) == null){
                continue;
            }

            switch (field) {
                case LINKED_PROJECTS:
                    project = updateEnumMap(
                            Project._Fields.LINKED_PROJECTS,
                            ProjectRelationship.class,
                            project,
                            projectAdditions,
                            projectDeletions);
                    break;
                case RELEASE_ID_TO_USAGE:
                    project = updateStringMap(
                            Project._Fields.RELEASE_ID_TO_USAGE,
                            project,
                            projectAdditions,
                            projectDeletions);
                    break;
                case ATTACHMENTS:
                    project.setAttachments( updateAttachments(
                            project.getAttachments(),
                            projectAdditions.getAttachments(),
                            projectDeletions.getAttachments()));
                    break;
                default:
                    project = updateBasicField(
                            field,
                            Project.metaDataMap.get(field),
                            project,
                            projectAdditions,
                            projectDeletions);
            }

        }
        return project;
    }
}
