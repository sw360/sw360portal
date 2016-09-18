/*
 * Copyright Siemens AG, 2014-2016. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.datahandler.common;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import com.siemens.sw360.datahandler.thrift.ThriftClients;
import com.siemens.sw360.datahandler.thrift.ThriftUtils;
import com.siemens.sw360.datahandler.thrift.components.*;
import com.siemens.sw360.datahandler.thrift.licenses.License;
import com.siemens.sw360.datahandler.thrift.licenses.LicenseService;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.projects.ProjectLink;
import com.siemens.sw360.datahandler.thrift.projects.ProjectRelationship;
import com.siemens.sw360.datahandler.thrift.projects.ProjectService;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.thrift.vendors.Vendor;
import com.siemens.sw360.datahandler.thrift.vulnerabilities.Vulnerability;
import org.apache.log4j.Logger;
import org.apache.thrift.TEnum;
import org.apache.thrift.TException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.Iterables.transform;
import static com.siemens.sw360.datahandler.common.CommonUtils.joinStrings;
import static com.siemens.sw360.datahandler.common.CommonUtils.nullToEmptyMap;
import static com.siemens.sw360.datahandler.thrift.ThriftUtils.extractId;
import static org.apache.log4j.Logger.getLogger;

/**
 * @author Cedric.Bodet@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 * @author stefan.jaeger@evosoft.com
 * @author alex.borodin@evosoft.com
 */
public class SW360Utils {

    private final static Logger log = getLogger(SW360Utils.class);

    public static final String FORMAT_DATE = "yyyy-MM-dd";
    public static final String FORMAT_DATE_TIME = "yyyy-MM-dd HH:mm:ss";

    private static Joiner spaceJoiner = Joiner.on(" ");

    private SW360Utils() {
        // Utility class with only static functions
    }

    /**
     * Returns a string for the current date in the form "yyyy-MM-dd"
     */
    public static String getCreatedOn() {
        return new SimpleDateFormat(FORMAT_DATE).format(new Date());
    }

    /**
     * Returns a string for the current date in the form "yyyy-MM-dd HH:mm:ss"
     */
    public static String getCreatedOnTime() {
        return new SimpleDateFormat(FORMAT_DATE_TIME).format(new Date());
    }

    /**
     * Tries to parse a given date in format "yyyy-MM-dd HH:mm:ss" to a Date, returns null if it fails
     * @param date in format "yyyy-MM-dd HH:mm:ss"
     * @return Date
     */
    public static Date getDateFromTimeString(String date){
        try {
            return new SimpleDateFormat(FORMAT_DATE_TIME).parse(date);
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * returns a string of a given date in the form "yyyy-MM-dd HH:mm:ss"
     * @param date
     * @return {@link String}
     */
    public static String getDateTimeString(Date date){
        return new SimpleDateFormat(FORMAT_DATE_TIME).format(date);
    }

    /**
     * Filter BU to first three blocks
     */
    public static String getBUFromOrganisation(String organisation) {
        if(Strings.isNullOrEmpty(organisation)) return "";

        List<String> parts = Arrays.asList(organisation.toUpperCase().split("\\s"));

        int maxIndex = Math.min(parts.size(), 3);

        return spaceJoiner.join(parts.subList(0, maxIndex)).toUpperCase();
    }

    public static Set<String> filterBUSet(String organisation, Set<String> strings) {
        if (strings == null || isNullOrEmpty(organisation)) {
            return new HashSet<String>();
        }
        String bu = getBUFromOrganisation(organisation);
        return strings
                .stream()
                .filter(string-> bu.equals(string))
                .collect(Collectors.toSet());
    }

    /**
     * Set the vendor id if the vendor object is set
     */
    public static void setVendorId(Release release) {
        // Save the vendor ID, not its contents
        if (release.isSetVendor()) {
            Vendor vendor = release.getVendor();
            release.setVendorId(vendor.getId());
            release.unsetVendor();
        }
        if (isNullOrEmpty(release.getVendorId())){
            release.unsetVendorId();
        }
    }

    public static Set<String> getReleaseIds(Collection<Release> in) {
        return ImmutableSet.copyOf(getIdsIterable(in));
    }

    public static Set<String> getComponentIds(Collection<Component> in) {
        return ImmutableSet.copyOf(getIdsIterable(in));
    }

    public static Set<String> getProjectIds(Collection<Project> in) {
        return ImmutableSet.copyOf(getIdsIterable(in));
    }

    public static <T> Iterable<String> getIdsIterable(Iterable<T> projects) {
        return transform(projects, extractId());
    }

    public static String getVersionedName(String name, String version) {
        if (isNullOrEmpty(version)) {
            return name;
        } else {
            return name + " (" + version + ")";
        }
    }

    public static String printName(Component component) {
        if (component == null || isNullOrEmpty(component.getName())) {
            return "New Component";
        }

        return component.getName();
    }

    public static String printName(Release release) {
        if (release == null || isNullOrEmpty(release.getName())) {
            return "New Release";
        }

        return getVersionedName(release.getName(), release.getVersion());
    }

    public static String printName(Project project) {
        if (project == null || isNullOrEmpty(project.getName())) {
            return "New Project";
        }
        return getVersionedName(project.getName(), project.getVersion());
    }

    public static String printName(License license) {
        if (license == null || isNullOrEmpty(license.getId())) {
            return "New License";
        }
        return license.getId();
    }

    public static String printName(Vulnerability vulnerability) {
        if (vulnerability == null || isNullOrEmpty(vulnerability.getId())) {
            return "";
        }
        return vulnerability.getExternalId();
    }

    public static String printName(User user) {
        if (user == null || isNullOrEmpty(user.getEmail())) {
            return "New User";
        }
        return user.getEmail();
    }

    public static Collection<ProjectLink> getLinkedProjects(Map<String, ProjectRelationship> in, ThriftClients thriftClients, Logger log) {
        if (in != null) {
            try {
                ProjectService.Iface client = thriftClients.makeProjectClient();
                List<ProjectLink> linkedProjects = client.getLinkedProjects(in);
                return linkedProjects;
            } catch (TException e) {
                log.error("Could not get linked projects", e);
            }
        }
        return Collections.emptyList();
    }

    public static Collection<ProjectLink> getLinkedProjectsAsFlatList(Map<String, ProjectRelationship> in, ThriftClients thriftClients, Logger log) {
        return flattenProjectLinkTree(getLinkedProjects(in, thriftClients, log));
    }

    public static Collection<ProjectLink> flattenProjectLinkTree(Collection<ProjectLink> linkedProjects) {
        List<ProjectLink> result = new ArrayList<>();

        for (ProjectLink projectLink : linkedProjects) {
            result.add(projectLink);
            if (projectLink.isSetSubprojects()){
                result.addAll(flattenProjectLinkTree(projectLink.getSubprojects()));
            }
        }

        return result;
    }

    public static Collection<ReleaseLink> flattenReleaseLinkTree(Collection<ReleaseLink> linkedReleases) {
        List<ReleaseLink> result = new ArrayList<>();

        for (ReleaseLink releaseLink : linkedReleases) {
            result.add(releaseLink);
            if (releaseLink.isSetSubreleases()){
                result.addAll(flattenReleaseLinkTree(releaseLink.getSubreleases()));
            }
        }

        return result;
    }

    public static Collection<ReleaseLink> getLinkedReleaseRelationsAsFlatList(Map<String, ReleaseRelationship> in, ThriftClients thriftClients, Logger log) {
        return flattenReleaseLinkTree(getLinkedReleaseRelations(in, thriftClients, log));
    }

    public static Collection<ReleaseLink> getLinkedReleasesAsFlatList(Map<String, String> in, ThriftClients thriftClients, Logger log) {
        return flattenReleaseLinkTree(getLinkedReleases(in, thriftClients, log));
    }

    public static List<ReleaseLink> getLinkedReleases(Map<String, String> releaseUsage, ThriftClients thriftClients, Logger log) {
        if (releaseUsage != null) {
            try {
                ComponentService.Iface componentClient = thriftClients.makeComponentClient();
                final List<ReleaseLink> linkedReleases = componentClient.getLinkedReleases(releaseUsage);
                return linkedReleases;
            } catch (TException e) {
                log.error("Could not get linked releases", e);
            }
        }
        return Collections.emptyList();
    }


    public static List<ReleaseLink> getLinkedReleaseRelations(Map<String, ReleaseRelationship> releaseUsage, ThriftClients thriftClients, Logger log) {
        if (releaseUsage != null) {
            try {
                ComponentService.Iface componentClient = thriftClients.makeComponentClient();
                final List<ReleaseLink> linkedReleases = componentClient.getLinkedReleaseRelations(releaseUsage);
                return linkedReleases;
            } catch (TException e) {
                log.error("Could not get linked releases", e);
            }
        }
        return Collections.emptyList();
    }

    public static Predicate<String> startsWith(final String prefix) {
        return new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return input != null && input.startsWith(prefix);
            }
        };
    }

    public static List<String> getLicenseNamesFromIds(Collection<String> ids, String department) throws TException {
        final List<License> licenseList = getLicenses(ids, department);

        return getLicenseNamesFromLicenses(licenseList);
    }

    public static List<License> getLicenses(Collection<String> ids, String department) throws TException {
        if (ids != null && ids.size() > 0) {
            LicenseService.Iface client = new ThriftClients().makeLicenseClient();
            return client.getByIds(new HashSet<>(ids), department);
        } else return Collections.emptyList();
    }

    @NotNull
    public static List<String> getLicenseNamesFromLicenses(List<License> licenseList) {
        List<String> resultList = new ArrayList<>();
        for (License license : licenseList) {
            resultList.add(license.getFullname());
        }
        return resultList;
    }

    public static Map<String, License> getStringLicenseMap(User user, Set<String> licenseIds) {
        Map<String, License> idToLicense;

        try {
            final List<License> licenses = getLicenses(licenseIds, user.getDepartment());
            idToLicense = ThriftUtils.getIdMap(licenses);
        } catch (TException e) {
            idToLicense = Collections.emptyMap();
        }
        return idToLicense;
    }

    public static String fieldValueAsString(Object fieldValue) {
        if(fieldValue == null){
            return "";
        }
        if (fieldValue instanceof TEnum) {
            return nullToEmpty(ThriftEnumUtils.enumToString((TEnum) fieldValue));
        }
        if (fieldValue instanceof String) {
            return nullToEmpty((String) fieldValue);
        }
        if (fieldValue instanceof Map) {
            List<String> mapEntriesAsStrings = nullToEmptyMap(((Map<String, Object>) fieldValue)).entrySet().stream()
                    .map(e -> {
                        String valueString = e.getValue() != null ? e.getValue().toString():"";
                        return e.getKey() + " : " + valueString;
                    })
                    .collect(Collectors.toList());
            return joinStrings(mapEntriesAsStrings);
        }
        if (fieldValue instanceof Iterable){
            return joinStrings((Iterable<String>) fieldValue);
        }
        return fieldValue.toString();
    }

    public static String displayNameFor(String name, Map<String, String> nameToDisplayName){
        return nameToDisplayName.containsKey(name)? nameToDisplayName.get(name) : name;
    }

    public static <T> Map<String, T> putReleaseNamesInMap(Map<String, T> map, List<Release> releases) {
        if(map == null || releases == null) {
            return Collections.emptyMap();
        }
        Map<String, T> releaseNamesMap = new HashMap<>();
        releases.stream()
                .forEach(r -> releaseNamesMap.put(printName(r),map.get(r.getId())));
        return releaseNamesMap;
    }

    public static <T> Map<String, T> putProjectNamesInMap(Map<String, T> map, List<Project> projects) {
        if(map == null || projects == null) {
            return Collections.emptyMap();
        }
        Map<String, T> projectNamesMap = new HashMap<>();
        projects.stream()
                .forEach(p -> projectNamesMap.put(printName(p),map.get(p.getId())));
        return projectNamesMap;
    }

    public static List<String> getReleaseNames(List<Release> releases) {
        if (releases == null) return Collections.emptyList();
        return releases.stream().map(SW360Utils::printName).collect(Collectors.toList());
    }
}
