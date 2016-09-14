/*
 * Copyright (c) Bosch Software Innovations GmbH 2016.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.bosch.osmi.sw360.cvesearch.service;

import com.bosch.osmi.sw360.cvesearch.datasink.VulnerabilityConnector;
import com.bosch.osmi.sw360.cvesearch.datasource.CveSearchApiImpl;
import com.bosch.osmi.sw360.cvesearch.datasource.CveSearchData;
import com.bosch.osmi.sw360.cvesearch.datasource.CveSearchWrapper;
import com.bosch.osmi.sw360.cvesearch.entitytranslation.CveSearchDataTranslator;
import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.components.Component;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.cvesearch.CveSearchService;
import com.siemens.sw360.datahandler.thrift.cvesearch.UpdateType;
import com.siemens.sw360.datahandler.thrift.cvesearch.VulnerabilityUpdateStatus;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.bosch.osmi.sw360.cvesearch.helper.VulnerabilityUtils.*;

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

        Properties props = CommonUtils.loadProperties(CveSearchHandler.class, "/cvesearch.properties");
        String host = props.getProperty("cvesearch.host","https://localhost:5000");

        cveSearchWrapper = new CveSearchWrapper(new CveSearchApiImpl(host));
    }

    public VulnerabilityUpdateStatus updateForRelease(Release release) {
        Optional<List<CveSearchData>> cveSearchDatas = cveSearchWrapper.searchForRelease(release);
        if(!cveSearchDatas.isPresent()) {
            return new VulnerabilityUpdateStatus().setRequestStatus(RequestStatus.FAILURE);
        }

        CveSearchDataTranslator cveSearchDataTranslator = new CveSearchDataTranslator();
        List<CveSearchDataTranslator.VulnerabilityWithRelation> translated = cveSearchDatas.get().stream()
                .map(cveSearchData -> cveSearchDataTranslator.apply(cveSearchData))
                .map(vulnerabilityWithRelation -> {
                    vulnerabilityWithRelation.relation.setReleaseId(release.getId());
                    return vulnerabilityWithRelation;
                })
                .collect(Collectors.toList());

        VulnerabilityUpdateStatus updateStatus = getEmptyVulnerabilityUpdateStatus();
        for (CveSearchDataTranslator.VulnerabilityWithRelation vulnerabilityWithRelation : translated) {
            updateStatus = vulnerabilityConnector.addOrUpdate(vulnerabilityWithRelation.vulnerability,
                    vulnerabilityWithRelation.relation,
                    updateStatus);
        }

        return updateStatus;
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
    public RequestStatus update() throws TException {
        VulnerabilityUpdateStatus vulnerabilityUpdateStatus = fullUpdate();
        log.info("CveSearch update finished with status:" + vulnerabilityUpdateStatus.getRequestStatus());
        log.info("The following vulnerability/ies could not be imported:" + vulnerabilityUpdateStatus.getStatusToVulnerabilityIds().get(UpdateType.FAILED) + "\n"+
                        "The following vulnerability/ies were updated:" + vulnerabilityUpdateStatus.getStatusToVulnerabilityIds().get(UpdateType.UPDATED) + "\n"+
                        "The following vulnerability/ies were added:" + vulnerabilityUpdateStatus.getStatusToVulnerabilityIds().get(UpdateType.NEW));

        return vulnerabilityUpdateStatus.getRequestStatus();
    }

    @Override
    public Set<String> findCpes(String vendor, String product, String version) throws TException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
