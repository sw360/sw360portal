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
import com.siemens.sw360.datahandler.thrift.components.Component;
import com.siemens.sw360.datahandler.thrift.components.Release;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Strings.nullToEmpty;
import static com.siemens.sw360.datahandler.common.CommonUtils.joinStrings;

/**
 * Created by bodet on 06/02/15.
 *
 * @author cedric.bodet@tngtech.com
 */
public class ComponentExporter extends ExcelExporter<Component> {

    private static final List<String> HEADERS = ImmutableList.<String>builder()
            .add("Component Name")
            .add("Programming Languages")
            .add("Categories")
            .add("Operating Systems")
            .add("Software Platforms")
            .add("Created by")
            .add("Creation Date")
            .add("Releases")
            .build();

    public ComponentExporter() {
        super(new ComponentHelper());
    }

    protected static class ComponentHelper implements ExporterHelper<Component> {

        @Override
        public int getColumns() {
            return HEADERS.size();
        }

        @Override
        public List<String> getHeaders() {
            return HEADERS;
        }

        @Override
        public List<String> makeRow(Component component) {
            List<String> row = new ArrayList<>(getColumns());

            row.add(nullToEmpty(component.name));
            row.add(joinStrings(component.languages));
            row.add(joinStrings(component.categories));
            row.add(joinStrings(component.operatingSystems));
            row.add(joinStrings(component.softwarePlatforms));
            row.add(nullToEmpty(component.createdBy));
            row.add(nullToEmpty(component.createdOn));
            row.add(joinStrings(getVersions(component.releases)));

            return row;
        }

        private static List<String> getVersions(Collection<Release> releases) {
            if (releases == null) return Collections.emptyList();

            List<String> versions = new ArrayList<>(releases.size());
            for (Release release : releases) {
                versions.add(nullToEmpty(release.name));
            }
            return versions;
        }
    }

}
