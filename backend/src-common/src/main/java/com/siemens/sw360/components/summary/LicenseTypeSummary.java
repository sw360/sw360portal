/*
 * Copyright Siemens AG, 2016. Part of the SW360 Portal Project.
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

import com.siemens.sw360.datahandler.thrift.licenses.LicenseType;

import static com.siemens.sw360.datahandler.thrift.ThriftUtils.copyField;
import static com.siemens.sw360.datahandler.thrift.licenses.LicenseType._Fields;

/**
 *
 *
 * @author birgit.heydenreich@tngtech.com
 */
public class LicenseTypeSummary extends DocumentSummary<LicenseType> {

    @Override
    protected LicenseType summary(SummaryType type, LicenseType document) {
        // Copy required details
        LicenseType copy = new LicenseType();

        switch (type) {
            case EXPORT_SUMMARY:
                copyField(document, copy, _Fields.LICENSE_TYPE);
                copyField(document, copy, _Fields.LICENSE_TYPE_ID);
                copyField(document, copy, _Fields.ID);
            default:
        }

        return copy;
    }
}
