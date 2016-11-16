/*
 * Copyright Bosch Software Innovations GmbH, 2016.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.sw360.licenseinfo.outputGenerators;

import com.google.common.collect.Sets;
import org.eclipse.sw360.datahandler.thrift.SW360Exception;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.LicenseInfo;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.LicenseInfoParsingResult;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.LicenseNameWithText;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

import static org.eclipse.sw360.licenseinfo.LicenseInfoHandler.*;

public class LicenseInfoGenerator extends OutputGenerator<String> {

    public static final String LICENSE_INFO_TEMPLATE_FILE = "licenseInfoFile.vm";
    Logger log = Logger.getLogger(LicenseInfoGenerator.class);

    public LicenseInfoGenerator() {
        super("txt", "License information as TEXT", false, "text/plain");
    }

    @Override
    public String generateOutputFile(Collection<LicenseInfoParsingResult> projectLicenseInfoResults, String projectName) throws SW360Exception {
        try {
            VelocityContext vc = getConfiguredVelocityContext();

            Map<String, LicenseInfoParsingResult> licenseInfos = projectLicenseInfoResults.stream()
                    .collect(Collectors.toMap(this::getComponentLongName, li -> li, (li1, li2) -> li1));
            Set<String> licenses = projectLicenseInfoResults.stream()
                    .map(LicenseInfoParsingResult::getLicenseInfo)
                    .filter(Objects::nonNull)
                    .map(LicenseInfo::getLicenseNamesWithTexts)
                    .filter(Objects::nonNull)
                    .reduce(Sets::union)
                    .orElse(Collections.emptySet())
                    .stream()
                    .map(LicenseNameWithText::getLicenseText)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            vc.put(LICENSE_INFO_RESULTS_CONTEXT_PROPERTY, licenseInfos);
            vc.put(LICENSES_CONTEXT_PROPERTY, licenses);

            StringWriter sw = new StringWriter();
            Velocity.mergeTemplate(LICENSE_INFO_TEMPLATE_FILE, "utf-8", vc, sw);
            sw.close();
            return sw.toString();
        } catch (Exception e) {
            log.error("Could not generate licenseinfo file", e);
            return "License information could not be generated.\nAn exception occured: " + e.toString();
        }
    }
}

