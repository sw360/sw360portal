/*
 * Copyright Siemens AG, 2014-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
