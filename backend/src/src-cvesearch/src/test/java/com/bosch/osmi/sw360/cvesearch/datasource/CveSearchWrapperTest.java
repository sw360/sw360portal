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

import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.vendors.Vendor;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.bosch.osmi.sw360.cvesearch.datasource.CveSearchDataTestHelper.isEquivalent;

public class CveSearchWrapperTest {
    CveSearchApi cveSearchApi;
    CveSearchWrapper cveSearchWrapper;

    String VENDORNAME = "zyxel";
    String PRODUCTNAME = "zywall";
    String CPE = "cpe:2.3:a:zyxel:zywall:1050";

    private class ReleaseBuilder {
        private String releaseName, releaseVersion, cpe, vendorFullname, vendorShortname;

        public ReleaseBuilder setName(String releaseName) {
            this.releaseName = releaseName;
            return this;
        }

        public ReleaseBuilder setVersion(String releaseVersion) {
            this.releaseVersion = releaseVersion;
            return this;
        }

        public ReleaseBuilder setCpe(String cpe) {
            this.cpe = cpe;
            return this;
        }

        public ReleaseBuilder setVendorFullname(String vendorFullname) {
            this.vendorFullname = vendorFullname;
            return this;
        }

        public ReleaseBuilder setVendorShortname(String vendorShortname) {
            this.vendorShortname = vendorShortname;
            return this;
        }

        public Release get() {
            return new Release() {
                @Override
                public String getName() {
                    return releaseName;
                }
                @Override
                public boolean isSetName() {
                    return name!=null;
                }
                @Override
                public String getVersion() {
                    return releaseVersion;
                }
                @Override
                public boolean isSetVersion() {
                    return releaseVersion!=null;
                }
                @Override
                public Vendor getVendor() {
                    return new Vendor() {
                        @Override
                        public String getFullname() {
                            return vendorFullname;
                        }
                        @Override
                        public boolean isSetFullname() {
                            return vendorFullname!=null;
                        }
                        @Override
                        public String getShortname() {
                            return vendorShortname;
                        }
                        @Override
                        public boolean isSetShortname() {
                            return vendorShortname!=null;
                        }
                    };
                }
                @Override
                public String getCpeid() {
                    return cpe;
                }
                @Override
                public boolean isSetCpeid() {
                    return cpe!=null;
                }
            };
        }
    }

    @Before
    public void setUp() {
        cveSearchApi = new CveSearchApiImpl("https://cve.circl.lu");

        cveSearchWrapper = new CveSearchWrapper(cveSearchApi);
    }

    @Ignore
    @Test
    public void testLargeData() throws IOException {
        Release release = new ReleaseBuilder()
                .setName("server")
                .get();

        Optional<List<CveSearchData>> resultWrapped = cveSearchWrapper.searchForRelease(release);
        assert(resultWrapped.isPresent());
        assert(resultWrapped.get() != null);
    }

    @Test
    public void compareToSearchByCPE() throws IOException {
        Release release = new ReleaseBuilder()
                .setName("blindstring")
                .setVendorFullname("blindstring")
                .setVendorShortname("blindstring")
                .setCpe(CPE)
                .get();

        List<CveSearchData> resultDirect = cveSearchApi.cvefor(CPE);

        Optional<List<CveSearchData>> resultWrapped = cveSearchWrapper.searchForRelease(release);

        assert(resultWrapped.isPresent());
        assert(resultWrapped.get() != null);

        assert(isEquivalent(resultDirect,resultWrapped.get()));
    }

    @Test
    public void compareToBDPdata() throws IOException {
        Release release = new ReleaseBuilder()
                .setName(VENDORNAME + " " + PRODUCTNAME)
                .get();

        List<CveSearchData> resultDirect = cveSearchApi.search(VENDORNAME, PRODUCTNAME);

        Optional<List<CveSearchData>> resultWrapped = cveSearchWrapper.searchForRelease(release);

        assert(resultWrapped.isPresent());
        assert(resultWrapped.get() != null);
        assert(isEquivalent(resultDirect,resultWrapped.get()));
    }

    @Ignore("meanwhile cveSearchWrapper implementation changed, test maybe suitable for later use")
    @Test
    public  void compareToWithoutWrapper() throws IOException {
        Release release = new ReleaseBuilder()
                .setName(PRODUCTNAME)
                .setVendorFullname(VENDORNAME)
                .get();

        List<CveSearchData> resultDirect = cveSearchApi.search(VENDORNAME, PRODUCTNAME);

        Optional<List<CveSearchData>> resultWrapped = cveSearchWrapper.searchForRelease(release);

        assert(resultWrapped.isPresent());
        assert(resultWrapped.get() != null);
        assert(resultWrapped.get().size() > 0);
        assert(isEquivalent(resultDirect,resultWrapped.get()));
    }
}
