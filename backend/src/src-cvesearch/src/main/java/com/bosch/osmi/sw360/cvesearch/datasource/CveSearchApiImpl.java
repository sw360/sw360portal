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

import com.bosch.osmi.sw360.cvesearch.datasource.json.CveSearchJsonParser;
import com.google.common.base.Strings;
import com.google.gson.reflect.TypeToken;
import com.siemens.sw360.datahandler.common.CommonUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.lang.reflect.Type;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.function.Function;

public class CveSearchApiImpl implements CveSearchApi {

    Logger log = Logger.getLogger(CveSearchApiImpl.class);

    private String host;

    private String CVE_SEARCH_SEARCH  = "search";
    private String CVE_SEARCH_CVEFOR  = "cvefor";
    private String CVE_SEARCH_CVE     = "cve";
    public String CVE_SEARCH_WILDCARD = ".*";

    private Type LIST_TARGET_TYPE = new TypeToken<List<CveSearchData>>(){}.getType();
    private Type SINGLE_TARGET_TYPE = new TypeToken<CveSearchData>(){}.getType();

    public CveSearchApiImpl(String host) {
        this.host = host;
    }

    private Object getParsedContentFor(String query, CveSearchJsonParser parser) throws IOException {
        log.debug("Execute query: " + query);
        InputStream is = new URL(query).openStream();

        if (is != null) {
            try (BufferedReader content = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")))) {
                return parser.parseJsonBuffer(content);
            }
        }
        return null;
    }

    private String composeQuery(String call, String ... path) throws UnsupportedEncodingException {
        String query = host + "/api/" + call;
        for (String p : path){
            query += "/" + URLEncoder.encode(p,"UTF-8");
        }
        return query;
    }

    private List<CveSearchData> getParsedCveSearchDatas(String query) throws IOException {
        return (List<CveSearchData>) getParsedContentFor(query, new CveSearchJsonParser(LIST_TARGET_TYPE));
    }

    private CveSearchData getParsedCveSearchData(String query) throws IOException {
        return (CveSearchData) getParsedContentFor(query, new CveSearchJsonParser(SINGLE_TARGET_TYPE));
    }

    @Override
    public List<CveSearchData> search(String vendor, String product) throws IOException {
        Function<String,String> unifyer = s -> {
            if(Strings.isNullOrEmpty(s)) {
                return CVE_SEARCH_WILDCARD;
            }
            return CommonUtils.nullToEmptyString(s).replace(" ", "_").toLowerCase();
        };

        String query = composeQuery(CVE_SEARCH_SEARCH,
                unifyer.apply(vendor),
                unifyer.apply(product));

        return getParsedCveSearchDatas(query);
    }

    @Override
    public List<CveSearchData> cvefor(String cpe) throws IOException {
        String query = composeQuery(CVE_SEARCH_CVEFOR, cpe.toLowerCase());

        return getParsedCveSearchDatas(query);
    }

    @Override
    public CveSearchData cve(String cve) throws IOException {
        String query = composeQuery(CVE_SEARCH_CVE, cve.toUpperCase());

        return getParsedCveSearchData(query);
    }

}
