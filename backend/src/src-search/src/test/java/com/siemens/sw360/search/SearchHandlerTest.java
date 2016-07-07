/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.search;

import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SearchHandlerTest {

    SearchHandler handler;

    @Before
    public void setUp() throws Exception {
        handler = new SearchHandler();
    }

    @Test(expected = TException.class)
    public void testSearchNull() throws Exception {
        handler.search(null, null);
    }

    public void testSearchEmpty() throws Exception {
        assertThat(handler.search("", null).size(), is(0));
    }
}
