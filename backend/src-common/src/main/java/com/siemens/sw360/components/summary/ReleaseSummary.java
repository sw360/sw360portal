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
package com.siemens.sw360.components.summary;

import com.google.common.base.Strings;
import com.siemens.sw360.datahandler.db.VendorRepository;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.components.Release._Fields;

import static com.siemens.sw360.datahandler.thrift.ThriftUtils.copyField;

/**
 * Created by bodet on 17/02/15.
 *
 * @author cedric.bodet@tngtech.com
 */
public class ReleaseSummary extends DocumentSummary<Release> {

    private final VendorRepository vendorRepository;

    public ReleaseSummary() {
        // Create summary without database connection
        this(null);
    }

    public ReleaseSummary(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    @Override
    protected Release summary(SummaryType type, Release document) {
        Release copy = new Release();
        copyField(document, copy, _Fields.ID);
        copyField(document, copy, _Fields.NAME);
        copyField(document, copy, _Fields.VERSION);
        copyField(document, copy, _Fields.COMPONENT_ID);
        copyField(document, copy, _Fields.CLEARING_TEAM_TO_FOSSOLOGY_STATUS);
        copyField(document, copy, _Fields.FOSSOLOGY_ID);

        if(document.isSetVendorId() ) {
            final String vendorId = document.getVendorId();
            if (!Strings.isNullOrEmpty(vendorId)) {
                copy.setVendor(vendorRepository.get(vendorId));
            }
        }

        if (type != SummaryType.SHORT) {
            copyField(document, copy, _Fields.CPEID);
            copyField(document, copy, _Fields.CREATED_BY);
            copyField(document, copy, _Fields.MAINLINE_STATE);
            copyField(document, copy, _Fields.CLEARING_STATE);
            copyField(document, copy, _Fields.RELEASE_DATE);
            copyField(document, copy, _Fields.LANGUAGES);
            copyField(document, copy, _Fields.OPERATING_SYSTEMS);
            copyField(document, copy, _Fields.ATTACHMENTS);
            copyField(document, copy, _Fields.MAIN_LICENSE_IDS);
        }

        return copy;
    }

}
