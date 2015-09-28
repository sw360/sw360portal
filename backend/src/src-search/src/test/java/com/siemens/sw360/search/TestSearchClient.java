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

import com.siemens.sw360.datahandler.thrift.search.SearchResult;
import com.siemens.sw360.datahandler.thrift.search.SearchService;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;

import java.io.IOException;
import java.util.List;

/**
 * Small client for testing a service
 *
 * @author cedric.bodet@tngtech.com
 */
public class TestSearchClient {

    private static final String searchtext = "s*";

    public static void main(String[] args) throws TException, IOException {
        THttpClient thriftClient = new THttpClient("http://127.0.0.1:8085/search/thrift");
        TProtocol protocol = new TCompactProtocol(thriftClient);
        SearchService.Iface client = new SearchService.Client(protocol);

        List<SearchResult> results = client.search(searchtext, null);
        //List<SearchResult> results = new SearchHandler().search(searchtext);


        //  http://localhost:5984/_fti/local/sw360db/_design/lucene/all?q=type:project%20AND%20P1*

        System.out.println("Fetched " + results.size() + " from search service");
        for (SearchResult result : results) {
            System.out.println(result.getId() + "(" + result.getType() + "): " + result.getName() + " (" + result.getScore() + ")");
        }
    }

}
