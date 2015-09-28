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
