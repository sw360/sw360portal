/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
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
import org.eclipse.sw360.datahandler.thrift.components.Component;
import org.eclipse.sw360.datahandler.thrift.components.ComponentService;
import org.eclipse.sw360.datahandler.thrift.components.Release;
import org.eclipse.sw360.exporter.ReleaseExporter.ReleaseHelper;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.nullToEmpty;
import static org.eclipse.sw360.datahandler.common.CommonUtils.nullToEmptySet;
import static org.eclipse.sw360.datahandler.common.SW360Utils.fieldValueAsString;
import static org.eclipse.sw360.datahandler.common.SW360Utils.getReleaseNames;
import static org.eclipse.sw360.datahandler.thrift.components.Component._Fields.*;

public class ComponentExporter extends ExcelExporter<Component> {
    private static final Logger log = Logger.getLogger(ProjectExporter.class);
    private static ReleaseHelper releaseHelper;

    public static final Map<String, String> nameToDisplayName;
    static {
        nameToDisplayName = new HashMap<>();
        nameToDisplayName.put(Component._Fields.NAME.getFieldName(), "component name");
        nameToDisplayName.put(Component._Fields.CREATED_ON.getFieldName(), "creation date");
        nameToDisplayName.put(Component._Fields.COMPONENT_TYPE.getFieldName(), "component type");
        nameToDisplayName.put(Component._Fields.CREATED_BY.getFieldName(), "created by");
        nameToDisplayName.put(Component._Fields.RELEASE_IDS.getFieldName(), "releases");
        nameToDisplayName.put(Component._Fields.MAIN_LICENSE_IDS.getFieldName(), "main license IDs");
        nameToDisplayName.put(Component._Fields.SOFTWARE_PLATFORMS.getFieldName(), "software platforms");
        nameToDisplayName.put(Component._Fields.OPERATING_SYSTEMS.getFieldName(), "operating systems");
        nameToDisplayName.put(Component._Fields.VENDOR_NAMES.getFieldName(), "vendor names");
    }

    private static final List<Component._Fields> COMPONENT_IGNORED_FIELDS = ImmutableList.<Component._Fields>builder()
            .add(ID)
            .add(REVISION)
            .add(DOCUMENT_STATE)
            .add(PERMISSIONS)
            .add(RELEASES)
            .build();

    public static final List<Component._Fields> COMPONENT_RENDERED_FIELDS = Component.metaDataMap.keySet()
            .stream()
            .filter(k -> ! COMPONENT_IGNORED_FIELDS.contains(k))
            .collect(Collectors.toList());

    protected static List<String> HEADERS = new ArrayList<>();

    public ComponentExporter(ComponentService.Iface componentClient, boolean extendedByReleases) {
        super(new ComponentHelper(componentClient, extendedByReleases));
        releaseHelper = new ReleaseHelper(componentClient);
        HEADERS = COMPONENT_RENDERED_FIELDS
                .stream()
                .map(Component._Fields::getFieldName)
                .map(n -> SW360Utils.displayNameFor(n, nameToDisplayName))
                .collect(Collectors.toList());
        if(extendedByReleases){
            HEADERS.addAll(releaseHelper.getHeaders());
        }
    }

    protected static class ComponentHelper implements ExporterHelper<Component> {

        private final ComponentService.Iface componentClient;
        private List<Release> releases;
        private boolean extendedByReleases;

        private ComponentHelper(ComponentService.Iface componentClient, boolean extendedByReleases){
            this.componentClient = componentClient;
            this.extendedByReleases = extendedByReleases;
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
        public SubTable makeRows(Component component) throws SW360Exception {
            return extendedByReleases
                    ? makeRowsWithReleases(component)
                    : makeRowForComponentOnly(component);
        }

        protected SubTable makeRowsWithReleases(Component component) throws SW360Exception {
            releases = getReleases(component);
            SubTable table = new SubTable();

            if(releases.size() > 0) {
                for (Release release : releases) {
                    List<String> currentRow = makeRowForComponent(component);
                    currentRow.addAll(releaseHelper.makeRows(release).elements.get(0));
                    table.addRow(currentRow);
                }
            } else {
                List<String> componentRowWithEmptyReleaseFields = makeRowForComponent(component);
                for(int i = 0; i < releaseHelper.getColumns(); i++){
                    componentRowWithEmptyReleaseFields.add("");
                }
                table.addRow(componentRowWithEmptyReleaseFields);
            }
            return table;
        }

        private List<String> makeRowForComponent(Component component) throws SW360Exception {
            if(! component.isSetAttachments()){
                component.setAttachments(Collections.EMPTY_SET);
            }
            List<String> row = new ArrayList<>(getColumns());
            for(Component._Fields renderedField : COMPONENT_RENDERED_FIELDS) {
                addFieldValueToRow(row, renderedField, component);
            }
            return row;
        }

        private void addFieldValueToRow(List<String> row, Component._Fields field, Component component) throws SW360Exception {
            if(component.isSet(field)) {
                Object fieldValue = component.getFieldValue(field);
                switch(field) {
                    case RELEASE_IDS:
                        row.add(fieldValueAsString(getReleaseNames(getReleases(component))));
                        break;
                    case ATTACHMENTS:
                        row.add(component.attachments.size() + "");
                        break;
                    default:
                        row.add(fieldValueAsString(fieldValue));
                }
            } else {
                row.add("");
            }
        }

        private SubTable makeRowForComponentOnly(Component component) throws SW360Exception{
            releases = getReleases(component);
            return new SubTable(makeRowForComponent(component));
        }

        private static List<String> getVersions(Collection<Release> releases) {
            if (releases == null) return Collections.emptyList();

            List<String> versions = new ArrayList<>(releases.size());
            for (Release release : releases) {
                versions.add(nullToEmpty(release.name));
            }
            return versions;
        }

        private List<Release> getReleases(Component component) throws SW360Exception{
            List<Release> releasesByIdsForExport;
            try {
                releasesByIdsForExport = componentClient.getReleasesByIdsForExport(nullToEmptySet(component.releaseIds));
            } catch (TException e) {
                throw new SW360Exception("Error fetching release information");
            }
            return releasesByIdsForExport;
        }
    }

}
