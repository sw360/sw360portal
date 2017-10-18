/*
 * Copyright Siemens AG, 2017. Part of the SW360 Portal Attachment.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.sw360.rest.resourceserver.attachment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransportException;
import org.eclipse.sw360.datahandler.thrift.attachments.Attachment;
import org.eclipse.sw360.datahandler.thrift.components.ComponentService;
import org.eclipse.sw360.datahandler.thrift.components.Release;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Sw360AttachmentService {
    @Value("${sw360.thrift-server-url:http://localhost:8080}")
    private String thriftServerUrl;

    public AttachmentInfo getAttachmentBySha1ForUser(String sha1, User sw360User) {
        try {
            ComponentService.Iface sw360ComponentClient = getThriftComponentClient();
            List<Release> releases = sw360ComponentClient.getReleaseSummary(sw360User);
            for (Release release : releases) {
                final Set<Attachment> attachments = release.getAttachments();
                if (attachments != null && attachments.size() > 0) {
                    for (Attachment attachment : attachments) {
                        if (sha1.equals(attachment.getSha1())) {
                            return new AttachmentInfo(attachment, release);
                        }
                    }
                }
            }
        } catch (TException e) {
            log.error("Cannot get attachment from sw360 with sha1: " + sha1);
        }
        return null;
    }

    public AttachmentInfo getAttachmentByIdForUser(String id, User sw360User) {
        try {
            ComponentService.Iface sw360ComponentClient = getThriftComponentClient();
            List<Release> releases = sw360ComponentClient.getReleaseSummary(sw360User);
            for (Release release : releases) {
                final Set<Attachment> attachments = release.getAttachments();
                if (attachments != null && attachments.size() > 0) {
                    for (Attachment attachment : attachments) {
                        if (id.equals(attachment.getAttachmentContentId())) {
                            return new AttachmentInfo(attachment, release);
                        }
                    }
                }
            }
        } catch (TException e) {
            log.error("Cannot get attachment from sw360 with id: " + id);
        }
        return null;
    }

    private ComponentService.Iface getThriftComponentClient() throws TTransportException {
        THttpClient thriftClient = new THttpClient(thriftServerUrl + "/components/thrift");
        TProtocol protocol = new TCompactProtocol(thriftClient);
        return new ComponentService.Client(protocol);
    }
}
