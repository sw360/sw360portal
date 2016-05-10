/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
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

package com.bosch.osmi.bdp.access.impl.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author muj1be
 * @since 11/19/15.
 */
public class Util {

    public static String getHomeDir(){
        return System.getProperty("user.home");
    }

    public static Properties readBdpAccessImplProperties() throws IOException {
        String homeDir = getHomeDir();
        String propertyPath = homeDir + File.separator + ".bdp-access-impl.properties";
        return readProperties(propertyPath);
    }

    public static Properties readProperties(String path) throws IOException {
        Properties props = new Properties();
        InputStream input = new FileInputStream(path);
        props.load(input);
        return props;
    }

}
