package com.bosch.osmi.sw360.cvesearch.datasource.heuristics;

import com.bosch.osmi.sw360.cvesearch.datasource.CveSearchApi;
import com.bosch.osmi.sw360.cvesearch.datasource.CveSearchGuesser;
import com.bosch.osmi.sw360.cvesearch.datasource.matcher.Match;
import com.siemens.sw360.datahandler.thrift.components.Release;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.siemens.sw360.datahandler.common.CommonUtils.nullToEmptyString;
import static java.util.Collections.*;

public class SearchLevels {

    private static final String CPE_WILDCARD      = ".*";
    private static final String CPE_NEEDLE_PREFIX = "cpe:2.3:.:";

    private List<SearchLevel> searchLevels;

    public class NeedleWithMeta {
        public String needle;
        public String description;
        public NeedleWithMeta(String needle, String description){
            this.needle = needle;
            this.description = description;
        }
    }

    @FunctionalInterface
    public interface SearchLevel {
        List<NeedleWithMeta> apply(Release release) throws IOException;
    }

    public SearchLevels() {
        searchLevels = new ArrayList<>();

        // Level 1. search by full cpe
        searchLevels.add(mkSearchLevel("by CPE",
                r -> r.isSetCpeid() && isCpe(r.getCpeid().toLowerCase()),
                r -> r.getCpeid().toLowerCase()));
    }

    public List<List<NeedleWithMeta>> apply(Release release) throws IOException {
        List<List<NeedleWithMeta>> result = new ArrayList<>();
        for(SearchLevel searchLevel : searchLevels){
            result.add(searchLevel.apply(release));
        }
        return result;
    }

    //==================================================================================================================
    public SearchLevels addBasicSearchlevels() {
        // Level 2.
        searchLevels.add(mkSearchLevel("by VENDOR_FULL_NAME:NAME:VERSION",
                r -> r.isSetVersion() && r.isSetVendor() && r.getVendor().isSetFullname(),
                r -> r.getVendor().getFullname(),
                Release::getName,
                Release::getVersion));

        // Level 3.
        searchLevels.add(mkSearchLevel("by VENDOR_SHORT_NAME:NAME:VERSION",
                r -> r.isSetVersion() && r.isSetVendor() && r.getVendor().isSetShortname(),
                r ->r.getVendor().getShortname(),
                Release::getName,
                Release::getVersion));

        // Level 4.
        searchLevels.add(mkSearchLevel("by VENDOR_FULL_NAME:NAME",
                r -> r.isSetVendor() && r.getVendor().isSetFullname(),
                r ->r.getVendor().getFullname(),
                Release::getName));

        // Level 5.
        searchLevels.add(mkSearchLevel("by VENDOR_SHORT_NAME:NAME",
                r -> r.isSetVendor() && r.getVendor().isSetShortname(),
                r -> r.getVendor().getShortname(),
                Release::getName));

        // Level 6.
        searchLevels.add(mkSearchLevel("by .*:NAME:VERSION",
                r -> r.isSetVersion(),
                Release::getName,
                Release::getVersion));

        // Level 7.
        searchLevels.add(mkSearchLevel("by .*:NAME",
                r -> true,
                Release::getName));

        return this;
    }

    //==================================================================================================================
    public SearchLevels addGuessingSearchLevels(CveSearchApi cveSearchApi, int vendorThreshold, int productThreshold, int cutoff) {
        CveSearchGuesser cveSearchGuesser = new CveSearchGuesser(cveSearchApi);
        cveSearchGuesser.setVendorThreshold(vendorThreshold);
        cveSearchGuesser.setProductThreshold(productThreshold);
        cveSearchGuesser.setCutoff(cutoff);

        // Level 2. search by guessed vendors and products with version
        //private String description = "Guessing Heuristic (lvl 2)";
        searchLevels.add(release -> guessForRelease(cveSearchGuesser, release, true));

        // Level 3. search by guessed vendors and products without version
        //private String description = "Guessing Heuristic (lvl 3)";
        searchLevels.add(release -> guessForRelease(cveSearchGuesser, release, false));

        return this;
    }


    protected List<NeedleWithMeta> guessForRelease(CveSearchGuesser cveSearchGuesser, Release release, boolean useVersionInformation) throws IOException {
        if(useVersionInformation && !release.isSetVersion()){
            return EMPTY_LIST;
        }

        List<Match> vendorProductList;

        String productHaystack = release.getName();
        if (release.isSetVendor() &&
                (release.getVendor().isSetShortname() || release.getVendor().isSetFullname())) {
            String vendorHaystack = nullToEmptyString(release.getVendor().getShortname()) + " " +
                    nullToEmptyString(release.getVendor().getFullname());
            vendorProductList = cveSearchGuesser.guessVendorAndProducts(vendorHaystack, productHaystack);
        } else {
            vendorProductList = cveSearchGuesser.guessVendorAndProducts(productHaystack);
        }

        String cpeNeedlePostfix = ":" + (useVersionInformation ? release.getVersion() : "") + ".*";
        Function<String,String> cpeBuilder = cpeNeedle -> CPE_NEEDLE_PREFIX + cpeNeedle + cpeNeedlePostfix;

        return vendorProductList.stream()
                .map(match -> new NeedleWithMeta(cpeBuilder.apply(match.getNeedle()),
                        "heuristic (distance " + match.getDistance() + ")"))
                .collect(Collectors.toList());
    }

    //==================================================================================================================
    protected Function<Release,String> escapeGeneratorResult(Function<Release,String> generator) {
        return r -> generator.apply(r)
                .replace('/','.')
                .replace('\\','.')
                .replace('*','.')
                .replace('+','.')
                .replace('!','.')
                .replace('?','.')
                .replace('^','.')
                .replace('$','.')
                .replace('[','.')
                .replace(']','.')
                .replace(" ", CPE_WILDCARD)
                .toLowerCase();
    }

    protected Function<Release, String> implodeSearchNeedleGenerators(Function<Release,String> generator, Function<Release,String> ... generators){
        return Arrays.stream(generators)
                .map(this::escapeGeneratorResult)
                .reduce(generator,
                        (g1,g2) -> r -> g1.apply(r) + CPE_WILDCARD + g2.apply(r));
    }

    protected boolean isCpe(String potentialCpe){
        return (! (null == potentialCpe))
                && potentialCpe.startsWith("cpe:")
                && potentialCpe.length() > 10;
    }

    protected String cpeWrapper(String needle) {
        if (isCpe(needle)){
            return needle;
        }
        return CPE_NEEDLE_PREFIX + CPE_WILDCARD + nullToEmptyString(needle) + CPE_WILDCARD;
    }

    protected SearchLevel mkSearchLevel(String description,
                                        Predicate<Release> isPossible,
                                        Function<Release,String> generator,
                                        Function<Release,String> ... generators){
        Function<Release,String> implodedGenerators = implodeSearchNeedleGenerators(generator, generators);

        return r -> {
            if(isPossible.test(r)){
                return singletonList(new NeedleWithMeta(cpeWrapper(implodedGenerators.apply(r)), description));
            }
            return EMPTY_LIST;
        };
    }
}
