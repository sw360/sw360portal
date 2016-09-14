/*
 * Copyright (c) Bosch Software Innovations GmbH 2016.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.bosch.osmi.sw360.cvesearch.datasource;

import com.siemens.sw360.datahandler.common.CommonUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class CveSearchApiImplTest {

    String VENDOR  = "zyxel";
    String PRODUCT = "zywall";
    String CPE     = "cpe:2.3:a:zyxel:zywall:1050";
    String CVE     = "CVE-2008-1160";

    private CveSearchApiImpl cveSearchApi;

    @Before
    public void setUp() {
        Properties props = CommonUtils.loadProperties(CveSearchApiImplTest.class, "/cvesearch.properties");
        String host = props.getProperty("cvesearch.host","https://localhost:5000");
        cveSearchApi = new CveSearchApiImpl(host);
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
