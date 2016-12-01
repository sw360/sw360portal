/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.portal.common;


import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Set;

import static org.eclipse.sw360.datahandler.common.CommonUtils.splitToSet;
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
