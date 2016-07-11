/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.bosch.osmi.bdp.access.impl.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author johannes.kristan@bosch-si.com
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
