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
package com.siemens.sw360.search.db;

import com.github.ldriscoll.ektorplucene.LuceneResult;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.siemens.sw360.datahandler.common.DatabaseSettings;
import com.siemens.sw360.datahandler.couchdb.lucene.LuceneAwareDatabaseConnector;
import com.siemens.sw360.datahandler.couchdb.lucene.LuceneSearchView;
import com.siemens.sw360.datahandler.thrift.search.SearchResult;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class for accessing the Lucene connector on the CouchDB database
 *
 * @author cedric.bodet@tngtech.com
 */
public class DatabaseSearchHandler {

    private static final Logger log = Logger.getLogger(DatabaseSearchHandler.class);

    private static final LuceneSearchView luceneSearchView = new LuceneSearchView("lucene", "all",
            "function(doc) {" +
                    "    var ret = new Document();" +
                    "    if(!doc.type) return ret;" +
                    "    function idx(obj) {" +
                    "        for (var key in obj) {" +
                    "            switch (typeof obj[key]) {" +
                    "                case 'object':" +
                    "                    idx(obj[key]);" +
                    "                    break;" +
                    "                case 'function':" +
                    "                    break;" +
                    "                default:" +
                    "                    ret.add(obj[key]);" +
                    "                    break;" +
                    "            }" +
                    "        }" +
                    "    };" +
                    "    idx(doc);" +
                    "    ret.add(doc.type, {\"field\": \"type\"} );" +
                    "    return ret;" +
                    "}");

    private final LuceneAwareDatabaseConnector connector;

    public DatabaseSearchHandler(String url, String dbName) throws IOException {
        // Create the database connector and add the search view to couchDB
        connector = new LuceneAwareDatabaseConnector(url, dbName);
        connector.addView(luceneSearchView);
        connector.setResultLimit(DatabaseSettings.LUCENE_SEARCH_LIMIT);
    }

    /**
     * Search the database for a given string
     */
    public List<SearchResult> search(String text) {
        String queryString = text + "*";
        return getSearchResults(queryString);
    }

    /**
     * Search the database for a given string and types
     */
    public List<SearchResult> search(String text, final List<String> typeMask) {

        if (typeMask == null || typeMask.isEmpty()) {
            return search(text);
        }

        final Function<String, String> addType = new Function<String, String>() {
            @Override
            public String apply(String input) {
                return "type:"+input;
            }
        };

        String query  = "( "+ Joiner.on(" OR ").join(FluentIterable.from(typeMask).transform(addType)) + " ) AND " +text+"*";
        return getSearchResults(query);
    }

    private List<SearchResult> getSearchResults(String queryString) {
        LuceneResult queryLucene = connector.searchView(luceneSearchView, queryString);
        return convertLuceneResult(queryLucene);
    }

    private static List<SearchResult> convertLuceneResult(LuceneResult queryLucene) {
        List<SearchResult> results = new ArrayList<>();
        if (queryLucene != null) {
            for (LuceneResult.Row row : queryLucene.getRows()) {
                SearchResult result = makeSearchResult(row);
                if (result != null && !result.getName().isEmpty()) {
                    results.add(result);
                }
            }
        }
        return results;
    }

    /**
     * Transforms a LuceneResult row into a Thrift SearchResult object
     */
    private static SearchResult makeSearchResult(LuceneResult.Row row) {
        SearchResult result = new SearchResult();

        // Set row properties
        result.id = row.getId();
        result.score = row.getScore();

        // Get document and
        SearchDocument parser = new SearchDocument(row.getDoc());

        // Get basic search results information
        result.type = parser.getType();
        result.name = parser.getName();

        return result;
    }
}
