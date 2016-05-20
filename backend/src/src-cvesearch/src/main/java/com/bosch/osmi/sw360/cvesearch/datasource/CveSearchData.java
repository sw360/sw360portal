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

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CveSearchData {

    static public class VulnerableConfigurationEntry {
        private String title;
        private String id;

        public VulnerableConfigurationEntry(String title, String id) {
            this.title = title;
            this.id = id;
        }

        public VulnerableConfigurationEntry(String id) {
            this.title = id;
            this.id = id;
        }

        public Map<String,String> getAsMap() {
            Map<String,String> map = new HashMap<>();
            map.put("id", id);
            map.put("title", title);
            return map;
        }
    }

    private String id;
    private String Modified;
    private Set<String> references;
    private String Published;
    @SerializedName("cvss-time") private String cvss_time;
    private Set<VulnerableConfigurationEntry> vulnerable_configuration;
    private double cvss;
    private Set<String> vulnerable_configuration_2_2;
    private Map<String,String> impact;
    private Map<String,String> access;
    private String summary;
    private Map<String,String> map_cve_scip;
    private Map<String,String> map_cve_exploitdb;
    private Map<String,String> map_cve_bid;
    private Set<Set<Map<String,Integer>>> ranking;

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

    public String getCvss_time() {
        return cvss_time;
    }

    public Set<Map<String,String>> getVulnerable_configuration() {
        return vulnerable_configuration.stream()
                .map(vce -> vce.getAsMap())
                .collect(Collectors.toSet());
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

    public Map<String, String> getMap_cve_scip() {
        return map_cve_scip;
    }

    public Map<String, String> getMap_cve_exploitdb() {
        return map_cve_exploitdb;
    }

    public Map<String, String> getMap_cve_bid() {
        return map_cve_bid;
    }

    public Set<Set<Map<String, Integer>>> getRanking() {
        return ranking;
    }

}
