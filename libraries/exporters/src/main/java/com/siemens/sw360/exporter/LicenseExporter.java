/*
 * Copyright Siemens AG, 2014-2015. Part of the SW360 Portal Project.
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
package com.siemens.sw360.exporter;

import com.google.common.collect.ImmutableList;
import com.siemens.sw360.datahandler.thrift.licenses.License;
import com.siemens.sw360.datahandler.thrift.licenses.LicenseType;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * Created by bodet on 10/02/15.
 *
 * @author cedric.bodet@tngtech.com
 */
public class LicenseExporter extends ExcelExporter<License> {

    private static final int COLUMNS = 6;

    private static final List<String> HEADERS = ImmutableList.<String>builder()
            .add("License ID")
            .add("License Shortname")
            .add("License Fullame")
            .add("License Type")
            .add("GPL v2 Compatibility")
            .add("GPL v2 Compatibility")
            .build();

    public LicenseExporter() {
        super(new LicenseHelper());
    }

    private static class LicenseHelper implements ExporterHelper<License> {

        @Override
        public int getColumns() {
            return COLUMNS;
        }

        @Override
        public List<String> getHeaders() {
            return HEADERS;
        }

        @Override
        public List<String> makeRow(License license) {
            List<String> row = new ArrayList<>(COLUMNS);

            row.add(nullToEmpty(license.id));
            row.add(nullToEmpty(license.shortname));
            row.add(nullToEmpty(license.fullname));
            row.add(formatLicenseType(license.licenseType));
            row.add(formatBoolean(license.GPLv2Compat));
            row.add(formatBoolean(license.GPLv3Compat));

            return row;
        }

        private static String formatLicenseType(LicenseType type) {
            if (type == null || type.getType() == null) return "";
            return type.getType();
        }

        private static String formatBoolean(boolean value) {
            return value ? "yes" : "no";
        }

    }

}
