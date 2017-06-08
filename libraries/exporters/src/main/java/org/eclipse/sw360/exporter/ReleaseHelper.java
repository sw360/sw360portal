/*
 * Copyright Siemens AG, 2013-2017. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.exporter;

import org.apache.thrift.TException;
import org.eclipse.sw360.datahandler.common.UncheckedSW360Exception;
import org.eclipse.sw360.datahandler.thrift.ReleaseRelationship;
import org.eclipse.sw360.datahandler.thrift.SW360Exception;
import org.eclipse.sw360.datahandler.thrift.components.*;
import org.eclipse.sw360.datahandler.thrift.vendors.Vendor;

import java.util.*;
import java.util.stream.Collectors;

import static org.eclipse.sw360.datahandler.common.CommonUtils.nullToEmptySet;
import static org.eclipse.sw360.datahandler.common.SW360Utils.fieldValueAsString;
import static org.eclipse.sw360.datahandler.common.SW360Utils.putReleaseNamesInMap;
import static org.eclipse.sw360.exporter.ReleaseExporter.*;

class ReleaseHelper implements ExporterHelper<Release> {
    private final ComponentService.Iface client;

    private Map<String, Release> preloadedLinkedReleases = null;

    protected ReleaseHelper(ComponentService.Iface client) {
        this.client = client;
    }



    @Override
    public int getColumns() {
        return getHeaders().size();
    }

    @Override
    public List<String> getHeaders() {
        return HEADERS;
    }

    @Override
    public SubTable makeRows(Release release) throws SW360Exception {
        if (!release.isSetAttachments()) {
            release.setAttachments(Collections.emptySet());
        }
        List<String> row = new ArrayList<>();
        for (Release._Fields renderedField : RELEASE_RENDERED_FIELDS) {
            addFieldValueToRow(row, renderedField, release);
        }
        return new SubTable(row);
    }

    private void addFieldValueToRow(List<String> row, Release._Fields field, Release release) throws SW360Exception {
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
            case ECC_INFORMATION:
                addEccInformationToRow(release.getEccInformation(), row);
                break;
            case RELEASE_ID_TO_RELATIONSHIP:
                addReleaseIdToRelationShipToRow(release.getReleaseIdToRelationship(), row);
                break;
            case ATTACHMENTS:
                row.add(release.attachments.size() + "");
                break;
            default:
                Object fieldValue = release.getFieldValue(field);
                row.add(fieldValueAsString(fieldValue));
        }
    }

    private void addVendorToRow(Vendor vendor, List<String> row) throws SW360Exception {
        try {
            Vendor.metaDataMap
                    .keySet()
                    .stream()
                    .filter(f -> !VENDOR_IGNORED_FIELDS.contains(f))
                    .forEach(f -> {
                        if (vendor != null && vendor.isSet(f)) {
                            try {
                                row.add(fieldValueAsString(vendor.getFieldValue(f)));
                            } catch (SW360Exception e) {
                                throw new UncheckedSW360Exception(e);
                            }
                        } else {
                            row.add("");
                        }
                    });
        } catch (UncheckedSW360Exception e) {
            throw e.getSW360ExceptionCause();
        }

    }

    private void addCotsDetailsToRow(COTSDetails cotsDetails, List<String> row) throws SW360Exception {
        try {
            COTSDetails.metaDataMap.keySet().forEach(f -> {
                if (cotsDetails != null && cotsDetails.isSet(f)) {
                    try {
                        row.add(fieldValueAsString(cotsDetails.getFieldValue(f)));
                    } catch (SW360Exception e) {
                        throw new UncheckedSW360Exception(e);
                    }
                } else {
                    row.add("");
                }
            });
        } catch (UncheckedSW360Exception e) {
            throw e.getSW360ExceptionCause();
        }
    }

    private void addClearingInformationToRow(ClearingInformation clearingInformation, List<String> row) throws SW360Exception {
        try {
            ClearingInformation.metaDataMap.keySet().forEach(f -> {
                if (clearingInformation != null && clearingInformation.isSet(f)) {
                    try {
                        row.add(fieldValueAsString(clearingInformation.getFieldValue(f)));
                    } catch (SW360Exception e) {
                        throw new UncheckedSW360Exception(e);
                    }
                } else {
                    row.add("");
                }
            });
        } catch (UncheckedSW360Exception e) {
            throw e.getSW360ExceptionCause();
        }
    }

    private void addEccInformationToRow(EccInformation eccInformation, List<String> row) throws SW360Exception {
        try {
            EccInformation.metaDataMap.keySet().forEach(f -> {
                if (eccInformation != null && eccInformation.isSet(f)) {
                    try {
                        row.add(fieldValueAsString(eccInformation.getFieldValue(f)));
                    } catch (SW360Exception e) {
                        throw new UncheckedSW360Exception(e);
                    }
                } else {
                    row.add("");
                }
            });
        } catch (UncheckedSW360Exception e) {
            throw e.getSW360ExceptionCause();
        }
    }

    private void addReleaseIdToRelationShipToRow(Map<String, ReleaseRelationship> releaseIdToRelationship, List<String> row) throws SW360Exception {
        if (releaseIdToRelationship != null) {
            row.add(fieldValueAsString(putReleaseNamesInMap(releaseIdToRelationship, getReleases(releaseIdToRelationship
                    .keySet()))));
        } else {
            row.add("");
        }
    }

    public void setPreloadedLinkedReleases(Map<String, Release> preloadedLinkedReleases) {
        this.preloadedLinkedReleases = preloadedLinkedReleases;
    }

    List<Release> getReleases(Set<String> ids) throws SW360Exception {
        if (preloadedLinkedReleases != null){
            return getPreloadedReleases(ids);
        }
        List<Release> releasesByIdsForExport;
        try {
            releasesByIdsForExport = client.getReleasesByIdsForExport(nullToEmptySet(ids));
        } catch (TException e) {
            throw new SW360Exception("Error fetching release information");
        }
        return releasesByIdsForExport;
    }

    private List<Release> getPreloadedReleases(Set<String> ids) {
        return ids.stream().map(preloadedLinkedReleases::get).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
