/*
 * Copyright (c) Bosch Software Innovations GmbH 2016.
 * Part of the SW360 Portal Project.
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
package com.bosch.osmi.sw360.cvesearch.datasource.matcher;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ListMatcherTest {

    String needle1 = "needle1";
    String needle2 = "ndl2";
    String needle3 = "ndl3";
    ArrayList<String> needles;
    ListMatcher listMatcher;

    @Before
    public void prepare() {
        needles = new ArrayList<>();
        needles.add(needle1);
        needles.add(needle2);
        needles.add(needle3);
        listMatcher = new ListMatcher(needles);
    }

    @Test
    public void getMatchTestFullMatch() {

        List<StringMatch> matches = listMatcher.getMatches(needle2);

        assert(matches.get(0).getNeedle().equals(needle2));
        assert(matches.get(0).getDistance() == 0);
        assert(matches.get(1).getDistance() != 0);
        assert(matches.get(2).getDistance() != 0);
    }
}
