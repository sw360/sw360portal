/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 * With modifications by Bosch Software Innovations GmbH, 2016.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.search;

import org.eclipse.sw360.datahandler.common.DatabaseSettings;
import org.eclipse.sw360.datahandler.common.SW360Assert;
import org.eclipse.sw360.datahandler.thrift.search.SearchResult;
import org.eclipse.sw360.datahandler.thrift.search.SearchService;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.search.db.DatabaseSearchHandler;
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
