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

import com.siemens.sw360.components.summary.DocumentSummary;
import com.siemens.sw360.components.summary.SummaryType;
import com.siemens.sw360.datahandler.thrift.users.User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by bodet on 17/02/15.
 *
 * @author cedric.bodet@tngtech.com
 */
public class SummaryAwareRepository<T> extends DatabaseRepository<T> {

    protected final DocumentSummary<T> summary;

    public SummaryAwareRepository(Class<T> type, DatabaseConnector databaseConnector, DocumentSummary<T> summary) {
        super(type, databaseConnector);

        this.summary = summary;
    }

    public List<T> makeSummary(SummaryType type, Collection<String> ids) {
        if (ids == null) {
            return Collections.emptyList();
        }

        List<T> documents = get(ids);

        return makeSummaryFromFullDocs(type, documents);
    }

    public List<T> makeSummaryFromFullDocs(SummaryType type, Collection<T> docs) {
        return summary.makeSummary(type, docs);
    }


    public List<T> makeSummaryWithPermissions(SummaryType type, Collection<String> ids, User user) {
        if (ids == null) {
            return Collections.emptyList();
        }

        List<T> documents = get(ids);
        return makeSummaryWithPermissionsFromFullDocs(type, documents, user);
    }

    public List<T> makeSummaryWithPermissionsFromFullDocs(SummaryType type, Collection<T> docs, User user) {
        return summary.makeSummaryWithPermissionsFromFullDocs(type,docs,user);
    }

}
