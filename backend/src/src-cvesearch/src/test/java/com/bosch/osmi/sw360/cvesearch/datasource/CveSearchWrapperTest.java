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
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.bosch.osmi.sw360.cvesearch.datasource.CveSearchDataTestHelper.isEqivalent;

public class CveSearchWrapperTest {
    CveSearchApi cveSearchApi;
    CveSearchWrapper cveSearchWrapper;

    private String vendorName = "zyxel";
    private String productName = "zywall";

    private Release release;

    @Before
    public void setUp() {
        // TODO: Mock
        cveSearchApi = new CveSearchApiImpl("https://cve.circl.lu");

        cveSearchWrapper = new CveSearchWrapper(cveSearchApi);

        release = new Release() {
            @Override
            public String getName() {
                return productName;
            }
            public Vendor getVendor() {
                return new Vendor(){
                    @Override
                    public String getFullname(){
                        return vendorName;
                    }
                };
            }
        };
    }

    @Test
    public void compareToWithoutWrapper() throws IOException {
        List<CveSearchData> resultDirect = cveSearchApi.search(vendorName, productName);

        Optional<List<CveSearchData>> resultWrapped = cveSearchWrapper.searchForRelease(release);

        assert(resultWrapped.isPresent());

        assert(isEqivalent(resultDirect,resultWrapped.get()));
    }
}
