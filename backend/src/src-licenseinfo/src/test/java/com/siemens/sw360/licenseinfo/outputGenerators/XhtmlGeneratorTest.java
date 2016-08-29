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
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Text;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Created by heydenrb on 04.08.16.
 */
public class XhtmlGeneratorTest {
    Collection<LicenseInfoParsingResult> lipresults;
    Collection<LicenseInfoParsingResult> lipresults2;
    Collection<LicenseInfoParsingResult> lipresults3;
    Collection<LicenseInfoParsingResult> lipresultsEmpty;
    XhtmlGenerator xhtmlGenerator;

    @Before
    public void setUp() throws Exception {
        //first LicenseInfoParsingResult
        LicenseInfoParsingResult lipresult = new LicenseInfoParsingResult();

        LicenseInfo li = new LicenseInfo();
        Set<String> copyrights = new HashSet<String>(Arrays.asList("cr1", "cr2"));
        li.setCopyrights(copyrights);

        LicenseNameWithText lnt1 = new LicenseNameWithText().setLicenseName("l1").setLicenseText("t1");
        LicenseNameWithText lnt2 = new LicenseNameWithText().setLicenseName("l1").setLicenseText("t1");
        Set<LicenseNameWithText> licenseNameWithTexts = new HashSet<>(Arrays.asList(lnt1, lnt2));

        li.setLicenseNamesWithTexts(licenseNameWithTexts);

        lipresult.setLicenseInfo(li);
        lipresult.setName("myrelease");
        lipresult.setVersion("1");
        lipresult.setVendor("vendor");
        lipresults = Collections.singletonList(lipresult);

        //second LicenseInfoParsingResult
        LicenseInfoParsingResult lipresult2 = new LicenseInfoParsingResult();

        LicenseInfo li2 = new LicenseInfo();
        Set<String> copyrights2 = new HashSet<String>(Arrays.asList("CR1", "CR2"));
        li2.setCopyrights(copyrights2);

        LicenseNameWithText lnt11 = new LicenseNameWithText().setLicenseName("L1").setLicenseText("T1");
        LicenseNameWithText lnt12 = new LicenseNameWithText().setLicenseName("L1").setLicenseText("T2");
        Set<LicenseNameWithText> licenseNameWithTexts2 = new HashSet<>(Arrays.asList(lnt11, lnt12));

        li2.setLicenseNamesWithTexts(licenseNameWithTexts2);

        lipresult2.setLicenseInfo(li2);
        lipresult2.setName("myrelease");
        lipresult2.setVersion("2");
        lipresult2.setVendor("vendor");

        lipresults2 = Arrays.asList(lipresult,lipresult2);

        //LicenseInfoParsingResult with a single copyright and license
        LicenseInfoParsingResult lipresult3 = new LicenseInfoParsingResult();

        LicenseInfo li3 = new LicenseInfo();
        Set<String> copyrights3 = new HashSet<String>(Arrays.asList("cr"));
        li3.setCopyrights(copyrights3);

        LicenseNameWithText lnt31 = new LicenseNameWithText().setLicenseName("l1").setLicenseText("t1");
        Set<LicenseNameWithText> licenseNameWithTexts3 = new HashSet<>(Arrays.asList(lnt31));

        li3.setLicenseNamesWithTexts(licenseNameWithTexts3);

        lipresult3.setLicenseInfo(li3);
        lipresult3.setName("myrelease");
        lipresult3.setVersion("1");
        lipresult3.setVendor("vendor");
        lipresults3 = Collections.singletonList(lipresult3);

        //LicenseInfoParsingResult with a no copyright and license
        LicenseInfoParsingResult lipresultEmpty = new LicenseInfoParsingResult();

        LicenseInfo liEmpty = new LicenseInfo();
        Set<String> copyrightsEmpty = new HashSet();
        liEmpty.setCopyrights(copyrightsEmpty);

        Set<LicenseNameWithText> licenseNameWithTextsEmpty= new HashSet();

        liEmpty.setLicenseNamesWithTexts(licenseNameWithTextsEmpty);

        lipresultEmpty.setLicenseInfo(liEmpty);
        lipresultEmpty.setName("myrelease");
        lipresultEmpty.setVersion("1");
        lipresultEmpty.setVendor("vendor");
        lipresultsEmpty = Collections.singletonList(lipresultEmpty);

        xhtmlGenerator = new XhtmlGenerator();
    }

    @Test
    public void testGenerateOutputFile_EmptyCopyrightAndLicense() throws Exception {
        String xmlString = xhtmlGenerator.generateOutputFile(lipresultsEmpty, "myproject");
        Document document = DocumentHelper.parseText(xmlString);
        String copyrights = findCopyrights(document, "vendor_myrelease_1");
        String licenses = findLicenses(document,"vendor_myrelease_1");
        assertThat(copyrights.equals(""), is(true));
        assertThat(licenses.equals(""), is(true));
    }

    @Test
    public void testGenerateOutputFile_parseSingleCopyright() throws Exception {
        String xmlString = xhtmlGenerator.generateOutputFile(lipresults3, "myproject");
        Document document = DocumentHelper.parseText(xmlString);
        String copyrights = findCopyrights(document, "vendor_myrelease_1");
        assertThat(copyrights.equals("\ncr"), is(true));
    }

    @Test
    public void testGenerateOutputFile_parseCopyrights() throws Exception {
        String xmlString = xhtmlGenerator.generateOutputFile(lipresults, "myproject");
        Document document = DocumentHelper.parseText(xmlString);
        String copyrights = findCopyrights(document, "vendor_myrelease_1");
        assertThat(copyrights.contains("cr1"), is(true));
        assertThat(copyrights.contains("cr2"), is(true));
    }

    @Test
    public void testGenerateOutputFile_parseCopyrightsFromTwoReleases() throws Exception {
        String xmlString = xhtmlGenerator.generateOutputFile(lipresults2, "myproject");
        Document document = DocumentHelper.parseText(xmlString);
        String copyrights = findCopyrights(document, "vendor_myrelease_1");
        assertThat(copyrights.contains("cr1"), is(true));
        assertThat(copyrights.contains("cr2"), is(true));
        copyrights = findCopyrights(document, "vendor_myrelease_2");
        assertThat(copyrights.contains("CR1"), is(true));
        assertThat(copyrights.contains("CR2"), is(true));
    }

    private String findCopyrights(Document document, String releaseNameString) {
        List list = document.selectNodes("//*[local-name()='li'][@id='" + releaseNameString + "']/*[@class='copyrights']/text()");
        StringBuffer result = new StringBuffer();

        for (Iterator iter = list.iterator(); iter.hasNext(); ) {
            Text text = (Text) iter.next();
            if(! "".equals(text.getStringValue().trim())) {
                result.append(text.getStringValue());
            }
        }
        return result.toString();
    }

    @Test
    public void testGenerateOutputFile_parseSingleLicense() throws Exception {
        String xmlString = xhtmlGenerator.generateOutputFile(lipresults3, "myproject");
        Document document = DocumentHelper.parseText(xmlString);
        String licenses = findLicenses(document, "vendor_myrelease_1");
        assertThat(licenses.equals("\nt1\n"), is(true));
    }
    @Test
    public void testGenerateOutputFile_parseLicenses() throws Exception {
        String xmlString = xhtmlGenerator.generateOutputFile(lipresults, "myproject");
        Document document = DocumentHelper.parseText(xmlString);
        String licenses = findLicenses(document, "vendor_myrelease_1");
        assertThat(licenses.contains("t1"), is(true));
    }

    @Test
    public void testGenerateOutputFile_parseLicensesFromTwoReleases() throws Exception {
        String xmlString = xhtmlGenerator.generateOutputFile(lipresults2, "myproject");
        Document document = DocumentHelper.parseText(xmlString);
        String licenses = findLicenses(document, "vendor_myrelease_1");
        assertThat(licenses.contains("t1"), is(true));
        licenses = findLicenses(document, "vendor_myrelease_2");
        assertThat(licenses.contains("T1"), is(true));
        assertThat(licenses.contains("T2"), is(true));
    }

    private String findLicenses(Document document, String releaseNameString) {
        List list = document.selectNodes("//*[local-name()='li'][@id='"+ releaseNameString + "']/*[local-name()='ul'][@class='licenseEntries']");
        StringBuffer result = new StringBuffer();

        for (Iterator iter = list.iterator(); iter.hasNext(); ) {
            Element element = (Element) iter.next();
            for (Object liObject : element.content()) {
                Element liElement = (Element) liObject;
                String licenseEntryId = liElement.attribute("id").getValue();
                String licenseTextId = licenseEntryId.replace("licenseEntry","licenseText");
                String licenseText = ((Text) document.selectNodes("//*[local-name()='pre'][@id='"+ licenseTextId+ "']/text()").get(0)).getStringValue();
                result.append(licenseText);
                result.append("\n");
            }
        }

        return result.toString();
    }
}

