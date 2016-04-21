/*
 * Copyright Siemens AG, 2013-2016. Part of the SW360 Portal Project.
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

import com.siemens.sw360.components.summary.LicenseSummary;
import com.siemens.sw360.components.summary.SummaryType;
import com.siemens.sw360.datahandler.couchdb.DatabaseConnector;
import com.siemens.sw360.datahandler.couchdb.SummaryAwareRepository;
import com.siemens.sw360.datahandler.thrift.licenses.License;
import org.ektorp.support.View;

import java.util.List;

/**
 * CRUD access for the License class
 *
 * @author cedric.bodet@tngtech.com
 */
@View(name = "all", map = "function(doc) { if (doc.type == 'license') emit(null, doc._id) }")
public class LicenseRepository extends SummaryAwareRepository<License> {

    private static final String BY_NAME_VIEW = "function(doc) { if(doc.type == 'license') { emit(doc.fullname, doc) } }";
    private static final String BY_SHORT_NAME_VIEW = "function(doc) { if(doc.type == 'license') { emit(doc._id, doc) } }";

    public LicenseRepository(DatabaseConnector db) {
        super(License.class, db, new LicenseSummary());

        initStandardDesignDocument();
    }

    @View(name = "byname", map = BY_NAME_VIEW)
    public List<License> searchByName(String name) {
        return queryByPrefix("byname", name);
    }

    @View(name = "byshortname", map = BY_SHORT_NAME_VIEW)
    public List<License> searchByShortName(String name) {
        return queryByPrefix("byshortname", name);
    }
    public List<License> searchByShortName(List<String> names) {
        return queryByIds("byshortname", names);
    }

    public List<License> getLicenseSummary() {
        return makeSummaryFromFullDocs(SummaryType.SUMMARY, queryView("byname"));
    }

    public List<License> getLicenseSummaryForExport() {
        return makeSummaryFromFullDocs(SummaryType.EXPORT_SUMMARY, queryView("byname"));
    }

}
