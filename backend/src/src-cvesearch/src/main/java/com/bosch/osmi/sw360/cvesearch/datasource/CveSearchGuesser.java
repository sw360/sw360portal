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

import com.bosch.osmi.sw360.cvesearch.datasource.matcher.ListMatcher;
import com.bosch.osmi.sw360.cvesearch.datasource.matcher.StringMatch;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class CveSearchGuesser {

    private final CveSearchApi cveSearchApi;
    private ListMatcher vendorMatcher;
    private Map<String,ListMatcher> productMatchers;

    public CveSearchGuesser(CveSearchApi cveSearchApi) throws IOException {
        this.cveSearchApi=cveSearchApi;
        vendorMatcher = new ListMatcher(this.cveSearchApi.allVendorNames());
        productMatchers = new HashMap<>();
    }

    public void addProductGuesserIfNeeded(String vendor) throws IOException {
        if(! productMatchers.containsKey(vendor)) {
            productMatchers.put(vendor, new ListMatcher(this.cveSearchApi.allProductsOfVendor(vendor)));
        }
    }

    public List<String> getBest(List<StringMatch> matches) {
        List<String> bestMatches = new ArrayList<>();
        int minDistance = matches.get(0).getDistance();

        Iterator<StringMatch> matchesIterator = matches.iterator();
        StringMatch current;
        do{
            current = matchesIterator.next();
            if(current.getDistance() > minDistance){
                break;
            }
            bestMatches.add(current.getNeedle());
        }while(matchesIterator.hasNext());

        return bestMatches;
    }

    public List<String> guessVendors(String vendorHaystack) {
        return getBest(vendorMatcher.getMatches(vendorHaystack));
    }

    public String guessVendor(String vendorHaystack) {
        return vendorMatcher.getMatches(vendorHaystack)
                .get(0)
                .getNeedle();
    }

    public List<String> guessProducts(String vendor, String productHaystack) throws IOException {
        addProductGuesserIfNeeded(vendor);
        return getBest(productMatchers.get(vendor).getMatches(productHaystack));
    }

    public String guessProduct(String vendor, String productHaystack) throws IOException {
        addProductGuesserIfNeeded(vendor);
        return productMatchers.get(vendor)
                .getMatches(productHaystack)
                .get(0)
                .getNeedle();
    }

    public List<String> guessVendorAndProducts(String haystack) throws IOException {
        return guessVendorAndProducts(haystack, haystack);
    }

    public List<String> guessVendorAndProducts(String vendorHaystack, String productHaystack) throws IOException {
        List<String> result = new ArrayList<>();
        List<String> vendors = guessVendors(vendorHaystack);
        for (String vendor :vendors) {
            result.addAll(guessProducts(vendor, productHaystack).stream()
                    .map(product -> vendor + ":" + product)
                    .collect(Collectors.toList()));
        }
        return result;
    }

    public String guessVendorAndProduct(String vendorHaystack, String productHaystack) throws IOException {
        String vendor = guessVendor(vendorHaystack);
        String product = guessProduct(vendor, productHaystack);

        return vendor + ":" + product;
    }
}
