/*
 * Copyright Siemens AG, 2014-2016. Part of the SW360 Portal Project.
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
package com.siemens.sw360.components.summary;

import com.siemens.sw360.datahandler.thrift.licenses.License;

import static com.siemens.sw360.datahandler.thrift.ThriftUtils.copyField;
import static com.siemens.sw360.datahandler.thrift.licenses.License._Fields;

/**
 * Created by bodet on 17/02/15.
 *
 * @author cedric.bodet@tngtech.com
 */
public class LicenseSummary extends DocumentSummary<License> {

    @Override
    protected License summary(SummaryType type, License document) {
        // Copy required details
        License copy = new License();

        switch (type) {
            case EXPORT_SUMMARY:
                copyField(document, copy, _Fields.GPLV2_COMPAT);
                copyField(document, copy, _Fields.REVIEWDATE);
                copyField(document, copy, _Fields.RISKS);
            case SUMMARY:
                copyField(document, copy, _Fields.LICENSE_TYPE);
            default:
                copyField(document, copy, _Fields.ID);
                copyField(document, copy, _Fields.FULLNAME);
                copyField(document, copy, _Fields.LICENSE_TYPE_DATABASE_ID);
        }

        return copy;
    }


}
