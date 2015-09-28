/*
 * Copyright Siemens AG, 2014-2015. Part of the SW360 Portal Project.
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
package com.siemens.sw360.datahandler.couchdb;

import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbInstance;

import java.net.MalformedURLException;

/**
 * Class for connecting to a given CouchDB instance
 */
public class DatabaseInstance extends StdCouchDbInstance {

    private final String url;

    /**
     * Builds a CouchDB instance using ektorp
     *
     * @param url URL of the CouchDB instance
     * @throws MalformedURLException
     */
    public DatabaseInstance(String url) throws MalformedURLException {
        super(new StdHttpClient.Builder().url(url).build());
        this.url = url;
        DatabaseInstanceTracker.track(this);
    }

    @Override
    public void createDatabase(String dbName) {
        if (!checkIfDbExists(dbName)) {
            super.createDatabase(dbName);
        }
    }

    public void destroy() {
        getConnection().shutdown();
    }

    public String getUrl() {
        return url;
    }
}
