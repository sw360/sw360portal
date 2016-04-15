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

import com.siemens.sw360.datahandler.thrift.components.*;
import com.siemens.sw360.datahandler.thrift.moderation.ModerationRequest;
import org.apache.thrift.protocol.TType;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Class for comparing a document with its counterpart in the database
 * Writes the difference (= additions and deletions) to the moderation request
 *
 * @author birgit.heydenreicht@tngtech.com
 */
public class ClearingInformationModerationRequestGenerator extends ModerationRequestGenerator<ClearingInformation._Fields, ClearingInformation> {

    @Override
    public ModerationRequest setAdditionsAndDeletions(ModerationRequest request, ClearingInformation updateCI, ClearingInformation actualCI){
        updateDocument = updateCI;
        actualDocument = actualCI;

        documentAdditions = null;
        documentDeletions = null;

        for(ClearingInformation._Fields field : ClearingInformation._Fields.values()){
            if(ClearingInformation.metaDataMap.get(field).valueMetaData.type == TType.BOOL ||
                    ClearingInformation.metaDataMap.get(field).valueMetaData.type == TType.I32){
                if(actualCI.getFieldValue(field) != updateCI.getFieldValue(field)){
                    if(documentAdditions == null){
                        documentAdditions = new ClearingInformation();
                    }
                    if(documentDeletions == null){
                        documentDeletions = new ClearingInformation();
                    }
                    documentAdditions.setFieldValue(field, updateCI.getFieldValue(field));
                    documentDeletions.setFieldValue(field, actualCI.getFieldValue(field));
                }
                continue;
            }

            if(ClearingInformation.metaDataMap.get(field).valueMetaData.type == TType.STRING) {
                if (isNullOrEmpty((String) actualCI.getFieldValue(field))
                        && isNullOrEmpty((String) updateCI.getFieldValue(field))) {
                    continue;
                }

                if (actualCI.isSet(field) && !updateCI.isSet(field)) {
                    if (documentDeletions == null) {
                        documentDeletions = new ClearingInformation();
                    }
                    documentDeletions.setFieldValue(field, actualCI.getFieldValue(field));
                    continue;
                }
                if (updateCI.isSet(field) && !actualCI.isSet(field)) {
                    if (documentAdditions == null) {
                        documentAdditions = new ClearingInformation();
                    }
                    documentAdditions.setFieldValue(field, updateCI.getFieldValue(field));
                    continue;
                }
                if (!(actualCI.getFieldValue(field).equals(updateCI.getFieldValue(field)))) {
                    if (documentAdditions == null) {
                        documentAdditions = new ClearingInformation();
                    }
                    if (documentDeletions == null) {
                        documentDeletions = new ClearingInformation();
                    }
                    documentAdditions.setFieldValue(field, updateCI.getFieldValue(field));
                    documentDeletions.setFieldValue(field, actualCI.getFieldValue(field));
                }
            }
        }
        request.getReleaseAdditions().setClearingInformation(documentAdditions);
        request.getReleaseDeletions().setClearingInformation(documentDeletions);
        return request;
    }
}
