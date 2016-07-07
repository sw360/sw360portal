/*
 * Copyright Siemens AG, 2014-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.datahandler.couchdb;

import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.common.ConcatClosingInputStream;
import com.siemens.sw360.datahandler.common.Duration;
import com.siemens.sw360.datahandler.thrift.SW360Exception;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentContent;
import com.siemens.sw360.datahandler.thrift.attachments.DatabaseAddress;
import org.apache.log4j.Logger;
import org.ektorp.AttachmentInputStream;
import org.ektorp.DocumentNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Iterator;

import static com.siemens.sw360.datahandler.common.SW360Assert.assertNotNull;

/**
 * Created by bodet on 03/02/15.
 *
 * @author cedric.bodet@tngtech.com
 * @author daniele.fognini@tngtech.com
 */
public class AttachmentStreamConnector {
    private static Logger log = Logger.getLogger(AttachmentStreamConnector.class);

    protected final DatabaseConnector connector;
    private final AttachmentContentDownloader attachmentContentDownloader;
    private final Duration downloadTimeout;

    /**
     * @param address         Thrift object representing the database address
     * @param downloadTimeout timeout for downloading remote attachments
     * @throws java.net.MalformedURLException if the given database address not a valid url.
     * @todo remove this mess of constructors and use dependency injection
     */
    public AttachmentStreamConnector(DatabaseAddress address, Duration downloadTimeout) throws MalformedURLException {
        this(new DatabaseConnector(address.getUrl(), address.getDbName()), downloadTimeout);
    }

    public AttachmentStreamConnector(DatabaseConnector connector, Duration downloadTimeout) {
        this(connector, new AttachmentContentDownloader(), downloadTimeout);
    }

    public AttachmentStreamConnector(DatabaseConnector connector, AttachmentContentDownloader attachmentContentDownloader, Duration downloadTimeout) {
        this.connector = connector;
        this.attachmentContentDownloader = attachmentContentDownloader;
        this.downloadTimeout = downloadTimeout;
    }

    /**
     * Get an input stream to download the attachment
     */
    public InputStream getAttachmentStream(AttachmentContent attachment) throws SW360Exception {
        assertNotNull(attachment);

        if (attachment.isOnlyRemote()) {
            attachment = downloadRemoteAttachmentAndUpdate(attachment);
        }

        return readAttachmentStream(attachment);
    }

    private AttachmentContent downloadRemoteAttachmentAndUpdate(AttachmentContent attachmentContent) throws SW360Exception {
        final InputStream downloadStream;

        try {
            downloadStream = attachmentContentDownloader.download(attachmentContent, downloadTimeout);
        } catch (IOException e) {
            String msg = "Cannot download attachment " + attachmentContent.getId() + " from URL";
            log.error(msg, e);
            throw new SW360Exception(msg);
        }


        uploadAttachment(attachmentContent, downloadStream);

        attachmentContent = connector.get(AttachmentContent.class, attachmentContent.getId());
        attachmentContent.setOnlyRemote(false);
        connector.update(attachmentContent);

        return attachmentContent;
    }

    protected InputStream readAttachmentStream(AttachmentContent attachment) {
        int partsCount = -1;

        if (attachment.isSetPartsCount()) {
            partsCount = CommonUtils.toUnsignedInt(attachment.getPartsCount());
        }

        if (partsCount < 0) {
            return connector.getAttachment(attachment.getId(), attachment.getFilename());
        } else {
            return getConcatenatedAttachmentPartsStream(attachment, partsCount);
        }
    }

    protected InputStream getConcatenatedAttachmentPartsStream(final AttachmentContent attachment, final int partsCount) {
        Iterator<InputStream> streams = new Iterator<InputStream>() {
            int part = 1; // the first is part 1 not 0!

            @Override
            public boolean hasNext() {
                return part <= partsCount; // the first is part 1 not 0!
            }

            @Override
            public InputStream next() {
                String attachmentId = attachment.getId();
                String partFileName = getPartFileName(attachment, part);
                part++;
                try {
                    return connector.getAttachment(attachmentId, partFileName);
                } catch (DocumentNotFoundException e) {
                    log.error("Cannot find part " + (part - 1) + " of attachment " + attachmentId, e);
                    return null;
                }
            }

            @Override
            public void remove() {
                //this should be a no-op.
            }
        };

        return new ConcatClosingInputStream(streams);
    }

    /**
     * Upload a single part attachment using the provided metadata
     */
    public void uploadAttachment(AttachmentContent attachment, InputStream stream) throws SW360Exception {
        addAttachmentTo(attachment.getId(), attachment.getFilename(), stream);
    }

    /**
     * Upload a part of an attachment using the provided metadata
     */
    public void uploadAttachmentPart(AttachmentContent attachmentContent, int part, InputStream stream) throws SW360Exception {
        // Extract required data
        assertNotNull(attachmentContent);

        String partFileName = getPartFileName(attachmentContent, part);

        addAttachmentTo(attachmentContent.getId(), partFileName, stream);
    }

    private void addAttachmentTo(String attachmentContentId, String filename, InputStream stream) {
        String contentType = "application/octet-stream";

        AttachmentInputStream attachmentInputStream = new AttachmentInputStream(filename, stream, contentType);
        String revision = connector.getCurrentRevision(attachmentContentId);
        connector.createAttachment(attachmentContentId, revision, attachmentInputStream);
    }

    /**
     * Get an input stream to download a part of the attachment
     */
    public InputStream getAttachmentPartStream(AttachmentContent attachment, int part) throws SW360Exception {
        assertNotNull(attachment);

        return connector.getAttachment(attachment.getId(), getPartFileName(attachment, part));
    }

    private String getPartFileName(AttachmentContent attachment, int part) {
        return attachment.getFilename() + "_part" + part;
    }
}
