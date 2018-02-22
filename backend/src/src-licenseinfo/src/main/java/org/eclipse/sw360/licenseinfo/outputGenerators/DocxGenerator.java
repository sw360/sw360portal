/*
 * Copyright Bosch Software Innovations GmbH, 2016.
 * With modifications by Siemens AG, 2018.
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

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.xmlbeans.XmlException;
import org.eclipse.sw360.datahandler.thrift.SW360Exception;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.LicenseInfo;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.LicenseInfoParsingResult;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.LicenseInfoRequestStatus;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.LicenseNameWithText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import static org.eclipse.sw360.datahandler.common.CommonUtils.nullToEmptyString;
import static org.eclipse.sw360.licenseinfo.outputGenerators.DocxUtils.*;

public class DocxGenerator extends OutputGenerator<byte[]> {

    private static final String UNKNOWN_LICENSE_NAME = "Unknown license name";
    private static final String UNKNOWN_FILE_NAME = "Unknown file name";

    public DocxGenerator() {
        super("docx", "License information as DOCX", true, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }

    @Override
    public byte[] generateOutputFile(Collection<LicenseInfoParsingResult> projectLicenseInfoResults, String projectName, String projectVersion, String licenseInfoHeaderText) throws SW360Exception {
        try {
            XWPFDocument document = new XWPFDocument(this.getClass().getResourceAsStream("/templateFrontpageContent.docx"));
            fillDocument(document, projectLicenseInfoResults, projectName, projectVersion, licenseInfoHeaderText);
            ByteArrayOutputStream docxOutputStream = new ByteArrayOutputStream();
            document.write(docxOutputStream);
            docxOutputStream.close();
            return docxOutputStream.toByteArray();
        } catch (XmlException e) {
            throw new SW360Exception("Got XmlException while generating docx document: " + e.getMessage());
        } catch (IOException ioe) {
            throw new SW360Exception("Got IOException when generating docx document: " + ioe.getMessage());
        }
    }

    private void fillDocument(XWPFDocument document, Collection<LicenseInfoParsingResult> projectLicenseInfoResults,
                              String projectName, String projectVersion, String licenseInfoHeaderText) throws XmlException {
        replaceText(document, "$license-info-header", licenseInfoHeaderText);
        replaceText(document, "$project-name", projectName);
        replaceText(document, "$project-version", projectVersion);

        fillReleaseBulletList(document, projectLicenseInfoResults);
        fillReleaseDetailList(document, projectLicenseInfoResults);
        fillLicenseList(document, projectLicenseInfoResults);
    }

    private void fillReleaseBulletList(XWPFDocument document, Collection<LicenseInfoParsingResult> projectLicenseInfoResults) throws XmlException {
        List<String> releaseList = new ArrayList<>();
        for (LicenseInfoParsingResult result : projectLicenseInfoResults) {
            releaseList.add(getComponentLongName(result));
        }
        addBulletList(document, releaseList, true);
        addPageBreak(document);
    }

    private void fillReleaseDetailList(XWPFDocument document, Collection<LicenseInfoParsingResult> projectLicenseInfoResults) {
        XWPFRun releaseTitleTextRun = document.createParagraph().createRun();
        addFormattedText(releaseTitleTextRun, "Detailed Releases Information", FONT_SIZE + 2, true);
        XWPFRun releaseInfoTextRun = document.createParagraph().createRun();
        setText(releaseInfoTextRun, "Please note the following license conditions and copyright " +
                "notices applicable to Open Source Software and/or other components (or parts thereof):");
        addNewLines(document, 0);

        for (LicenseInfoParsingResult parsingResult : projectLicenseInfoResults) {
            addReleaseTitle(document, parsingResult);
            if (parsingResult.getStatus() == LicenseInfoRequestStatus.SUCCESS) {
                XWPFRun copyrightTitleRun = document.createParagraph().createRun();
                addFormattedText(copyrightTitleRun, "Copyrights", FONT_SIZE, true);
                for (String copyright : getReleaseCopyrights(parsingResult)) {
                    XWPFParagraph copyPara = document.createParagraph();
                    copyPara.setSpacingAfter(0);
                    setText(copyPara.createRun(), copyright);
                }
                XWPFRun licensesTitleRun = document.createParagraph().createRun();
                addNewLines(licensesTitleRun, 1);
                addFormattedText(licensesTitleRun, "Licenses", FONT_SIZE, true);
                for (String licenseName : getReleasesLicenses(parsingResult)) {
                    XWPFParagraph licensePara = document.createParagraph();
                    licensePara.setSpacingAfter(0);
                    addBookmarkHyperLink(licensePara, licenseName, licenseName);
                }
            } else {
                XWPFRun errorRun = document.createParagraph().createRun();
                String errorText = nullToEmptyString(parsingResult.getMessage());
                String filename = getFilename(parsingResult);
                addFormattedText(errorRun, String.format("Error reading license information: %s", errorText), FONT_SIZE, false, ALERT_COLOR);
                addFormattedText(errorRun, String.format("Source file: %s", filename), FONT_SIZE, false, ALERT_COLOR);
            }
            addNewLines(document, 1);
        }
        addPageBreak(document);
    }

    private void addReleaseTitle(XWPFDocument document, LicenseInfoParsingResult parsingResult) {
        String releaseTitle = getComponentLongName(parsingResult);
        XWPFParagraph releaseTitleParagraph = document.createParagraph();
        releaseTitleParagraph.setStyle(STYLE_HEADING);
        addBookmark(releaseTitleParagraph, releaseTitle, releaseTitle);
        addNewLines(document, 0);
    }

    private Set<String> getReleaseCopyrights(LicenseInfoParsingResult licenseInfoParsingResult) {
        Set<String> copyrights = Collections.emptySet();
        if (licenseInfoParsingResult.isSetLicenseInfo()) {
            LicenseInfo licenseInfo = licenseInfoParsingResult.getLicenseInfo();
            if (licenseInfo.isSetCopyrights()) {
                copyrights = licenseInfo.getCopyrights();
            }
        }
        return copyrights;
    }

    private Set<String> getReleasesLicenses(LicenseInfoParsingResult licenseInfoParsingResult) {
        Set<String> licenses = new HashSet<>();
        if (licenseInfoParsingResult.isSetLicenseInfo()) {
            LicenseInfo licenseInfo = licenseInfoParsingResult.getLicenseInfo();
            if (licenseInfo.isSetLicenseNamesWithTexts()) {
                for (LicenseNameWithText licenseNameWithText : licenseInfo.getLicenseNamesWithTexts()) {
                    licenses.add(licenseNameWithText.isSetLicenseName()
                            ? licenseNameWithText.getLicenseName()
                            : UNKNOWN_LICENSE_NAME);
                }
            }
        }
        return licenses;
    }

    private String getFilename(LicenseInfoParsingResult licenseInfoParsingResult) {
        return Optional.ofNullable(licenseInfoParsingResult.getLicenseInfo())
                .map(LicenseInfo::getFilenames)
                .flatMap(l -> l.stream().findFirst())
                .orElse(UNKNOWN_FILE_NAME);
    }

    private void fillLicenseList(XWPFDocument document, Collection<LicenseInfoParsingResult> projectLicenseInfoResults) {
        List<LicenseNameWithText> licenseNameWithTexts = OutputGenerator.getSortedLicenseNameWithTexts(projectLicenseInfoResults);
        XWPFRun licenseHeaderRun = document.createParagraph().createRun();
        addFormattedText(licenseHeaderRun, "License texts", FONT_SIZE + 2, true);
        addNewLines(document, 0);

        for (LicenseNameWithText licenseNameWithText : licenseNameWithTexts) {
            XWPFParagraph licenseParagraph = document.createParagraph();
            licenseParagraph.setStyle(STYLE_HEADING);
            String licenseName = licenseNameWithText.isSetLicenseName() ? licenseNameWithText.getLicenseName() : UNKNOWN_LICENSE_NAME;
            addBookmark(licenseParagraph, licenseName, licenseName);
            setText(document.createParagraph().createRun(), nullToEmptyString(licenseNameWithText.getLicenseText()));
            addNewLines(document, 1);
        }
    }
}
