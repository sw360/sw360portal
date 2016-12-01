package org.eclipse.sw360.cvesearch.datasource.heuristics;

import org.eclipse.sw360.datahandler.thrift.components.Release;
import org.junit.Test;

public class SearchLevelsTest {
    @Test
    public void implodeTestOneGenerator() {
        assert(new SearchLevels().implodeSearchNeedleGenerators(r -> "a")
                .apply(new Release())
                .equals("a"));
    }

    @Test
    public void implodeTestThreeGenerators() {
        assert(new SearchLevels().implodeSearchNeedleGenerators(r -> "a", r -> "b", r ->"c")
                .apply(new Release())
                .equals("a.*b.*c"));
    }

    @Test
    public void implodeTestFunction() {
        String name = "name";
        assert(new SearchLevels().implodeSearchNeedleGenerators(r -> "a", r -> r.getName())
                .apply(new Release().setName(name))
                .equals("a.*" + name));
    }

    @Test
    public void isCpeTestNull() {
        assert(new SearchLevels().isCpe(null) == false);
    }

    @Test
    public void isCpeTestEmpty() {
        assert(new SearchLevels().isCpe("") == false);
    }

    @Test
    public void isCpeTest_cpe() {
        assert(new SearchLevels().isCpe("cpe") == false);
    }

    @Test
    public void isCpeTestTrue() {
        assert(new SearchLevels().isCpe("cpe:2.3:a:vendor:product:version"));
    }

    @Test
    public void isCpeTestOldFormat() {
        assert(new SearchLevels().isCpe("cpe:/a:vendor:product:version"));
    }

    @Test
    public void isCpeTestPattern() {
        assert(new SearchLevels().isCpe("cpe:2.3:.*prod.*"));
    }

}