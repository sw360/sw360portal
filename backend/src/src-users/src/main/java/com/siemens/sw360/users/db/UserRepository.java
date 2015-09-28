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
