/*
 * Copyright Siemens AG, 2013-2016. Part of the SW360 Portal Project.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License Version 2.0 as published by the
 * Free Software Foundation with classpath exception.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License version 2.0 for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (please see the COPYING file); if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package com.siemens.sw360.importer;

import com.google.common.collect.ImmutableSet;
import com.siemens.sw360.datahandler.thrift.components.*;
import com.siemens.sw360.datahandler.thrift.vendors.Vendor;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author johannes.najjar@tngtech.com
 */
public class ComponentCSVRecordBuilderTest {

    @Test
    public void testFillComponent() throws Exception {
        final Component component = new Component();
        component.setName("Name").setBlog("BLog").setCategories(ImmutableSet.of("cat2", "cat3"))
                .setComponentType(ComponentType.COTS).setCreatedBy("theCreator")
                .setCreatedOn("the4thDay").setDescription("desc").setHomepage("homePage")
                .setBlog("blog also").setMailinglist("Mighty Mails")
                .setSoftwarePlatforms(ImmutableSet.of("Firefox", "Python"));

        final ComponentCSVRecordBuilder componentCSVRecordBuilder = new ComponentCSVRecordBuilder().fill(component);
        final ComponentCSVRecord build = componentCSVRecordBuilder.build();

        assertThat(build.getComponent(), is(component));


    }

    @Test
    public void testFillRelease() throws Exception {

        Release release = new Release();

        release.setName("Name").setCreatedBy("theCreator").setCreatedOn("the5thDay")
                .setVersion("6").setCpeid("cpe2.3://///***").setReleaseDate("theDayOfTheRelease")
                .setDownloadurl("http://www.siemens.com").setMainlineState(MainlineState.MAINLINE)
                .setClearingState(ClearingState.NEW_CLEARING)
                .setContacts(ImmutableSet.of("me", "myself", "and", "I")).setContacts(ImmutableSet.of("are", "singing"))
                .setModerators(ImmutableSet.of("and", "dancing")).setSubscribers(ImmutableSet.of("to", "a"))
                .setLanguages(ImmutableSet.of("silent", "tune")).setOperatingSystems(ImmutableSet.of("which", "is", "licensed"))
                .setMainLicenseIds(ImmutableSet.of("under", "GPL"));


        final ComponentCSVRecordBuilder componentCSVRecordBuilder = new ComponentCSVRecordBuilder().fill(release);
        final ComponentCSVRecord build = componentCSVRecordBuilder.build();
        assertThat(build.getRelease(null, null, null), is(release));

    }

    @Test
    public void testFillVendor() throws Exception {
        Vendor vendor = new Vendor();
        vendor.setFullname("VendorName").setShortname("Ven").setUrl("http://www.siemens.com");
        final ComponentCSVRecordBuilder componentCSVRecordBuilder = new ComponentCSVRecordBuilder().fill(vendor);
        final ComponentCSVRecord build = componentCSVRecordBuilder.build();
        assertThat(build.getVendor(), is(vendor));
    }

    @Test
    public void testFillClearingInfo() throws Exception {
        final ClearingInformation clearingInformation = new ClearingInformation();


        clearingInformation.setAL("AL").setECCN("ECCN").setExternalSupplierID("C4S").setAssessorContactPerson("JN")
                .setAssessorDepartment("T").setAdditionalRequestInfo("NG").setEvaluated("eval").setProcStart("proc")
                .setRequestID("req").setScanned("e").setClearingStandard("CL").setComment("wittyh comment")
                .setExternalUrl("share me").setBinariesOriginalFromCommunity(true).setBinariesSelfMade(false)
                .setComponentLicenseInformation(true).setSourceCodeDelivery(true)
                .setSourceCodeOriginalFromCommunity(false).setSourceCodeToolMade(false)
                .setSourceCodeSelfMade(false).setScreenshotOfWebSite(true)
                .setFinalizedLicenseScanReport(false).setLicenseScanReportResult(true)
                .setLegalEvaluation(true).setLicenseAgreement(false)
                .setComponentClearingReport(false).setCountOfSecurityVn(2323);

        final ComponentCSVRecordBuilder componentCSVRecordBuilder = new ComponentCSVRecordBuilder().fill(clearingInformation);

        final ComponentCSVRecord build = componentCSVRecordBuilder.build();
        assertThat(build.getClearingInformation(), is(clearingInformation));
    }

    @Test
    public void testFillRepository() throws Exception {
        final Repository repository = new Repository();
        repository.setRepositorytype(RepositoryType.ALIENBRAIN).setUrl("http://www.siemens.com");
        final ComponentCSVRecordBuilder componentCSVRecordBuilder = ComponentCSVRecord.builder().fill(repository);
        final ComponentCSVRecord build = componentCSVRecordBuilder.build();
        assertThat(build.getRepository(), is(repository));
    }
}