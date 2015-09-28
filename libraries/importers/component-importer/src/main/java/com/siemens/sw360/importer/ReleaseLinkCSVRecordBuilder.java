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

import com.siemens.sw360.datahandler.common.ThriftEnumUtils;
import com.siemens.sw360.datahandler.thrift.components.Component;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.components.ReleaseRelationship;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author johannes.najjar@tngtech.com
 */
public class ReleaseLinkCSVRecordBuilder extends CustomizedCSVRecordBuilder<ReleaseLinkCSVRecord> {
    private String componentName;
    private String releaseName;
    private String releaseVersion;

    private String linkedComponentName;
    private String linkedReleaseName;
    private String linkedReleaseVersion;
    private ReleaseRelationship relationship;

    ReleaseLinkCSVRecordBuilder(CSVRecord record) {
        int i = 0;
        componentName = record.get(i++);
        releaseName = record.get(i++);
        releaseVersion = record.get(i++);
        linkedComponentName = record.get(i++);
        linkedReleaseName = record.get(i++);
        linkedReleaseVersion = record.get(i++);
        relationship = ThriftEnumUtils.stringToEnum(record.get(i), ReleaseRelationship.class);
    }

    ReleaseLinkCSVRecordBuilder() {
        componentName =null;
        releaseName =null;
        releaseVersion =null;
        linkedComponentName =null;
        linkedReleaseName =null;
        linkedReleaseVersion =null;
        relationship = null;
    }

    @Override
    public ReleaseLinkCSVRecord build() {
        return new ReleaseLinkCSVRecord(componentName, releaseName, releaseVersion,
                linkedComponentName, linkedReleaseName,
                linkedReleaseVersion, relationship);
    }

    public void fill(Component component) {
        setComponentName(component.getName());
    }

    public void fill (Release release) {
        setReleaseName(release.getName());
        setReleaseVersion(release.getVersion());
    }

    public void fillLinking(Component component) {
        setLinkedComponentName(component.getName());
    }

    public void fillLinking (Release release) {
        setLinkedReleaseName(release.getName());
        setLinkedReleaseVersion(release.getVersion());
    }

    //Auto generated setters
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    public void setLinkedComponentName(String linkedComponentName) {
        this.linkedComponentName = linkedComponentName;
    }

    public void setLinkedReleaseName(String linkedReleaseName) {
        this.linkedReleaseName = linkedReleaseName;
    }

    public void setLinkedReleaseVersion(String linkedReleaseVersion) {
        this.linkedReleaseVersion = linkedReleaseVersion;
    }

    public void setRelationship(ReleaseRelationship relationship) {
        this.relationship = relationship;
    }
}
