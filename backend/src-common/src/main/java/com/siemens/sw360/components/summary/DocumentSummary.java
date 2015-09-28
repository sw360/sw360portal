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
package com.siemens.sw360.components.summary;

import com.siemens.sw360.datahandler.permissions.DocumentPermissions;
import com.siemens.sw360.datahandler.permissions.PermissionUtils;
import com.siemens.sw360.datahandler.thrift.users.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by bodet on 17/02/15.
 *
 * @author cedric.bodet@tngtech.com
 *
 * This does some processing of the documents to trim unneeded fields away and fill computed fields.
 */
public abstract class DocumentSummary<T> {

    protected abstract T summary(SummaryType type, T document);

    public T makeSummary(SummaryType type, T document) {
        if (document == null) return null;
        return summary(type, document);
    }

    public List<T> makeSummary(SummaryType type, Collection<T> fullDocuments) {
        if (fullDocuments == null) return Collections.emptyList();

        List<T> documents = new ArrayList<>(fullDocuments.size());
        for (T fullDocument : fullDocuments) {
            T document = makeSummary(type, fullDocument);
            if (document != null) documents.add(document);
        }
        return documents;
    }

    public T makeSummaryWithPermissions(SummaryType type, T document, User user) {
        if (document == null) return null;
        DocumentPermissions<T> permissions = PermissionUtils.makePermission(document, user);
        T summary = makeSummary(type, document);
        permissions.fillPermissionsInOther(summary);
        return summary;
    }

    public List<T> makeSummaryWithPermissionsFromFullDocs(SummaryType type, Collection<T> docs, User user) {
        if (docs == null) {
            return Collections.emptyList();
        }

        List<T> documents = new ArrayList<>();
        for (T doc : docs) {
            T document = makeSummaryWithPermissions(type, doc, user);
            if (document != null) {
                documents.add(document);
            }
        }
        return documents;
    }

}
