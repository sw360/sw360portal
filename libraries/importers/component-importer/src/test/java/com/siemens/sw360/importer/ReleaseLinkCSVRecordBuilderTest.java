/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
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

import com.siemens.sw360.datahandler.common.SW360Utils;
import com.siemens.sw360.datahandler.thrift.components.Component;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.components.ReleaseRelationship;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author johannes.najjar@tngtech.com
 */
public class ReleaseLinkCSVRecordBuilderTest {
    @Test
    public void testFillComponent() throws Exception {
        final String componentName = "myCompo";

        final Component component = new Component();
        component.setName(componentName);

        final ReleaseLinkCSVRecordBuilder releaseLinkCSVRecordBuilder = new ReleaseLinkCSVRecordBuilder();
        releaseLinkCSVRecordBuilder.fill(component);

        final ReleaseLinkCSVRecord filledRecord = releaseLinkCSVRecordBuilder.build();

        assertThat(filledRecord.getComponentName(), is(componentName));
    }

    @Test
    public void testFillRelease() throws Exception {
        final String releaseName =  "myRelease";
        final String releaseVersion =  "1.862b";

        final Release release = new Release();
        release.setName(releaseName).setVersion(releaseVersion);
        final ReleaseLinkCSVRecordBuilder releaseLinkCSVRecordBuilder = new ReleaseLinkCSVRecordBuilder();
        releaseLinkCSVRecordBuilder.fill(release);
        final ReleaseLinkCSVRecord filledRecord = releaseLinkCSVRecordBuilder.build();

        assertThat(filledRecord.getReleaseIdentifier(), is(SW360Utils.getVersionedName(releaseName, releaseVersion)));
    }

    @Test
    public void testFillLinkedComponent() throws Exception {
        final String componentName = "myCompo";

        final Component component = new Component();
        component.setName(componentName);

        final ReleaseLinkCSVRecordBuilder releaseLinkCSVRecordBuilder = new ReleaseLinkCSVRecordBuilder();
        releaseLinkCSVRecordBuilder.fillLinking(component);

        final ReleaseLinkCSVRecord filledRecord = releaseLinkCSVRecordBuilder.build();

        assertThat(filledRecord.getLinkedComponentName(), is(componentName));
    }

    @Test
    public void testFillLinkedRelease() throws Exception {
        final String releaseName =  "myRelease";
        final String releaseVersion =  "1.862b";

        final Release release = new Release();
        release.setName(releaseName).setVersion(releaseVersion);
        final ReleaseLinkCSVRecordBuilder releaseLinkCSVRecordBuilder = new ReleaseLinkCSVRecordBuilder();
        releaseLinkCSVRecordBuilder.fillLinking(release);
        final ReleaseLinkCSVRecord filledRecord = releaseLinkCSVRecordBuilder.build();

        assertThat(filledRecord.getLinkedReleaseIdentifier(), is(SW360Utils.getVersionedName(releaseName, releaseVersion)));
    }


    @Test
    public void testReleaseReleationship() throws Exception {
        final ReleaseRelationship releaseRelationship = ReleaseRelationship.CONTAINED;

        final ReleaseLinkCSVRecordBuilder releaseLinkCSVRecordBuilder = new ReleaseLinkCSVRecordBuilder();
        releaseLinkCSVRecordBuilder.setRelationship(releaseRelationship);
        final ReleaseLinkCSVRecord filledRecord = releaseLinkCSVRecordBuilder.build();

        assertThat(filledRecord.getRelationship(), is (releaseRelationship));
    }
}