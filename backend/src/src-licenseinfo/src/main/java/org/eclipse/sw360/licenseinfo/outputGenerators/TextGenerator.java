/*
 * Copyright Bosch Software Innovations GmbH, 2016.
 * With modifications by Siemens AG, 2017-2018.
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

import org.apache.log4j.Logger;
import org.eclipse.sw360.datahandler.thrift.SW360Exception;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.LicenseInfoParsingResult;

import java.util.Collection;

public class TextGenerator extends OutputGenerator<String> {
    private static final Logger LOGGER = Logger.getLogger(TextGenerator.class);
    private static final String LICENSE_INFO_TEMPLATE_FILE = "textLicenseInfoFile.vm";

    public TextGenerator() {
        super("txt", "License information as TEXT", false, "text/plain");
    }

    @Override
    public String generateOutputFile(Collection<LicenseInfoParsingResult> projectLicenseInfoResults, String projectName, String projectVersion, String licenseInfoHeaderText) throws SW360Exception {
        try {
            return renderTemplateWithDefaultValues(projectLicenseInfoResults, LICENSE_INFO_TEMPLATE_FILE, licenseInfoHeaderText);
        } catch (Exception e) {
            LOGGER.error("Could not generate text licenseinfo file for project " + projectName, e);
            return "License information could not be generated.\nAn exception occurred: " + e.toString();
        }
    }
}
