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

package com.siemens.sw360.moderation.db;

import com.siemens.sw360.components.summary.ModerationRequestSummary;
import com.siemens.sw360.components.summary.SummaryType;
import com.siemens.sw360.datahandler.couchdb.DatabaseConnector;
import com.siemens.sw360.datahandler.couchdb.SummaryAwareRepository;
import com.siemens.sw360.datahandler.thrift.moderation.ModerationRequest;
import org.ektorp.support.View;

import java.util.List;

/**
 * CRUD access for the ModerationRequest class
 *
 * @author cedric.bodet@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 */
@View(name = "all", map = "function(doc) { if (doc.type == 'moderation') emit(null, doc._id) }")
public class ModerationRequestRepository extends SummaryAwareRepository<ModerationRequest> {

    private static final String DOCUMENTS_VIEW = "function(doc) { " +
            "  if (doc.type == 'moderation') {" +
            "    emit(doc.documentId, doc._id);" +
            "  }" +
            "}";

    private static final String USERS_VIEW = "function(doc) { " +
            "  if (doc.type == 'moderation') {" +
            "    emit(doc.requestingUser, doc);" +
            "    }" +
            "}";

    private static final String MODERATORS_VIEW = "function(doc) {" +
            "  if (doc.type == 'moderation') {" +
            "    for(var i in doc.moderators) {" +
            "      emit(doc.moderators[i], doc);" +
            "    }" +
            "  }" +
            "}";

    public ModerationRequestRepository(DatabaseConnector db) {
        super(ModerationRequest.class, db, new ModerationRequestSummary());

        initStandardDesignDocument();
    }

    @View(name = "documents", map = DOCUMENTS_VIEW)
    public List<ModerationRequest> getRequestsByDocumentId(String documentId) {
        return queryView("documents", documentId);
    }

    @View(name = "moderators", map = MODERATORS_VIEW)
    public List<ModerationRequest> getRequestsByModerator(String moderator) {
        return makeSummaryFromFullDocs(SummaryType.SHORT, queryView("moderators", moderator));
    }

    @View(name = "users", map = USERS_VIEW)
    public List<ModerationRequest> getRequestsByRequestingUser(String user) {
        return makeSummaryFromFullDocs(SummaryType.SHORT, queryView("users", user));
    }

}
