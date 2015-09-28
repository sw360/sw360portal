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

package com.siemens.sw360.portal.common;

import com.liferay.portal.kernel.portlet.PortletResponseUtil;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.util.PortalUtil;
import com.siemens.sw360.datahandler.common.Duration;
import com.siemens.sw360.datahandler.couchdb.AttachmentStreamConnector;
import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.SW360Exception;
import com.siemens.sw360.datahandler.thrift.ThriftClients;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentContent;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentService;
import com.siemens.sw360.datahandler.thrift.attachments.DatabaseAddress;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.ektorp.DocumentNotFoundException;

import javax.portlet.PortletRequest;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.siemens.sw360.datahandler.common.CommonUtils.closeQuietly;
import static java.net.URLConnection.guessContentTypeFromName;
import static java.net.URLConnection.guessContentTypeFromStream;

/**
 * Portlet helpers
 *
 * @author cedric.bodet@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 * @author daniele.fognini@tngtech.com
 */
public class AttachmentPortletUtils {

    private static final Logger log = Logger.getLogger(AttachmentPortletUtils.class);
    private final ThriftClients thriftClients;

    private AttachmentStreamConnector connector;
    // TODO add Config class and DI
    private final Duration downloadTimeout = Duration.durationOf(30, TimeUnit.SECONDS);

    public AttachmentPortletUtils() {
        thriftClients = new ThriftClients();
    }

    public AttachmentPortletUtils(ThriftClients thriftClients) {
        this.thriftClients = thriftClients;
    }

    private synchronized void makeConnector() throws TException {
        if (connector == null) {
            try {
                AttachmentService.Iface client = thriftClients.makeAttachmentClient();
                DatabaseAddress address = client.getDatabaseAddress();
                connector = new AttachmentStreamConnector(address, downloadTimeout);
            } catch (TException e) {
                log.error("Could not get database address from attachment client...", e);
                throw e;
            } catch (MalformedURLException e) {
                log.error("Invalid database address received...", e);
                throw new TException(e);
            }
        }
    }

    private AttachmentStreamConnector getConnector() throws TException {
        if (connector == null) makeConnector();
        return connector;
    }


    public void serveFile(ResourceRequest request, ResourceResponse response) {
        String id = request.getParameter(PortalConstants.ATTACHMENT_ID);

        try {
            AttachmentService.Iface client = thriftClients.makeAttachmentClient();
            AttachmentContent attachment = client.getAttachmentContent(id);
            InputStream attachmentStream = getConnector().getAttachmentStream(attachment);
            try {
                PortletResponseUtil.sendFile(request, response, attachment.getFilename(), attachmentStream, attachment.getContentType());
            } catch (IOException e) {
                log.error("cannot finish writing response", e);
                response.setProperty(ResourceResponse.HTTP_STATUS_CODE, "500");
            } finally {
                closeQuietly(attachmentStream, log);
            }
        } catch (TException e) {
            log.error("Problem getting the attachment content from the backend", e);
        }
    }

    public boolean uploadAttachmentPartFromRequest(PortletRequest request, String fileUploadName) throws IOException, TException {
        final UploadPortletRequest uploadPortletRequest = PortalUtil.getUploadPortletRequest(request);
        final InputStream stream = uploadPortletRequest.getFileAsStream(fileUploadName);

        final ResumableUpload resumableUpload = ResumableUpload.from(uploadPortletRequest);
        AttachmentContent attachment = null;

        if (resumableUpload.isValid()) {
            final AttachmentStreamConnector attachmentStreamConnector = getConnector();

            attachment = getAttachmentContent(resumableUpload, stream);

            if (attachment != null) {
                try {
                    attachmentStreamConnector.uploadAttachmentPart(attachment, resumableUpload.getChunkNumber(), stream);
                } catch (TException e) {
                    log.error("Error saving attachment part", e);
                    return false;
                }
            }
        }

        return attachment != null;
    }

    private AttachmentContent getAttachmentContent(ResumableUpload resumableUpload, InputStream stream) throws IOException, TException {
        if (!resumableUpload.isValid()) {
            return null;
        }

        final AttachmentContent attachment = getAttachmentContent(resumableUpload);
        if (resumableUpload.getChunkNumber() == 1) {
            String fileName = resumableUpload.getFilename();

            String contentType = resumableUpload.getFileType();

            if (isNullOrEmpty(contentType)) {
                contentType = guessContentTypeFromStream(stream);
            }
            if (isNullOrEmpty(contentType)) {
                contentType = guessContentTypeFromName(fileName);
            }
            if (isNullOrEmpty(contentType)) {
                contentType = "text";
            }

            int partsCount = resumableUpload.getTotalChunks();

            attachment.setContentType(contentType)
                    .setFilename(fileName)
                    .setOnlyRemote(false)
                    .setPartsCount(Integer.toString(partsCount));

            return updateAttachmentContent(attachment);
        } else {
            return attachment;
        }
    }

    private AttachmentContent getAttachmentContent(ResumableUpload resumableUpload) {
        AttachmentContent attachment = null;
        if (resumableUpload.hasAttachmentId()) {
            try {
                AttachmentService.Iface client = thriftClients.makeAttachmentClient();
                attachment = client.getAttachmentContent(resumableUpload.getAttachmentId());
            } catch (TException e) {
                log.error("Error retrieving attachment", e);
            }
        }
        return attachment;
    }

    private AttachmentContent updateAttachmentContent(AttachmentContent attachment) throws TException {
        try {
            AttachmentService.Iface client = thriftClients.makeAttachmentClient();
            client.updateAttachmentContent(attachment);
        } catch (SW360Exception e) {
            log.error("Error updating attachment", e);
            return null;
        }
        return attachment;
    }

    public AttachmentContent createAttachmentContent(ResourceRequest request) throws IOException {
        String filename = request.getParameter("fileName");
        AttachmentContent attachmentContent = new AttachmentContent()
                .setContentType("application/octet-stream")
                .setFilename(filename);

        try {
            AttachmentService.Iface client = thriftClients.makeAttachmentClient();
            attachmentContent = client.makeAttachmentContent(attachmentContent);
        } catch (TException e) {
            log.error("Error creating attachment", e);
            attachmentContent = null;
        }

        return attachmentContent;
    }

    public boolean uploadAttachmentPart(PortletRequest request, String fileUploadName) throws IOException {
        try {
            return uploadAttachmentPartFromRequest(request, fileUploadName);
        } catch (TException e) {
            log.error("Error getting attachment and saving it", e);
            return false;
        }
    }

    public RequestStatus cancelUpload(ResourceRequest request) {
        String attachmentId = request.getParameter(PortalConstants.ATTACHMENT_ID);
        try {
            AttachmentService.Iface client = thriftClients.makeAttachmentClient();
            return client.deleteAttachmentContent(attachmentId);
        } catch (TException e) {
            log.error("Error deleting attachment from backend", e);
            return RequestStatus.FAILURE;
        }
    }

    public boolean checkAttachmentExistsFromRequest(ResourceRequest request) {
        ResumableUpload resumableUpload = ResumableUpload.from(request);

        final AttachmentContent attachment = getAttachmentContent(resumableUpload);

        return alreadyHavePart(attachment, resumableUpload);
    }

    private boolean alreadyHavePart(AttachmentContent attachment, ResumableUpload resumableUpload) {
        if (attachment == null || !resumableUpload.isValid()) {
            return false;
        }

        AttachmentStreamConnector attachmentStreamConnector;
        try {
            attachmentStreamConnector = getConnector();
        } catch (TException e) {
            log.error("no connector", e);
            return false;
        }

        try {
            attachmentStreamConnector.getAttachmentPartStream(attachment, resumableUpload.getChunkNumber()).close();
            return true;
        } catch (SW360Exception e) {
            log.error("cannot check if part already exists", e);
            return false;
        } catch (IOException | DocumentNotFoundException ignored) {
            return false;
        }
    }

}
