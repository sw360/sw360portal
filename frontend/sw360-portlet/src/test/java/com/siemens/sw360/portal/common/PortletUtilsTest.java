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
package com.siemens.sw360.portal.common;


import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Set;

import static com.siemens.sw360.datahandler.common.CommonUtils.splitToSet;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

public class PortletUtilsTest {

    @Test
    public void testSplitToSet() throws Exception {

        Set<String> setOne = splitToSet("a, , b ,  ");
        assertThat(setOne, Matchers.containsInAnyOrder("a", "b"));

        assertThat(splitToSet("a, b,"), containsInAnyOrder("a", "b"));
        assertThat(splitToSet("a, b,"), containsInAnyOrder("a", "b"));
        assertThat(splitToSet("b, a, b , a b"), containsInAnyOrder("a", "a b", "b"));
    }
}
