/*
 * Copyright Siemens AG, 2014-2016. Part of the SW360 Portal Project.
 * With modifications by Bosch Software Innovations GmbH, 2016.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.exporter;

import com.google.common.collect.ImmutableList;
import com.siemens.sw360.datahandler.common.SW360Utils;
import com.siemens.sw360.datahandler.thrift.components.ComponentService;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.exporter.ReleaseExporter.ReleaseHelper;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import java.util.*;
import java.util.stream.Collectors;

import static com.siemens.sw360.datahandler.common.CommonUtils.joinStrings;
import static com.siemens.sw360.datahandler.common.CommonUtils.nullToEmptySet;
import static com.siemens.sw360.datahandler.thrift.projects.Project._Fields.*;

public class ProjectExporter extends ExcelExporter<Project> {

    private static final Logger log = Logger.getLogger(ProjectExporter.class);
    private static boolean extendedByReleases;
    private static ReleaseHelper releaseHelper;

    public static final Map<String, String> nameToDisplayName;
    static {
        nameToDisplayName = new HashMap<>();
        nameToDisplayName.put("id", "project ID");
        nameToDisplayName.put("name", "project name");
        nameToDisplayName.put("state", "project state");
        nameToDisplayName.put("createdBy", "created by");
        nameToDisplayName.put("createdOn", "creation date");
        nameToDisplayName.put("projectResponsible", "project responsible");
        nameToDisplayName.put("leadArchitect", "project lead architect");
        nameToDisplayName.put("tag", "project tag");
        nameToDisplayName.put("businessUnit", "group");
        nameToDisplayName.put("releaseIds", "release IDs");
        nameToDisplayName.put("releaseClearingStateSummary", "release clearing state summary");
        nameToDisplayName.put("externalIds", "external IDs");
        nameToDisplayName.put("visbility", "visibility");
        nameToDisplayName.put("projectType", "project type");
        nameToDisplayName.put("linkedProjects", "linked projects");
        nameToDisplayName.put("releaseIdToUsage", "release IDs with usage");
        nameToDisplayName.put("clearingTeam", "clearing team");
        nameToDisplayName.put("preevaluationDeadline", "pre-evaluation deadline");
        nameToDisplayName.put("systemTestStart", "system test start");
        nameToDisplayName.put("systemTestEnd", "system test end");
        nameToDisplayName.put("deliveryStart", "delivery start");
        nameToDisplayName.put("phaseOutSince", "phase out since");
    }

    private static final List<Project._Fields> IGNORED_FIELDS = ImmutableList.<Project._Fields>builder()
            .add(REVISION)
            .add(ATTACHMENTS)
            .add(DOCUMENT_STATE)
            .add(PERMISSIONS)
            .build();

    public static final List<Project._Fields> RENDERED_FIELDS = Project.metaDataMap.keySet()
            .stream()
            .filter(k -> ! IGNORED_FIELDS.contains(k))
            .collect(Collectors.toList());

    protected static List<String> HEADERS = new ArrayList<>();

    public ProjectExporter(ComponentService.Iface client, boolean extendedByReleases) {
        super(new ProjectHelper(client));
        releaseHelper = new ReleaseHelper();
        this.extendedByReleases = extendedByReleases;
        HEADERS = RENDERED_FIELDS
                .stream()
                .map(Project._Fields::getFieldName)
                .map(n -> SW360Utils.displayNameFor(n, nameToDisplayName))
                .collect(Collectors.toList());
        if (extendedByReleases) {
            ((ArrayList) HEADERS).addAll(releaseHelper.getHeaders());
        }
    }

    private static class ProjectHelper implements ExporterHelper<Project> {

        private final ComponentService.Iface client;

        private ProjectHelper(ComponentService.Iface client) {
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
        public ExcelSubTable makeRows(Project project) {
            if (extendedByReleases) {
                return makeRowsWithReleases(project);
            } else {
                return new ExcelSubTable(makeRowForProjectOnly(project));
            }
        }

        private ExcelSubTable makeRowsWithReleases(Project project) {
            List<Release> releases = getReleases(project);
            ExcelSubTable table = new ExcelSubTable();

            if(releases.size() >0) {
                for (Release release : releases) {
                    List<String> currentRow = makeRowForProjectOnly(project);
                    currentRow.addAll(releaseHelper.makeRows(release).elements.get(0));
                    table.addRow(currentRow);
                }
            } else {
                List<String> projectRowWithEmptyReleaseFields = makeRowForProjectOnly(project);
                for(int i = 0; i < releaseHelper.getColumns(); i++){
                    projectRowWithEmptyReleaseFields.add("");
                }
                table.addRow(projectRowWithEmptyReleaseFields);
            }
            return table;
        }

        private List<String> makeRowForProjectOnly(Project project) {
            List<String> row = new ArrayList<>(getColumns());

            for (Project._Fields renderedField : RENDERED_FIELDS) {
                if(project.isSet(renderedField)) {
                    Object fieldValue = project.getFieldValue(renderedField);

                    if (renderedField.equals(RELEASE_IDS)) {
                        row.add(joinStrings(getReleaseNames(project)));
                    } else {
                        row.add(SW360Utils.fieldValueAsString(fieldValue));
                    }
                } else {
                    row.add("");
                }
            }

            return row;
        }

        private List<String> getReleaseNames(Project project) {
            if (project.releaseIds == null) return Collections.emptyList();
            return getReleases(project).stream().map(SW360Utils::printName).collect(Collectors.toList());
        }

        private List<Release> getReleases(Project project) {
            List<Release> releasesByIdsForExport;
            try {
                releasesByIdsForExport = client.getReleasesByIdsForExport(nullToEmptySet(project.releaseIds));
            } catch (TException e) {
                log.error("Error fetching release information", e);
                releasesByIdsForExport = Collections.emptyList();
            }
            return releasesByIdsForExport;
        }
    }

}
