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


import org.eclipse.sw360.datahandler.thrift.SW360Exception;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.LicenseInfoParsingResult;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.OutputFormatInfo;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.tools.ToolManager;

import java.util.Collection;
import java.util.Properties;

public abstract class OutputGenerator<T> {

    public static final String VELOCITY_TOOLS_FILE = "velocity-tools.xml";
    private final String OUTPUT_TYPE;
    private final String OUTPUT_DESCRIPTION;
    private final boolean IS_OUTPUT_BINARY;

    OutputGenerator(String outputType, String outputDescription, boolean isOutputBinary){
        OUTPUT_TYPE = outputType;
        OUTPUT_DESCRIPTION = outputDescription;
        IS_OUTPUT_BINARY = isOutputBinary;
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

    public OutputFormatInfo getOutputFormatInfo() {
        return new OutputFormatInfo()
                .setFileExtension(getOutputType())
                .setDescription(getOutputDescription())
                .setIsOutputBinary(isOutputBinary())
                .setGeneratorClassName(this.getClass().getName());
    }

    public String getComponentLongName(LicenseInfoParsingResult li) {
        return String.format("%s %s %s", li.getVendor(), li.getName(), li.getVersion()).trim();
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
}
