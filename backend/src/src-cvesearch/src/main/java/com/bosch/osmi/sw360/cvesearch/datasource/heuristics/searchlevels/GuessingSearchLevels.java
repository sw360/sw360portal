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
import com.siemens.sw360.datahandler.thrift.components.Release;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.siemens.sw360.datahandler.common.CommonUtils.nullToEmptyString;

public class GuessingSearchLevels implements SearchLevelGenerator{

    private final CveSearchApi cveSearchApi;
    private final CveSearchGuesser cveSearchGuesser;
    private static final String CPE_NEEDLE_PREFIX  = "cpe:2.3:.:";
    private static final String CPE_NEEDLE_POSTFIX = ":.*";

    public GuessingSearchLevels(CveSearchApi cveSearchApi) {
        this.cveSearchApi = cveSearchApi;
        this.cveSearchGuesser = new CveSearchGuesser(cveSearchApi);
    }

    @Override
    public List<String> apply(Release release) throws IOException {
        List<String> vendorProductList;

        String productHaystack = release.getName();
        if (release.getVendor().isSetShortname() || release.getVendor().isSetFullname()) {
            String vendorHaystack = nullToEmptyString(release.getVendor().getShortname()) + " " +
                    nullToEmptyString(release.getVendor().getFullname());
            vendorProductList = cveSearchGuesser.guessVendorAndProducts(vendorHaystack, productHaystack);
        } else {
            vendorProductList = cveSearchGuesser.guessVendorAndProducts(productHaystack);
        }

        return vendorProductList.stream()
                .map(cpeNeedle -> CPE_NEEDLE_PREFIX + cpeNeedle + CPE_NEEDLE_POSTFIX)
                .collect(Collectors.toList());
    }
}
