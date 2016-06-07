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

import com.siemens.sw360.datahandler.thrift.components.Release;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.bosch.osmi.sw360.cvesearch.datasource.heuristics.searchlevels.SearchLevelsHelper.isCpe;
import static com.bosch.osmi.sw360.cvesearch.datasource.heuristics.searchlevels.SearchLevelsHelper.mkSearchLevel;

public class BaseSearchLevel implements SearchLevelGenerator{
    List<SearchLevel> searchLevels;

    public BaseSearchLevel() {
        searchLevels = new ArrayList<>();

        // Level 1. search by full cpe
        searchLevels.add(mkSearchLevel(r -> r.isSetCpeid() && isCpe(r.getCpeid().toLowerCase()),
                Release::getCpeid));
    }

    @Override
    public List<List<String>> apply(Release release) throws IOException {
        List<List<String>> result = new ArrayList<>();
        for(SearchLevel searchLevel : searchLevels){
            result.add(searchLevel.apply(release));
        }
        return result;
    }
}
