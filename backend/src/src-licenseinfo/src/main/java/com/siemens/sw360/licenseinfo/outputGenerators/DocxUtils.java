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

import com.siemens.sw360.datahandler.thrift.licenseinfo.LicenseInfo;
import com.siemens.sw360.datahandler.thrift.licenseinfo.LicenseInfoParsingResult;
import com.siemens.sw360.datahandler.thrift.licenseinfo.LicenseNameWithText;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import static com.siemens.sw360.datahandler.common.CommonUtils.nullToEmptyString;

public class DocxUtils {

    private DocxUtils() {
        //only static members
    }

    public static void cleanUpTemplate(XWPFDocument document) {
        replaceText(document, "$Heading1", "");
        replaceText(document, "$Heading2", "");
    }

    public static void addHeader(XWPFDocument document, String headerText) throws IOException {
        XWPFHeaderFooterPolicy policy = document.getHeaderFooterPolicy();
            XWPFHeader headerD = policy.createHeader(policy.DEFAULT);
            XWPFRun run = headerD.getParagraphs().get(0).createRun();
            addNewLines(run, 2);
            addFormattedText(run, headerText, 12);
            addNewLines(run, 2);
    }

    public static void setProjectNameInDocument(XWPFDocument document, String projectName) {
        replaceText(document, "$", projectName);
    }

    public static XWPFTable createTableAndAddReleasesTableHeaders(XWPFDocument document, String[] headers) {
        if (headers.length < 4) {
            throw new IllegalArgumentException("Too few table headers found. Need 4 table headers.");
        }
        XWPFTable table = document.createTable(1, 4);
        styleTable(table);
        XWPFTableRow headerRow = table.getRow(0);

        for (int headerCount = 0; headerCount < headers.length; headerCount++) {
            XWPFParagraph paragraph = headerRow.getCell(headerCount).getParagraphs().get(0);
            styleTableHeaderParagraph(paragraph);

            XWPFRun run = paragraph.createRun();
            addFormattedText(run, headers[headerCount], 12, true);

            paragraph.setWordWrap(true);
        }
        return table;
    }

    private static void styleTable(XWPFTable table) {
        table.setRowBandSize(1);
        table.setWidth(1);
        table.setColBandSize(1);
        table.setCellMargins(1, 1, 100, 30);
    }

    public static void fillReleasesTable(XWPFTable table, Collection<LicenseInfoParsingResult> projectLicenseInfoResults) {

        for (LicenseInfoParsingResult result : projectLicenseInfoResults) {
            Set<String> copyrights = Collections.EMPTY_SET;
            Set<LicenseNameWithText> licenseNamesWithTexts = Collections.EMPTY_SET;
            if (result.isSetLicenseInfo()) {
                LicenseInfo licenseInfo = result.getLicenseInfo();
                if (licenseInfo.isSetCopyrights()) {
                    copyrights = licenseInfo.getCopyrights();
                }
                if (licenseInfo.isSetLicenseNamesWithTexts()) {
                    licenseNamesWithTexts = licenseInfo.getLicenseNamesWithTexts();
                }
            }
            String releaseName = nullToEmptyString(result.getName());
            String version = nullToEmptyString(result.getVersion());

            addTableRow(table, releaseName, version, licenseNamesWithTexts, copyrights);
        }
    }

    private static void addTableRow(XWPFTable table, String releaseName, String version, Set<LicenseNameWithText> licenseNamesWithTexts, Set<String> copyrights) {
        XWPFTableRow row = table.createRow();

        XWPFParagraph currentParagraph = row.getCell(0).getParagraphs().get(0);
        styleTableHeaderParagraph(currentParagraph);
        XWPFRun currentRun = currentParagraph.createRun();
        addFormattedText(currentRun, releaseName, 12);

        currentParagraph = row.getCell(1).getParagraphs().get(0);
        styleTableHeaderParagraph(currentParagraph);
        currentRun = currentParagraph.createRun();
        addFormattedText(currentRun, version, 12);

        currentParagraph = row.getCell(2).getParagraphs().get(0);
        styleTableHeaderParagraph(currentParagraph);
        currentRun = currentParagraph.createRun();
        for (LicenseNameWithText licenseNameWithText : licenseNamesWithTexts) {
            String licenseName = licenseNameWithText.isSetLicenseName()
                    ? licenseNameWithText.getLicenseName()
                    : "Unknown license name";
            addFormattedText(currentRun, licenseName, 12);
            addNewLines(currentRun, 1);
        }

        currentParagraph = row.getCell(3).getParagraphs().get(0);
        styleTableHeaderParagraph(currentParagraph);
        currentRun = currentParagraph.createRun();
        for (String copyright : copyrights) {
            addFormattedText(currentRun, copyright, 12);
            addNewLines(currentRun, 1);
        }
    }

    private static void styleTableHeaderParagraph(XWPFParagraph paragraph) {
        paragraph.setIndentationLeft(0);
        paragraph.setWordWrap(true);
        paragraph.setAlignment(ParagraphAlignment.LEFT);
    }

    public static void addLicenseTextsHeader(XWPFDocument document, String header) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        addPageBreak(run);
        XWPFParagraph textParagraph = document.createParagraph();
        XWPFRun textRun = textParagraph.createRun();
        textParagraph.setStyle("Heading1");
        textRun.setText(header);

        addNewLines(textRun, 1);
    }

    public static void addLicenseTexts(XWPFDocument document, Collection<LicenseInfoParsingResult> projectLicenseInfoResults) {
        projectLicenseInfoResults.stream()
                .map(LicenseInfoParsingResult::getLicenseInfo)
                .filter(Objects::nonNull)
                .map(LicenseInfo::getLicenseNamesWithTexts)
                .filter(Objects::nonNull)
                .forEach(set -> {
                    set.forEach(lt -> {

                        XWPFParagraph licenseParagraph = document.createParagraph();
                        licenseParagraph.setStyle("Heading2");
                        XWPFRun licenseRun = licenseParagraph.createRun();
                        String licenseName = lt.isSetLicenseName() ? lt.getLicenseName() : "Unknown license name";
                        licenseRun.setText(licenseName);
                        addNewLines(licenseRun, 1);

                        XWPFParagraph licenseTextParagraph = document.createParagraph();
                        XWPFRun licenseTextRun = licenseTextParagraph.createRun();
                        addFormattedText(licenseTextRun, nullToEmptyString(lt.getLicenseText()), 12);
                        addNewLines(licenseTextRun, 1);
                    });
                });
    }

    private static void addNewLines(XWPFRun run, int numberOfNewlines) {
        for (int count = 0; count < numberOfNewlines; count++) {
            run.addCarriageReturn();
            run.addBreak(BreakType.TEXT_WRAPPING);
        }
    }

    private static void addPageBreak(XWPFRun run) {
        run.addBreak(BreakType.TEXT_WRAPPING);
        run.addBreak(BreakType.PAGE);
    }

    private static void addFormattedText(XWPFRun run, String text, String fontFamily, int fontSize, boolean bold) {
        run.setFontSize(fontSize);
        run.setFontFamily(fontFamily);
        run.setBold(bold);
        run.setText(text);
    }

    private static void addFormattedText(XWPFRun run, String text, int fontSize, boolean bold) {
        addFormattedText(run, text, "Calibri", fontSize, bold);
    }

    private static void addFormattedText(XWPFRun run, String text, int fontSize) {
        addFormattedText(run, text, fontSize, false);
    }

    private static void replaceText(XWPFDocument document, String findText, String replaceText){

        for (int i = 0; i < document.getParagraphs().size(); i++ ) {
                XWPFParagraph p = document.getParagraphs().get(i);
                for (int z = 0; z < p.getRuns().size(); z++) {
                    XWPFRun run = p.getRuns().get(z);
                    String text = run.getText(0);
                    if(text != null && text.contains(findText)) {
                        run.setText(text.replace(findText, replaceText),0);
                    }
                }
        }
    }
}

