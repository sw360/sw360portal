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

package com.siemens.sw360.moderation.db;

import com.google.common.collect.Sets;
import com.siemens.sw360.datahandler.thrift.moderation.ModerationRequest;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.projects.ProjectRelationship;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Class for comparing a document with its counterpart in the database
 * Writes the difference (= additions and deletions) to the moderation request
 *
 * @author birgit.heydenreicht@tngtech.com
 */
public class ProjectModerationRequestGenerator extends ModerationRequestGenerator<Project._Fields, Project> {

    @Override
    public ModerationRequest setAdditionsAndDeletions(ModerationRequest request, Project updateProject, Project actualProject){
        updateDocument = updateProject;
        actualDocument = actualProject;

        documentAdditions = new Project();
        documentDeletions = new Project();
        //required fields:
        documentAdditions.setName(updateProject.getName());
        documentAdditions.setId(actualProject.getId());
        documentDeletions.setName(actualProject.getName());
        documentDeletions.setId(actualProject.getId());

        for (Project._Fields field : Project._Fields.values()) {

            if(actualProject.getFieldValue(field) == null){
                documentAdditions.setFieldValue(field, updateProject.getFieldValue(field));

            } else if (updateProject.getFieldValue(field) == null){
                documentDeletions.setFieldValue(field,actualProject.getFieldValue(field));

            } else if(!actualProject.getFieldValue(field).equals(updateProject.getFieldValue(field))) {
                switch (field) {
                    //ignored fields and concluded fields
                    case PERMISSIONS:
                    case DOCUMENT_STATE:
                    case RELEASE_IDS:
                    case RELEASE_CLEARING_STATE_SUMMARY:
                        break;
                    case ATTACHMENTS:
                        dealWithAttachments(Project._Fields.ATTACHMENTS);
                        break;
                    case LINKED_PROJECTS:
                        dealWithEnumMap(Project._Fields.LINKED_PROJECTS, ProjectRelationship.class);
                        break;
                    case RELEASE_ID_TO_USAGE:
                        dealWithStringMap(Project._Fields.RELEASE_ID_TO_USAGE);
                        break;
                    default:
                        dealWithBaseTypes(field, Project.metaDataMap.get(field));
                }
            }
        }

        request.setProjectAdditions(documentAdditions);
        request.setProjectDeletions(documentDeletions);
        return request;
    }
}
