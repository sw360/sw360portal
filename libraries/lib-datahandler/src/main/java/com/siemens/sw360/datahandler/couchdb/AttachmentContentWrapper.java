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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentContent;

public class AttachmentContentWrapper extends DocumentWrapper<AttachmentContent> {

    @JsonProperty("issetBitfield")
    private byte __isset_bitfield = 0; //TODO set it

    /**
     * must have a copy of all fields in @see Attachment
     */
    public String id; // optional
    public String revision; // optional
    public String type; // optional
    public boolean onlyRemote; // required
    public String remoteUrl; // optional
    public String filename; // required
    public String contentType; // required
    public String partsCount; // optional


    @Override
    public void updateNonMetadata(AttachmentContent source) {
        filename = source.getFilename();
        type = source.getType();
        contentType = source.getContentType();
        remoteUrl = source.getRemoteUrl();
        partsCount = source.getPartsCount();
        remoteUrl = source.getRemoteUrl();
        onlyRemote = source.isOnlyRemote();
    }
}
