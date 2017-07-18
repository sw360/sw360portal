/*
 * Copyright Bosch Software Innovations GmbH, 2016.
 * With modifications by Siemens AG, 2017.
 * Part of the SW360 Portal Project.
 *
 * SPDX-License-Identifier: EPL-1.0
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.sw360.licenseinfo.outputGenerators;


import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.eclipse.sw360.datahandler.common.SW360Utils;
import org.eclipse.sw360.datahandler.thrift.SW360Exception;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.LicenseInfo;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.LicenseInfoParsingResult;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.LicenseNameWithText;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.OutputFormatInfo;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.tools.ToolManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class OutputGenerator<T> {

    public static final String VELOCITY_TOOLS_FILE = "velocity-tools.xml";
    private final String OUTPUT_TYPE;
    private final String OUTPUT_DESCRIPTION;
    private final boolean IS_OUTPUT_BINARY;
    private final String OUTPUT_MIME_TYPE;

    OutputGenerator(String outputType, String outputDescription, boolean isOutputBinary, String mimeType){
        OUTPUT_TYPE = outputType;
        OUTPUT_DESCRIPTION = outputDescription;
        IS_OUTPUT_BINARY = isOutputBinary;
        OUTPUT_MIME_TYPE = mimeType;
    }

    public abstract T generateOutputFile(Collection<LicenseInfoParsingResult> projectLicenseInfoResults, String projectName) throws SW360Exception;

    public String getOutputType() {
        return OUTPUT_TYPE;
    }

    public String getOutputDescription() {
        return OUTPUT_DESCRIPTION;
    }

    public boolean isOutputBinary() {
        return IS_OUTPUT_BINARY;
    }

    public String getOutputMimeType() {
        return OUTPUT_MIME_TYPE;
    }

    public OutputFormatInfo getOutputFormatInfo() {
        return new OutputFormatInfo()
                .setFileExtension(getOutputType())
                .setDescription(getOutputDescription())
                .setIsOutputBinary(isOutputBinary())
                .setGeneratorClassName(this.getClass().getName())
                .setMimeType(getOutputMimeType());
    }

    public String getComponentLongName(LicenseInfoParsingResult li) {
        return SW360Utils.getReleaseFullname(li.getVendor(), li.getName(), li.getVersion());
    }

    public VelocityContext getConfiguredVelocityContext() throws Exception {
        Properties p = new Properties();
        p.setProperty("resource.loader", "class");
        p.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(p);
        ToolManager velocityToolManager = new ToolManager();
        velocityToolManager.configure(VELOCITY_TOOLS_FILE);
        return new VelocityContext(velocityToolManager.createContext());
    }

    @NotNull
    protected SortedMap<String, LicenseInfoParsingResult> getSortedLicenseInfos(Collection<LicenseInfoParsingResult> projectLicenseInfoResults) {
        Map<String, LicenseInfoParsingResult> licenseInfos = projectLicenseInfoResults.stream()
                .collect(Collectors.toMap(this::getComponentLongName, li -> li, (li1, li2) -> li1));
        return sortStringKeyedMap(licenseInfos);
    }

    @NotNull
    protected SortedMap<String, Set<String>> getSortedAcknowledgements(Map<String, LicenseInfoParsingResult> sortedLicenseInfos) {
        Map<String, Set<String>> acknowledgements = Maps.filterValues(Maps.transformValues(sortedLicenseInfos, pr -> Optional
                .ofNullable(pr.getLicenseInfo())
                .map(LicenseInfo::getLicenseNamesWithTexts)
                .filter(Objects::nonNull)
                .map(s -> s
                        .stream()
                        .map(LicenseNameWithText::getAcknowledgements)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet())), set -> !set.isEmpty());
        return sortStringKeyedMap(acknowledgements);
    }

    @NotNull
    protected List<LicenseNameWithText> getSortedLicenseNameWithTexts(Collection<LicenseInfoParsingResult> projectLicenseInfoResults) {
        Set<LicenseNameWithText> licenseNamesWithText = projectLicenseInfoResults.stream()
                .map(LicenseInfoParsingResult::getLicenseInfo)
                .filter(Objects::nonNull)
                .map(LicenseInfo::getLicenseNamesWithTexts)
                .filter(Objects::nonNull)
                .reduce(Sets::union)
                .orElse(Collections.emptySet());
        List<LicenseNameWithText> lnwtsList = new ArrayList<>();
        lnwtsList.addAll(licenseNamesWithText);
        lnwtsList.sort(Comparator.comparing(LicenseNameWithText::getLicenseName, String.CASE_INSENSITIVE_ORDER));
        return lnwtsList;
    }

    private static <U> SortedMap<String, U> sortStringKeyedMap(Map<String, U> unsorted){
        SortedMap<String, U> sorted = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        sorted.putAll(unsorted);
        if (sorted.size() != unsorted.size()){
            // there were key collisions and some data was lost -> throw away the sorted map and sort by case sensitive order
            sorted = new TreeMap<>();
            sorted.putAll(unsorted);
        }
        return sorted;
    }
}
