/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.siemens.sw360.datahandler.db;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.siemens.sw360.datahandler.couchdb.DatabaseConnector;
import com.siemens.sw360.datahandler.couchdb.DatabaseRepository;
import com.siemens.sw360.datahandler.permissions.PermissionUtils;
import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.RequestSummary;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentContent;
import com.siemens.sw360.datahandler.thrift.users.User;
import org.ektorp.DocumentOperationResult;
import org.ektorp.ViewQuery;
import org.ektorp.support.View;

import java.util.List;
import java.util.Set;

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
        if (!PermissionUtils.isAdmin(user))
            return requestSummary.setRequestStatus(RequestStatus.FAILURE);

        final List<AttachmentContent> allAttachmentContents = getAll();
        final Set<AttachmentContent> unusedAttachmentContents = FluentIterable.from(allAttachmentContents).filter(new Predicate<AttachmentContent>() {
            @Override
            public boolean apply(AttachmentContent input) {
                return !usedIds.contains(input.getId());
            }
        }).toSet();

        requestSummary.setTotalElements(allAttachmentContents.size());
        requestSummary.setTotalAffectedElements(unusedAttachmentContents.size());

        final List<DocumentOperationResult> documentOperationResults = deleteBulk(unusedAttachmentContents);
        requestSummary.setRequestStatus(RequestStatus.SUCCESS);
        return requestSummary;
    }
}
