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

package com.siemens.sw360.datahandler.test;

import com.siemens.sw360.testthrift.TestObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestServiceHandlerTest {

    private static final String TEST_ID = "abcdef";
    private static final String TEST_REV = "123456";
    private static final String TEST_NAME = "Super License 3.2";

    TestObject object;
    TestServiceHandler handler;

    @Before
    public void setUp() throws Exception {
        // Prepare object
        object = new TestObject();
        object.setId(TEST_ID);
        object.setRevision(TEST_REV);
        object.setName(TEST_NAME);
        // Prepare handler
        handler = new TestServiceHandler();
    }

    @Test
    public void testTest() throws Exception {
        TestObject returnValue = handler.test(object);
        assertNull(object.getText());
        assertEquals(returnValue.getText(), TestServiceHandler.testText);
    }
}