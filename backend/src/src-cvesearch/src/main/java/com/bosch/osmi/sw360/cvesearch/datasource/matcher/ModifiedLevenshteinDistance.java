/*
 * Copyright (c) Bosch Software Innovations GmbH 2016.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.bosch.osmi.sw360.cvesearch.datasource.matcher;

import static com.siemens.sw360.datahandler.common.CommonUtils.nullToEmptyString;

public class ModifiedLevenshteinDistance {

    public static Match levenshteinMatch(String needle, String haystack){
        return new Match(needle,
                calculateModifiedLevenshteinDistance(needle,nullToEmptyString(haystack).toLowerCase().replace(' ', '_')));
    }

    /**
     * This is a modified Levenshtein distance in which
     * - skipping prefixes and postfixes of the haystack does not cost anything
     * - if one of the strings is empty the distance Integer.MAX_VALUE is returned
     *
     * @param needle
     * @param haystack
     * @return the modified Levenshtein distance between the needle and the haystack
     */
    public static int calculateModifiedLevenshteinDistance(String needle, String haystack) {
        return calculateModifiedLevenshteinDistance(needle, haystack,'_');
    }

    /**
     * This is a modified Levenshtein distance in which
     * - skipping prefixes and postfixes of the haystack does not cost anything
     * - if one of the strings is empty the distance Integer.MAX_VALUE is returned
     *
     * @param needle
     * @param haystack
     * @param space the chosen representation of the separator
     * @return the modified Levenshtein distance between the needle and the haystack
     */
    public static int calculateModifiedLevenshteinDistance(String needle, String haystack, char space){

        if (needle.length() == 0 || haystack.length() == 0){
            return Integer.MAX_VALUE;
        }

        int needleLength = needle.length() + 1;
        int haystackLength = haystack.length() + 1;

        int[] oldcost = new int[needleLength];
        int[] curcost = new int[needleLength];

        for (int i = 0; i < needleLength; i++) oldcost[i] = i;

        int savedCostsWhenSippedSpaceSeparatedPrefix     = 0;
        int minimalCostsWhenSkippedSpaceSeperatedPostfix = Integer.MAX_VALUE;
        for (int j = 1; j < haystackLength; j++) {
            //=========================================================================================================
            if (j > 0 && haystack.charAt(j - 1) == space) {
                // skipping prefix of haystack does not cost anything, if it ends with a space
                savedCostsWhenSippedSpaceSeparatedPrefix = j;
            }
            curcost[0] = j - savedCostsWhenSippedSpaceSeparatedPrefix;

            //=========================================================================================================
            for(int i = 1; i < needleLength; i++) {
                int costReplaceCurChar = (needle.charAt(i - 1) == haystack.charAt(j - 1)) ? 0 : 1;

                int costReplace = oldcost[i - 1] + costReplaceCurChar;
                int costInsert  = oldcost[i] + 1;
                int costDelete  = curcost[i - 1] + 1;

                curcost[i] = minimum(costInsert, costDelete, costReplace);
            }

            //=========================================================================================================
            if(haystack.charAt(j - 1) == space) {
                // skipping postfix of haystack does not cost anything, if it starts with a space
                minimalCostsWhenSkippedSpaceSeperatedPostfix = Math.min(
                        minimalCostsWhenSkippedSpaceSeperatedPostfix,
                        oldcost[needleLength - 1]);
            }

            //=========================================================================================================
            int[] swap = oldcost; oldcost = curcost; curcost = swap;
        }

        return Math.min(oldcost[needleLength - 1], minimalCostsWhenSkippedSpaceSeperatedPostfix);
    }

    private static int minimum(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }
}
