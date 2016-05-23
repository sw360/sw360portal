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

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class CveSearchApiImplTest {

    String HOST    = "https://cve.circl.lu";
    String VENDOR  = "zyxel";
    String PRODUCT = "zywall";
    String CPE     = "cpe:2.3:a:zyxel:zywall:1050";
    String CVE     = "CVE-2008-1160";

    private CveSearchApiImpl cveSearchApi;

    @Before
    public void setUp() {
        cveSearchApi = new CveSearchApiImpl(HOST);
    }

    @Test
    public void exactSearchTest() throws IOException {
        List<CveSearchData> result;
        result = cveSearchApi.search(VENDOR,PRODUCT);
        assert(result != null);
    }

    @Test
    public void exactCveforTest() throws IOException {
        List<CveSearchData> result;
        result = cveSearchApi.cvefor(CPE);
        assert(result != null);
    }

    @Test
    public void exactCveTest() throws IOException {
        CveSearchData result;
        result = cveSearchApi.cve(CVE);
        assert(result != null);
    }
}
