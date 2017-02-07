/*
 * Copyright Siemens AG, 2014-2015.
 * Copyright Bosch Software Innovations GmbH, 2017.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.datahandler.couchdb;

import org.apache.log4j.Logger;
import org.eclipse.sw360.datahandler.common.Duration;
import org.eclipse.sw360.datahandler.thrift.SW360Exception;
import org.eclipse.sw360.datahandler.thrift.attachments.AttachmentContent;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author daniele.fognini@tngtech.com
 */
public class AttachmentContentDownloader {

    private static final Logger log = Logger.getLogger(AttachmentContentDownloader.class);

    /**
     * download an incomplete AttachmentContent from its URL
     *
     * @todo setup DI and move timeout to a member
     */
    public InputStream download(AttachmentContent attachmentContent, Duration timeout) throws IOException {
        int millisTimeout = ((Number) timeout.toMillis()).intValue();

        URL remoteURL = new URL(attachmentContent.getRemoteUrl());
        URLConnection urlConnection = remoteURL.openConnection();

        urlConnection.setConnectTimeout(millisTimeout);
        urlConnection.setReadTimeout(millisTimeout);

        InputStream downloadStream = urlConnection.getInputStream();
        return new BufferedInputStream(downloadStream);
    }

    private int getResponseCode(HttpURLConnection httpUrlConnection) throws IOException {
        httpUrlConnection.setConnectTimeout(1000);
        httpUrlConnection.setReadTimeout(1000);
        httpUrlConnection.setRequestMethod("HEAD");
        try {
            return httpUrlConnection.getResponseCode();
        } catch (java.net.SocketTimeoutException e) {
            return 404;
        }
    }

    public int getResponseCode(AttachmentContent attachmentContent) throws IOException {
        if(!attachmentContent.isOnlyRemote()){
            return 200;
        }

        try {
            URL remoteURL = new URL(attachmentContent.getRemoteUrl());
            HttpURLConnection urlConnection = (HttpURLConnection) remoteURL.openConnection();
            return getResponseCode(urlConnection);
        } catch (MalformedURLException e) {
            log.error("The attachment content with id=" + attachmentContent.getId() + " contains an invialid retomeUrl=" + attachmentContent.getRemoteUrl());
            return 404;
        } catch (ProtocolException | UnknownHostException e) {
            return 404;
        }
    }

    public Map<String, Integer> getResponseCodes(Collection<AttachmentContent> attachmentContents) throws IOException {
        Map<String, Integer> mapOfReturnCodes = new HashMap<>();
        for (AttachmentContent attachmentContent : attachmentContents) {
            mapOfReturnCodes.put(attachmentContent.getId(), getResponseCode(attachmentContent));
        }
        return mapOfReturnCodes;
    }
}
