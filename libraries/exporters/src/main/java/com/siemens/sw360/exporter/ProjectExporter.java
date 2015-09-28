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
package com.siemens.sw360.exporter;

import com.google.common.collect.ImmutableList;
import com.siemens.sw360.datahandler.thrift.components.ComponentService;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Strings.nullToEmpty;
import static com.siemens.sw360.datahandler.common.CommonUtils.joinStrings;
import static com.siemens.sw360.datahandler.common.SW360Utils.printName;

/**
 * Created by bodet on 06/02/15.
 *
 * @author cedric.bodet@tngtech.com
 */
public class ProjectExporter extends ExcelExporter<Project> {

    private static final Logger log = Logger.getLogger(ProjectExporter.class);

    private static final int COLUMNS = 9;

    private static final List<String> HEADERS = ImmutableList.<String>builder()
            .add("Project ID")
            .add("Project Name")
            .add("Project State")
            .add("Created by")
            .add("Creation Date")
            .add("Project Manager Name")
            .add("Project Manager Email")
            .add("Business Unit")
            .add("Releases")
            .build();

    public ProjectExporter(ComponentService.Iface client) {
        super(new ProjectHelper(client));
    }

    private static class ProjectHelper implements ExporterHelper<Project> {

        private final ComponentService.Iface client;

        private ProjectHelper(ComponentService.Iface client) {
            this.client = client;
        }

        @Override
        public int getColumns() {
            return COLUMNS;
        }

        @Override
        public List<String> getHeaders() {
            return HEADERS;
        }

        @Override
        public List<String> makeRow(Project project) {
            List<String> row = new ArrayList<>(COLUMNS);

            row.add(nullToEmpty(project.id));
            row.add(nullToEmpty(project.name));
            row.add("Mainline");
            row.add(nullToEmpty(project.createdBy));
            row.add(nullToEmpty(project.createdOn));
            row.add("");
            row.add(nullToEmpty(project.leadArchitect));
            row.add(nullToEmpty(project.businessUnit));
            row.add(joinStrings(getReleases(project.releaseIds)));

            return row;
        }

        private List<String> getReleases(Set<String> ids) {
            if (ids == null) return Collections.emptyList();


            List<Release> releasesByIdsForExport;
            try {
                releasesByIdsForExport = client.getReleasesByIdsForExport(ids);
            } catch (TException e) {
                log.error("Error fetching release information", e);
                releasesByIdsForExport=Collections.emptyList();
            }

            List<String> releaseNames = new ArrayList<>(ids.size());

            for (Release release : releasesByIdsForExport) {
                releaseNames.add(printName(release));
            }

            return releaseNames;
        }
    }

}
