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
package com.siemens.sw360.datahandler.db;

import com.siemens.sw360.datahandler.couchdb.DatabaseConnector;
import com.siemens.sw360.datahandler.couchdb.lucene.LuceneAwareDatabaseConnector;
import com.siemens.sw360.datahandler.couchdb.lucene.LuceneSearchView;
import com.siemens.sw360.datahandler.thrift.vendors.Vendor;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Lucence search for the Vendor class
 *
 * @author cedric.bodet@tngtech.com
 * @author johannes.najjar@tngtech.com
 * @author gerrit.grenzebach@tngtech.com
 */
public class VendorSearch {

    private static final LuceneSearchView luceneSearchView
            = new LuceneSearchView("lucene", "vendors",
            "function(doc) {" +
                    "  if(doc.type == 'vendor') { " +
                    "      var ret = new Document();" +
                    "      ret.add(doc.shortname);  " +
                    "      ret.add(doc.fullname);  " +
                    "      return ret;" +
                    "  }" +
                    "}");

    private final LuceneAwareDatabaseConnector connector;

    public VendorSearch(DatabaseConnector databaseConnector) throws IOException {
        // Creates the database connector and adds the lucene search view
        connector = new LuceneAwareDatabaseConnector(databaseConnector);
        connector.addView(luceneSearchView);
    }

    public List<Vendor> search(String searchText) {
        // Query the search view for the provided text
        return connector.searchView(Vendor.class, luceneSearchView, searchText+"*");
    }

    public Set<String> searchIds(String searchText) {
        // Query the search view for the provided text
        return connector.searchIds(Vendor.class, luceneSearchView, searchText+"*");
    }
}

