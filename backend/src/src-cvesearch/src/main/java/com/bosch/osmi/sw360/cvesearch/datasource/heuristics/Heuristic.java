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
import com.siemens.sw360.datahandler.thrift.components.Release;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Heuristic {

    private final SearchLevels searchLevels;
    private final CveSearchApi cveSearchApi;
    private final int maxDepth;
    private Logger log = Logger.getLogger(Heuristic.class);

    public Heuristic(SearchLevels searchLevels, CveSearchApi cveSearchApi) {
        this.searchLevels = searchLevels;
        this.cveSearchApi = cveSearchApi;
        this.maxDepth = 0;
    }

    public Heuristic(SearchLevels searchLevels, CveSearchApi cveSearchApi, int maxDepth) {
        this.searchLevels = searchLevels;
        this.cveSearchApi = cveSearchApi;
        this.maxDepth = maxDepth;
    }

    public List<CveSearchData> run(Release release) throws IOException {
        int level = 0;

        List<List<SearchLevels.NeedleWithMeta>> evaluatedSearchLevels = searchLevels.apply(release);
        List<CveSearchData> result = new ArrayList<>();

        for(List<SearchLevels.NeedleWithMeta> evaluatedSearchLevel: evaluatedSearchLevels){
            level++;

            for(SearchLevels.NeedleWithMeta needleWithMeta: evaluatedSearchLevel){
                try {
                    result.addAll(cveSearchApi.cvefor(needleWithMeta.needle).stream()
                            .map(cveSearchData -> cveSearchData
                                    .setUsedNeedle(needleWithMeta.needle)
                                    .setMatchedBy(needleWithMeta.description))
                            .collect(Collectors.toList()));
                } catch (IOException e) {
                    log.error("IOException in searchlevel=" + level +
                            "\n\twith description=" + needleWithMeta.description +
                            "\n\twith needle=" + needleWithMeta.needle +
                            "\n\twith msg=" + e.getMessage());
                }
            }
            if(result.size() > 0){
                return result;
            }
            if(level == maxDepth){
                log.info("reached maximal level.");
                break;
            }
        }

        return result;
    }
}
