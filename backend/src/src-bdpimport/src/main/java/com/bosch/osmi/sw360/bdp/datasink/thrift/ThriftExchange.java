/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.bosch.osmi.sw360.bdp.datasink.thrift;

import com.bosch.osmi.sw360.bdp.entitytranslation.TranslationConstants;
import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.thrift.components.Component;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.licenses.License;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.thrift.vendors.Vendor;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.siemens.sw360.datahandler.common.CommonUtils.nullToEmptyList;

public class ThriftExchange {

    private static final Logger logger = Logger.getLogger(ThriftExchange.class);
    private ThriftApi thriftApi;

    public ThriftExchange(ThriftApi thriftApi) {
        this.thriftApi = thriftApi;
    }

    public List<Vendor> getAllVendors() {
        List<Vendor> allVendors = null;
        try {
            allVendors = getVendorClient().getAllVendors();
        } catch (TException e) {
            logger.error("Could not fetch Vendor list:" + e);
        }
        return allVendors;
    }

    public Project getAccessibleProject(String projectName, User user) throws TException{
        return nullToEmptyList(getAccessibleProjectsSummary(user))
                .stream()
                .collect(Collectors.toMap(Project::getName, Function.identity()))
                .get(projectName);
    }

    public Project getAccessibleProjectByBdpId(String bdpId, User user) throws TException{
        return nullToEmptyList(getAccessibleProjectsSummary(user))
                .stream()
                .filter(project -> project.isSetExternalIds())
                .filter(project -> ! isNullOrEmpty(project.getExternalIds().get(TranslationConstants.BDP_ID)))
                .collect(Collectors.toMap(project -> project.getExternalIds().get(TranslationConstants.BDP_ID), Function.identity()))
                .get(bdpId);
    }

    protected List<Project> getAccessibleProjectsSummary(User user) {
        List<Project> accessibleProjectsSummary = null;
        try {
            accessibleProjectsSummary = getProjectClient().getAccessibleProjectsSummary(user);
        } catch (TException e) {
            logger.error("Could not fetch Project list for user with email=[" + user.getEmail() + "]:" + e);
        }
        return accessibleProjectsSummary;
    }

    private com.siemens.sw360.datahandler.thrift.projects.ProjectService.Iface getProjectClient() {
        return thriftApi.getProjectClient();
    }

    public Optional<List<Release>> searchReleaseByNameAndVersion(String name, String version) {
        List<Release> releases = null;
        try {
            releases = thriftApi.getComponentClient().searchReleaseByName(name);
        } catch (TException e) {
            logger.error("Could not fetch Release list for name=[" + name + "], version=[" + version + "]:" + e);
        }

        if (releases != null) {
            return Optional.of(releases.stream()
                    .filter(r -> r.getVersion().equals(version))
                    .collect(Collectors.toList()));
        } else {
            return Optional.empty();
        }
    }

    public Optional<List<Component>> searchComponentByName(String name) {
        try {
            return Optional.of(thriftApi.getComponentClient().searchComponentForExport(name));
        } catch (TException e) {
            logger.error("Could not fetch Component list for name=[" + name + "]:" + e);
            return Optional.empty();
        }
    }

    public Optional<Component> getComponentById(String componentId, User user) {
        try {
            return Optional.of(getComponentClient().getComponentById(componentId, user));
        } catch (TException e) {
            logger.error("Could not fetch Component for user with email=[" + user.getEmail() + "], id=[" + componentId + "]:" + e);
            return Optional.empty();
        }
    }

    public Optional<List<License>> searchLicenseByName(String name) {
        return getFilteredLicenseList(license -> CommonUtils.nullToEmptyString(license.getFullname()).equals(name),
                "name=[" + name + "]:"
        );
    }

    public Optional<List<License>> searchLicenseByBdpId(String bdpId) {
        return getFilteredLicenseList(license ->
                        license.isSetExternalIds() ?
                                CommonUtils.nullToEmptyString(license.getExternalIds().get(TranslationConstants.BDP_ID)).equals(bdpId) :
                                false,
                "bdpId=[" + bdpId + "]:"
        );
    }

    private Optional<List<License>> getFilteredLicenseList(Predicate<License> filter, String selector) {
        try {
            return Optional.of(thriftApi.getLicenseClient()
                    .getLicenses()
                    .stream()
                    .filter(filter)
                    .collect(Collectors.toList()));
        } catch (TException e) {
            logger.error("Could not fetch License list for " + selector + ": " + e);
            return Optional.empty();
        }
    }

    protected List<Component> getComponentSummary(User user) {
        List<Component> componentSummary = null;
        try {
            componentSummary = getComponentClient().getComponentSummary(user);
        } catch (TException e) {
            logger.error("Could not fetch Component list for user with email=[" + user.getEmail() + "]:" + e);
        }
        return componentSummary;
    }

    protected List<Release> getReleaseSummary(User user) {
        List<Release> releaseSummary = null;
        try {
            releaseSummary = getComponentClient().getReleaseSummary(user);
        } catch (TException e) {
            logger.error("Could not fetch Releasse list for user with email=[" + user.getEmail() + "]:" + e);
        }
        return releaseSummary;
    }

    public List<License> getLicenses() {
        List<License> licenses = null;
        try {
            licenses = thriftApi.getLicenseClient().getLicenses();
        } catch (TException e) {
            logger.error("Could not fetch License list:" + e);
        }
        return licenses;
    }

    private com.siemens.sw360.datahandler.thrift.users.UserService.Iface getUserClient() {
        return thriftApi.getUserClient();
    }

    /**
     * Add the Vendor to DB. Required fields are: fullname, shortname, url.
     *
     * @param vendor Vendor to be added
     * @return VendorId-String from DB.
     */
    public String addVendor(Vendor vendor) {
        String vendorId = null;
        try {
            vendorId = getVendorClient().addVendor(vendor);
        } catch (TException e) {
            logger.error("Could not add Vendor:" + e);
        }
        return vendorId;
    }

    private com.siemens.sw360.datahandler.thrift.vendors.VendorService.Iface getVendorClient() {
        return thriftApi.getVendorClient();
    }

    /**
     * Add the Component to DB. Required fields are: name.
     *
     * @param component Component to be added
     * @param user
     * @return ComponentId-String from DB.
     */
    public String addComponent(Component component, User user) {
        String componentId = null;
        try {
            componentId = getComponentClient().addComponent(component, user);
        } catch (TException e) {
            logger.error("Could not add Component for user with email=[" + user.getEmail() + "]:" + e);
        }
        return componentId;
    }

    /**
     * Add the Release to DB. Required fields are: name, version, componentId.
     *
     * @param release Release to be added
     * @param user
     * @return releaseId-String from DB.
     */
    public String addRelease(Release release, User user) {
        String releaseId = null;
        try {
            releaseId = getComponentClient().addRelease(release, user);
        } catch (TException e) {
            logger.error("Could not add Release for user with email=[" + user.getEmail() + "]:" + e);
        }
        return releaseId;
    }

    public String addProject(Project project, User user) {
        String projectId = null;
        try {
            projectId = thriftApi.getProjectClient().addProject(project, user);
        } catch (TException e) {
            logger.error("Could not add Project for user with email=[" + user.getEmail() + "]:" + e);
        }
        return projectId;
    }

    public String addLicense(License license, User user) {
        List<License> licenses = null;
        try {
            licenses = thriftApi.getLicenseClient().addLicenses(Arrays.asList(license));
        } catch (TException e) {
            logger.error("Could not add License for user with email=[" + user.getEmail() + "]:" + e);
        }
        return licenses == null ? null : licenses.get(0).getId();
    }

    private com.siemens.sw360.datahandler.thrift.components.ComponentService.Iface getComponentClient() {
        return thriftApi.getComponentClient();
    }

}
