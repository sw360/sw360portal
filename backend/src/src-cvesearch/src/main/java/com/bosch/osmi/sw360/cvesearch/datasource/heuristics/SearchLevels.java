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

    @FunctionalInterface
    public interface SearchLevel {
        List<String> apply(Release release) throws IOException;
    }

    public SearchLevels() {
        searchLevels = new ArrayList<>();

        // Level 1. search by full cpe
        searchLevels.add(mkSearchLevel(r -> r.isSetCpeid() && isCpe(r.getCpeid().toLowerCase()),
                r -> r.getCpeid().toLowerCase()));
    }

    public List<List<String>> apply(Release release) throws IOException {
        List<List<String>> result = new ArrayList<>();
        for(SearchLevel searchLevel : searchLevels){
            result.add(searchLevel.apply(release));
        }
        return result;
    }

    //==================================================================================================================
    public SearchLevels addBasicSearchlevels() {
        // Level 2. search by: VENDOR_FULL_NAME:NAME:VERSION
        searchLevels.add(mkSearchLevel(r -> r.isSetVersion() && r.isSetVendor() && r.getVendor().isSetFullname(),
                r -> r.getVendor().getFullname(),
                Release::getName,
                Release::getVersion));

        // Level 3. search by: VENDOR_SHORT_NAME:NAME:VERSION
        searchLevels.add(mkSearchLevel(r -> r.isSetVersion() && r.isSetVendor() && r.getVendor().isSetShortname(),
                r ->r.getVendor().getShortname(),
                Release::getName,
                Release::getVersion));

        // Level 4. search by: VENDOR_FULL_NAME:NAME
        searchLevels.add(mkSearchLevel(r -> r.isSetVendor() && r.getVendor().isSetFullname(),
                r ->r.getVendor().getFullname(),
                Release::getName));

        // Level 5. search by: VENDOR_SHORT_NAME:NAME
        searchLevels.add(mkSearchLevel(r -> r.isSetVendor() && r.getVendor().isSetShortname(),
                r -> r.getVendor().getShortname(),
                Release::getName));

        // Level 6. search by: .*:NAME:VERSION
        searchLevels.add(mkSearchLevel(r -> r.isSetVersion(),
                Release::getName,
                Release::getVersion));

        // Level 7. search by: .*:NAME
        searchLevels.add(mkSearchLevel(r -> true,
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
        searchLevels.add(r -> guessForRelease(cveSearchGuesser, r, true));

        // Level 3. search by guessed vendors and products without version
        searchLevels.add(r -> guessForRelease(cveSearchGuesser, r, false));

        return this;
    }

    protected List<String> guessForRelease(CveSearchGuesser cveSearchGuesser, Release release, boolean useVersionInformation) throws IOException {
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
                .map(Match::getNeedle)
                .map(cpeBuilder)
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

    protected SearchLevel mkSearchLevel(Predicate<Release> isPossible, Function<Release,String> generator, Function<Release,String> ... generators){
        Function<Release,String> implodedGenerators = implodeSearchNeedleGenerators(generator, generators);

        return r -> {
            if(isPossible.test(r)){
                return singletonList(cpeWrapper(implodedGenerators.apply(r)));
            }
            return EMPTY_LIST;
        };
    }
}
