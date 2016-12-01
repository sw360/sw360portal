/*
 * Copyright Siemens AG, 2013-2016. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.sw360.moderation.db;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.eclipse.sw360.datahandler.thrift.attachments.Attachment;
import org.eclipse.sw360.datahandler.thrift.moderation.ModerationRequest;
import org.apache.log4j.Logger;
import org.apache.thrift.TBase;
import org.apache.thrift.TEnum;
import org.apache.thrift.TFieldIdEnum;
import org.apache.thrift.meta_data.FieldMetaData;
import org.apache.thrift.protocol.TType;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class for comparing a document with its counterpart in the database
 * Writes the difference (= additions and deletions) to the moderation request
 *
 * @author birgit.heydenreicht@tngtech.com
 */
public abstract class ModerationRequestGenerator<U extends TFieldIdEnum, T extends TBase<T, U>> {
    protected T documentAdditions;
    protected T documentDeletions;
    protected T updateDocument;
    protected T actualDocument;

    Logger log = Logger.getLogger(ModerationRequestGenerator.class);

    public abstract ModerationRequest setAdditionsAndDeletions(ModerationRequest request, T updateDocument, T actualDocument);

    protected void dealWithBaseTypes(U field, FieldMetaData fieldMetaData){
        if (fieldMetaData.valueMetaData.type == TType.SET) {
            dealWithStringSets(field);
        } else if (fieldMetaData.valueMetaData.type == TType.STRING ||
                   fieldMetaData.valueMetaData.type == TType.ENUM ||
                   fieldMetaData.valueMetaData.type == TType.STRUCT) {
            dealWithStringsEnumsStructs(field);
        } else {
            log.error("Unknown project field in ModerationRequestGenerator: " + field.getFieldName());
        }
    }

    private void dealWithStringSets(U field) {
        documentDeletions.setFieldValue(field,
                Sets.difference((Set<String>) actualDocument.getFieldValue(field), (Set<String>) updateDocument.getFieldValue(field)));
        documentAdditions.setFieldValue(field,
                Sets.difference((Set<String>) updateDocument.getFieldValue(field), (Set<String>) actualDocument.getFieldValue(field)));
    }

    private void dealWithStringsEnumsStructs(U field) {
        documentAdditions.setFieldValue(field, updateDocument.getFieldValue(field));
        documentDeletions.setFieldValue(field, actualDocument.getFieldValue(field));
    }

    protected <S> void dealWithEnumMap(U field, Class<? extends TEnum> S) {
        Map<String,S> addedMap = (Map<String, S>) updateDocument.getFieldValue(field);
        if(addedMap == null) {
            addedMap = new HashMap<>();
        }
        Map<String,S> actualMap = (Map<String, S>) actualDocument.getFieldValue(field);
        for(Map.Entry<String, S> entry : actualMap.entrySet()){
            addedMap.remove(entry);
        }

        Map<String,S> deletedMap = (Map<String, S>) actualDocument.getFieldValue(field);
        if (deletedMap == null) {
            deletedMap = new HashMap<>();
        }
        Map<String,S> updateMap = (Map<String, S>) updateDocument.getFieldValue(field);
        for(Map.Entry<String, S> entry : updateMap.entrySet()){
            deletedMap.remove(entry);
        }

        //determine changes in common linkedProjects
        Set<String> commonKeys = Sets.intersection(updateMap.keySet(), actualMap.keySet());
        for(String id : commonKeys) {
            S actual = actualMap.get(id);
            S update = updateMap.get(id);
            if(! actual.equals(update)) {
                addedMap.put(id, update);
                deletedMap.put(id, actual);
            }
        }
        if(!addedMap.isEmpty()) {
            documentAdditions.setFieldValue(field, addedMap);
        }
        if(!deletedMap.isEmpty()) {
            documentDeletions.setFieldValue(field, deletedMap);
        }
    }

    protected void dealWithStringMap(U field) {
        Map<String,String> addedMap = (Map<String, String>) updateDocument.getFieldValue(field);
        if(addedMap == null) {
            addedMap = new HashMap<>();
        }
        Map<String,String> actualMap = (Map<String, String>) actualDocument.getFieldValue(field);
        for(Map.Entry<String, String> entry : actualMap.entrySet()){
            addedMap.remove(entry);
        }

        Map<String,String> deletedMap = (Map<String, String>) actualDocument.getFieldValue(field);
        if (deletedMap == null) {
            deletedMap = new HashMap<>();
        }
        Map<String,String> updateMap = (Map<String, String>) updateDocument.getFieldValue(field);
        for(Map.Entry<String, String> entry : updateMap.entrySet()){
            deletedMap.remove(entry);
        }

        //determine changes in common linkedProjects
        Set<String> commonKeys = Sets.intersection(updateMap.keySet(), actualMap.keySet());
        for(String id : commonKeys) {
            String actual = actualMap.get(id);
            String update = updateMap.get(id);
            if(! actual.equals(update)) {
                addedMap.put(id, update);
                deletedMap.put(id, actual);
            }
        }
        if(!addedMap.isEmpty()) {
            documentAdditions.setFieldValue(field, addedMap);
        }
        if(!deletedMap.isEmpty()) {
            documentDeletions.setFieldValue(field, deletedMap);
        }
    }

    protected void dealWithAttachments(U attachmentField){
        Set<Attachment> actualAttachments = (Set<Attachment>) actualDocument.getFieldValue(attachmentField);
        Set<Attachment> updateAttachments = (Set<Attachment>) updateDocument.getFieldValue(attachmentField);
        Map<String, Attachment> actualAttachmentMap = Maps.uniqueIndex(actualAttachments, Attachment::getAttachmentContentId);
        Set<String> actualAttachmentIds = actualAttachmentMap.keySet();
        Map<String, Attachment> updateAttachmentMap = Maps.uniqueIndex(updateAttachments, Attachment::getAttachmentContentId);
        Set<String> updateAttachmentIds = updateAttachmentMap.keySet();

        Set<Attachment> attachmentAdditions = updateAttachmentMap
                .values()
                .stream()
                .filter(attachment -> !actualAttachmentIds.contains(attachment.getAttachmentContentId()))
                .collect(Collectors.toSet());

        Set<Attachment> attachmentDeletions = actualAttachmentMap
                .values()
                .stream()
                .filter(attachment -> !updateAttachmentIds.contains(attachment.getAttachmentContentId()))
                .collect(Collectors.toSet());

        //determine changes in common attachments
        Set<String> commonAttachmentIds = Sets.intersection(actualAttachmentIds, updateAttachmentIds);
        for(String id : commonAttachmentIds) {
            Attachment actual = actualAttachmentMap.get(id);
            Attachment update = updateAttachmentMap.get(id);
            if(actual != null && !actual.equals(update)) {
                attachmentAdditions.add(getAdditionsFromCommonAttachment(actual, update));
                attachmentDeletions.add(getDeletionsFromCommonAttachment(actual, update));
            }
        }
        documentAdditions.setFieldValue(attachmentField, attachmentAdditions);
        documentDeletions.setFieldValue(attachmentField, attachmentDeletions);
    }

    protected Attachment getAdditionsFromCommonAttachment(Attachment actual, Attachment update){
        //new attachments with required fields set
        Attachment additions = new Attachment()
                .setAttachmentContentId(actual.getAttachmentContentId())
                .setFilename(actual.getFilename());

        for (Attachment._Fields field : Attachment._Fields.values()) {
            if ( (!actual.isSet(field) && update.isSet(field)) ||
                    (actual.isSet(field) && !actual.getFieldValue(field).equals(update.getFieldValue(field)))) {
                additions.setFieldValue(field, update.getFieldValue(field));
            }
        }
        return additions;
    }

    protected Attachment getDeletionsFromCommonAttachment(Attachment actual, Attachment update){
        //new attachments with required fields set
        Attachment deletions = new Attachment()
                .setAttachmentContentId(actual.getAttachmentContentId())
                .setFilename(actual.getFilename());

        for (Attachment._Fields field : Attachment._Fields.values()) {
            if ( (!actual.isSet(field) && update.isSet(field)) ||
                    (actual.isSet(field) && !actual.getFieldValue(field).equals(update.getFieldValue(field)))) {
                deletions.setFieldValue(field, actual.getFieldValue(field));
            }
        }
        return deletions;
    }

}
