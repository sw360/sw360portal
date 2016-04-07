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
import com.siemens.sw360.datahandler.thrift.components.Component;
import com.siemens.sw360.datahandler.thrift.moderation.ModerationRequest;
import org.apache.log4j.Logger;
import org.apache.thrift.protocol.TType;

import java.util.Set;

/**
 * Class for comparing a document with its counterpart in the database
 * Writes the difference (= additions and deletions) to the moderation request
 *
 * @author birgit.heydenreicht@tngtech.com
 */
public class ComponentModerationRequestGenerator extends ModerationRequestGenerator<Component._Fields, Component> {

    @Override
    public ModerationRequest setAdditionsAndDeletions(ModerationRequest request, Component updateComponent, Component actualComponent){
        updateDocument = updateComponent;
        actualDocument = actualComponent;

        documentAdditions = new Component();
        documentDeletions = new Component();
        //required fields:
        documentAdditions.setName(updateComponent.getName());
        documentAdditions.setId(actualComponent.getId());
        documentDeletions.setName(actualComponent.getName());
        documentDeletions.setId(actualComponent.getId());

        for (Component._Fields field : Component._Fields.values()) {

            if(actualComponent.getFieldValue(field) == null){
                documentAdditions.setFieldValue(field, updateComponent.getFieldValue(field));

            } else if (updateComponent.getFieldValue(field) == null){
                documentDeletions.setFieldValue(field, actualComponent.getFieldValue(field));

            } else if(!actualComponent.getFieldValue(field).equals(updateComponent.getFieldValue(field))) {
                switch (field) {
                    //ignored fields
                    case PERMISSIONS:
                    case DOCUMENT_STATE:
                    //releases and related fields are not updated via moderation requests
                    case RELEASES:
                    case RELEASE_IDS:
                    case MAIN_LICENSE_IDS:
                    case LANGUAGES:
                    case OPERATING_SYSTEMS:
                    case VENDOR_NAMES:
                        break;
                    case ATTACHMENTS:
                        dealWithAttachments(Component._Fields.ATTACHMENTS);
                        break;
                    default:
                        dealWithBaseTypes(field, Component.metaDataMap.get(field));
                }
            }
        }

        request.setComponentAdditions(documentAdditions);
        request.setComponentDeletions(documentDeletions);
        return request;
    }
}
