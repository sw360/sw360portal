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

package com.siemens.sw360.attachments.db;

import com.siemens.sw360.datahandler.common.DatabaseSettings;
import com.siemens.sw360.datahandler.common.Duration;
import com.siemens.sw360.datahandler.couchdb.AttachmentConnector;
import com.siemens.sw360.datahandler.couchdb.DatabaseConnector;
import com.siemens.sw360.datahandler.thrift.SW360Exception;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentContent;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.siemens.sw360.datahandler.common.Duration.durationOf;
import static java.lang.String.format;
import static org.apache.log4j.Logger.getLogger;

/**
 * Utility to retrieve remote attachments
 *
 * @author daniele.fognini@tngtech.com
 */
public class RemoteAttachmentDownloader {
    private static final Logger log = getLogger(RemoteAttachmentDownloader.class);

    public static void main(String[] args) throws MalformedURLException {
        String couchDbUrl = DatabaseSettings.COUCH_DB_URL;
        String dbAttachments = DatabaseSettings.COUCH_DB_ATTACHMENTS;

        Duration downloadTimeout = durationOf(30, TimeUnit.SECONDS);
        retrieveRemoteAttachments(couchDbUrl, dbAttachments, downloadTimeout);
    }

    public static int retrieveRemoteAttachments(String couchDbUrl, String dbAttachments, Duration downloadTimeout) throws MalformedURLException {
        AttachmentConnector attachmentConnector = new AttachmentConnector(couchDbUrl, dbAttachments, downloadTimeout);
        AttachmentRepository attachmentRepository = new AttachmentRepository(new DatabaseConnector(couchDbUrl, dbAttachments));

        List<AttachmentContent> remoteAttachments = attachmentRepository.getOnlyRemoteAttachments();
        log.info(format("we have %d remote attachments to retrieve", remoteAttachments.size()));

        int count = 0;

        for (AttachmentContent attachmentContent : remoteAttachments) {
            if (!attachmentContent.isOnlyRemote()) {
                log.info(format("skipping attachment (%s), which should already be available", attachmentContent.getId()));
                continue;
            }

            String attachmentContentId = attachmentContent.getId();
            log.info(format("retrieving attachment (%s) {filename=%s}", attachmentContentId, attachmentContent.getFilename()));
            log.debug("url is " + attachmentContent.getRemoteUrl());

            try {
                InputStream content = attachmentConnector.getAttachmentStream(attachmentContent);
                if (content == null) {
                    log.error("null content retrieving attachment " + attachmentContentId);
                    continue;
                }
                try {
                    long length = length(content);
                    log.info(format("retrieved attachment (%s), it was %d bytes long", attachmentContentId, length));
                    count++;
                } catch (IOException e) {
                    log.error("attachment was downloaded but somehow not available in database " + attachmentContentId, e);
                }
            } catch (SW360Exception e) {
                log.error("cannot retrieve attachment " + attachmentContentId, e);
            }
        }

        return count;
    }

    protected static long length(InputStream stream) throws IOException {
        long length = 0;
        try {
            while (stream.read() >= 0) {
                ++length;
            }
        } finally {
            stream.close();
        }
        return length;
    }
}
