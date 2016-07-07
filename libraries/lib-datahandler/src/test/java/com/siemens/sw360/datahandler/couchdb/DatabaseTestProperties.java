/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.siemens.sw360.datahandler.couchdb;

import com.siemens.sw360.datahandler.common.CommonUtils;
import org.apache.log4j.Logger;

import java.util.Properties;

/**
 * Properties class for database connection tests.
 *
 * @author cedric.bodet@tngtech.com
 */
public class DatabaseTestProperties {

    public static final String PROPERTIES_FILE_PATH = "/databasetest.properties";

    public static final String COUCH_DB_URL;
    public static final String COUCH_DB_DATABASE;

    static {
        Properties props = CommonUtils.loadProperties(DatabaseTestProperties.class, PROPERTIES_FILE_PATH);

        COUCH_DB_URL = props.getProperty("couch_db_url", "http://localhost:5984");
        COUCH_DB_DATABASE = props.getProperty("couch_db_database", "datahandlertestdb");
    }
}
