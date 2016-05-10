/*
 * Copyright Siemens AG, 2014-2016. Part of the SW360 Portal Project.
 * With modifications by Bosch Software Innovations GmbH, 2016.
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

import com.siemens.sw360.datahandler.thrift.licenses.License;
import com.siemens.sw360.commonIO.ConvertRecord;
import com.siemens.sw360.datahandler.thrift.licenses.LicenseType;
import org.apache.log4j.Logger;

import static com.siemens.sw360.commonIO.ConvertRecord.*;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by bodet on 10/02/15.
 *
 * @author cedric.bodet@tngtech.com
 */
public class LicenseExporter extends ExcelExporter<License> {
    private static final Logger log = Logger.getLogger(LicenseExporter.class);

    public LicenseExporter(Function<Logger, List<LicenseType>> getLicenseTypes) {
        super(new LicenseHelper(() -> getLicenseTypes.apply(log)));
    }

    private static class LicenseHelper implements ExporterHelper<License> {
        private final ConvertRecord.Serializer<License> converter;
        private Supplier<List<LicenseType>> getLicenseTypes;
        private HashMap<String, String> formattedStringToTypeId = new HashMap<>();
        int indexOfTypeOrId;

        public LicenseHelper(Supplier<List<LicenseType>> getLicenseTypes) {
            this.getLicenseTypes = getLicenseTypes;
            converter = licenseSerializer();
            indexOfTypeOrId = converter.headers().indexOf("Type");
        }

        public void fillLicenseTypeIdToFormattedString() {
            formattedStringToTypeId.put("","");
            List<LicenseType> licenseTypes = getLicenseTypes.get();
            for (LicenseType licenseType: licenseTypes) {
                String formattedLicenseType = getFormattedStringForLicenseType(licenseType);
                formattedStringToTypeId.put(String.valueOf(licenseType.getLicenseTypeId()),
                        formattedLicenseType);
                formattedStringToTypeId.put(String.valueOf(licenseType.getId()),
                        formattedLicenseType);
            }
        }

        private String getFormattedStringForLicenseType(LicenseType licenseType) {
            return licenseType.getLicenseTypeId() + ": " + licenseType.getLicenseType();
        }

        @Override
        public int getColumns() {
            return converter.headers().size();
        }

        @Override
        public List<String> getHeaders() {
            return converter.headers();
        }

        private List<String> formatRow(List<String> row) {
            if(formattedStringToTypeId.size() == 0) {
                fillLicenseTypeIdToFormattedString();
            }

            row.set(indexOfTypeOrId, formattedStringToTypeId.get(row.get(indexOfTypeOrId)));
            return row;
        }

        @Override
        public List<String> makeRow(License license) {
            return formatRow(converter.transformer().apply(license));
        }
    }

}
