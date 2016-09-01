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
import com.siemens.sw360.datahandler.common.SW360Utils;
import com.siemens.sw360.datahandler.thrift.components.Release;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.siemens.sw360.datahandler.thrift.components.Release._Fields.*;


public class ReleaseExporter extends ExcelExporter<Release> {
    public static final Map<String, String> nameToDisplayName;
    static {
        nameToDisplayName = new HashMap<>();
        nameToDisplayName.put("id", "release ID");
        nameToDisplayName.put("cpeid", "CPE ID");
        nameToDisplayName.put("componentId", "component ID");
        nameToDisplayName.put("releaseDate", "release date");
        nameToDisplayName.put("externalids", "external IDs");
        nameToDisplayName.put("createdOn", "created on");
        nameToDisplayName.put("createdBy", "created by");
        nameToDisplayName.put("mainlineState", "mainline state");
        nameToDisplayName.put("clearingState", "clearing state");
        nameToDisplayName.put("fossologyId", "fossology id");
        nameToDisplayName.put("clearingTeamToFossologyStatus", "clearing team with FOSSology status");
        nameToDisplayName.put("attachmentInFossology", "attachment in FOSSology");
        nameToDisplayName.put("clearingInformation", "clearing information");
        nameToDisplayName.put("cotsDetails", "COTS details");
        nameToDisplayName.put("mainLicenseIds", "main license IDs");
        nameToDisplayName.put("downloadurl", "downloadurl");
        nameToDisplayName.put("releaseIdToRelationship", "release IDs with relationship");
    }
    private static final List<Release._Fields> IGNORED_FIELDS = ImmutableList.<Release._Fields>builder()
            .add(REVISION)
            .add(ATTACHMENTS)
            .add(DOCUMENT_STATE)
            .add(PERMISSIONS)
            .add(VENDOR_ID)
            .build();

    public static final List<Release._Fields> RENDERED_FIELDS = Release.metaDataMap.keySet()
            .stream()
            .filter(k -> ! IGNORED_FIELDS.contains(k))
            .collect(Collectors.toList());

    private static final List<String> HEADERS = makeHeaders();

    public ReleaseExporter() {
        super(new ReleaseHelper());
    }

    private static List<String> makeHeaders(){
        List<String> headers = RENDERED_FIELDS
                .stream()
                .map(Release._Fields::getFieldName)
                .map(n -> SW360Utils.displayNameFor(n, nameToDisplayName))
                .collect(Collectors.toList());
        return headers;
    }

    protected static class ReleaseHelper implements ExporterHelper<Release> {

        @Override
        public int getColumns() {
            return HEADERS.size();
        }

        @Override
        public List<String> getHeaders() {
            return HEADERS;
        }

        @Override
        public ExcelSubTable makeRows(Release release) {
            List<String> row = new ArrayList<>(getColumns());
            for(Release._Fields field : RENDERED_FIELDS){
                if(release.isSet(field)) {
                    Object fieldValue = release.getFieldValue(field);
                    row.add(SW360Utils.fieldValueAsString(fieldValue));
                } else {
                    row.add("");
                }
            }
            return new ExcelSubTable(row);
        }
    }

}
