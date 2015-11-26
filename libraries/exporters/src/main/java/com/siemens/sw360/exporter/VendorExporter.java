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
import com.siemens.sw360.datahandler.common.ThriftEnumUtils;
import com.siemens.sw360.datahandler.thrift.vendors.Vendor;
import org.apache.thrift.TEnum;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Strings.nullToEmpty;
import static com.siemens.sw360.datahandler.common.CommonUtils.joinStrings;
import static com.siemens.sw360.datahandler.thrift.vendors.Vendor._Fields.*;

/**
 * Created by jn on 03.03.15.
 *
 * @author Johannes.Najjar@tngtech.com
 */
public class VendorExporter  extends  ExcelExporter<Vendor>{

    public static final List<Vendor._Fields> RENDERED_FIELDS = ImmutableList.<Vendor._Fields>builder()
            .add(FULLNAME)
            .add(SHORTNAME)
            .add(URL)
            .build();

    private static final List<String> HEADERS = ImmutableList.<String>builder()
            .add("Vendor Fullname")
            .add("Vendor Shortname")
            .add("URL")
            .build();

    public VendorExporter() {
        super(new VendorHelper());
    }

    private static class VendorHelper implements ExporterHelper<Vendor> {

        @Override
        public int getColumns() {
            return HEADERS.size();
        }

        @Override
        public List<String> getHeaders() {
            return HEADERS;
        }

        @Override
        public List<String> makeRow(Vendor vendor) {
            List<String> row = new ArrayList<>(getColumns());

            for (Vendor._Fields renderedField : RENDERED_FIELDS) {
                Object fieldValue = vendor.getFieldValue(renderedField);

                if (fieldValue instanceof TEnum) {
                    row.add(nullToEmpty(ThriftEnumUtils.enumToString((TEnum) fieldValue)));
                } else if (fieldValue instanceof String) {
                    row.add(nullToEmpty((String) fieldValue));
                } else {
                    row.add("");
                }
            }
            return row;
        }
    }
}
