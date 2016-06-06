package com.bosch.osmi.sw360.cvesearch.datasource;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class CveSearchGuesserTest {

    private CveSearchGuesser cveSearchGuesser;
    private CveSearchApiImpl cveSearchApi;

    @Before
    public void setup() throws IOException {
        cveSearchApi = new CveSearchApiImpl("https://cve.circl.lu");
        this.cveSearchGuesser = new CveSearchGuesser(cveSearchApi);
    }

    @Test
    public void guessVendorTestApacheFullMatch() throws Exception {
        String apache = "apache";
        String result = this.cveSearchGuesser.guessVendor(apache);
        assert(result.equals(apache));
    }

    @Test
    public void guessProductTestMavenFullMatch() throws Exception {
        String apache = "apache";
        String maven  = "maven";
        String result = this.cveSearchGuesser.guessProduct(apache,maven);
        assert(result.equals(maven));
    }

}