/*
 * Copyright Siemens AG, 2013-2016. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.attachments;

import com.siemens.sw360.datahandler.db.AttachmentDatabaseHandler;
import com.siemens.sw360.datahandler.common.DatabaseSettings;
import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.RequestSummary;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentContent;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentService;
import com.siemens.sw360.datahandler.thrift.attachments.DatabaseAddress;
import com.siemens.sw360.datahandler.thrift.users.User;
import org.apache.thrift.TException;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Set;

import static com.siemens.sw360.datahandler.common.SW360Assert.*;
import static com.siemens.sw360.datahandler.thrift.ThriftValidate.validateAttachment;

/**
 * Implementation of the Thrift service
 *
 * @author cedric.bodet@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 * @author alex.borodin@evosoft.com
 */
public class AttachmentHandler implements AttachmentService.Iface {

    private final AttachmentDatabaseHandler handler;

    public AttachmentHandler() throws MalformedURLException {
        handler = new AttachmentDatabaseHandler(DatabaseSettings.COUCH_DB_URL, DatabaseSettings.COUCH_DB_DATABASE, DatabaseSettings.COUCH_DB_ATTACHMENTS);
    }

    @Override
    public DatabaseAddress getDatabaseAddress() throws TException {
        return handler.getDatabaseAddress();
    }

    @Override
    public AttachmentContent makeAttachmentContent(AttachmentContent attachmentContent) throws TException {
        validateAttachment(attachmentContent);
        assertIdUnset(attachmentContent.getId());

        return handler.add(attachmentContent);
    }

    @Override
    public List<AttachmentContent> makeAttachmentContents(List<AttachmentContent> attachmentContents) throws TException {
        return handler.makeAttachmentContents(attachmentContents);
    }

    @Override
    public AttachmentContent getAttachmentContent(String id) throws TException {
        assertNotEmpty(id);
        return handler.getAttachmentContent(id);
    }

    @Override
    public void updateAttachmentContent(AttachmentContent attachment) throws TException {
        validateAttachment(attachment);
        handler.updateAttachmentContent(attachment);
    }

    @Override
    public RequestSummary bulkDelete(List<String> ids) throws TException {
        return handler.bulkDelete(ids);
    }

    @Override
    public RequestStatus deleteAttachmentContent(String attachmentId) throws TException {
        return handler.deleteAttachmentContent(attachmentId);
    }

    @Override
    public RequestSummary vacuumAttachmentDB(User user, Set<String> usedIds) throws TException {
        assertUser(user);
        return handler.vacuumAttachmentDB(user, usedIds);
    }

    @Override
    public String getSha1FromAttachmentContentId(String attachmentContentId){
        return handler.getSha1FromAttachmentContentId(attachmentContentId);
    }
}
