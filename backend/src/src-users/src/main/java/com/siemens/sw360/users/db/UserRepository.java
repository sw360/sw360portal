/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.users.db;

import com.google.common.base.Strings;
import com.siemens.sw360.components.summary.SummaryType;
import com.siemens.sw360.components.summary.UserSummary;
import com.siemens.sw360.datahandler.couchdb.DatabaseConnector;
import com.siemens.sw360.datahandler.couchdb.SummaryAwareRepository;
import com.siemens.sw360.datahandler.thrift.users.User;
import org.ektorp.support.View;

import java.util.List;

/**
 * CRUD access for the User class
 *
 * @author cedric.bodet@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 */

@View(name = "all", map = "function(doc) { if (doc.type == 'user') emit(null, doc._id) }")
public class UserRepository extends SummaryAwareRepository<User> {
    public UserRepository(DatabaseConnector databaseConnector) {
        super(User.class, databaseConnector, new UserSummary());
        initStandardDesignDocument();
    }

    private static final String BY_NAME_VIEW =
            "function(doc) {" +
                    "  if (doc.type == 'user') {" +
                    "    emit(doc.lastname, doc);" +
                    "  }" +
                    "}";

    @View(name = "byname", map = BY_NAME_VIEW)
    public List<User> searchByName(String name) {

        final List<User> byname;
        if (Strings.isNullOrEmpty(name)) {
            byname = queryView("byname");
        } else {
            byname = queryByPrefix("byname", name);
        }
        return makeSummaryFromFullDocs(SummaryType.SUMMARY, byname);
    }

}
