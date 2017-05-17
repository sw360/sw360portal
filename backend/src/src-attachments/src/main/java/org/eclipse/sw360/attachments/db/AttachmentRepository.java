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

package org.eclipse.sw360.attachments.db;

import org.eclipse.sw360.datahandler.couchdb.DatabaseConnector;
import org.eclipse.sw360.datahandler.couchdb.DatabaseRepository;
import org.eclipse.sw360.datahandler.permissions.PermissionUtils;
import org.eclipse.sw360.datahandler.thrift.RequestStatus;
import org.eclipse.sw360.datahandler.thrift.RequestSummary;
import org.eclipse.sw360.datahandler.thrift.attachments.AttachmentContent;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.ektorp.DocumentOperationResult;
import org.ektorp.ViewQuery;
import org.ektorp.support.View;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CRUD access for the Attachment class
 *
 * @author cedric.bodet@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 * @author daniele.fognini@tngtech.com
 */
@View(name = "all", map = "function(doc) { if (doc.type == 'attachment') emit(null, doc._id) }")
public class AttachmentRepository extends DatabaseRepository<AttachmentContent> {

    public AttachmentRepository(DatabaseConnector db) {
        super(AttachmentContent.class, db);

        initStandardDesignDocument();
    }

    private static final String ONLY_REMOTE_VIEW = "function(doc) { if(doc.type == 'attachment' && doc.onlyRemote) { emit(null, doc) } }";

    @View(name = "onlyRemotes", map = ONLY_REMOTE_VIEW)
    public List<AttachmentContent> getOnlyRemoteAttachments() {
        ViewQuery query = createQuery("onlyRemotes");
        query.includeDocs(false);
        return queryView(query);
    }

    public RequestSummary vacuumAttachmentDB(User user, final Set<String> usedIds) {
        final RequestSummary requestSummary = new RequestSummary();
        if (!PermissionUtils.isAdmin(user) || usedIds == null)
            return requestSummary.setRequestStatus(RequestStatus.FAILURE);

        final List<AttachmentContent> allAttachmentContents = getAll();
        final Set<AttachmentContent> unusedAttachmentContents = allAttachmentContents.stream()
                .filter(input -> !usedIds.contains(input.getId()))
                .collect(Collectors.toSet());

        requestSummary.setTotalElements(allAttachmentContents.size());
        requestSummary.setTotalAffectedElements(unusedAttachmentContents.size());

        final List<DocumentOperationResult> documentOperationResults = deleteBulk(unusedAttachmentContents);
        String msg = documentOperationResults.stream()
                .map(dor -> dor.toString())
                .reduce("", (s1,s2)-> s1 + "\n" + s2);
        log.info("vacuumAttachmentDB gave the following output:\n" + msg);
        requestSummary.setRequestStatus(RequestStatus.SUCCESS);
        requestSummary.setMessage(msg);
        return requestSummary;
    }
}
