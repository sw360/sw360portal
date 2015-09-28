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
package com.siemens.sw360.search.common;

import com.siemens.sw360.datahandler.common.CommonUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

/**
 * Properties class for the user service.
 *
 * @author cedric.bodet@tngtech.com
 */
public class SearchConstants {

    public static final String PROPERTIES_FILE_PATH = "/search.properties";
    public static final int NAME_MAX_LENGTH;

    static {
        Properties props = CommonUtils.loadProperties(SearchConstants.class, PROPERTIES_FILE_PATH);

        NAME_MAX_LENGTH = Integer.parseInt(props.getProperty("search.name.max.length", "64"));
    }

    private SearchConstants() {
        // Utility class with only static functions
    }

}
