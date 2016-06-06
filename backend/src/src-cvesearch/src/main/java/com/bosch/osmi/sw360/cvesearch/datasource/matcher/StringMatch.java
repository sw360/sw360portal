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

public class StringMatch {

    private String needle;
    private int distance;

    public StringMatch(String needle, String haystack){
        this.needle = needle;

        this.distance = getDistance(needle, haystack.toLowerCase().replace(' ', '_'));
    }

    public String getNeedle() {
        return needle;
    }

    public int getDistance() {
        return distance;
    }

    // This is a modified Levenstein distance in which
    // - skipping prefixes and postfixes of the haystack does not cost anything
    // - if one of the strings is empty the distance Integer.MAX_VALUE is returned
    protected int getDistance(String needle, String haystack){

        if (needle.length() == 0 || haystack.length() == 0){
            return Integer.MAX_VALUE;
        }

        // see: https://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance

        int needleLength = needle.length() + 1;
        int haystackLength = haystack.length() + 1;

        int[] cost = new int[needleLength];
        int[] newcost = new int[needleLength];

        for (int i = 0; i < needleLength; i++) cost[i] = i;

        int savedCostsWhenSippedSpaceSeperatedPrefix     = 0;
        int maximalCostsWhenSkippedSpaceSeperatedPostfix = Integer.MAX_VALUE;
        for (int j = 1; j < haystackLength; j++) {
            //=========================================================================================================
            if (j > 0 && haystack.charAt(j - 1) == '_') {
                savedCostsWhenSippedSpaceSeperatedPrefix = j;
                newcost[0] = 0; // skipping prefix of haystack does not cost anything, if it ends with a space
            }else{
                newcost[0] = j - savedCostsWhenSippedSpaceSeperatedPrefix;
            }

            //=========================================================================================================
            for(int i = 1; i < needleLength; i++) {
                int match = (needle.charAt(i - 1) == haystack.charAt(j - 1)) ? 0 : 1;

                int cost_replace = cost[i - 1] + match;
                int cost_insert  = cost[i] + 1;
                int cost_delete  = newcost[i - 1] + 1;

                newcost[i] = minimum(cost_insert, cost_delete, cost_replace);
            }

            //=========================================================================================================
            if(haystack.charAt(j - 1) == '_') {
                maximalCostsWhenSkippedSpaceSeperatedPostfix = Math.min(
                        maximalCostsWhenSkippedSpaceSeperatedPostfix,
                        newcost[needleLength - 1] - 1);
            }

            //=========================================================================================================
            int[] swap = cost; cost = newcost; newcost = swap;
        }

        return Math.min(cost[needleLength - 1], maximalCostsWhenSkippedSpaceSeperatedPostfix);
    }

    private int minimum(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }
}
