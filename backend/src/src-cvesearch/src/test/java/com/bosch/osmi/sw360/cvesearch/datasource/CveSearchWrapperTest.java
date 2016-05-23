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

import static com.bosch.osmi.sw360.cvesearch.datasource.CveSearchDataTestHelper.isEqivalent;

public class CveSearchWrapperTest {
    CveSearchApi cveSearchApi;
    CveSearchWrapper cveSearchWrapper;

    String VENDORNAME = "zyxel";
    String PRODUCTNAME = "zywall";
    String CPE = "cpe:2.3:a:zyxel:zywall:1050";

    private class ReleaseGen{
        private String name, version, cpe, vendorFullname, vendorShortname;

        public ReleaseGen setName(String name) {
            this.name = name;
            return this;
        }

        public ReleaseGen setVersion(String version) {
            this.version = version;
            return this;
        }

        public ReleaseGen setCpe(String cpe) {
            this.cpe = cpe;
            return this;
        }

        public ReleaseGen setVendorFullname(String vendorFullname) {
            this.vendorFullname = vendorFullname;
            return this;
        }

        public ReleaseGen setVendorShortname(String vendorShortname) {
            this.vendorShortname = vendorShortname;
            return this;
        }

        public Release get() {
            return new Release() {
                @Override
                public String getName() {
                    return name;
                }
                @Override
                public boolean isSetName() {
                    return name!=null;
                }
                @Override
                public String getVersion() {
                    return version;
                }
                @Override
                public boolean isSetVersion() {
                    return version!=null;
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

    @Test
    public  void compareToWithoutWrapper() throws IOException {
        Release release = new ReleaseGen()
                .setName(PRODUCTNAME)
                .setVendorFullname(VENDORNAME)
                .get();

        List<CveSearchData> resultDirect = cveSearchApi.search(VENDORNAME, PRODUCTNAME);

        List<CveSearchData> resultWrapped = cveSearchWrapper.searchForRelease(release);

        assert(resultWrapped != null);
        assert(resultWrapped.size() > 0);
        assert(isEqivalent(resultDirect,resultWrapped));
    }

    @Ignore
    @Test
    public void testLargeData() throws IOException {
        Release release = new ReleaseGen()
                .setVendorShortname("apache")
                .get();

        List<CveSearchData> resultWrapped = cveSearchWrapper.searchForRelease(release);
        assert(resultWrapped != null);
    }

    @Ignore
    @Test
    public void testVeryLargeData() throws IOException {
        Release release = new ReleaseGen().get();

        List<CveSearchData> resultWrapped = cveSearchWrapper.searchForRelease(release);
        assert(resultWrapped != null);
    }

    @Test
    public void compareToSearchByCPE() throws IOException {
        Release release = new ReleaseGen()
                .setName("blindstring")
                .setVendorFullname("blindstring")
                .setVendorShortname("blindstring")
                .setCpe(CPE)
                .get();

        List<CveSearchData> resultDirect = cveSearchApi.cvefor(CPE);

        List<CveSearchData> resultWrapped = cveSearchWrapper.searchForRelease(release);

        assert(resultWrapped != null);

        assert(isEqivalent(resultDirect,resultWrapped));
    }

    @Ignore
    @Test
    public void compareToBDPdata() throws IOException {
        Release release = new ReleaseGen()
                .setName(VENDORNAME + " " + PRODUCTNAME)
                .get();

        List<CveSearchData> resultDirect = cveSearchApi.search(VENDORNAME, PRODUCTNAME);

        List<CveSearchData> resultWrapped = cveSearchWrapper.searchForRelease(release);

        assert(resultWrapped != null);

        assert(isEqivalent(resultDirect,resultWrapped));
    }
}
