/*
 * Copyright Bosch Software Innovations GmbH, 2016.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.siemens.sw360.licenseinfo.outputGenerators;

import com.siemens.sw360.datahandler.thrift.SW360Exception;
import com.siemens.sw360.datahandler.thrift.licenseinfo.LicenseInfoParsingResult;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;

import static com.siemens.sw360.licenseinfo.outputGenerators.DocxUtils.*;

public class DocxGenerator extends OutputGenerator<byte[]> {

    public DocxGenerator() {
        super("docx", "License information as DOCX", true);
    }

    @Override
    public byte[] generateOutputFile(Collection<LicenseInfoParsingResult> projectLicenseInfoResults, String projectName) throws SW360Exception {
        try {
            XWPFDocument document = new XWPFDocument(this.getClass().getResourceAsStream("/blank.docx"));

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

        String header = "OSS Attributions for " + projectName;
        addHeader(document, header);
        addFooter(document);

        String title = "Open Source Software Attributions for project\n \"" + projectName + "\"";
        addTitle(document, title);

        String subtitle = "This document is provided as part of the fulfillment of OSS license conditions and does not require " +
                "users to take any action before or while using the product.";
        addSubtitle(document, subtitle);

        String notice = "Notice: "
                + "This is a draft copy of the attribution document. The project team shall fill-in the information wherever required and also verifies the document for completeness."
                + " This notice shall be removed once the document is finalized. ";
        addNotice(document, notice);
        addTableOfContent(document);
        addListParagraph(document, "List of used Open Source Components");

        String listNotice =
                "This document contains a list of open source software (OSS) components used within the product under the terms "
                        + "of the respective licenses. The source code corresponding to the open source components is also provided along "
                        + "with the product wherever mandated by the respective OSS license.";
        addListNotice(document, listNotice);
        String[] tableHeaders = {"Name of OSS Component",
                "Version of OSS Component",
                "Name and Version of License (see Appendix for License Text)",
                "More Information"};
        XWPFTable table = createTableAndAddReleasesTableHeaders(document, tableHeaders);
        fillReleasesTable(table, projectLicenseInfoResults);
        addLicenseTextsHeader(document, "Appendix - License Texts");
        addLicenseTexts(document, projectLicenseInfoResults);
    }


}

