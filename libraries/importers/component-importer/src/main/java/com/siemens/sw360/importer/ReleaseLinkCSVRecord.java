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

import com.siemens.sw360.commonIO.SampleOptions;
import com.siemens.sw360.datahandler.common.SW360Utils;
import com.siemens.sw360.datahandler.thrift.components.ReleaseRelationship;
import org.apache.commons.csv.CSVRecord;

import java.util.ArrayList;

/**
 * @author johannes.najjar@tngtech.com
 */
public class ReleaseLinkCSVRecord extends  ComponentAwareCSVRecord{
    //linking Release done by inheritance

    // linked Release
    private final String linkedComponentName;
    private final String linkedReleaseName;
    private final String linkedReleaseVersion;
    private final ReleaseRelationship relationship;

    ReleaseLinkCSVRecord(String componentName, String releaseName, String releaseVersion,
                                String linkedComponentName, String linkedReleaseName,
                                String linkedReleaseVersion, ReleaseRelationship relationship) {
        super(componentName, releaseName, releaseVersion);
        this.linkedComponentName = linkedComponentName;
        this.linkedReleaseName = linkedReleaseName;
        this.linkedReleaseVersion = linkedReleaseVersion;
        this.relationship = relationship;
    }

    @Override
    public Iterable<String> getCSVIterable() {
        final ArrayList<String> elements = new ArrayList<>();

        elements.add(componentName);
        elements.add(releaseName);
        elements.add(releaseVersion);
        elements.add(linkedComponentName);
        elements.add(linkedReleaseName);
        elements.add(linkedReleaseVersion);
        elements.add(relationship.name());

        return elements;
    }

    public static Iterable<String> getCSVHeaderIterable() {
        final ArrayList<String> elements = new ArrayList<>();

        elements.add("componentName");
        elements.add("releaseName");
        elements.add("releaseVersion");
        elements.add("linkedComponentName");
        elements.add("linkedReleaseName");
        elements.add("linkedReleaseVersion");
        elements.add("ReleaseRelationship");

        return elements;
    }

    public static Iterable<String> getSampleInputIterable() {
        final ArrayList<String> elements = new ArrayList<>();

        elements.add("componentName");
        elements.add("releaseName");
        elements.add(SampleOptions.VERSION_OPTION);
        elements.add("linkedComponentName");
        elements.add("linkedReleaseName");
        elements.add(SampleOptions.VERSION_OPTION);
        elements.add(SampleOptions.RELEASE_RELEATIONSHIP_OPTIONS);

        return elements;
    }

    public String getLinkedReleaseIdentifier() {
        return SW360Utils.getVersionedName(linkedReleaseName, linkedReleaseVersion);
    }

    public String getLinkedComponentName() {
        return linkedComponentName;
    }

    public ReleaseRelationship getRelationship() {
        return relationship;
    }

    public static ReleaseLinkCSVRecordBuilder builder(){
        return new ReleaseLinkCSVRecordBuilder();
    }

    public static ReleaseLinkCSVRecordBuilder builder( CSVRecord in){
        return new ReleaseLinkCSVRecordBuilder( in );
    }

}
