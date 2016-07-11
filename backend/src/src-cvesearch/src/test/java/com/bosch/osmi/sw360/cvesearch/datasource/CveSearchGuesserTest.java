package com.bosch.osmi.sw360.cvesearch.datasource;

import com.bosch.osmi.sw360.cvesearch.datasource.matcher.Match;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

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
    public void getBestZERO() throws Exception {
        assert(cveSearchGuesser.getBest(Collections.emptyList(), Integer.MAX_VALUE).size() == 0);
    }

    @Test
    public void guessVendorTestApacheFullMatch() throws Exception {
        String apache = "apache";

        String result2 = this.cveSearchGuesser.guessVendors(apache).get(0).getNeedle();
        assert(result2.equals(apache));
    }

    @Test
    public void guessProductTestMavenFullMatch() throws Exception {
        String apache = "apache";
        String maven  = "maven";

        String result2 = this.cveSearchGuesser.guessProducts(apache,maven).get(0).getNeedle();
        assert(result2.equals(maven));
    }

    @Test
    public void guessProductTestApacheFullMatchWithThreshold() throws Exception {
        String apache = "apache";
        this.cveSearchGuesser.setVendorThreshold(5);
        List<Match> result = this.cveSearchGuesser.guessVendors(apache);
        assert(result.size() > 1);
    }
}