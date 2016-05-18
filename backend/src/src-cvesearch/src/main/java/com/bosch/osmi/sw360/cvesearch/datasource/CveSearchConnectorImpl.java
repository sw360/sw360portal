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
import com.siemens.sw360.datahandler.thrift.components.Release;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;

public class CveSearchConnectorImpl implements CveSearchConnector<String> {

    // TODO: move to properties file
    private String baseURL="https://cve.circl.lu/api/";

    private String doPOST(String query) throws IOException {
        String fullQuery = baseURL + URLEncoder.encode(query,"UTF-8");
        InputStream is = new URL(fullQuery).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            Gson gson = new Gson();
            return gson.fromJson(rd,String.class);
        } finally {
            is.close();
        }
    }

    private String searchByFullInfo(Release needle) throws IOException {
        String query = "search/" + needle.getVendor() + "/" + needle.getName();
        return doPOST(query);
    }

    @Override
    public String getVulnerabilities(Release needle) throws IOException {
        return searchByFullInfo(needle);
    }
}
