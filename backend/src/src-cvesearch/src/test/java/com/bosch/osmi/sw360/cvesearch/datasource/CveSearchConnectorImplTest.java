package com.bosch.osmi.sw360.cvesearch.datasource;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;

public class CveSearchConnectorImplTest {

    private CveSearchConnectorImpl cveSearchConnector;

    @Before
    public void setUp() {
        cveSearchConnector = new CveSearchConnectorImpl("https://cve.circl.lu");
    }

    @Test
    public void exactSearchTest() throws IOException {
        Collection<CveSearchData> result;
        result = cveSearchConnector.search("zyxel","zywall");
        assert(result != null);
    }
}
