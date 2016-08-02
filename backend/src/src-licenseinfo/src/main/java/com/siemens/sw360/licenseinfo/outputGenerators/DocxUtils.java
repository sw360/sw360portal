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
import org.apache.log4j.Logger;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import static com.siemens.sw360.datahandler.common.CommonUtils.nullToEmptyString;

public class DocxUtils {

    static Logger log = Logger.getLogger(DocxUtils.class);

    private DocxUtils() {
        //only static members
    }

    public static void addHeader(XWPFDocument document, String headerText) throws IOException {
        XWPFHeaderFooterPolicy policy = document.getHeaderFooterPolicy();
        if (policy.getDefaultHeader() == null && policy.getFirstPageHeader() == null
                && policy.getDefaultFooter() == null) {
            XWPFHeader headerD = policy.createHeader(policy.DEFAULT);
            XWPFRun run = headerD.getParagraphs().get(0).createRun();
            addNewLines(run, 2);
            addFormattedText(run, headerText, 12);
            addNewLines(run, 2);
        }
    }

    public static void addFooter(XWPFDocument document) throws IOException{
        // create footer
        XWPFHeaderFooterPolicy policy = document.getHeaderFooterPolicy();
        CTP ctpFooter = CTP.Factory.newInstance();

        XWPFParagraph[] parsFooter;

        CTPPr ctppr = ctpFooter.addNewPPr();
        CTString pst = ctppr.addNewPStyle();
        pst.setVal("style21");
        CTJc ctjc = ctppr.addNewJc();
        ctjc.setVal(STJc.RIGHT);
        ctppr.addNewRPr();

        CTR ctr = ctpFooter.addNewR();
        ctr.addNewRPr();
        CTFldChar fch = ctr.addNewFldChar();
        fch.setFldCharType(STFldCharType.BEGIN);

        ctr = ctpFooter.addNewR();
        ctr.addNewInstrText().setStringValue(" PAGE ");

        ctpFooter.addNewR().addNewFldChar().setFldCharType(STFldCharType.SEPARATE);

        ctpFooter.addNewR().addNewT().setStringValue("1");

        ctpFooter.addNewR().addNewFldChar().setFldCharType(STFldCharType.END);

        XWPFParagraph footerParagraph = new XWPFParagraph(ctpFooter, document);
        XWPFRun footerRun = footerParagraph.createRun();
        addNewLines(footerRun,1);

        parsFooter = new XWPFParagraph[1];

        parsFooter[0] = footerParagraph;

        policy.createFooter(XWPFHeaderFooterPolicy.DEFAULT, parsFooter);
    }

    public static void addTitle(XWPFDocument document, String header) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = paragraph.createRun();

        addNewLines(run,3);
        addFormattedText(run, header, 16, true);

        run.addBreak(BreakType.TEXT_WRAPPING);
    }

    public static void addSubtitle(XWPFDocument document, String subtitle) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        addNewLines(run, 3);
        addFormattedText(run, subtitle, 12);
    }

    public static void addNotice(XWPFDocument document, String notice) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();

        addNewLines(run, 5);
        run.setColor("CC0000");
        addFormattedText(run, notice, 14, true);
        addPageBreak(run);
    }

    public static void addTableOfContent(XWPFDocument document) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        addFormattedText(run, "", 12);
        document.createTOC();
        addPageBreak(run);
    }

    public static void addListParagraph(XWPFDocument document, String title) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setStyle("Heading 1");
        paragraph.setIndentationLeft(5);
        XWPFRun run = paragraph.createRun();

        addFormattedText(run, title, 14, true);
    }

    public static void addListNotice(XWPFDocument document, String notice) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();

        addFormattedText(run, notice, 12);
        run.addBreak(BreakType.TEXT_WRAPPING);
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
        XWPFParagraph paragraph = row.getCell(0).getParagraphs().get(0);
        styleTableHeaderParagraph(paragraph);
        XWPFRun run = paragraph.createRun();
        addFormattedText(run, releaseName, 12);

        paragraph = row.getCell(1).getParagraphs().get(0);
        styleTableHeaderParagraph(paragraph);
        run = paragraph.createRun();
        addFormattedText(run, version, 12);

        paragraph = row.getCell(2).getParagraphs().get(0);
        styleTableHeaderParagraph(paragraph);
        run = paragraph.createRun();
        for(LicenseNameWithText licenseNameWithText : licenseNamesWithTexts){
            String licenseName = licenseNameWithText.isSetLicenseName()
                    ? licenseNameWithText.getLicenseName()
                    :"Unknown license name";
            addFormattedText(run, licenseName, 12);
            addNewLines(run,1);
        }

        paragraph = row.getCell(3).getParagraphs().get(0);
        styleTableHeaderParagraph(paragraph);
        run = paragraph.createRun();
        for(String copyright:copyrights) {
            addFormattedText(run, copyright, 12);
            addNewLines(run,1);
        }
    }


    public static XWPFTable createTableAndAddReleasesTableHeaders(XWPFDocument document, String[] headers) {
        if (headers.length < 4) {
            throw new IllegalArgumentException("Too few table headers found. Need 4 table headers.");
        }
        XWPFTable table = document.createTable(1,4);
        table.setRowBandSize(1);
        table.setWidth(1);
        table.setColBandSize(1);
        table.setCellMargins(1, 1, 100, 30);

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
        textParagraph.setStyle("Heading 1");
        addFormattedText(textRun, header, 14, true);
        addNewLines(textRun,1);
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
                        licenseParagraph.setStyle("Heading 2");
                        XWPFRun licenseRun = licenseParagraph.createRun();
                        String licenseName = lt.isSetLicenseName() ? lt.getLicenseName() : "Unknown license name";
                        addFormattedText(licenseRun, licenseName, 12, true);
                        addNewLines(licenseRun,1);

                        XWPFParagraph licenseTextParagraph = document.createParagraph();
                        XWPFRun licenseTextRun = licenseTextParagraph.createRun();
                        addFormattedText(licenseTextRun, nullToEmptyString(lt.getLicenseText()), 12);
                        addNewLines(licenseTextRun,1);
                    });
                });
    }

    public static void addNewLines(XWPFRun run, int numberOfNewlines) {
        for (int count = 0; count < numberOfNewlines; count++) {
            run.addCarriageReturn();
            run.addBreak(BreakType.TEXT_WRAPPING);
        }
    }

    public static void addPageBreak(XWPFRun run) {
        run.addBreak(BreakType.TEXT_WRAPPING);
        run.addBreak(BreakType.PAGE);
    }

    public static void addFormattedText(XWPFRun run, String text, String fontFamily, int fontSize, boolean bold) {
        run.setFontSize(fontSize);
        run.setFontFamily(fontFamily);
        run.setBold(bold);
        run.setText(text);
    }

    public static void addFormattedText(XWPFRun run, String text, int fontSize, boolean bold) {
        addFormattedText(run, text, "Calibri", fontSize, bold);
    }

    public static void addFormattedText(XWPFRun run, String text, int fontSize) {
        addFormattedText(run, text, fontSize, false);
    }
}

