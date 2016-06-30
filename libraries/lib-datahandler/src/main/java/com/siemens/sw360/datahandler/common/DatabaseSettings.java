/*
 * Copyright Siemens AG, 2014-2016. Part of the SW360 Portal Project.
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
package com.siemens.sw360.datahandler.common;

import java.util.Properties;

/**
 * Constants for the database address
 *
 * @author cedric.bodet@tngtech.com
 * @author stefan.jaeger@evosoft.com
 */
public class DatabaseSettings {

    public static final String PROPERTIES_FILE_PATH = "/couchdb.properties";

    public static final String COUCH_DB_URL;
    public static final String COUCH_DB_DATABASE;
    public static final String COUCH_DB_ATTACHMENTS;
    public static final String COUCH_DB_FOSSOLOGY;
    public static final String COUCH_DB_USERS;
    public static final String COUCH_DB_VM;

    public static final int LUCENE_SEARCH_LIMIT;

    static {
        Properties props = CommonUtils.loadProperties(DatabaseSettings.class, PROPERTIES_FILE_PATH);

        COUCH_DB_URL = props.getProperty("couchdb.url", "http://localhost:5984");
        COUCH_DB_DATABASE = props.getProperty("couchdb.database", "sw360db");
        COUCH_DB_USERS = props.getProperty("couchdb.usersdb", "sw360users");
        COUCH_DB_ATTACHMENTS = props.getProperty("couchdb.attachments", "sw360attachments");
        COUCH_DB_FOSSOLOGY = props.getProperty("couchdb.fossologyKeys", "sw360fossologyKeys");
        COUCH_DB_VM = props.getProperty("couchdb.vulnerability_management", "sw360vm");

        LUCENE_SEARCH_LIMIT = Integer.parseInt(props.getProperty("lucenesearch.limit", "25"));
    }


    private DatabaseSettings() {
        // Utility class with only static functions
    }

}
