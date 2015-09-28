/*
 * Copyright Siemens AG, 2014-2015. Part of the SW360 Portal Project.
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.siemens.sw360.datahandler.thrift.SW360Exception;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by bodet on 11/02/15.
 *
 * @author cedric.bodet@tngtech.com
 */
public class JacksonUtils {

    private JacksonUtils() {
        // Utility class with only static functions
    }

    public static boolean arrayContains(ArrayNode array, String needle) {
        for (JsonNode jsonNode : array) {
            if (jsonNode.isTextual() && needle.equals(jsonNode.textValue())) {
                return true;
            }
        }
        return false;
    }

    public static int arrayPosition(ArrayNode array, String needle) {
        for (int i = 0; i < array.size(); i++) {
            JsonNode jsonNode = array.get(i);
            if (jsonNode.isTextual() && needle.equals(jsonNode.textValue())) {
                return i;
            }
        }
        return -1;
    }

    public static Set<String> extractSet(ArrayNode array) throws SW360Exception {
        Set<String> result = new HashSet<>();

        for (JsonNode jsonNode : array) {
            if (jsonNode.isTextual())
                result.add(jsonNode.textValue());
            else
                throw new SW360Exception("Non textual string ?!");
        }
        return result;
    }

}
