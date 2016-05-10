/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
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

package com.bosch.osmi.sw360.bdp.datasource;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class BdpSettings {

    public static final String BDP_FILE = "/etc/sw360/bdp.properties";

    public static final String BDP_SERVER_NAME;
    public static final String BDP_USER_NAME;
    public static final String BDP_PASSWORD;

    public static Properties readProperties(String path) {
        Properties props = new Properties();
        try(InputStream input = new FileInputStream(path)) {
            props.load(input);
        } catch (Exception e) {}

        return props;
    }

    static {
        Properties properties = readProperties(BDP_FILE);

        BDP_SERVER_NAME = properties.getProperty("bdp.server.name");
        BDP_USER_NAME = properties.getProperty("bdp.user.name");
        BDP_PASSWORD = properties.getProperty("bdp.password");
    }

    private BdpSettings(){ /*Hidden*/ }
}
