package org.eclipse.sw360.cvesearch.datasource.heuristics;

import org.eclipse.sw360.datahandler.thrift.components.Release;
import org.junit.Before;
import org.junit.Test;

public class SearchLevelsTest {

    SearchLevels searchLevels;

    @Before
    public void prepare() {
        searchLevels = new SearchLevels(null);
    }

    @Test
    public void isCpeTestNull() {
        assert(!searchLevels.isCpe(null));
    }

    @Test
    public void isCpeTestEmpty() {
        assert(!searchLevels.isCpe(""));
    }

    @Test
    public void isCpeTest_cpe() {
        assert(!searchLevels.isCpe("cpe"));
    }

    @Test
    public void isCpeTestTrue() {
        assert(searchLevels.isCpe("cpe:2.3:a:vendor:product:version"));
    }

    @Test
    public void isCpeTestOldFormat() {
        assert(searchLevels.isCpe("cpe:/a:vendor:product:version"));
    }

    @Test
    public void isCpeTestPattern() {
        assert(searchLevels.isCpe("cpe:2.3:.*prod.*"));
    }

    @Test
    public void cleanupNewCpeTest() {
        String newCpe = "cpe:2.3:a:Vendor:Product:Version";
        assert(searchLevels.cleanupCPE(newCpe).equals(newCpe.toLowerCase()));
    }

    @Test
    public void cleanupOldCpeTest() {
        String oldCpe = "cpe:/a:vendor:product:version:~~~~";
        String newCpe = "cpe:2.3:a:vendor:product:version";
        assert(searchLevels.cleanupCPE(oldCpe).equals(newCpe));
    }
}