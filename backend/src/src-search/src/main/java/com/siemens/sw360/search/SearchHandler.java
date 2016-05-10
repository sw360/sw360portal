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
package com.siemens.sw360.search;

import com.siemens.sw360.datahandler.common.DatabaseSettings;
import com.siemens.sw360.datahandler.common.SW360Assert;
import com.siemens.sw360.datahandler.thrift.search.SearchResult;
import com.siemens.sw360.datahandler.thrift.search.SearchService;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.search.db.DatabaseSearchHandler;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Implementation of the Thrift service
 *
 * @author cedric.bodet@tngtech.com
 */
public class SearchHandler implements SearchService.Iface {

    private static final Logger log = Logger.getLogger(SearchHandler.class);

    private final DatabaseSearchHandler db;

    public SearchHandler() throws IOException {
        db = new DatabaseSearchHandler(DatabaseSettings.COUCH_DB_URL, DatabaseSettings.COUCH_DB_DATABASE);
    }


    @Override
    public List<SearchResult> searchFiltered(String text, User user, List<String> typeMask) throws TException {
        if(text == null) throw new TException("Search text was null.");
        if("".equals(text)) return Collections.emptyList();

        // Query new and old database
        List<SearchResult> results = db.search(text, typeMask, user);
        Collections.sort(results, new SearchResultComparator());

        if (log.isTraceEnabled())
            log.trace("Search for " + text + " returned " + results.size() + " results");

        return results;
    }

    @Override
    public List<SearchResult> search(String text, User user) throws TException {
        return searchFiltered(text,user,null);
    }


    /**
     * Comparator to provide ordered search results
     */
    public class SearchResultComparator implements Comparator<SearchResult> {

        @Override
        public int compare(SearchResult o1, SearchResult o2) {
            return -Double.compare(o1.getScore(), o2.getScore());
        }

    }

}
