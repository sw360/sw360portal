/*
 * Copyright Siemens AG, 2013-2015.
 * Copyright Bosch Software Innovations GmbH, 2017.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.sw360.portal.common;

import com.liferay.portal.kernel.portlet.PortletResponseUtil;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.util.PortalUtil;
import org.eclipse.sw360.datahandler.common.CommonUtils;
import org.eclipse.sw360.datahandler.common.Duration;
import org.eclipse.sw360.datahandler.couchdb.AttachmentStreamConnector;
import org.eclipse.sw360.datahandler.thrift.RequestStatus;
import org.eclipse.sw360.datahandler.thrift.SW360Exception;
import org.eclipse.sw360.datahandler.thrift.ThriftClients;
import org.eclipse.sw360.datahandler.thrift.attachments.Attachment;
import org.eclipse.sw360.datahandler.thrift.attachments.AttachmentContent;
import org.eclipse.sw360.datahandler.thrift.attachments.AttachmentService;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.ektorp.DocumentNotFoundException;

import javax.portlet.PortletRequest;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.net.URLConnection.guessContentTypeFromName;
import static java.net.URLConnection.guessContentTypeFromStream;
import static org.apache.commons.io.FilenameUtils.getName;

/**
 * Portlet helpers
 *
 * @author cedric.bodet@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 * @author daniele.fognini@tngtech.com
 * @author birgit.heydenreich@tngtech.com
 */
public class AttachmentPortletUtils {

    private static final Logger log = Logger.getLogger(AttachmentPortletUtils.class);
    private static final String DEFAULT_ATTACHMENT_BUNDLE_NAME = "AttachmentBundle.zip";
    private final ThriftClients thriftClients;
    private AttachmentService.Iface client;

    private AttachmentStreamConnector connector;
    // TODO add Config class and DI
    private final Duration downloadTimeout = Duration.durationOf(30, TimeUnit.SECONDS);

    public AttachmentPortletUtils() {
        this(new ThriftClients());
    }

    public AttachmentPortletUtils(ThriftClients thriftClients) {
        this.thriftClients = thriftClients;
        client = thriftClients.makeAttachmentClient();
    }

    private synchronized void makeConnector() throws TException {
        if (connector == null) {
            try {
                connector = new AttachmentStreamConnector(downloadTimeout);
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

    protected InputStream getStreamToServeAFile(List<AttachmentContent> attachments) throws TException, IOException {
        if(attachments == null || attachments.size() == 0){
            throw new SW360Exception("Tried to download empty set of Attachments");
        }else if(attachments.size() == 1){
            return getConnector().getAttachmentStream(attachments.get(0));
        } else {
            return getConnector().getAttachmentBundleStream(attachments.stream().collect(Collectors.toSet()));
        }
    }

    public void serveFile(ResourceRequest request, ResourceResponse response) throws IOException {
        serveFile(request, response, Optional.empty());
    }

    protected Optional<Set<String>> getAttachmentIDsFromRequent(ResourceRequest request, ResourceResponse response) {
        String[] rawIds = request.getParameterValues(PortalConstants.ATTACHMENT_ID);
        if(rawIds == null || rawIds.length == 0){
            log.warn("no attachmentId was found in the request passed to serveFile");
            response.setProperty(ResourceResponse.HTTP_STATUS_CODE, "400");
            return Optional.empty();
        }
        return Optional.of(new HashSet<>(Arrays.asList(rawIds)));
    }

    protected Optional<List<AttachmentContent>> getAttachmentContentsByIds(Collection<String> attachmentIds, ResourceResponse response) {
        try {
            List<AttachmentContent> attachments = new ArrayList<>();
            for(String id : attachmentIds){
                attachments.add(client.getAttachmentContent(id));
            }
            return Optional.of(attachments);
        } catch (TException e) {
            log.error("Problem getting the AttachmentContents from the backend", e);
            response.setProperty(ResourceResponse.HTTP_STATUS_CODE, "500");
        }
        return Optional.empty();
    }

    public void serveFile(ResourceRequest request, ResourceResponse response, Optional<String> downloadFileName) throws IOException {
        getAttachmentIDsFromRequent(request, response)
                .ifPresent(ids -> serveAttachmentBundleByIds(ids, request, response, downloadFileName));
    }

    public void serveAttachmentBundleByIds(Collection<String> attachmentIds, ResourceRequest request, ResourceResponse response, Optional<String> downloadFileName){
        getAttachmentContentsByIds(attachmentIds, response)
                .ifPresent(attachments -> serveAttachmentBundle(attachments, request, response, downloadFileName));
    }

    protected void serveAttachmentBundle(List<AttachmentContent> attachments, ResourceRequest request, ResourceResponse response, Optional<String> downloadFileName){
        String filename;
        String contentType;
        if(attachments.size() == 1){
            AttachmentContent singleAttachment = attachments.stream().findAny().get();
            filename = downloadFileName
                    .orElse(singleAttachment.getFilename());
            contentType = singleAttachment.getContentType();
        } else {
            filename = downloadFileName
                    .orElse(DEFAULT_ATTACHMENT_BUNDLE_NAME);
            contentType = "application/zip";
        }

        try (InputStream attachmentStream = getStreamToServeAFile(attachments)) {
            PortletResponseUtil.sendFile(request, response, filename, attachmentStream, contentType);
        } catch (TException e) {
            log.error("Problem getting the attachment content from the backend", e);
            response.setProperty(ResourceResponse.HTTP_STATUS_CODE, "500");
        } catch (IOException e) {
            log.error("cannot finish writing response", e);
            response.setProperty(ResourceResponse.HTTP_STATUS_CODE, "500");
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

    public Attachment getAttachmentForDisplay(User user, String attachmentContentId) {
        try {
            String filename = client.getAttachmentContent(attachmentContentId).getFilename();
            return CommonUtils.getNewAttachment(user, attachmentContentId, filename);
        } catch (TException e) {
            log.error("Could not get attachment content", e);
        }
        return null;
    }

    public void deleteAttachments(Set<String> attachmentContentIds){
        try {
            for(String id: attachmentContentIds) {
                client.deleteAttachmentContent(id);
            }
        } catch (TException e){
            log.error("Could not delete attachments from database.",e);
        }
    }

    public AttachmentContent addRemoteAttachment(ResourceRequest request) {
        String rawUrl = request.getParameter("url");
        if (rawUrl == null || rawUrl.length() == 0){
            log.info("Empty URL given in request, ignore request");
            return null;
        }
        URL url;
        try {
            url = new URL(rawUrl);
        } catch (MalformedURLException e) {
            log.info("Invalid URL given in request, ignore request: ", e);
            return null;
        }

        String filename = request.getParameter("Filename");
        if (filename == null || filename.length() == 0){
            filename = getName(url.getPath());
        }

        AttachmentContent attachmentContent = new AttachmentContent()
                .setContentType("application/octet-stream")
                .setFilename(filename)
                .setRemoteUrl(url.toString())
                .setOnlyRemote(true)
                .setWantsToStayRemote(true);

        AttachmentService.Iface client = thriftClients.makeAttachmentClient();
        try {
            attachmentContent = client.makeAttachmentContent(attachmentContent);
        } catch (TException e) {
            log.error("failed to write remote attachment to DB. url=["+url.toString()+"] filename=["+filename, e);
            return null;
        }
        return attachmentContent;
    }
}
