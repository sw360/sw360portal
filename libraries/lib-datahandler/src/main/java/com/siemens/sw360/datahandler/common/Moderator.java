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
package com.siemens.sw360.datahandler.common;

import com.siemens.sw360.datahandler.thrift.ThriftClients;
import com.siemens.sw360.datahandler.thrift.moderation.ModerationRequest;
import com.siemens.sw360.datahandler.thrift.moderation.ModerationService;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import java.util.Collections;
import java.util.List;


/**
 * Base class for Moderators
 *
 * @author johannes.najjar@tngtech.com
 */
public class Moderator {

    protected final ThriftClients thriftClients;
    private static final Logger log = Logger.getLogger(Moderator.class);

    public Moderator(ThriftClients thriftClients) {
        this.thriftClients = thriftClients;
    }

    public void notifyModeratorOnDelete(String documentId) {
        try {
            ModerationService.Iface client = thriftClients.makeModerationClient();
            client.deleteRequestsOnDocument(documentId);
        } catch (TException e) {
            log.error("Could not notify moderation client, that I delete document with id " + documentId, e);
        }
    }

    public List<ModerationRequest> getModerationRequestsForDocumentId(String documentId) {
        try {
            ModerationService.Iface client = thriftClients.makeModerationClient();
            return client.getModerationRequestByDocumentId(documentId);

        } catch (TException e) {
            log.error("Could not get moderations for Document " + documentId, e);
        }
        return Collections.emptyList();
    }
}
