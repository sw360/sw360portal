/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.exporter;

import com.google.common.collect.ImmutableList;
import com.siemens.sw360.datahandler.thrift.components.*;
import com.siemens.sw360.datahandler.thrift.vendors.Vendor;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import java.util.*;
import java.util.stream.Collectors;

import static com.siemens.sw360.datahandler.common.CommonUtils.nullToEmptySet;
import static com.siemens.sw360.datahandler.common.SW360Utils.*;
import static com.siemens.sw360.datahandler.thrift.components.Release._Fields.*;


public class ReleaseExporter extends ExcelExporter<Release> {

    private static final Logger log = Logger.getLogger(ReleaseExporter.class);
    public static final Map<String, String> nameToDisplayName;

    static {
        nameToDisplayName = new HashMap<>();
        nameToDisplayName.put(Release._Fields.ID.getFieldName(), "release ID");
        nameToDisplayName.put(Release._Fields.CPEID.getFieldName(), "CPE ID");
        nameToDisplayName.put(Release._Fields.COMPONENT_ID.getFieldName(), "component ID");
        nameToDisplayName.put(Release._Fields.RELEASE_DATE.getFieldName(), "release date");
        nameToDisplayName.put(Release._Fields.EXTERNAL_IDS.getFieldName(), "external IDs");
        nameToDisplayName.put(Release._Fields.CREATED_ON.getFieldName(), "created on");
        nameToDisplayName.put(Release._Fields.CREATED_BY.getFieldName(), "created by");
        nameToDisplayName.put(Release._Fields.MAINLINE_STATE.getFieldName(), "mainline state");
        nameToDisplayName.put(Release._Fields.CLEARING_STATE.getFieldName(), "clearing state");
        nameToDisplayName.put(Release._Fields.FOSSOLOGY_ID.getFieldName(), "fossology id");
        nameToDisplayName.put(Release._Fields.CLEARING_TEAM_TO_FOSSOLOGY_STATUS.getFieldName(),
                "clearing team with FOSSology status");
        nameToDisplayName.put(Release._Fields.ATTACHMENT_IN_FOSSOLOGY.getFieldName(), "attachment in FOSSology");
        nameToDisplayName.put(Release._Fields.CLEARING_INFORMATION.getFieldName(), "clearing information");
        nameToDisplayName.put(Release._Fields.COTS_DETAILS.getFieldName(), "COTS details");
        nameToDisplayName.put(Release._Fields.MAIN_LICENSE_IDS.getFieldName(), "main license IDs");
        nameToDisplayName.put(Release._Fields.DOWNLOADURL.getFieldName(), "downloadurl");
        nameToDisplayName.put(Release._Fields.RELEASE_ID_TO_RELATIONSHIP.getFieldName(), "release IDs with relationship");
    }

    private static final List<Release._Fields> IGNORED_FIELDS = ImmutableList.<Release._Fields>builder()
            .add(REVISION)
            .add(ATTACHMENTS)
            .add(DOCUMENT_STATE)
            .add(PERMISSIONS)
            .add(VENDOR_ID)
            .build();

    private static final List<Vendor._Fields> VENDOR_IGNORED_FIELDS = ImmutableList.<Vendor._Fields>builder()
            .add(Vendor._Fields.PERMISSIONS)
            .add(Vendor._Fields.REVISION)
            .add(Vendor._Fields.ID)
            .add(Vendor._Fields.TYPE)
            .build();

    public static final List<Release._Fields> RENDERED_FIELDS = Release.metaDataMap.keySet()
            .stream()
            .filter(k -> !IGNORED_FIELDS.contains(k))
            .collect(Collectors.toList());

    private static final List<String> HEADERS = makeHeaders();

    public ReleaseExporter(ComponentService.Iface client) {
        super(new ReleaseHelper(client));
    }

    private static List<String> makeHeaders() {
        List<String> headers = new ArrayList();
        for (Release._Fields field : RENDERED_FIELDS) {
            addToHeaders(headers, field);
        }
        return headers;
    }

    private static void addToHeaders(List<String> headers, Release._Fields field) {
        switch (field) {
            case VENDOR:
                Vendor.metaDataMap.keySet().stream()
                        .filter(f -> ! VENDOR_IGNORED_FIELDS.contains(f))
                        .forEach(f -> headers.add("vendor " + f.getFieldName()));
                break;
            case COTS_DETAILS:
                COTSDetails.metaDataMap.keySet().stream()
                        .forEach(f -> headers.add("COTS details: " + f.getFieldName()));
                break;
            case CLEARING_INFORMATION:
                ClearingInformation.metaDataMap.keySet().stream()
                        .forEach(f -> headers.add("clearing information: " + f.getFieldName()));
                break;
            default:
                headers.add(displayNameFor(field.getFieldName(), nameToDisplayName));
        }
    }

    protected static class ReleaseHelper implements ExporterHelper<Release> {

        private final ComponentService.Iface client;

        protected ReleaseHelper(ComponentService.Iface client) {
            this.client = client;
        }

        @Override
        public int getColumns() {
            return HEADERS.size();
        }

        @Override
        public List<String> getHeaders() {
            return HEADERS;
        }

        @Override
        public SubTable makeRows(Release release) {
            List<String> row = new ArrayList<>();
            for (Release._Fields field : RENDERED_FIELDS) {
                switch (field) {
                    case VENDOR:
                        addVendorToRow(release.getVendor(), row);
                        break;
                    case COTS_DETAILS:
                        addCotsDetailsToRow(release.getCotsDetails(), row);
                        break;
                    case CLEARING_INFORMATION:
                        addClearingInformationToRow(release.getClearingInformation(), row);
                        break;
                    case RELEASE_ID_TO_RELATIONSHIP:
                        addReleaseIdToRelationShipToRow(release.getReleaseIdToRelationship(), row);
                        break;
                    default:
                        if (release.isSet(field)) {
                            Object fieldValue = release.getFieldValue(field);
                            row.add(fieldValueAsString(fieldValue));
                        } else {
                            row.add("");
                        }
                }
            }
            return new SubTable(row);
        }

        private void addVendorToRow(Vendor vendor, List<String> row) {
            if (vendor != null) {
                Vendor.metaDataMap.keySet().stream()
                        .filter(f -> ! VENDOR_IGNORED_FIELDS.contains(f))
                        .forEach(f -> {
                            if (vendor.isSet(f)) {
                                row.add(fieldValueAsString(vendor.getFieldValue(f)));
                            } else {
                                row.add("");
                            }
                        });
            } else {
                Vendor.metaDataMap.keySet().stream()
                        .filter(f -> ! VENDOR_IGNORED_FIELDS.contains(f))
                        .forEach(f -> row.add(""));
            }
        }

        private void addCotsDetailsToRow(COTSDetails cotsDetails, List<String> row) {
            if (cotsDetails != null) {
                COTSDetails.metaDataMap.keySet().stream()
                        .forEach(f -> {
                            if (cotsDetails.isSet(f)) {
                                row.add(fieldValueAsString(cotsDetails.getFieldValue(f)));
                            } else {
                                row.add("");
                            }
                        });
            } else {
                COTSDetails.metaDataMap.keySet().stream()
                        .forEach(f -> row.add(""));
            }
        }

        private void addClearingInformationToRow(ClearingInformation clearingInformation, List<String> row) {
            if (clearingInformation != null) {
                ClearingInformation.metaDataMap.keySet().stream()
                        .forEach(f -> {
                            if (clearingInformation.isSet(f)) {
                                row.add(fieldValueAsString(clearingInformation.getFieldValue(f)));
                            } else {
                                row.add("");
                            }
                        });
            } else {
                ClearingInformation.metaDataMap.keySet().stream()
                        .forEach(f -> row.add(""));
            }
        }

        private void addReleaseIdToRelationShipToRow(Map<String, ReleaseRelationship> releaseIdToRelationship, List<String> row) {
            if (releaseIdToRelationship != null) {
                row.add(fieldValueAsString(putReleaseNamesInMap(
                        releaseIdToRelationship,
                        getReleases(releaseIdToRelationship.keySet()))));
            } else {
                row.add("");
            }
        }

        private List<Release> getReleases(Set<String> ids) {
            List<Release> releasesByIdsForExport;
            try {
                releasesByIdsForExport = client.getReleasesByIdsForExport(nullToEmptySet(ids));
            } catch (TException e) {
                log.error("Error fetching release information", e);
                releasesByIdsForExport = Collections.emptyList();
            }
            return releasesByIdsForExport;
        }
    }
}
