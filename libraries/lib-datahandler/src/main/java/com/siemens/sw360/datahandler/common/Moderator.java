/*
 * Copyright Siemens AG, 2014-2016. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.datahandler.common;

import com.google.common.collect.Maps;
import com.siemens.sw360.datahandler.thrift.ThriftClients;
import com.siemens.sw360.datahandler.thrift.attachments.Attachment;
import com.siemens.sw360.datahandler.thrift.moderation.ModerationRequest;
import com.siemens.sw360.datahandler.thrift.moderation.ModerationService;
import org.apache.log4j.Logger;
import org.apache.thrift.TBase;
import org.apache.thrift.TEnum;
import org.apache.thrift.TException;
import org.apache.thrift.TFieldIdEnum;
import org.apache.thrift.meta_data.FieldMetaData;
import org.apache.thrift.protocol.TType;

import java.util.*;


/**
 * Base class for Moderators
 *
 * @author johannes.najjar@tngtech.com
 * @author birgit.heydenreich@tngtech.com
 */
public abstract class Moderator<U extends TFieldIdEnum, T extends TBase<T, U>> {

    protected final ThriftClients thriftClients;
    private static final Logger log = Logger.getLogger(Moderator.class);

    public Moderator(ThriftClients thriftClients) {
        this.thriftClients = thriftClients;
    }

    public void notifyModeratorOnDelete(String documentId) {
        try {
            ModerationService.Iface client = thriftClients.makeModerationClient();
            client.deleteRequestsOnDocument(documentId);
        } catch (TException e) {
            log.error("Could not notify moderation client, that I delete document with id " + documentId, e);
        }
    }

    public List<ModerationRequest> getModerationRequestsForDocumentId(String documentId) {
        try {
            ModerationService.Iface client = thriftClients.makeModerationClient();
            return client.getModerationRequestByDocumentId(documentId);

        } catch (TException e) {
            log.error("Could not get moderations for Document " + documentId, e);
        }
        return Collections.emptyList();
    }

    protected T updateBasicField(U field, FieldMetaData fieldMetaData, T project, T projectAdditions, T projectDeletions) {
        switch (fieldMetaData.valueMetaData.type) {
            case TType.SET:
                Set<String> originalSet = (Set<String>) project.getFieldValue(field);
                originalSet.removeAll((Set<String>) projectDeletions.getFieldValue(field));
                originalSet.addAll((Set<String>) projectAdditions.getFieldValue(field));
                break;

            case TType.STRING:
            case TType.ENUM:
                project.setFieldValue(field, projectAdditions.getFieldValue(field));
                break;

            default:
                log.error("Unknown project field in ProjectModerator: " + field.getFieldName());
        }
        return project;
    }

    protected Set<Attachment> updateAttachments(Set<Attachment> attachments,
                                              Set<Attachment> attachmentAdditions,
                                              Set<Attachment> attachmentDeletions) {
        Map<String, Attachment> attachmentMap = Maps.uniqueIndex(attachments, Attachment::getAttachmentContentId);

        for(Attachment update : attachmentAdditions){
            String id = update.getAttachmentContentId();
            if(attachmentMap.containsKey(id)){
                Attachment actual = attachmentMap.get(id);
                for(Attachment._Fields field : Attachment._Fields.values()){
                    if (update.isSet(field)) {
                        actual.setFieldValue(field, update.getFieldValue(field));
                    }
                }
            } else {
                attachments.add(update);
            }
        }

        Map<String, Attachment> additionsMap = Maps.uniqueIndex(attachmentAdditions, Attachment::getAttachmentContentId);
        for (Attachment delete : attachmentDeletions) {
            if (!additionsMap.containsKey(delete.getAttachmentContentId())) {
                attachments.remove(delete);
            }
        }

        return attachments;
    }

    protected<S> T updateEnumMap(U field, Class<? extends TEnum> S, T document, T documentAdditions, T documentDeletions) {

        if (documentAdditions.isSet(field)) {
            for (Map.Entry<String, S> entry : ((Map<String,S>) documentAdditions.getFieldValue(field)).entrySet()) {
                if(!document.isSet(field)){
                    document.setFieldValue(field,new HashMap<>());
                }
                Map<String, S> documentMap = (Map<String, S>) document.getFieldValue(field);
                if (documentMap.containsKey(entry.getKey())) {
                    documentMap.replace(entry.getKey(), entry.getValue());
                } else {
                    documentMap.put(entry.getKey(), entry.getValue());
                }
            }
        }
        if (documentDeletions.isSet(field) && document.isSet(field)) {
            for (Map.Entry<String, S> entry : ((Map<String, S>) documentDeletions.getFieldValue(field)).entrySet()) {
                if (!documentAdditions.isSet(field) ||
                        !((Map<String, S>) documentAdditions.getFieldValue(field)).containsKey(entry.getKey())) {
                    //if it's not in documentAdditions, entry must be deleted, not updated
                    ((HashMap<String, S>) document.getFieldValue(field)).remove(entry.getKey());
                }
            }
        }
        return document;
    }

    protected T updateStringMap(U field, T document, T documentAdditions, T documentDeletions) {

        if (documentAdditions.isSet(field)) {
            for (Map.Entry<String, String> entry : ((Map<String,String>) documentAdditions.getFieldValue(field)).entrySet()) {
                if(!document.isSet(field)){
                    document.setFieldValue(field,new HashMap<>());
                }
                Map<String, String> documentMap = (Map<String, String>) document.getFieldValue(field);
                if (documentMap.containsKey(entry.getKey())) {
                    documentMap.replace(entry.getKey(), entry.getValue());
                } else {
                    documentMap.put(entry.getKey(), entry.getValue());
                }
            }
        }
        if (documentDeletions.isSet(field) && document.isSet(field)) {
            for (Map.Entry<String, String> entry : ((Map<String, String>) documentDeletions.getFieldValue(field)).entrySet()) {
                if (!documentAdditions.isSet(field) ||
                        !((Map<String, String>) documentAdditions.getFieldValue(field)).containsKey(entry.getKey())) {
                    //if it's not in documentAdditions, entry must be deleted, not updated
                    ((HashMap<String, String>) document.getFieldValue(field)).remove(entry.getKey());
                }
            }
        }
        return document;
    }
}
