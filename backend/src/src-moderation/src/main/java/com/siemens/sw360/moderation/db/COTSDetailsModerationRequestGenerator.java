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

import com.siemens.sw360.datahandler.thrift.components.COTSDetails;
import com.siemens.sw360.datahandler.thrift.components.ClearingInformation;
import com.siemens.sw360.datahandler.thrift.moderation.ModerationRequest;
import org.apache.thrift.protocol.TType;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Class for comparing a document with its counterpart in the database
 * Writes the difference (= additions and deletions) to the moderation request
 *
 * @author birgit.heydenreicht@tngtech.com
 */
public class COTSDetailsModerationRequestGenerator extends ModerationRequestGenerator<COTSDetails._Fields, COTSDetails> {

    @Override
    public ModerationRequest setAdditionsAndDeletions(ModerationRequest request, COTSDetails update, COTSDetails actual){
        updateDocument = update;
        actualDocument = actual;

        documentAdditions = null;
        documentDeletions = null;

        for(COTSDetails._Fields field : COTSDetails._Fields.values()){
            if(COTSDetails.metaDataMap.get(field).valueMetaData.type == TType.BOOL ||
                    COTSDetails.metaDataMap.get(field).valueMetaData.type == TType.I32){
                if(actual.getFieldValue(field) != update.getFieldValue(field)){
                    if(documentAdditions == null){
                        documentAdditions = new COTSDetails();
                    }
                    if(documentDeletions == null){
                        documentDeletions = new COTSDetails();
                    }
                    documentAdditions.setFieldValue(field, update.getFieldValue(field));
                    documentDeletions.setFieldValue(field, actual.getFieldValue(field));
                }
                continue;
            }

            if(COTSDetails.metaDataMap.get(field).valueMetaData.type == TType.STRING) {
                if (isNullOrEmpty((String) actual.getFieldValue(field))
                        && isNullOrEmpty((String) update.getFieldValue(field))) {
                    continue;
                }

                if (actual.isSet(field) && !update.isSet(field)) {
                    if (documentDeletions == null) {
                        documentDeletions = new COTSDetails();
                    }
                    documentDeletions.setFieldValue(field, actual.getFieldValue(field));
                    continue;
                }
                if (update.isSet(field) && !actual.isSet(field)) {
                    if (documentAdditions == null) {
                        documentAdditions = new COTSDetails();
                    }
                    documentAdditions.setFieldValue(field, update.getFieldValue(field));
                    continue;
                }
                if (!(actual.getFieldValue(field).equals(update.getFieldValue(field)))) {
                    if (documentAdditions == null) {
                        documentAdditions = new COTSDetails();
                    }
                    if (documentDeletions == null) {
                        documentDeletions = new COTSDetails();
                    }
                    documentAdditions.setFieldValue(field, update.getFieldValue(field));
                    documentDeletions.setFieldValue(field, actual.getFieldValue(field));
                }
            }
        }
        request.getReleaseAdditions().setCotsDetails(documentAdditions);
        request.getReleaseDeletions().setCotsDetails(documentDeletions);
        return request;
    }
}
