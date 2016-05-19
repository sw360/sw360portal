/*
 * Copyright (c) Bosch Software Innovations GmbH 2016.
 * Part of the SW360 Portal Project.
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
package com.bosch.osmi.sw360.cvesearch.datasource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.siemens.sw360.datahandler.thrift.components.Release;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Collection;

public class CveSearchConnectorImpl implements CveSearchConnector<Collection<CveSearchData>> {

    // TODO: move to properties file
    private String baseURL;

    public CveSearchConnectorImpl(){
        baseURL = "https://cve.circl.lu/api/";
    }

    public CveSearchConnectorImpl(String host){
        baseURL = host + "/api/";
    }

    private Object parseContent(BufferedReader content, Type type) {
        // final Gson gson = new Gson();
        final Gson gson = new GsonBuilder().create();
        return gson.fromJson(content,type);
    }

    private Object getParsedContentFor(String query, Type type) throws IOException {
        String fullQuery = baseURL + query;
        InputStream is = new URL(fullQuery).openStream();

        if (is != null) {
            try {
                BufferedReader content = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                return parseContent(content, type);
            } finally {
                is.close();
            }
        }
        return null;
    }

    protected Collection<CveSearchData> search(String vendor, String product) throws IOException {
        String query = "search/" + URLEncoder.encode(vendor,"UTF-8") + "/" + URLEncoder.encode(product,"UTF-8");
        Type collectionType = new TypeToken<Collection<CveSearchData>>(){}.getType();
        return (Collection<CveSearchData>) getParsedContentFor(query, collectionType);
    }

    private Collection<CveSearchData> searchByFullInfo(Release needle) throws IOException {
        return search(needle.getVendor().getFullname(),needle.getName());
    }

    @Override
    public Collection<CveSearchData> getVulnerabilities(Release needle) throws IOException {
        return searchByFullInfo(needle);
    }
}
