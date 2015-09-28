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
package com.siemens.sw360.search.db;

import com.siemens.sw360.datahandler.common.SW360Constants;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.siemens.sw360.search.common.SearchConstants.NAME_MAX_LENGTH;

/**
 * Helper class to help parse JSON documents from lucene-ektorp
 *
 * @author cedric.bodet@tngtech.com
 */
class SearchDocument {

    /**
     * Map representation of the document
     */
    private final Map<String, Object> document;

    private final String type;

    /**
     * Constructor, create empty hashmap if the one provided is null, avoiding null pointer exception
     */
    SearchDocument(Map<String, Object> document) {
        if (document != null) {
            this.document = Collections.unmodifiableMap(document);
        } else {
            this.document = new HashMap<>();
        }
        // Set the type of the document
        type = getProperty("type");
    }

    /**
     * Get document type
     */
    String getType() {
        return type;
    }

    /**
     * Get document name
     */
    String getName() {
        if (!SW360Constants.MAP_FULLTEXT_SEARCH_NAME.containsKey(type)) {
            return "";
        } else {
            String name = getProperty(SW360Constants.MAP_FULLTEXT_SEARCH_NAME.get(type));
            return StringUtils.abbreviate(name, NAME_MAX_LENGTH);
        }
    }

    /**
     * Get property from document hashmap, returning an empty string in case of error
     */
    String getProperty(String key) {
        if (key == null) {
            return "";
        }

        if (!key.contains(" ")) {
            // Get a single key
            Object value = document.get(key);

            if (value instanceof String) {
                return (String) value;
            } else {
                return "";
            }
        } else {
            // Build a name containing all keys
            StringBuilder builder = new StringBuilder("");
            String[] parts = key.split(" ");
            for (String part : parts) {
                builder.append(getProperty(part)).append(' ');
            }
            return builder.toString();
        }
    }

}
