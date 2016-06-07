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
import org.junit.Test;

import static com.bosch.osmi.sw360.cvesearch.datasource.heuristics.searchlevels.SearchLevelsHelper.implodeSearchNeedleGenerators;
import static com.bosch.osmi.sw360.cvesearch.datasource.heuristics.searchlevels.SearchLevelsHelper.isCpe;

public class SearchLevelsHelperTest {

    @Test
    public void implodeTestOneGenerator() {
        assert(implodeSearchNeedleGenerators(r -> "a")
                .apply(new Release())
                .equals("a"));
    }

    @Test
    public void implodeTestThreeGenerators() {
        assert(implodeSearchNeedleGenerators(r -> "a", r -> "b", r ->"c")
                .apply(new Release())
                .equals("a.*b.*c"));
    }

    @Test
    public void implodeTestFunction() {
        String name = "name";
        assert(implodeSearchNeedleGenerators(r -> "a", r -> r.getName())
                .apply(new Release().setName(name))
                .equals("a.*" + name));
    }

//    @Test
//    public void implodeTestReal() {
//        assert(implodeSearchNeedleGenerators(r ->r.getVendor().getShortname(),
//                        Release::getName,
//                        Release::getVersion)
//                .apply(new ReleaseBuilder()
//                        .setVendorShortname("vendorShortname")
//                        .setName("name")
//                        .setVersion("1.2.3")
//                        .get())
//                .equals("vendorShortname.*name.*1.2.3"));
//    }

    @Test
    public void isCpeTestNull() {
        assert(isCpe(null) == false);
    }

    @Test
    public void isCpeTestEmpty() {
        assert(isCpe("") == false);
    }

    @Test
    public void isCpeTest_cpe() {
        assert(isCpe("cpe") == false);
    }

    @Test
    public void isCpeTestTrue() {
        assert(isCpe("cpe:2.3:a:vendor:product:version"));
    }

    @Test
    public void isCpeTestOldFormat() {
        assert(isCpe("cpe:/a:vendor:product:version"));
    }

    @Test
    public void isCpeTestPattern() {
        assert(isCpe("cpe:2.3:.*prod.*"));
    }

}