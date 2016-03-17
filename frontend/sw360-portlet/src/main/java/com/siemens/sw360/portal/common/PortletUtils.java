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
package com.siemens.sw360.portal.common;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.common.SW360Utils;
import com.siemens.sw360.datahandler.thrift.ModerationState;
import com.siemens.sw360.datahandler.thrift.Visibility;
import com.siemens.sw360.datahandler.thrift.attachments.Attachment;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentType;
import com.siemens.sw360.datahandler.thrift.attachments.CheckStatus;
import com.siemens.sw360.datahandler.thrift.components.*;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.projects.ProjectState;
import com.siemens.sw360.datahandler.thrift.projects.ProjectType;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.thrift.users.UserGroup;
import com.siemens.sw360.portal.users.UserCacheHolder;
import org.apache.log4j.Logger;
import org.apache.thrift.TBase;
import org.apache.thrift.TFieldIdEnum;
import org.apache.thrift.meta_data.FieldMetaData;

import javax.portlet.PortletRequest;
import java.util.*;

import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;

/**
 * Portlet helpers
 *
 * @author cedric.bodet@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 */
public class PortletUtils {

    private static final Logger log = Logger.getLogger(PortletUtils.class);

    private PortletUtils() {
        // Utility class with only static functions
    }

    public static ComponentType getComponentTypefromString(String enumNumber) {
        return ComponentType.findByValue(parseInt(enumNumber));
    }

    public static ClearingState getClearingStatefromString(String enumNumber) {
        return ClearingState.findByValue(parseInt(enumNumber));
    }

    public static RepositoryType getRepositoryTypefromString(String enumNumber) {
        return RepositoryType.findByValue(parseInt(enumNumber));
    }

    public static MainlineState getMainlineStatefromString(String enumNumber) {
        return MainlineState.findByValue(parseInt(enumNumber));
    }

    public static ModerationState getModerationStatusfromString(String enumNumber) {
        return ModerationState.findByValue(parseInt(enumNumber));
    }

    public static AttachmentType getAttachmentTypefromString(String enumNumber) {
        return AttachmentType.findByValue(parseInt(enumNumber));
    }
    public static CheckStatus getCheckStatusfromString(String enumNumber) {
        return CheckStatus.findByValue(parseInt(enumNumber));
    }

    public static ProjectState getProjectStateFromString(String enumNumber) {
        return ProjectState.findByValue(parseInt(enumNumber));
    }

    public static ProjectType getProjectTypeFromString(String enumNumber) {
        return ProjectType.findByValue(parseInt(enumNumber));
    }
    public static Visibility getVisibilityFromString(String enumNumber) {
        return  Visibility.findByValue(parseInt(enumNumber));
    }

    public static UserGroup getUserGroupFromString(String enumNumber) {
        return  UserGroup.findByValue(parseInt(enumNumber));
    }

    public static <U extends TFieldIdEnum, T extends TBase<T, U>> void setFieldValue(PortletRequest request, T instance, U field, FieldMetaData fieldMetaData, String prefix) {

        String value = request.getParameter(prefix + field.toString());
        if (value != null) {
            switch (fieldMetaData.valueMetaData.type) {

                case org.apache.thrift.protocol.TType.SET:
                    instance.setFieldValue(field, CommonUtils.splitToSet(value));
                    break;
                case org.apache.thrift.protocol.TType.ENUM:
                    if (!"".equals(value))
                        instance.setFieldValue(field, enumFromString(value, field));
                    break;
                case org.apache.thrift.protocol.TType.I32:
                    if (!"".equals(value))
                        instance.setFieldValue(field, Integer.parseInt(value));
                    break;
                case org.apache.thrift.protocol.TType.BOOL:
                    if (!"".equals(value))
                        instance.setFieldValue(field, true);
                    break;
                default:
                    instance.setFieldValue(field, value);
            }
        }
    }

    public static <U extends TFieldIdEnum> Object enumFromString(String value, U field) {
        if (field == Release._Fields.CLEARING_STATE)
            return getClearingStatefromString(value);
        else if (field == Component._Fields.COMPONENT_TYPE)
            return getComponentTypefromString(value);
        else if (field == Repository._Fields.REPOSITORYTYPE)
            return getRepositoryTypefromString(value);
        else if (field == Release._Fields.MAINLINE_STATE)
            return getMainlineStatefromString(value);
        else if (field == Project._Fields.STATE)
            return getProjectStateFromString(value);
        else if (field == Project._Fields.PROJECT_TYPE)
            return getProjectTypeFromString(value);
        else if (field == Project._Fields.VISBILITY)
            return getVisibilityFromString(value);
        else if (field == User._Fields.USER_GROUP)
            return getUserGroupFromString(value);
        else {
            log.error("Missing case in enumFromString, unknown field was " + field.toString());
            return null;
        }
    }


    public static Set<Attachment> updateAttachmentsFromRequest(PortletRequest request, Set<Attachment> documentAttachments) {
        if (documentAttachments == null) {
            log.info("UpdateAttachments called with null documentAttachments.");
            return null;
        }

        User user = UserCacheHolder.getUserFromRequest(request);
        String[] fileNames = request.getParameterValues(Release._Fields.ATTACHMENTS.toString() + Attachment._Fields.FILENAME);
        String[] ids = request.getParameterValues(Release._Fields.ATTACHMENTS.toString() + Attachment._Fields.ATTACHMENT_CONTENT_ID.toString());
        String[] createdComments = request.getParameterValues(Release._Fields.ATTACHMENTS.toString() + Attachment._Fields.CREATED_COMMENT.toString());
        String[] checkedComments = request.getParameterValues(Release._Fields.ATTACHMENTS.toString() + Attachment._Fields.CHECKED_COMMENT.toString());
        String[] checkStatuses = request.getParameterValues(Release._Fields.ATTACHMENTS.toString() + Attachment._Fields.CHECK_STATUS.toString());
        String[] atypes = request.getParameterValues(Release._Fields.ATTACHMENTS.toString() + Attachment._Fields.ATTACHMENT_TYPE.toString());

        if(ids == null || ids.length == 0) {
            return new HashSet<>();
        } else if (CommonUtils.oneIsNull(atypes, createdComments, checkedComments, fileNames)) {
            log.error("We have a problem with null arrays");
        } else if (
                atypes.length != createdComments.length ||
                atypes.length != ids.length ||
                atypes.length != checkedComments.length ||
                atypes.length != fileNames.length) {
            log.error("We have a problem length != other.length ");
        } else {
            Map<String, Attachment> documentAttachmentMap = documentAttachments.stream().collect(Collectors.toMap(Attachment::getAttachmentContentId, Function.identity()));
            Map<String, Attachment> documentAttachmentsInRequestMap = new HashMap<>();
            int length = atypes.length;

            for (int i = 0; i < length; ++i) {

                String id = ids[i];
                Attachment attachment;
                if (documentAttachmentMap.containsKey(id)) {
                    attachment = documentAttachmentMap.get(id);
                    documentAttachmentsInRequestMap.put(id,attachment);
                } else {
                    //the sha1 checksum is not computed here, but in the backend, when updating the component in the database
                    attachment = CommonUtils.getNewAttachment(user, id, fileNames[i]);
                    documentAttachments.add(attachment);
                }
                attachment.setCreatedComment(createdComments[i]);
                attachment.setAttachmentType(getAttachmentTypefromString(atypes[i]));
                if(attachment.checkedComment != checkedComments[i]|| attachment.checkStatus != getCheckStatusfromString(checkStatuses[i])){
                    attachment.setCheckedOn(SW360Utils.getCreatedOn());
                    attachment.setCheckedBy(UserCacheHolder.getUserFromRequest(request).getEmail());
                    attachment.setCheckedTeam(UserCacheHolder.getUserFromRequest(request).getDepartment());
                    attachment.setCheckStatus(getCheckStatusfromString(checkStatuses[i]));
                    attachment.setCheckedComment(checkedComments[i]);
                }
            }
            Set<String> removedAttachmentIds = Sets.difference(documentAttachmentMap.keySet(),documentAttachmentsInRequestMap.keySet());
            documentAttachments.removeIf(attachment -> removedAttachmentIds.contains(attachment.getAttachmentContentId()));
        }
        return documentAttachments;
    }


    public static Release cloneRelease(String emailFromRequest, Release release) {

        Release newRelease = release.deepCopy();

        //new DB object
        newRelease.unsetId();
        newRelease.unsetRevision();

        //new Owner
        newRelease.setCreatedBy(emailFromRequest);
        newRelease.setCreatedOn(SW360Utils.getCreatedOn());

        //release specifics
        newRelease.unsetCpeid();
        newRelease.unsetAttachments();
        newRelease.unsetClearingInformation();

        return newRelease;
    }

    public static Project cloneProject(String emailFromRequest, String department, Project project) {

        Project newProject = project.deepCopy();

        //new DB object
        newProject.unsetId();
        newProject.unsetRevision();

        //new Owner
        newProject.setCreatedBy(emailFromRequest);
        newProject.setCreatedOn(SW360Utils.getCreatedOn());
        newProject.setBusinessUnit(department);

        //project specifics
        newProject.unsetName();
        newProject.unsetAttachments();

        return newProject;
    }
}
