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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.bosch.osmi.sw360.cvesearch.datasource.heuristics.searchlevels.SearchLevelsHelper.isCpe;
import static com.bosch.osmi.sw360.cvesearch.datasource.heuristics.searchlevels.SearchLevelsHelper.mkSearchLevel;

public class BasicSearchLevels implements SearchLevelGenerator{

    List<SearchLevel> searchLevels;

    public BasicSearchLevels() {
        searchLevels = setupSearchlevels();
    }

    private List<SearchLevel> setupSearchlevels() {
        List<SearchLevel> searchLevels = new ArrayList<>();

        // Level 1. search by full cpe
        searchLevels.add(mkSearchLevel(r -> r.isSetCpeid() && isCpe(r.getCpeid().toLowerCase()),
                Release::getCpeid));

        // Level 2. search by: VENDOR_FULL_NAME:NAME:VERSION
        searchLevels.add(mkSearchLevel(r -> r.isSetVersion() && r.isSetVendor() && r.getVendor().isSetFullname(),
                r -> r.getVendor().getFullname(),
                Release::getName,
                Release::getVersion));

        // Level 3. search by: VENDOR_SHORT_NAME:NAME:VERSION
        searchLevels.add(mkSearchLevel(r -> r.isSetVersion() && r.isSetVendor() && r.getVendor().isSetShortname(),
                r ->r.getVendor().getShortname(),
                Release::getName,
                Release::getVersion));

        // Level 4. search by: VENDOR_FULL_NAME:NAME
        searchLevels.add(mkSearchLevel(r -> r.isSetVendor() && r.getVendor().isSetFullname(),
                r ->r.getVendor().getFullname(),
                Release::getName));

        // Level 5. search by: VENDOR_SHORT_NAME:NAME
        searchLevels.add(mkSearchLevel(r -> r.isSetVendor() && r.getVendor().isSetShortname(),
                r -> r.getVendor().getShortname(),
                Release::getName));

        // Level 6. search by: .*:NAME:VERSION
        searchLevels.add(mkSearchLevel(r -> r.isSetVersion(),
                Release::getName,
                Release::getVersion));

        // Level 7. search by: .*:NAME
        searchLevels.add(mkSearchLevel(r -> true,
                Release::getName));
        return searchLevels;
    }

    @Override
    public List<String> apply(Release release) {
        return searchLevels.stream()
                .flatMap(f -> f.apply(release).stream())
                .collect(Collectors.toList());
    }
}
