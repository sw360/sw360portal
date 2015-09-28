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
package com.siemens.sw360.fossology.config;

import com.siemens.sw360.datahandler.common.CommonUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.apache.log4j.Logger.getLogger;

/**
 * Constants for the fossology host connection
 *
 * @author daniele.fognini@tngtech.com
 */
@Component
public class FossologySettings {

    private static final String PROPERTIES_FILE_PATH = "/fossology.properties";

    private static final int FOSSOLOGY_CONNECTION_TIMEOUT = 30000;
    private static final long FOSSOLOGY_EXECUTION_TIMEOUT = 100000;
    private static final String FOSSOLOGY_HOST;
    private static final int FOSSOLOGY_PORT;
    private static final String FOSSOLOGY_SSH_USERNAME;
    private static final byte[] FOSSOLOGY_PRIVATE_KEY;
    private static final byte[] FOSSOLOGY_PUBLIC_KEY;

    private static final Logger log = getLogger(FossologySettings.class);

    static {

        Properties props = CommonUtils.loadProperties(FossologySettings.class, PROPERTIES_FILE_PATH);

        FOSSOLOGY_HOST = props.getProperty("fossology.host", "localhost");
        FOSSOLOGY_PORT = Integer.parseInt(props.getProperty("fossology.port", "22"));
        FOSSOLOGY_SSH_USERNAME = props.getProperty("fossology.user", "sw360");

        final String keyFilePath = props.getProperty("fossology.key.file", "/fossology.id_rsa");
        final String pubKeyFilePath = props.getProperty("fossology.key.pub.file", keyFilePath + ".pub");

        FOSSOLOGY_PRIVATE_KEY = loadKeyFile(keyFilePath);
        FOSSOLOGY_PUBLIC_KEY = loadKeyFile(pubKeyFilePath);
    }

    private static byte[] loadKeyFile(String keyFilePath) {
        byte[] fossologyPrivateKey = null;
        try {
            try (InputStream keyFileStream = FossologySettings.class.getResourceAsStream(keyFilePath)) {
                if (keyFileStream == null)
                    throw new IOException("cannot open " + keyFilePath);
                fossologyPrivateKey = IOUtils.toByteArray(keyFileStream);
            }
        } catch (IOException e) {
            log.error("Cannot load private key", e);
        }
        return fossologyPrivateKey;
    }

    public int getFossologyConnectionTimeout() {
        return FOSSOLOGY_CONNECTION_TIMEOUT;
    }

    public long getFossologyExecutionTimeout() {
        return FOSSOLOGY_EXECUTION_TIMEOUT;
    }

    public String getFossologyHost() {
        return FOSSOLOGY_HOST;
    }

    public int getFossologyPort() {
        return FOSSOLOGY_PORT;
    }

    public String getFossologySshUsername() {
        return FOSSOLOGY_SSH_USERNAME;
    }

    public byte[] getFossologyPrivateKey() {
        return FOSSOLOGY_PRIVATE_KEY;
    }

    public byte[] getFossologyPublicKey() {
        return FOSSOLOGY_PUBLIC_KEY;
    }
}
