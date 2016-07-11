/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
