/*
 * Copyright (c) Bosch Software Innovations GmbH 2016.
 * Part of the SW360 Portal Project.
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
package com.bosch.osmi.sw360.cvesearch.datasource;

import java.util.Map;
import java.util.Set;

public class CveSearchData {

    private String id;
    private String Modified;
    private Set<String> references;
    private String Published;
    // private String cvss-time;
    private Set<String> vulnerable_configuration;
    private double cvss;
    private Set<String> vulnerable_configuration_2_2;
    private Map<String,String> impact;
    private Map<String,String> access;
    private String summary;

    public Map<String, String> getAccess() {
        return access;
    }

    public String getId() {
        return id;
    }

    public String getModified() {
        return Modified;
    }

    public Set<String> getReferences() {
        return references;
    }

    public String getPublished() {
        return Published;
    }

    public Set<String> getVulnerable_configuration() {
        return vulnerable_configuration;
    }

    public double getCvss() {
        return cvss;
    }

    public Set<String> getVulnerable_configuration_2_2() {
        return vulnerable_configuration_2_2;
    }

    public Map<String, String> getImpact() {
        return impact;
    }

    public String getSummary() {
        return summary;
    }

}
