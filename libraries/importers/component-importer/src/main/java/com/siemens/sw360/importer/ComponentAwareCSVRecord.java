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

/**
 * @author johannes.najjar@tngtech.com
 */
public abstract class ComponentAwareCSVRecord implements CustomizedCSVRecord {
    protected final String componentName;
    protected final String releaseName;
    protected final String releaseVersion;

    protected ComponentAwareCSVRecord(String componentName, String releaseName, String releaseVersion) {
        this.componentName = componentName;
        this.releaseName = releaseName;
        this.releaseVersion = releaseVersion;
    }

    public String getReleaseIdentifier() {
        return SW360Utils.getVersionedName(releaseName, releaseVersion);
    }

    public String getComponentName() {
        return componentName;
    }
}
