/*
 * Copyright Siemens AG, 2014-2016.
 * Copyright Bosch Software Innovations GmbH, 2017.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.datahandler.couchdb;

import org.eclipse.sw360.datahandler.common.CommonUtils;
import org.eclipse.sw360.datahandler.common.ConcatClosingInputStream;
import org.eclipse.sw360.datahandler.common.DatabaseSettings;
import org.eclipse.sw360.datahandler.common.Duration;
import org.eclipse.sw360.datahandler.thrift.SW360Exception;
import org.eclipse.sw360.datahandler.thrift.attachments.AttachmentContent;
import org.apache.log4j.Logger;
import org.ektorp.AttachmentInputStream;
import org.ektorp.DocumentNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.MalformedURLException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.eclipse.sw360.datahandler.common.SW360Assert.assertNotNull;

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
     * @param downloadTimeout timeout for downloading remote attachments
     * @throws java.net.MalformedURLException if the given database address not a valid url.
     * @todo remove this mess of constructors and use dependency injection
     */
    public AttachmentStreamConnector(Duration downloadTimeout) throws MalformedURLException {
        this(new DatabaseConnector(DatabaseSettings.getConfiguredHttpClient().get(), DatabaseSettings.COUCH_DB_ATTACHMENTS), downloadTimeout);
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
     * It is highly recommended to close this stream after using to avoid connection leak
     */
    public InputStream getAttachmentStream(AttachmentContent attachment) throws SW360Exception {
        assertNotNull(attachment);

        if (attachment.isOnlyRemote()) {
            try {
                int responseCode = attachmentContentDownloader.getResponseCode(attachment);

                if (responseCode >= 300) {
                    String msg = "Tried to download remote attachment " + attachment.getId() + " but request returned response code=" + responseCode;
                    log.warn(msg);
                    throw new SW360Exception(msg);
                }
            } catch (IOException e) {
                String msg = "Failed to determine response code of attachment " + attachment.getId();
                log.error(msg, e);
                throw new SW360Exception(msg);
            }

            if (attachment.isWantsToStayRemote()) {
                return downloadRemoteAttachment(attachment);
            }else{
                attachment = downloadRemoteAttachmentAndUpdate(attachment);
            }
        }

        return readAttachmentStream(attachment);
    }

    /**
     * It is highly recommended to close this stream after using to avoid connection leak
     */
    public InputStream getAttachmentBundleStream(Set<AttachmentContent> attachments) throws IOException {
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);

        new Thread(() -> {
            byte[] buffer = new byte[1024];
            int length;

            try(ZipOutputStream zip = new ZipOutputStream(out)){
                for(AttachmentContent attachment : attachments) {
                    // TODO: handle attachments with equal name
                    ZipEntry zipEntry = new ZipEntry(attachment.getFilename());
                    zip.putNextEntry(zipEntry);

                    try(InputStream attachmentStream = getAttachmentStream(attachment)) {
                        while ((length = attachmentStream.read(buffer)) >= 0) {
                            zip.write(buffer, 0, length);
                        }
                    }

                    zip.closeEntry();
                }
            } catch (IOException | SW360Exception e) {
                log.error("failed to write zip stream", e);
            }
        }).start();

        return in;
    }

    private InputStream downloadRemoteAttachment(AttachmentContent attachmentContent) throws SW360Exception {
        try {
            return attachmentContentDownloader.download(attachmentContent, downloadTimeout);
        } catch (IOException e) {
            String msg = "Cannot download attachment " + attachmentContent.getId() + " from URL";
            log.error(msg, e);
            throw new SW360Exception(msg);
        }
    }

    private AttachmentContent downloadRemoteAttachmentAndUpdate(AttachmentContent attachmentContent) throws SW360Exception {
        final InputStream downloadStream = downloadRemoteAttachment(attachmentContent);

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

    public Map<String,Integer> getResponseCodes(List<AttachmentContent> attachments) throws IOException {
        return attachmentContentDownloader.getResponseCodes(attachments);
    }
}
