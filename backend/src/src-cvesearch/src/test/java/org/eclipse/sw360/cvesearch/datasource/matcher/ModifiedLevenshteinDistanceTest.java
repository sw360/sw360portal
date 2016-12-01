/*
 * Copyright (c) Bosch Software Innovations GmbH 2016.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.cvesearch.datasource.matcher;

import org.junit.Test;

import static org.eclipse.sw360.cvesearch.datasource.matcher.ModifiedLevenshteinDistance.levenshteinMatch;

public class ModifiedLevenshteinDistanceTest {

    @Test
    public void testBasicRules() {
        String needle = "needle";
        String neele = "neele";
        String needDle = "needDle";

        // equal strings have distance 0
        assert(levenshteinMatch(needle,needle).getDistance() == 0);

        // appending or prepending adds 1 to the distance
        assert(levenshteinMatch(needle,needle + "a").getDistance() == 1);
        assert(levenshteinMatch(needle, "a" + needle ).getDistance() == 1);
        assert(levenshteinMatch(needle, "a" + needle + "a").getDistance() == 2);
        assert(levenshteinMatch(needle, needDle).getDistance() == 1);
        assert(levenshteinMatch(needDle, needle).getDistance() == 1);

        // dropping adds 1 to the distance
        assert(levenshteinMatch(needle, neele).getDistance() == 1);
        assert(levenshteinMatch(neele, needle).getDistance() == 1);

        // should be able to find the best match
        assert(levenshteinMatch(needle, needle + " " + neele).getDistance() == 0);
        assert(levenshteinMatch(needle, neele + " " + needle).getDistance() == 0);

        // seperated by spaces does not change distance
        assert(levenshteinMatch(needle,needle + " a").getDistance() == 0);
        assert(levenshteinMatch(needle, "a " + needle ).getDistance() == 0);
        assert(levenshteinMatch(needle, "a" + needle + " a").getDistance() == 1);
        assert(levenshteinMatch(needle, "a " + needle + "a").getDistance() == 1);
        assert(levenshteinMatch(needle, "a " + needle + " a").getDistance() == 0);
    }

    @Test
    public void getDistancesEmptyNeedle(){
        assert(levenshteinMatch("", "haystack").getDistance() == Integer.MAX_VALUE);
    }

    @Test
    public void getDistancesEmptyHaystack(){
        assert(levenshteinMatch("needle", "").getDistance() == Integer.MAX_VALUE);
    }

    @Test
    public void getDistanceXtoSomethingWithoutX() {
        assert(levenshteinMatch("x","lorem ipsum").getDistance() > 0);
        assert(levenshteinMatch("x","y").getDistance() > 0);
        assert(levenshteinMatch("x","y ").getDistance() > 0);
        assert(levenshteinMatch("x"," y").getDistance() > 0);
        assert(levenshteinMatch("x"," y ").getDistance() > 0);
    }

    @Test
    public void getDistanceXToXX() {
        assert(levenshteinMatch("xx","x").getDistance() == 1);
        assert(levenshteinMatch("xx","x ").getDistance() == 1);
        assert(levenshteinMatch("xx"," x").getDistance() == 1);
        assert(levenshteinMatch("xx"," x ").getDistance() == 1);
        assert(levenshteinMatch("xx","y x ").getDistance() == 1);
        assert(levenshteinMatch("xx"," x y").getDistance() == 1);
        assert(levenshteinMatch("xx","y x y").getDistance() == 1);
    }

    @Test
    public void getDistanceTest() {
        String needle = "needle";
        String haystack = "haystack";

        Match match = levenshteinMatch(needle,haystack);

        assert(match.getNeedle().equals(needle));
        assert(match.getDistance() > 0);
    }

    @Test
    public void getDistanceTestFullMatch() {
        String needle = "needle";
        String haystack = "needle";

        Match match = levenshteinMatch(needle,haystack);

        assert(match.getDistance() == 0);
    }

    @Test
    public void getDistanceTestPartialMatch() {
        String needle = "needle";
        String haystack = "ndle";

        Match match = levenshteinMatch(needle,haystack);

        assert(match.getDistance() == 2);
    }

    @Test
    public void getDistanceTestUpToPrefixMatch() {
        String needle = "needle";
        String haystack = "prefix needle";

        Match match = levenshteinMatch(needle,haystack);

        assert(match.getDistance() == 0);
    }

    @Test
    public void getDistanceTestUpToPostfixMatch() {
        String needle = "needle";
        String haystack = "needle postfix";

        Match match = levenshteinMatch(needle,haystack);

        assert(match.getDistance() == 0);
    }

    @Test
    public void getDistanceTestFullSubstringMatch() {
        String needle = "needle";
        String noise = "bla";
        String haystack = "prefix needle " + noise + " postfix";

        Match match = levenshteinMatch(needle,haystack);

        assert(match.getDistance() == 0);
    }

    @Test
    public void getDistanceTestPartialSubstringMatch() {
        String needle = "needle";
        String noise = "bla";
        String haystack = "prefix needle" + noise + " postfix";

        Match match = levenshteinMatch(needle,haystack);

        assert(match.getDistance() == noise.length()) ;
    }
}
