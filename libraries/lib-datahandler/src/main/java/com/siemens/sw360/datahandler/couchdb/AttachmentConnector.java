/*
 * Copyright Siemens AG, 2014-2016. Part of the SW360 Portal Project.
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
package com.siemens.sw360.datahandler.couchdb;

import com.siemens.sw360.datahandler.common.Duration;
import com.siemens.sw360.datahandler.thrift.SW360Exception;
import com.siemens.sw360.datahandler.thrift.attachments.Attachment;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentContent;
import com.siemens.sw360.datahandler.thrift.attachments.DatabaseAddress;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashSet;

import static com.siemens.sw360.datahandler.common.CommonUtils.nullToEmptyCollection;
import static com.siemens.sw360.datahandler.common.SW360Assert.assertNotEmpty;
import static org.apache.commons.codec.digest.DigestUtils.sha1Hex;

import org.apache.log4j.Logger;

/**
 * Ektorp connector for uploading attachments
 *
 * @author cedric.bodet@tngtech.com
 */
public class AttachmentConnector extends AttachmentStreamConnector {

    private static Logger log = Logger.getLogger(AttachmentConnector.class);

    public AttachmentConnector(DatabaseAddress address, Duration downloadTimeout) throws MalformedURLException {
        super(address, downloadTimeout);
    }

    /**
     * @todo remove this mess of constructors and use dependency injection
     */
    public AttachmentConnector(String url, String dbName, Duration downloadTimeout) throws MalformedURLException {
        this(new DatabaseAddress(url, dbName), downloadTimeout);
    }

    /**
     * Update the database with new attachment metadata
     */
    public void updateAttachmentContent(AttachmentContent attachment) throws SW360Exception {
        connector.update(attachment);
    }

    /**
     * Get attachment metadata from attachmentId
     */
    public AttachmentContent getAttachmentContent(String attachmentContentId) throws SW360Exception {
        assertNotEmpty(attachmentContentId);

        return connector.get(AttachmentContent.class, attachmentContentId);
    }

    public void deleteAttachment(String id) {
        connector.deleteById(id);
    }

    public void deleteAttachments(Collection<Attachment> attachments) {
        final Collection<String> attachmentContentIds = new HashSet<>();

        for (Attachment attachment : nullToEmptyCollection(attachments)) {
            attachmentContentIds.add(attachment.getAttachmentContentId());
        }

        connector.deleteIds(attachmentContentIds, AttachmentContent.class);
    }

    public String getSha1FromAttachmentContentId(String attachmentContentId) {
        try {
            AttachmentContent attachmentContent = getAttachmentContent(attachmentContentId);
            InputStream attachmentStream = readAttachmentStream(attachmentContent);
            return sha1Hex(attachmentStream);
        } catch (SW360Exception e) {
            log.error("Problem retrieving content of attachment", e);
            return "";
        } catch (IOException e) {
            log.error("Problem computing the sha1 checksum", e);
            return "";
        }
    }
}
