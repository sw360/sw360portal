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

import static com.siemens.sw360.datahandler.common.CommonUtils.nullToEmptyString;

public class ModifiedLevenshteinDistance {

    protected static Match levenshteinMatch(String needle, String haystack){
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
    protected static int calculateModifiedLevenshteinDistance(String needle, String haystack){

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
            if (j > 0 && haystack.charAt(j - 1) == '_') {
                // skipping prefix of haystack does not cost anything, if it ends with a space
                savedCostsWhenSippedSpaceSeparatedPrefix = j;
            }
            curcost[0] = j - savedCostsWhenSippedSpaceSeparatedPrefix;

            //=========================================================================================================
            for(int i = 1; i < needleLength; i++) {
                int match = (needle.charAt(i - 1) == haystack.charAt(j - 1)) ? 0 : 1;

                int costReplace = oldcost[i - 1] + match;
                int costInsert  = oldcost[i] + 1;
                int costDelete  = curcost[i - 1] + 1;

                curcost[i] = minimum(costInsert, costDelete, costReplace);
            }

            //=========================================================================================================
            if(haystack.charAt(j - 1) == '_') {
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
