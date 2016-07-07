/*
 * Copyright Siemens AG, 2014-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
