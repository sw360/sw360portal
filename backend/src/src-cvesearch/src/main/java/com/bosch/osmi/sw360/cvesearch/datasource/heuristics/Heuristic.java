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
package com.bosch.osmi.sw360.cvesearch.datasource.heuristics;

import com.bosch.osmi.sw360.cvesearch.datasource.CveSearchApi;
import com.bosch.osmi.sw360.cvesearch.datasource.CveSearchData;
import com.bosch.osmi.sw360.cvesearch.datasource.heuristics.searchlevels.SearchLevelGenerator;
import com.siemens.sw360.datahandler.thrift.components.Release;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Heuristic {

    private final SearchLevelGenerator searchLevelGenerator;
    private final CveSearchApi cveSearchApi;
    private final boolean greedy;
    private final int maxDepth;
    private Logger log = Logger.getLogger(Heuristic.class);

    public Heuristic(SearchLevelGenerator searchLevelGenerator, CveSearchApi cveSearchApi) {
        this.searchLevelGenerator = searchLevelGenerator;
        this.cveSearchApi = cveSearchApi;
        this.greedy = false;
        this.maxDepth = 0;
    }

    public Heuristic(SearchLevelGenerator searchLevelGenerator, CveSearchApi cveSearchApi, boolean greedy) {
        this.searchLevelGenerator = searchLevelGenerator;
        this.cveSearchApi = cveSearchApi;
        this.greedy = greedy;
        this.maxDepth = 0;
    }

    public Heuristic(SearchLevelGenerator searchLevelGenerator, CveSearchApi cveSearchApi, boolean greedy, int maxDepth) {
        this.searchLevelGenerator = searchLevelGenerator;
        this.cveSearchApi = cveSearchApi;
        this.greedy = greedy;
        this.maxDepth = maxDepth;
    }

    public List<CveSearchData> run(Release release) throws IOException {
        int level = 0;

        List<String> cpeNeedles = searchLevelGenerator.apply(release);
        List<CveSearchData> result = new ArrayList<>();

        for(String cpeNeedle: cpeNeedles){
            level++;
            try {
                result.addAll(cveSearchApi.cvefor(cpeNeedle));
                if(result.size() > 0 && !greedy){
                    return result;
                }
            } catch (IOException e) {
                log.error("IOException in searchlevel=" + level + " with needle=" + cpeNeedle + " with msg=" + e.getMessage());
            }
            if(level == maxDepth){
                log.info("reached maximal level.");
                break;
            }
        }

        return result;
    }
}
