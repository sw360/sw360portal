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
package com.bosch.osmi.sw360.cvesearch.service;

import com.bosch.osmi.sw360.cvesearch.datasink.VulnerabilityConnector;
import com.bosch.osmi.sw360.cvesearch.datasource.CveSearchApiImpl;
import com.bosch.osmi.sw360.cvesearch.datasource.CveSearchData;
import com.bosch.osmi.sw360.cvesearch.datasource.CveSearchWrapper;
import com.bosch.osmi.sw360.cvesearch.entitytranslation.CveSearchDataToVulnerabilityTranslator;
import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.components.Component;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.cvesearch.CveSearchService;
import com.siemens.sw360.datahandler.thrift.cvesearch.UpdateType;
import com.siemens.sw360.datahandler.thrift.cvesearch.VulnerabilityUpdateStatus;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.vulnerabilities.Vulnerability;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import java.io.IOException;
import java.util.*;

import static com.bosch.osmi.sw360.cvesearch.helper.VulnerabilityUtils.*;
import static com.siemens.sw360.datahandler.common.CommonUtils.*;

public class CveSearchHandler implements CveSearchService.Iface {

    VulnerabilityConnector vulnerabilityConnector;
    CveSearchWrapper cveSearchWrapper;
    Logger log = Logger.getLogger(CveSearchHandler.class);


    public CveSearchHandler() {
        try {
            vulnerabilityConnector = new VulnerabilityConnector();
        } catch (IOException ioe) {
            log.error("Exception when creating CveSearchHandler", ioe);
        }
        cveSearchWrapper = new CveSearchWrapper(new CveSearchApiImpl("https://cve.circl.lu"));
    }

    public VulnerabilityUpdateStatus updateForRelease(Release release) {
        List<CveSearchData> cveSearchDatas = cveSearchWrapper.searchForRelease(release);
        List<Vulnerability> vulnerabilities = new CveSearchDataToVulnerabilityTranslator().applyToMany(cveSearchDatas);

        Map<UpdateType, List<Vulnerability>> statusToVulnerabilities = vulnerabilityConnector.addOrUpdateVulnerabilitiesAndSetIds(vulnerabilities);
        VulnerabilityUpdateStatus status = getUpdateStatusFromUpdateMap(statusToVulnerabilities);

        RequestStatus relationStatus = vulnerabilityConnector.addReleaseVulnerabilityRelationsIfNecessary(release.getId(),
                successIdsFromUpdateMap(statusToVulnerabilities));
        return status.setRequestStatus(reduceRequestStatus(status.getRequestStatus(), relationStatus));
    }

    @Override
    public VulnerabilityUpdateStatus updateForRelease(String releaseId) {
        Optional<Release> release = vulnerabilityConnector.getRelease(releaseId);
        Optional<VulnerabilityUpdateStatus> updateStatus = release.map(this::updateForRelease);
        return updateStatus.orElse(getEmptyVulnerabilityUpdateStatus(RequestStatus.FAILURE));
    }

    @Override
    public VulnerabilityUpdateStatus updateForComponent(String componentId) throws TException {
        Optional<Component> component = vulnerabilityConnector.getComponent(componentId);

        return component.map(
                c -> c.isSetReleaseIds()
                        ? c.getReleaseIds().stream()
                                .map(this::updateForRelease)
                                .reduce(getEmptyVulnerabilityUpdateStatus(),
                                        (r1, r2) -> reduceVulnerabilityUpdateStatus(r1,r2))
                        : getEmptyVulnerabilityUpdateStatus()
            ).orElse(getEmptyVulnerabilityUpdateStatus(RequestStatus.FAILURE));
    }

    @Override
    public VulnerabilityUpdateStatus updateForProject(String projectId) throws TException {
        Optional<Project> project = vulnerabilityConnector.getProject(projectId);

        return project.map(
                r -> r.isSetReleaseIdToUsage()
                        ? r.getReleaseIdToUsage().keySet().stream()
                                .map(this::updateForRelease)
                                .reduce(getEmptyVulnerabilityUpdateStatus(),
                                    (r1, r2) -> reduceVulnerabilityUpdateStatus(r1,r2))
                        : getEmptyVulnerabilityUpdateStatus()
            ).orElse(getEmptyVulnerabilityUpdateStatus(RequestStatus.FAILURE));
    }

    @Override
    public VulnerabilityUpdateStatus fullUpdate() throws TException {
        List<Release> allReleases = vulnerabilityConnector.getAllReleases();

        return allReleases.stream()
                .map(this::updateForRelease)
                .reduce(getEmptyVulnerabilityUpdateStatus(),
                        (r1, r2) -> reduceVulnerabilityUpdateStatus(r1,r2));
    }

    @Override
    public Set<String> findCpes(String vendor, String product, String version) throws TException {
        return null;
    }
}
