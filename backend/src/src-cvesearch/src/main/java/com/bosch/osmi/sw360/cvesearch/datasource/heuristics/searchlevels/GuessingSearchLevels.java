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

import com.bosch.osmi.sw360.cvesearch.datasource.CveSearchApi;
import com.bosch.osmi.sw360.cvesearch.datasource.CveSearchGuesser;
import com.bosch.osmi.sw360.cvesearch.datasource.matcher.Match;
import com.siemens.sw360.datahandler.thrift.components.Release;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.siemens.sw360.datahandler.common.CommonUtils.nullToEmptyString;

public class GuessingSearchLevels extends BaseSearchLevel{

    private final CveSearchGuesser cveSearchGuesser;
    private static final String CPE_NEEDLE_PREFIX  = "cpe:2.3:.:";

    public GuessingSearchLevels(CveSearchApi cveSearchApi) {
        super();
        this.cveSearchGuesser = new CveSearchGuesser(cveSearchApi);

        setupSearchlevels();
    }

    private void setupSearchlevels() {
        // Level 2. search by guessed vendors and products with version
        searchLevels.add(r -> this.guessForRelease(r, true));

        // Level 3. search by guessed vendors and products without version
        searchLevels.add(r -> this.guessForRelease(r, false));
    }

    public GuessingSearchLevels setVendorThreshold(int vendorThreshold) {
        cveSearchGuesser.setVendorThreshold(vendorThreshold);
        return this;
    }

    public GuessingSearchLevels setProductThreshold(int productThreshold) {
        cveSearchGuesser.setProductThreshold(productThreshold);
        return this;
    }

    protected List<String> guessForRelease(Release release, boolean useVersionInformation) throws IOException {
        if(useVersionInformation && !release.isSetVersion()){
            return Collections.EMPTY_LIST;
        }

        List<Match> vendorProductList;

        String productHaystack = release.getName();
        if (release.isSetVendor() &&
                (release.getVendor().isSetShortname() || release.getVendor().isSetFullname())) {
            String vendorHaystack = nullToEmptyString(release.getVendor().getShortname()) + " " +
                    nullToEmptyString(release.getVendor().getFullname());
            vendorProductList = cveSearchGuesser.guessVendorAndProducts(vendorHaystack, productHaystack);
        } else {
            vendorProductList = cveSearchGuesser.guessVendorAndProducts(productHaystack);
        }

        String cpeNeedlePostfix = ":" + (useVersionInformation ? release.getVersion() : "") + ".*";
        Function<String,String> cpeBuilder = cpeNeedle -> CPE_NEEDLE_PREFIX + cpeNeedle + cpeNeedlePostfix;

        return vendorProductList.stream()
                .map(Match::getNeedle)
                .map(cpeBuilder)
                .collect(Collectors.toList());
    }
}
