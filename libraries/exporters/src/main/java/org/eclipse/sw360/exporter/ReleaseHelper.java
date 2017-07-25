/*
 * Copyright Siemens AG, 2013-2017. Part of the SW360 Portal Project.
 *
 * SPDX-License-Identifier: EPL-1.0
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.exporter;

import org.apache.commons.collections4.MapUtils;
import org.apache.thrift.TException;
import org.eclipse.sw360.datahandler.common.ThriftEnumUtils;
import org.eclipse.sw360.datahandler.common.UncheckedSW360Exception;
import org.eclipse.sw360.datahandler.thrift.ReleaseRelationship;
import org.eclipse.sw360.datahandler.thrift.SW360Exception;
import org.eclipse.sw360.datahandler.thrift.ThriftUtils;
import org.eclipse.sw360.datahandler.thrift.components.*;
import org.eclipse.sw360.datahandler.thrift.projects.ProjectNamesWithMainlineStatesTuple;
import org.eclipse.sw360.datahandler.thrift.vendors.Vendor;

import java.util.*;
import java.util.stream.Collectors;

import static org.eclipse.sw360.datahandler.common.CommonUtils.nullToEmptySet;
import static org.eclipse.sw360.datahandler.common.SW360Utils.fieldValueAsString;
import static org.eclipse.sw360.datahandler.common.SW360Utils.putReleaseNamesInMap;
import static org.eclipse.sw360.exporter.ReleaseExporter.HEADERS;
import static org.eclipse.sw360.exporter.ReleaseExporter.HEADERS_EXTENDED_BY_ADDITIONAL_DATA;
import static org.eclipse.sw360.exporter.ReleaseExporter.RELEASE_RENDERED_FIELDS;
import static org.eclipse.sw360.exporter.ReleaseExporter.VENDOR_IGNORED_FIELDS;

class ReleaseHelper implements ExporterHelper<Release> {

    private final ComponentService.Iface cClient;

    /**
     * if a not empty map is assigned to this field, additional data has to be added
     * to each row
     */
    private final Map<Release, ProjectNamesWithMainlineStatesTuple> releaseToShortenedStringsMap;

    private Map<String, Release> preloadedLinkedReleases = null;
    private Map<String, Component> preloadedComponents = null;

    /**
     * @param cClient
     *            a {@link ComponentService.Iface} implementation
     * @throws SW360Exception
     */
    protected ReleaseHelper(ComponentService.Iface cClient) throws SW360Exception {
        this(cClient, null);
    }

    /**
     * @param cClient
     *            a {@link ComponentService.Iface} implementation
     * @param releaseToShortenedStringsMap
     *            has to be given if additional data like component type and project
     *            origin should be included - may be <code>null</code> or an empty
     *            map otherwise
     * @throws SW360Exception
     */
    protected ReleaseHelper(ComponentService.Iface cClient,
            Map<Release, ProjectNamesWithMainlineStatesTuple> releaseToShortenedStringsMap) throws SW360Exception {
        this.cClient = cClient;
        this.releaseToShortenedStringsMap = releaseToShortenedStringsMap;
        this.preloadedComponents = new HashMap<>();

        if (addAdditionalData()) {
            batchloadComponents(this.releaseToShortenedStringsMap.keySet().stream().map(Release::getComponentId)
                    .collect(Collectors.toSet()));
        }
    }

    @Override
    public int getColumns() {
        return getHeaders().size();
    }

    @Override
    public List<String> getHeaders() {
        return addAdditionalData() ? HEADERS_EXTENDED_BY_ADDITIONAL_DATA : HEADERS;
    }

    @Override
    public SubTable makeRows(Release release) throws SW360Exception {
        List<String> row = new ArrayList<>();
        for (Release._Fields renderedField : RELEASE_RENDERED_FIELDS) {
            addFieldValueToRow(row, renderedField, release);
        }
        return new SubTable(row);
    }

    private void addFieldValueToRow(List<String> row, Release._Fields field, Release release) throws SW360Exception {
        switch (field) {
            case COMPONENT_ID:
                // first, add data for given field
                row.add(release.getComponentId());

                // second, add joined data - but only if wanted
                // remark that headers have already been added accordingly
                if (addAdditionalData()) {
                    // add component type
                    Component component = this.preloadedComponents.get(release.componentId);
                    if (component != null) {
                        row.add(ThriftEnumUtils.enumToString(component.getComponentType()));
                    } else {
                        row.add("");
                    }

                    // add project origin
                    if (releaseToShortenedStringsMap.containsKey(release)) {
                        row.add(releaseToShortenedStringsMap.get(release).projectNames);
                    } else {
                        row.add("");
                    }
                }
                break;
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
                String size = Integer.toString(release.isSetAttachments() ? release.getAttachments().size() : 0);
                row.add(size);
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

    private boolean addAdditionalData() {
        return MapUtils.isNotEmpty(releaseToShortenedStringsMap);
    }

    public void setPreloadedLinkedReleases(Map<String, Release> preloadedLinkedReleases) throws SW360Exception {
        this.preloadedLinkedReleases = preloadedLinkedReleases;

        this.batchloadComponents(
                preloadedLinkedReleases.values().stream().map(Release::getComponentId).collect(Collectors.toSet()));
    }

    private void batchloadComponents(Set<String> cIds) throws SW360Exception {
        // components are only needed if additional data should be added
        if (addAdditionalData()) {
            try {
                List<Component> componentsShort = cClient.getComponentsShort(cIds);

                this.preloadedComponents.putAll(ThriftUtils.getIdMap(componentsShort));
            } catch (TException e) {
                throw new SW360Exception(
                        "Could not get Components for ids [" + cIds + "] because of:\n" + e.getMessage());
            }
        }
    }

    List<Release> getReleases(Set<String> ids) throws SW360Exception {
        if (preloadedLinkedReleases != null){
            return getPreloadedReleases(ids);
        }
        List<Release> releasesByIdsForExport;
        try {
            releasesByIdsForExport = cClient.getReleasesByIdsForExport(nullToEmptySet(ids));
        } catch (TException e) {
            throw new SW360Exception("Error fetching release information");
        }

        // update preload cache so that it is available on next call to this method
        setPreloadedLinkedReleases(ThriftUtils.getIdMap(releasesByIdsForExport));

        return releasesByIdsForExport;
    }

    private List<Release> getPreloadedReleases(Set<String> ids) {
        return ids.stream().map(preloadedLinkedReleases::get).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
