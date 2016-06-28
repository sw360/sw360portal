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
import java.util.function.BiConsumer;

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

        public Map.Entry<String,String> getAsMapEntry() {
            return new HashMap.SimpleImmutableEntry<>(id,title);
        }
    }

    private String id;
    @SerializedName("Modified") private String modified;
    private Set<String> references;
    @SerializedName("Published") private String published;
    @SerializedName("cvss-time") private String cvss_time;
    private Set<VulnerableConfigurationEntry> vulnerable_configuration;
    private Double cvss;
    private String cwe;
    private Map<String,String> impact;
    private Map<String,String> access;
    private String summary;
    private Map<String,String> map_cve_bid;
    private Map<String,String> map_cve_debian;
    private Map<String,String> map_cve_exploitdb;
    private Map<String,String> map_cve_gedora;
    private Map<String,String> map_cve_hp;
    private Map<String,String> map_cve_iavm;
    private Map<String,String> map_cve_msf;
    private Map<String,String> map_cve_nessus;
    private Map<String,String> map_cve_openvas;
    private Map<String,String> map_cve_osvdb;
    private Map<String,String> map_cve_oval;
    private Map<String,String> map_cve_saint;
    private Map<String,String> map_cve_scip;
    private Map<String,String> map_cve_suse;
    private Map<String,String> map_cve_vmware;
    private Map<String,String> map_redhat_bugzilla;
    private Set<Set<Map<String,Integer>>> ranking; // only filled, when `cve`-api is used, not `cvefor`

    public Map<String, String> getAccess() {
        return access;
    }

    public String getId() {
        return id;
    }

    public String getModified() {
        return modified;
    }

    public Set<String> getReferences() {
        return references;
    }

    public String getPublished() {
        return published;
    }

    public String getCvss_time() {
        return cvss_time;
    }

    public Map<String,String> getVulnerable_configuration() {
        Map<String,String> toReturn = new HashMap<>();
        vulnerable_configuration.stream()
                .map(VulnerableConfigurationEntry::getAsMapEntry)
                .forEach(entry -> toReturn.put(entry.getKey(), entry.getValue()));
        return toReturn;
    }

    public Double getCvss() {
        return cvss;
    }

    public String getCwe() {
        return cwe;
    }

    public Map<String, String> getImpact() {
        return impact;
    }

    public String getSummary() {
        return summary;
    }

    public Map<String,Map<String,String>> getMap_cve_all(){
        Map<String,Map<String,String>> mapOfAll = new HashMap<>();

        BiConsumer<String, Map<String,String>> f = (title, map) -> mapOfAll.put(title, map);

        f.accept("bid"            , map_cve_bid);
        f.accept("debian"         , map_cve_debian);
        f.accept("exploitdb"      , map_cve_exploitdb);
        f.accept("gedora"         , map_cve_gedora);
        f.accept("hp"             , map_cve_hp);
        f.accept("iavm"           , map_cve_iavm);
        f.accept("msf"            , map_cve_msf);
        f.accept("nessus"         , map_cve_nessus);
        f.accept("openvas"        , map_cve_openvas);
        f.accept("osvdb"          , map_cve_osvdb);
        f.accept("oval"           , map_cve_oval);
        f.accept("saint"          , map_cve_saint);
        f.accept("scip"           , map_cve_scip);
        f.accept("suse"           , map_cve_suse);
        f.accept("vmware"         , map_cve_vmware);
        f.accept("redhat_bugzilla", map_redhat_bugzilla);

        return mapOfAll;
    }

    public Set<Set<Map<String, Integer>>> getRanking() {
        return ranking;
    }

}
