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
import org.apache.log4j.Logger;
import org.apache.poi.xwpf.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;

public class DocxGenerator extends OutputGenerator {

    Logger log = Logger.getLogger(DocxGenerator.class);

    public DocxGenerator() {
        super("docx", "License information as DOCX");
    }

    @Override
    public String generateOutputFile(Collection<LicenseInfoParsingResult> projectLicenseInfoResults) throws SW360Exception {
        try {
            XWPFDocument document = new XWPFDocument(new FileInputStream(
                    new File("blank.docx")));
            fillDocument(document, projectLicenseInfoResults);
            FileOutputStream docxFileOutputStream = new FileOutputStream(new File(
                    "/tmp/LicenseInfo.docx"));
            document.write(docxFileOutputStream);
            docxFileOutputStream.close();

            return "";
        } catch (IOException ioe) {
            throw new SW360Exception("Could not open blank.docx. Got IOException: " + ioe.getMessage());
        }
    }

    private void fillDocument(XWPFDocument document, Collection<LicenseInfoParsingResult> projectLicenseInfoResults){
        String header = "Open Source Software Attributions\n";
        setHeader(document, header);
    }

    private void setHeader(XWPFDocument document, String header) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);

        addNewLines(paragraph, 3);

        XWPFRun run = paragraph.createRun();
        run.setBold(Boolean.TRUE);
        run.setFontSize(16);
        run.setFontFamily("Calibri");

        run.setText(header);
        run.addBreak(BreakType.TEXT_WRAPPING);
    }

    private void addNewLines(XWPFParagraph paragraph, int numberOfNewlines){
        XWPFRun run = paragraph.createRun();
        for(int count = 0; count <numberOfNewlines; count ++){
            run.addCarriageReturn();
            run.addBreak(BreakType.TEXT_WRAPPING);
        }

    }
}

