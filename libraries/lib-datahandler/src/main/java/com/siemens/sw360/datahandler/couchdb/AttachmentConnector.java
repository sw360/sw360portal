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

import com.google.common.collect.Sets;
import com.siemens.sw360.datahandler.common.Duration;
import com.siemens.sw360.datahandler.thrift.SW360Exception;
import com.siemens.sw360.datahandler.thrift.attachments.Attachment;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentContent;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.siemens.sw360.datahandler.common.CommonUtils.nullToEmptyCollection;
import static com.siemens.sw360.datahandler.common.SW360Assert.assertNotEmpty;
import static org.apache.commons.codec.digest.DigestUtils.sha1Hex;

import org.apache.log4j.Logger;

/**
 * Ektorp connector for uploading attachments
 *
 * @author cedric.bodet@tngtech.com
 * @author alex.borodin@evosoft.com
 */
public class AttachmentConnector extends AttachmentStreamConnector {

    private static Logger log = Logger.getLogger(AttachmentConnector.class);

    public AttachmentConnector(DatabaseConnector connector, Duration downloadTimeout) {
        super(connector, downloadTimeout);
    }

    /**
     * @todo remove this mess of constructors and use dependency injection
     */
    public AttachmentConnector(String url, String dbName, Duration downloadTimeout) throws MalformedURLException {
        this(new DatabaseConnector(url, dbName), downloadTimeout);
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
        Set<String> attachmentContentIds = getAttachmentContenIds(attachments);
        deleteAttachmentsByIds(attachmentContentIds);
    }

    private void deleteAttachmentsByIds(Collection<String> attachmentContentIds) {
        connector.deleteIds(attachmentContentIds, AttachmentContent.class);
    }

    private Set<String> getAttachmentContenIds(Collection<Attachment> attachments) {
        return nullToEmptyCollection(attachments).stream()
                .map(Attachment::getAttachmentContentId)
                .collect(Collectors.toSet());
    }

    public void deleteAttachmentDifference(Set<Attachment> before, Set<Attachment> after){
        // it is important to take the set difference between sets of ids, not of attachments themselves
        // otherwise, when `after` contains the same attachment (with the same id), but with one field changed (e.g. sha1),
        // then they are considered unequal and the set difference will contain this attachment and therefore
        // deleteAttachments(Collection<Attachment>) will delete an attachment that is present in `after`
        deleteAttachmentsByIds(Sets.difference(getAttachmentContenIds(before), getAttachmentContenIds(after)));
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

    public void setSha1ForAttachments(Set<Attachment> attachments){
        for(Attachment attachment : attachments){
            if(isNullOrEmpty(attachment.getSha1())){
                String sha1 = getSha1FromAttachmentContentId(attachment.getAttachmentContentId());
                attachment.setSha1(sha1);
            }
        }
    }
}
