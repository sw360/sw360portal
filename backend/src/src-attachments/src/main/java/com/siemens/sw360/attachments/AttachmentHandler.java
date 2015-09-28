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
package com.siemens.sw360.attachments;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.siemens.sw360.attachments.db.AttachmentRepository;
import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.common.DatabaseSettings;
import com.siemens.sw360.datahandler.couchdb.AttachmentConnector;
import com.siemens.sw360.datahandler.couchdb.DatabaseConnector;
import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.RequestSummary;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentContent;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentService;
import com.siemens.sw360.datahandler.thrift.attachments.DatabaseAddress;
import com.siemens.sw360.datahandler.thrift.users.User;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.ektorp.DocumentOperationResult;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.siemens.sw360.datahandler.common.Duration.durationOf;
import static com.siemens.sw360.datahandler.common.SW360Assert.*;
import static com.siemens.sw360.datahandler.thrift.ThriftValidate.validateAttachment;

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

    private final DatabaseAddress address;


    public AttachmentHandler() throws MalformedURLException {
        DatabaseConnector databaseConnector = new DatabaseConnector(DatabaseSettings.COUCH_DB_URL, DatabaseSettings.COUCH_DB_ATTACHMENTS);
        attachmentConnector = new AttachmentConnector(DatabaseSettings.COUCH_DB_URL, DatabaseSettings.COUCH_DB_ATTACHMENTS, durationOf(30, TimeUnit.SECONDS));
        repository = new AttachmentRepository(databaseConnector);

        address = databaseConnector.getAddress();
    }

    @Override
    public DatabaseAddress getDatabaseAddress() throws TException {
        return address;
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

}
