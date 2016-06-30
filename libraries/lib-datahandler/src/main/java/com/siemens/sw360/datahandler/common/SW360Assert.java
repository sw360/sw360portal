/*
 * Copyright Siemens AG, 2014-2016. Part of the SW360 Portal Project.
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

import com.siemens.sw360.datahandler.thrift.SW360Exception;
import com.siemens.sw360.datahandler.thrift.users.User;
import org.slf4j.Logger;

import java.util.Collection;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Assertion utilities, throwing thrift exception.
 *
 * @author cedric.bodet@tngtech.com
 * @author stefan.jaeger@evosoft.com
 */
public class SW360Assert {
    private static final Logger log = getLogger(SW360Assert.class);

    private SW360Assert() {
        // Utility class with only static functions
    }

    public static <T> T assertNotNull(T object) throws SW360Exception {
        assertNotNull(object, "Invalid null input!");
        return object;
    }

    public static <T> T assertNotNull(T object, String messageFormat, Object... args) throws SW360Exception {
        if (object == null) {
            throw fail(messageFormat, args);
        }
        return object;
    }

    public static <T> T assertNull(T object) throws SW360Exception {
        assertNull(object, "Invalid null input!");
        return object;
    }

    public static <T> T assertNull(T object, String messageFormat, Object... args) throws SW360Exception {
        if (object != null) {
            throw fail(messageFormat, args);
        }
        return object;
    }

    public static void assertId(String id) throws SW360Exception {
        assertNotEmpty(id, "Invalid empty ID!");
    }

    public static User assertUser(User user) throws SW360Exception {
        assertNotNull(user, "Invalid, user is null!");
        assertNotEmpty(user.getEmail(), "User Email was empty in " + user.toString());
        assertNotEmpty(user.getDepartment(), "User Department was empty in " + user.toString());
        return user;
    }

    public static void assertNotEmpty(String string) throws SW360Exception {
        assertNotEmpty(string, "Invalid empty input!");
    }

    public static void assertNotEmpty(String string, String message) throws SW360Exception {
        if (isNullOrEmpty(string)) {
            throw fail(message);
        }
    }

    public static <T extends Collection<?>> T assertNotEmpty(T collection) throws SW360Exception {
        return assertNotEmpty(collection, "Invalid empty input!");
    }

    public static <T extends Collection<?>> T assertNotEmpty(T collection, String message) throws SW360Exception {
        if (collection == null || collection.isEmpty()) {
            throw fail(message);
        }
        return collection;
    }

    public static void assertIdUnset(String id) throws SW360Exception {
        assertEmpty(id, "ID already set, cannot add to database!");
    }

    public static void assertEmpty(String string) throws SW360Exception {
        assertEmpty(string, "Invalid non-empty input!");
    }

    public static void assertEmpty(String string, String message) throws SW360Exception {
        if (string != null && !string.trim().isEmpty()) {
            throw fail(message);
        }
    }

    public static void failIf(boolean condition, String messageFormat, Object... args) throws SW360Exception {
        if (condition) {
            throw fail(messageFormat, args);
        }
    }

    public static SW360Exception fail(String messageFormat, Object... args) throws SW360Exception {
        SW360Exception sw360Exception = new SW360Exception();
        throw fail(sw360Exception, messageFormat, args);
    }

    public static SW360Exception fail(Throwable t, String messageFormat, Object... args) throws SW360Exception {
        String message = String.format(messageFormat, args);
        log.error(message, t);
        throw new SW360Exception(message);
    }

    public static void assertEquals(Object expected, Object actual) throws SW360Exception {
        assertEquals(expected, actual, "Objects do not compare equal!");
    }

    public static void assertEquals(Object expected, Object actual, String message) throws SW360Exception {
        if (expected == null && actual == null) {
            return;
        }
        if (expected != null && expected.equals(actual)) {
            return;
        }
        throw fail(message);
    }

    public static void assertValidUrl(String url) throws SW360Exception {
        assertNotEmpty(url);
        if (!CommonUtils.isValidUrl(url)) {
            throw fail("not a valid url: '%s'", url);
        }
    }

    public static void assertTrue(boolean condition) throws SW360Exception {
        failIf(!condition, "condition not fulfilled");
    }
}
