/*
 * Copyright Siemens AG, 2014-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.components.summary;

import com.google.common.base.Strings;
import com.siemens.sw360.datahandler.db.VendorRepository;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.components.Release._Fields;
import com.siemens.sw360.exporter.ReleaseExporter;

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
        if(type == SummaryType.DETAILED_EXPORT_SUMMARY){
           setDetailedExportSummaryFields(document, copy);
        } else {
            setShortSummaryFields(document,copy);
            if (type != SummaryType.SHORT) {
               setAdditionalFieldsForSummariesOtherThanShortAndDetailedExport(document, copy);
            }
        }
        if (document.isSetVendorId()) {
            final String vendorId = document.getVendorId();
            if (!Strings.isNullOrEmpty(vendorId)) {
                copy.setVendor(vendorRepository.get(vendorId));
            }
        }
        return copy;
    }

    private void setDetailedExportSummaryFields(Release document, Release copy) {
        for (_Fields renderedField : ReleaseExporter.RELEASE_RENDERED_FIELDS) {
            copyField(document, copy, renderedField);
        }
    }

    private void setShortSummaryFields(Release document, Release copy) {
        copyField(document, copy, _Fields.ID);
        copyField(document, copy, _Fields.NAME);
        copyField(document, copy, _Fields.VERSION);
        copyField(document, copy, _Fields.COMPONENT_ID);
        copyField(document, copy, _Fields.CLEARING_TEAM_TO_FOSSOLOGY_STATUS);
        copyField(document, copy, _Fields.FOSSOLOGY_ID);
    }

    private void setAdditionalFieldsForSummariesOtherThanShortAndDetailedExport(Release document, Release copy){
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

}
