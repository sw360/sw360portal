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
import com.bosch.osmi.sw360.cvesearch.datasource.matcher.Match;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class CveSearchGuesser {

    private final CveSearchApi cveSearchApi;
    private ListMatcher vendorMatcher;
    private Map<String,ListMatcher> productMatchers;

    private int vendorThreshold = 0;
    private int productThreshold = 0;

    public CveSearchGuesser(CveSearchApi cveSearchApi) {
        this.cveSearchApi=cveSearchApi;
        vendorMatcher = null;
        productMatchers = new HashMap<>();
    }

    public void setVendorThreshold(int vendorThreshold) {
        this.vendorThreshold = vendorThreshold;
    }

    public void setProductThreshold(int productThreshold) {
        this.productThreshold = productThreshold;
    }

    public void addVendorGuesserIfNeeded() throws IOException {
        if(vendorMatcher == null) {
            vendorMatcher = new ListMatcher(cveSearchApi.allVendorNames());
        }
    }

    public void addProductGuesserIfNeeded(String vendor) throws IOException {
        if(! productMatchers.containsKey(vendor)) {
            productMatchers.put(vendor, new ListMatcher(cveSearchApi.allProductsOfVendor(vendor)));
        }
    }

    public List<Match> getBest(List<Match> matches, int threshold) {
        List<Match> bestMatches = new ArrayList<>();
        int minDistance = matches.get(0).getDistance();

        Iterator<Match> matchesIterator = matches.iterator();
        Match current;
        do{
            current = matchesIterator.next();
            if(current.getDistance() > minDistance + threshold){
                break;
            }
            bestMatches.add(current);
        }while(matchesIterator.hasNext());

        return bestMatches;
    }

    public List<Match> guessVendors(String vendorHaystack) throws IOException {
        addVendorGuesserIfNeeded();
        return getBest(vendorMatcher.getMatches(vendorHaystack), vendorThreshold);
    }

    public Match guessVendor(String vendorHaystack) throws IOException {
        addVendorGuesserIfNeeded();
        return vendorMatcher.getMatches(vendorHaystack)
                .get(0);
    }

    public List<Match> guessProducts(String vendor, String productHaystack) throws IOException {
        addProductGuesserIfNeeded(vendor);
        return getBest(productMatchers.get(vendor).getMatches(productHaystack), productThreshold);
    }

    public Match guessProduct(String vendor, String productHaystack) throws IOException {
        addProductGuesserIfNeeded(vendor);
        return productMatchers.get(vendor)
                .getMatches(productHaystack)
                .get(0);
    }

    public List<Match> guessVendorAndProducts(String haystack) throws IOException {
        return guessVendorAndProducts(haystack, haystack);
    }

    public List<Match> guessVendorAndProducts(String vendorHaystack, String productHaystack) throws IOException {
        List<Match> result = new ArrayList<>();
        List<Match> vendors = guessVendors(vendorHaystack);

        for (Match vendor : vendors) {
            result.addAll(guessProducts(vendor.getNeedle(), productHaystack).stream()
                    .map(product -> vendor.concat(product))
                    .collect(Collectors.toList()));
        }

        result.sort((sm1,sm2) -> sm1.compareTo(sm2));
        return getBest(result, 0);

    }

    public Match guessVendorAndProduct(String vendorHaystack, String productHaystack) throws IOException {
        Match vendor = guessVendor(vendorHaystack);
        Match product = guessProduct(vendor.getNeedle(), productHaystack);

        return vendor.concat(product);
    }
}
