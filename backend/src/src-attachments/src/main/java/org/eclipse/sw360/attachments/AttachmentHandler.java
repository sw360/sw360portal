/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * SPDX-License-Identifier: EPL-1.0
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.attachments;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import org.eclipse.sw360.attachments.db.AttachmentRepository;
import org.eclipse.sw360.datahandler.common.CommonUtils;
import org.eclipse.sw360.datahandler.common.DatabaseSettings;
import org.eclipse.sw360.datahandler.couchdb.AttachmentConnector;
import org.eclipse.sw360.datahandler.couchdb.DatabaseConnector;
import org.eclipse.sw360.datahandler.thrift.RequestStatus;
import org.eclipse.sw360.datahandler.thrift.RequestSummary;
import org.eclipse.sw360.datahandler.thrift.attachments.AttachmentContent;
import org.eclipse.sw360.datahandler.thrift.attachments.AttachmentService;
import org.eclipse.sw360.datahandler.thrift.attachments.DatabaseAddress;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.ektorp.DocumentOperationResult;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.eclipse.sw360.datahandler.common.Duration.durationOf;
import static org.eclipse.sw360.datahandler.common.SW360Assert.*;
import static org.eclipse.sw360.datahandler.thrift.ThriftValidate.validateAttachment;

/**
 * Implementation of the Thrift service
 *
 * @author cedric.bodet@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 */
public class AttachmentHandler implements AttachmentService.Iface {

    public static final String ATTACHMENTS_FIELD_NAME = "attachments";
    private static final Logger log = Logger.getLogger(AttachmentHandler.class);

    private final AttachmentRepository repository;
    private final AttachmentConnector attachmentConnector;


    public AttachmentHandler() throws MalformedURLException {
        DatabaseConnector databaseConnector = new DatabaseConnector(DatabaseSettings.getConfiguredHttpClient(), DatabaseSettings.COUCH_DB_ATTACHMENTS);
        attachmentConnector = new AttachmentConnector(DatabaseSettings.getConfiguredHttpClient(), DatabaseSettings.COUCH_DB_ATTACHMENTS, durationOf(30, TimeUnit.SECONDS));
        repository = new AttachmentRepository(databaseConnector);
    }

    @Override
    public AttachmentContent makeAttachmentContent(AttachmentContent attachmentContent) throws TException {
        validateAttachment(attachmentContent);
        assertIdUnset(attachmentContent.getId());

        repository.add(attachmentContent);
        return attachmentContent;

    }

    @Override
    public List<AttachmentContent> makeAttachmentContents(List<AttachmentContent> attachmentContents) throws TException {
        final List<DocumentOperationResult> documentOperationResults = repository.executeBulk(attachmentContents);
        if (!documentOperationResults.isEmpty())
            log.error("Failed Attachment store results " + documentOperationResults);

        return FluentIterable.from(attachmentContents).filter(new Predicate<AttachmentContent>() {
            @Override
            public boolean apply(AttachmentContent input) {
                return input.isSetId();
            }
        }).toList();
    }

    @Override
    public AttachmentContent getAttachmentContent(String id) throws TException {
        assertNotEmpty(id);

        AttachmentContent attachment = repository.get(id);
        assertNotNull(attachment, "Cannot find "+ id + " in database.");
        validateAttachment(attachment);

        return attachment;
    }

    @Override
    public void updateAttachmentContent(AttachmentContent attachment) throws TException {
        validateAttachment(attachment);
        attachmentConnector.updateAttachmentContent(attachment);
    }

    @Override
    public RequestSummary bulkDelete(List<String> ids) throws TException {
        final List<DocumentOperationResult> documentOperationResults = repository.deleteIds(ids);
        return CommonUtils.getRequestSummary(ids, documentOperationResults);

    }

    @Override
    public RequestStatus deleteAttachmentContent(String attachmentId) throws TException {
        attachmentConnector.deleteAttachment(attachmentId);

        return RequestStatus.SUCCESS;
    }

    @Override
    public RequestSummary vacuumAttachmentDB(User user, Set<String> usedIds) throws TException {
        assertUser(user);
        return repository.vacuumAttachmentDB(user, usedIds);
    }

    @Override
    public String getSha1FromAttachmentContentId(String attachmentContentId){
        return attachmentConnector.getSha1FromAttachmentContentId(attachmentContentId);
    }
}
