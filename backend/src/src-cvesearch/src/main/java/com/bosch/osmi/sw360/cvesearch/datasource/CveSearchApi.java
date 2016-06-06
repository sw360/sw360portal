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

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface CveSearchApi {

    public List<CveSearchData> search(String vendor, String product) throws IOException;
    public List<CveSearchData> cvefor(String cpe) throws IOException;
    public CveSearchData cve(String cve) throws IOException;

    public List<String> allVendorNames() throws IOException;
    public List<String> allProductsOfVendor(String vendorName) throws IOException;

}
