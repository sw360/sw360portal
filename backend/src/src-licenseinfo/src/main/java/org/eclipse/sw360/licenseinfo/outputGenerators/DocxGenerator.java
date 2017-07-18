/*
 * Copyright Bosch Software Innovations GmbH, 2016.
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

import org.eclipse.sw360.datahandler.thrift.SW360Exception;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.LicenseInfoParsingResult;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;

import static org.eclipse.sw360.licenseinfo.outputGenerators.DocxUtils.*;

public class DocxGenerator extends OutputGenerator<byte[]> {

    public DocxGenerator() {
        super("docx", "License information as DOCX", true, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }

    @Override
    public byte[] generateOutputFile(Collection<LicenseInfoParsingResult> projectLicenseInfoResults, String projectName) throws SW360Exception {
        try {
            XWPFDocument document = new XWPFDocument(this.getClass().getResourceAsStream("/templateFrontpageContent.docx"));

            fillDocument(document, projectLicenseInfoResults, projectName);

            ByteArrayOutputStream docxOutputStream = new ByteArrayOutputStream();
            document.write(docxOutputStream);
            docxOutputStream.close();
            return docxOutputStream.toByteArray();
        } catch (IOException ioe) {
            throw new SW360Exception("Got IOException when generating docx document: " + ioe.getMessage());
        }
    }

    private void fillDocument(XWPFDocument document, Collection<LicenseInfoParsingResult> projectLicenseInfoResults, String projectName) throws IOException {

        cleanUpTemplate(document);
        setProjectNameInDocument(document, projectName);

        String[] tableHeaders = {"Name of OSS Component",
                "Version of OSS Component",
                "Name and Version of License (see Appendix for License Text)",
                "Acknowledgements",
                "More Information"};
        XWPFTable table = createTableAndAddReleasesTableHeaders(document, tableHeaders);
        fillReleasesTable(table, projectLicenseInfoResults);

        addLicenseTextsHeader(document, "Appendix - License Texts");
        addLicenseTexts(document, projectLicenseInfoResults);
    }
}

