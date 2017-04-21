/*
 * Copyright Siemens AG, 2014-2017. Part of the SW360 Portal Project.
 * With modifications by Bosch Software Innovations GmbH, 2016.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.exporter;

import com.google.common.collect.ImmutableList;
import org.eclipse.sw360.datahandler.common.SW360Utils;
import org.eclipse.sw360.datahandler.thrift.SW360Exception;
import org.eclipse.sw360.datahandler.thrift.components.ComponentService;
import org.eclipse.sw360.datahandler.thrift.components.Release;
import org.eclipse.sw360.datahandler.thrift.projects.Project;
import org.eclipse.sw360.datahandler.thrift.projects.ProjectService;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.exporter.ReleaseExporter.ReleaseHelper;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import java.util.*;
import java.util.stream.Collectors;

import static org.eclipse.sw360.datahandler.common.CommonUtils.nullToEmptySet;
import static org.eclipse.sw360.datahandler.common.SW360Utils.*;
import static org.eclipse.sw360.datahandler.thrift.projects.Project._Fields.*;

public class ProjectExporter extends ExcelExporter<Project> {

    private static final Logger log = Logger.getLogger(ProjectExporter.class);
    private static boolean extendedByReleases;
    private static ReleaseHelper releaseHelper;

    public static final Map<String, String> nameToDisplayName;
    static {
        nameToDisplayName = new HashMap<>();
        nameToDisplayName.put(Project._Fields.ID.getFieldName(), "project ID");
        nameToDisplayName.put(Project._Fields.NAME.getFieldName(), "project name");
        nameToDisplayName.put(Project._Fields.STATE.getFieldName(), "project state");
        nameToDisplayName.put(Project._Fields.CREATED_BY.getFieldName(), "created by");
        nameToDisplayName.put(Project._Fields.CREATED_ON.getFieldName(), "creation date");
        nameToDisplayName.put(Project._Fields.PROJECT_RESPONSIBLE.getFieldName(), "project responsible");
        nameToDisplayName.put(Project._Fields.LEAD_ARCHITECT.getFieldName(), "project lead architect");
        nameToDisplayName.put(Project._Fields.TAG.getFieldName(), "project tag");
        nameToDisplayName.put(Project._Fields.BUSINESS_UNIT.getFieldName(), "group");
        nameToDisplayName.put(Project._Fields.RELEASE_IDS.getFieldName(), "releases");
        nameToDisplayName.put(Project._Fields.RELEASE_CLEARING_STATE_SUMMARY.getFieldName(),
                "release clearing state summary");
        nameToDisplayName.put(Project._Fields.EXTERNAL_IDS.getFieldName(), "external IDs");
        nameToDisplayName.put(Project._Fields.VISBILITY.getFieldName(), "visibility");
        nameToDisplayName.put(Project._Fields.PROJECT_TYPE.getFieldName(), "project type");
        nameToDisplayName.put(Project._Fields.LINKED_PROJECTS.getFieldName(), "linked projects with relationship");
        nameToDisplayName.put(Project._Fields.RELEASE_ID_TO_USAGE.getFieldName(), "releases with usage");
        nameToDisplayName.put(Project._Fields.CLEARING_TEAM.getFieldName(), "clearing team");
        nameToDisplayName.put(Project._Fields.PREEVALUATION_DEADLINE.getFieldName(), "pre-evaluation deadline");
        nameToDisplayName.put(Project._Fields.SYSTEM_TEST_START.getFieldName(), "system test start");
        nameToDisplayName.put(Project._Fields.SYSTEM_TEST_END.getFieldName(), "system test end");
        nameToDisplayName.put(Project._Fields.DELIVERY_START.getFieldName(), "delivery start");
        nameToDisplayName.put(Project._Fields.PHASE_OUT_SINCE.getFieldName(), "phase out since");
        nameToDisplayName.put(Project._Fields.PROJECT_OWNER.getFieldName(), "project owner");
        nameToDisplayName.put(Project._Fields.OWNER_ACCOUNTING_UNIT.getFieldName(), "owner accounting unit");
        nameToDisplayName.put(Project._Fields.OWNER_GROUP.getFieldName(), "owner group");
    }

    private static final List<Project._Fields> PROJECT_IGNORED_FIELDS = ImmutableList.<Project._Fields>builder()
            .add(REVISION)
            .add(DOCUMENT_STATE)
            .add(PERMISSIONS)
            .add(RELEASE_IDS)
            .build();

    public static final List<Project._Fields> PROJECT_RENDERED_FIELDS = Project.metaDataMap.keySet()
            .stream()
            .filter(k -> ! PROJECT_IGNORED_FIELDS.contains(k))
            .collect(Collectors.toList());

    protected static List<String> HEADERS = new ArrayList<>();

    public ProjectExporter(ComponentService.Iface componentClient, ProjectService.Iface projectClient, User user, boolean extendedByReleases) {
        super(new ProjectHelper(componentClient, projectClient, user));
        releaseHelper = new ReleaseHelper(componentClient);
        this.extendedByReleases = extendedByReleases;
        HEADERS = PROJECT_RENDERED_FIELDS
                .stream()
                .map(Project._Fields::getFieldName)
                .map(n -> SW360Utils.displayNameFor(n, nameToDisplayName))
                .collect(Collectors.toList());
        if (extendedByReleases) {
            ((ArrayList) HEADERS).addAll(releaseHelper.getHeaders());
        }
    }

    protected static class ProjectHelper implements ExporterHelper<Project> {

        private final ComponentService.Iface componentClient;
        private final ProjectService.Iface projectClient;
        private final User user;
        private List<Release> releases;

        private ProjectHelper(ComponentService.Iface componentClient, ProjectService.Iface projectClient, User user) {
            this.componentClient = componentClient;
            this.projectClient = projectClient;
            this.user = user;
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
        public SubTable makeRows(Project project) throws SW360Exception {
            return extendedByReleases
                    ? makeRowsWithReleases(project)
                    : makeRowForProjectOnly(project);
        }

        protected SubTable makeRowsWithReleases(Project project) throws SW360Exception {
            releases = getReleases(project);
            SubTable table = new SubTable();

            if(releases.size() > 0) {
                for (Release release : releases) {
                    List<String> currentRow = makeRowForProject(project);
                    currentRow.addAll(releaseHelper.makeRows(release).elements.get(0));
                    table.addRow(currentRow);
                }
            } else {
                List<String> projectRowWithEmptyReleaseFields = makeRowForProject(project);
                for(int i = 0; i < releaseHelper.getColumns(); i++){
                    projectRowWithEmptyReleaseFields.add("");
                }
                table.addRow(projectRowWithEmptyReleaseFields);
            }
            return table;
        }

        private List<String> makeRowForProject(Project project) throws SW360Exception {
            if(! project.isSetAttachments()){
                project.setAttachments(Collections.EMPTY_SET);
            }
            List<String> row = new ArrayList<>(getColumns());
            for(Project._Fields renderedField : PROJECT_RENDERED_FIELDS) {
                addFieldValueToRow(row, renderedField, project);
            }

            return row;
        }

        private void addFieldValueToRow(List<String> row, Project._Fields field, Project project) throws SW360Exception {
            if(project.isSet(field)) {
                Object fieldValue = project.getFieldValue(field);
                switch(field) {
                    case RELEASE_IDS:
                        row.add(fieldValueAsString(getReleaseNames(releases)));
                        break;
                    case RELEASE_ID_TO_USAGE:
                        row.add(fieldValueAsString(putReleaseNamesInMap(project.releaseIdToUsage, releases)));
                        break;
                    case LINKED_PROJECTS:
                        row.add(fieldValueAsString(putProjectNamesInMap(
                                project.getLinkedProjects(),
                                getProjects(project.getLinkedProjects().keySet(), user)
                        )));
                        break;
                    case ATTACHMENTS:
                        row.add(project.attachments.size() + "");
                        break;
                    default:
                        row.add(fieldValueAsString(fieldValue));
                }
            } else {
                row.add("");
            }
        }

        private SubTable makeRowForProjectOnly(Project project) throws SW360Exception {
            releases = getReleases(project);
            return  new SubTable(makeRowForProject(project));
        }

        private List<Release> getReleases(Project project) throws SW360Exception {
            List<Release> releasesByIdsForExport;
            try {
                releasesByIdsForExport = componentClient.getReleasesByIdsForExport(nullToEmptySet(project.releaseIds));
            } catch (TException e) {
                throw new SW360Exception("Error fetching release information");
            }
            return releasesByIdsForExport;
        }

        private List<Project> getProjects(Set<String> ids, User user) throws SW360Exception {
            List<Project> projects;
            try {
                projects = projectClient.getProjectsById(new ArrayList<>(ids), user);
            } catch (TException e) {
                throw new SW360Exception("Error fetching linked projects");
            }
            return projects;
        }
    }

}
