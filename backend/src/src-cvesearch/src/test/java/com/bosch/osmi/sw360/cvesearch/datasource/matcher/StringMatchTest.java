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

import org.junit.Test;

public class StringMatchTest {

    @Test
    public void testBasicRules() {
        String needle = "needle";
        String neele = "neele";
        String needDle = "needDle";

        // equal strings have distance 0
        assert(new StringMatch(needle,needle).getDistance() == 0);

        // appending or prepending adds 1 to the distance
        assert(new StringMatch(needle,needle + "a").getDistance() == 1);
        assert(new StringMatch(needle, "a" + needle ).getDistance() == 1);
        assert(new StringMatch(needle, "a" + needle + "a").getDistance() == 2);
        assert(new StringMatch(needle, needDle).getDistance() == 1);
        assert(new StringMatch(needDle, needle).getDistance() == 1);

        // dropping adds 1 to the distance
        assert(new StringMatch(needle, neele).getDistance() == 1);
        assert(new StringMatch(neele, needle).getDistance() == 1);

        // should be able to find the best match
        assert(new StringMatch(needle, needle + " " + neele).getDistance() == 0);
        assert(new StringMatch(needle, neele + " " + needle).getDistance() == 0);

        // seperated by spaces does not change distance
        assert(new StringMatch(needle,needle + " a").getDistance() == 0);
        assert(new StringMatch(needle, "a " + needle ).getDistance() == 0);
        assert(new StringMatch(needle, "a" + needle + " a").getDistance() == 1);
        assert(new StringMatch(needle, "a " + needle + "a").getDistance() == 1);
        assert(new StringMatch(needle, "a " + needle + " a").getDistance() == 0);
    }

    @Test
    public void getDistancesEmptyNeedle(){
        assert(new StringMatch("", "haystack").getDistance() == Integer.MAX_VALUE);
    }

    @Test
    public void getDistancesEmptyHaystack(){
        assert(new StringMatch("needle", "").getDistance() == Integer.MAX_VALUE);
    }

    @Test
    public void getDistanceApacheToX() {
        assert(new StringMatch("x","lorem ipsum").getDistance() > 0);
    }

    @Test
    public void getDistanceXToXX() {
        assert(new StringMatch("xx","x").getDistance() == 1);
        assert(new StringMatch("xx","x ").getDistance() == 1);
        assert(new StringMatch("xx"," x").getDistance() == 1);
        assert(new StringMatch("xx"," x ").getDistance() == 1);
    }

    @Test
    public void getDistanceTest() {
        String needle = "needle";
        String haystack = "haystack";

        StringMatch match = new StringMatch(needle,haystack);

        assert(match.getNeedle().equals(needle));
        assert(match.getDistance() > 0);
    }

    @Test
    public void getDistanceTestFullMatch() {
        String needle = "needle";
        String haystack = "needle";

        StringMatch match = new StringMatch(needle,haystack);

        assert(match.getDistance() == 0);
    }

    @Test
    public void getDistanceTestPartialMatch() {
        String needle = "needle";
        String haystack = "ndle";

        StringMatch match = new StringMatch(needle,haystack);

        assert(match.getDistance() == 2);
    }

    @Test
    public void getDistanceTestUpToPrefixMatch() {
        String needle = "needle";
        String haystack = "prefix needle";

        StringMatch match = new StringMatch(needle,haystack);

        assert(match.getDistance() == 0);
    }

    @Test
    public void getDistanceTestUpToPostfixMatch() {
        String needle = "needle";
        String haystack = "needle postfix";

        StringMatch match = new StringMatch(needle,haystack);

        assert(match.getDistance() == 0);
    }

    @Test
    public void getDistanceTestFullSubstringMatch() {
        String needle = "needle";
        String haystack = "prefix needle bla postfix";

        StringMatch match = new StringMatch(needle,haystack);

        assert(match.getDistance() == 0);
    }

    @Test
    public void getDistanceTestPartialSubstringMatch() {
        String needle = "needle";
        String haystack = "prefix needlebla postfix";

        StringMatch match = new StringMatch(needle,haystack);

        assert(match.getDistance() == 3) ;
    }
}
