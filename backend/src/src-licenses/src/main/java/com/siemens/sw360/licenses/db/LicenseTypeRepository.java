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
package com.siemens.sw360.licenses.db;

import com.siemens.sw360.components.summary.LicenseTypeSummary;
import com.siemens.sw360.components.summary.SummaryType;
import com.siemens.sw360.datahandler.couchdb.DatabaseConnector;
import com.siemens.sw360.datahandler.couchdb.SummaryAwareRepository;
import com.siemens.sw360.datahandler.thrift.licenses.LicenseType;
import org.ektorp.support.View;

import java.util.List;

/**
 * @author birgit.heydenreich@tngtech.com
 */
@View(name = "all", map = "function(doc) { if (doc.type == 'licenseType') emit(null, doc._id) }")
public class LicenseTypeRepository extends SummaryAwareRepository<LicenseType> {

    private static final String BY_NAME_VIEW = "function(doc) { if(doc.type == 'licenseType') { emit(doc.fullname, doc) } }";

    public LicenseTypeRepository(DatabaseConnector db) {
        super(LicenseType.class, db, new LicenseTypeSummary());

        initStandardDesignDocument();
    }

    @View(name = "byname", map = BY_NAME_VIEW)
    public List<LicenseType> searchByName(String name) {
        return queryByPrefix("byname", name);
    }

    public List<LicenseType> getLicenseTypeSummaryForExport() {
        return makeSummaryFromFullDocs(SummaryType.EXPORT_SUMMARY, queryView("byname"));
    }
}
