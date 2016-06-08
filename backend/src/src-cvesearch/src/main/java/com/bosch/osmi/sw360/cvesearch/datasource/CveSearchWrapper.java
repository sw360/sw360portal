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
package com.bosch.osmi.sw360.cvesearch.datasource;

import com.bosch.osmi.sw360.cvesearch.datasource.heuristics.Heuristic;
import com.bosch.osmi.sw360.cvesearch.datasource.heuristics.SearchLevels;
import com.siemens.sw360.datahandler.thrift.components.Release;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;

public class CveSearchWrapper {

    // TODO: finetuning of these parameters
    private final int VENDOR_THRESHOLD  = 1;
    private final int PRODUCT_THRESHOLD = 0;
    private final int CUTOFF            = 10;

    Logger log = Logger.getLogger(CveSearchWrapper.class);

    private Heuristic heuristic;

    public CveSearchWrapper(CveSearchApi cveSearchApi) {
        SearchLevels searchLevels = new SearchLevels();

        // TODO: remove once decided
        if (false) {
            searchLevels.addBasicSearchlevels();
        }else{
            searchLevels.addGuessingSearchLevels(cveSearchApi, VENDOR_THRESHOLD, PRODUCT_THRESHOLD, CUTOFF);
        }

        heuristic = new Heuristic(searchLevels, cveSearchApi);
    }

    public Optional<List<CveSearchData>> searchForRelease(Release release) {
        try {
            return Optional.of(heuristic.run(release));
        } catch (IOException e) {
            log.error("Was not able to search for release with name=" + release.getName() + " and id=" + release.getId(), e);
        }
        return Optional.empty();
    }
}
