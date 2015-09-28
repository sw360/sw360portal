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

package com.siemens.sw360.datahandler.couchdb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.siemens.sw360.testthrift.TestObject;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @author cedric.bodet@tngtech.com
 */
public class MapperFactoryTest {

    private static final String TEST_ID = "abcdef";
    private static final String TEST_REV = "123456";
    private static final String TEST_NAME = "Super License 3.2";

    ObjectMapper mapper;
    TestObject object;

    @Before
    public void setUp() throws Exception {
        // Prepare mapper
        MapperFactory factory = new MapperFactory(ImmutableList.<Class>of(TestObject.class), Collections.<Class>emptyList());
        mapper = factory.createObjectMapper();
        // Prepare object
        object = new TestObject();
        object.setId(TEST_ID);
        object.setRevision(TEST_REV);
        object.setName(TEST_NAME);
    }

    @Test
    public void testLicenseSerialization() {
        // Serialize the object (as node)
        JsonNode node = mapper.valueToTree(object);
        // Check that fields are present
        assertTrue("_id present", node.has("_id"));
        assertTrue("_rev present", node.has("_rev"));
        assertTrue("name present", node.has("name"));
        // Text was not set
        assertFalse("Text not set", node.has("text"));
    }

    @Test
    public void testLicenseContent() {
        // Serialize the object (as node)
        JsonNode node = mapper.valueToTree(object);
        // Check field values
        assertEquals(TEST_ID, node.get("_id").textValue());
        assertEquals(TEST_REV, node.get("_rev").textValue());
        assertEquals(TEST_NAME, node.get("name").textValue());
    }

    @Test
    public void testLicenseDeserialization() throws Exception {
        // Serialize the object (as string)
        String string = mapper.writeValueAsString(object);
        // Deserialize the object
        TestObject parsedObject = mapper.readValue(string, TestObject.class);

        // Check field values
        assertEquals(TEST_ID, parsedObject.getId());
        assertEquals(TEST_REV, parsedObject.getRevision());
        assertEquals(TEST_NAME, parsedObject.getName());
        assertNull("test not present", parsedObject.getText());
    }

    @Test
    public void testNullValues() throws Exception {
        // Null _id and _rev should not be serialized, as they are not accepted by CouchDB
        object.unsetId();
        object.unsetRevision();
        // Serialize the object (as node)
        JsonNode node = mapper.valueToTree(object);
        // Check that null-fields are not-present
        assertFalse("_id present", node.has("_id"));
        assertFalse("_rev present", node.has("_rev"));
        // Name should still be present
        assertTrue("name present", node.has("name"));
    }
}