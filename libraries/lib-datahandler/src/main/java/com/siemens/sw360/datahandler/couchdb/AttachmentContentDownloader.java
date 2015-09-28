/*
 * Copyright Siemens AG, 2014-2015. Part of the SW360 Portal Project.
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
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentContent;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author daniele.fognini@tngtech.com
 */
public class AttachmentContentDownloader {
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
}
