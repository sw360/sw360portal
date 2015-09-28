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

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SearchDocumentTest {

    private Map<String, Object> document;
    private SearchDocument parser;


    @Before
    public void setUp() throws Exception {
        document = new HashMap<>();

        document.put("type", "license");
        document.put("fullname", "testfullname");
        document.put("testkey", "testvalue");

        parser = new SearchDocument(document);

    }

    @Test
    public void testGetType() throws Exception {
        assertEquals("license", parser.getType());
    }

    @Test
    public void testGetName() throws Exception {
        assertEquals("testfullname", parser.getName());
    }

    @Test
    public void testGetProperty() throws Exception {
        assertEquals("testvalue", parser.getProperty("testkey"));
    }

    @Test
    public void testGetTypeInvalid() throws Exception {
        document.remove("type");
        parser = new SearchDocument(document);

        assertNotNull(parser.getType());
        assertEquals("", parser.getType());
    }

    @Test
    public void testGetNameInvalid1() throws Exception {
        document.remove("fullname");
        parser = new SearchDocument(document);

        assertNotNull(parser.getName());
        assertEquals("", parser.getName());
    }

    @Test
    public void testGetNameInvalid2() throws Exception {
        document.put("type", "feuwife");
        parser = new SearchDocument(document);

        assertNotNull(parser.getName());
        assertEquals("", parser.getName());
    }
}
