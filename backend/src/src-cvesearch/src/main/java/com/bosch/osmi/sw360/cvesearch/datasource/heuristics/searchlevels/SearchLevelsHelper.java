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
package com.bosch.osmi.sw360.cvesearch.datasource.heuristics.searchlevels;

import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.thrift.components.Release;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Predicate;

public class SearchLevelsHelper {

    private static final String CPE_WILDCARD      = ".*";
    private static final String CPE_NEEDLE_PREFIX = "cpe:2.3:" + CPE_WILDCARD + ":" + CPE_WILDCARD;

    public static Function<Release, String> implodeSearchNeedleGenerators(Function<Release,String> generator, Function<Release,String> ... generators){
        return Arrays.stream(generators)
                .reduce(generator,
                        (s1,s2) -> r -> s1.apply(r) + CPE_WILDCARD + s2.apply(r));
    }

    public static boolean isCpe(String potentialCpe){
        if(null == potentialCpe){
            return false;
        }
        return potentialCpe.startsWith("cpe:") && potentialCpe.length() > 10;
    }

    public static String cpeWrapper(String needle) {
        if (isCpe(needle)){
            return needle;
        }
        return CPE_NEEDLE_PREFIX + CommonUtils.nullToEmptyString(needle).replace(" ", CPE_WILDCARD).toLowerCase() + CPE_WILDCARD;
    }

    public static SearchLevelGenerator.SearchLevel mkSearchLevel(Predicate<Release> isPossible, Function<Release,String> generator, Function<Release,String> ... generators){
        Function<Release,String> implodedGenerators = implodeSearchNeedleGenerators(generator, generators);

        return r -> {
            if(isPossible.test(r)){
                return Collections.singletonList(cpeWrapper(implodedGenerators.apply(r)));
            }
            return Collections.EMPTY_LIST;
        };
    }
}
