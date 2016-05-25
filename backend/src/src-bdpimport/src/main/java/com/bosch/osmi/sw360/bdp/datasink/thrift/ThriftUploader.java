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

import com.bosch.osmi.bdp.access.api.model.ProjectInfo;
import com.bosch.osmi.sw360.bdp.datasource.BdpApiAccessWrapper;
import com.bosch.osmi.sw360.bdp.entitytranslation.BdpComponentToSw360ComponentTranslator;
import com.bosch.osmi.sw360.bdp.entitytranslation.BdpComponentToSw360ReleaseTranslator;
import com.bosch.osmi.sw360.bdp.entitytranslation.BdpLicenseToSw360LicenseTranslator;
import com.bosch.osmi.sw360.bdp.entitytranslation.BdpProjectInfoToSw360ProjectTranslator;
import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.components.Component;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.licenses.License;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.thrift.importstatus.ImportStatus;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

// TODO: this contains logic and knowledge of the inner relations => move them to entitytranslation?
public class ThriftUploader {

    private static final Logger logger = Logger.getLogger(ThriftUploader.class);

    private ThriftExchange thriftExchange;
    private BdpApiAccessWrapper bdpApiAccessWrapper;

    public ThriftUploader(ThriftExchange thriftExchange, BdpApiAccessWrapper bdpApiAccessWrapper) {
        this.thriftExchange = thriftExchange;
        this.bdpApiAccessWrapper = bdpApiAccessWrapper;
    }

    protected <T> Optional<String> searchExistingEntityId(Optional<List<T>> nomineesOpt, Function<T, String> idExtractor, String nameBdp, String nameSW360) {
        return nomineesOpt.flatMap(
                nominees -> {
                    Optional<String> nomineeId = nominees.stream()
                            .findFirst()
                            .map(idExtractor);
                    if (nomineeId.isPresent()) {
                        logger.info(nameBdp + " to import matches a " + nameSW360 + " with id: " + nomineeId.get());
                        nominees.stream()
                                .skip(1)
                                .forEach(n -> logger.error(nameBdp + " to import would also match a " + nameSW360 + " with id: " + idExtractor.apply(n)));
                    }
                    return nomineeId;
                }
        );
    }

    protected <T> Optional<String> searchExistingEntityId(Optional<List<T>> nomineesOpt, Function<T, String> idExtractor, String name) {
        return searchExistingEntityId(nomineesOpt, idExtractor, name, name);
    }

    protected String getOrCreateLicenseId(com.bosch.osmi.bdp.access.api.model.License licenseBdp, User user) {
        logger.info("Try to import bdp License: " + licenseBdp.getName());

        Optional<String> potentialLicenseId = searchExistingEntityId(thriftExchange.searchLicenseByBdpId(licenseBdp.getId()),
                License::getId,
                "License");
        if (potentialLicenseId.isPresent()) {
            return potentialLicenseId.get();
        } else {
            License licenseSW360 = new BdpLicenseToSw360LicenseTranslator().apply(licenseBdp);
            String licenseId = thriftExchange.addLicense(licenseSW360, user);
            logger.info("Imported license: " + licenseId);
            return licenseId;
        }
    }

    protected String getOrCreateComponent(com.bosch.osmi.bdp.access.api.model.Component componentBdp, User user) {
        logger.info("Try to import bdp Component: " + componentBdp.getName());

        Optional<String> potentialReleaseId = searchExistingEntityId(thriftExchange.searchReleaseByNameAndVersion(componentBdp.getName(),
                componentBdp.getComponentVersion()),
                Release::getId,
                "Component",
                "Release");
        if (potentialReleaseId.isPresent()) {
            return potentialReleaseId.get();
        }

        Release releaseSW360 = new BdpComponentToSw360ReleaseTranslator().apply(componentBdp);
        releaseSW360.getModerators().add(user.getEmail());

        Optional<String> potentialComponentId = searchExistingEntityId(thriftExchange.searchComponentByName(componentBdp.getName()),
                Component::getId,
                "Component");
        String componentId;
        if (potentialComponentId.isPresent()) {
            componentId = potentialComponentId.get();
        } else {
            Component componentSW360 = new BdpComponentToSw360ComponentTranslator().apply(componentBdp);
            componentId = thriftExchange.addComponent(componentSW360, user);
        }
        releaseSW360.setComponentId(componentId);

        String licenseId = getOrCreateLicenseId(componentBdp.getLicense(), user);
        releaseSW360.setMainLicenseIds(new HashSet<>(Collections.singletonList(licenseId)));

        return thriftExchange.addRelease(releaseSW360, user);
    }

    protected Set<String> getOrCreateComponents(ProjectInfo projectBdp, User user) {
        Collection<com.bosch.osmi.bdp.access.api.model.Component> componentsBdp = projectBdp.getProject().getComponents();
        if (componentsBdp == null) {
            return Collections.EMPTY_SET;
        }

        Set<String> releaseIds = componentsBdp.stream()
                .map(c -> getOrCreateComponent(c, user))
                .collect(Collectors.toSet());

        if (releaseIds.size() != componentsBdp.size()) {
            logger.warn("expected to get " + componentsBdp.size() + " different ids of releases but got " + releaseIds.size());
        } else {
            logger.info("The expected number of releases was imported.");
        }

        return releaseIds;
    }

    protected Optional<String> createProject(String bdpId, User user) throws TException  {
        logger.info("Try to import bdp Project: " + bdpId);
        logger.info("Sw360-User: " + user.email);
        logger.info("Remote-Credentials: " + bdpApiAccessWrapper.getEmailAddress());

        com.bosch.osmi.bdp.access.api.model.ProjectInfo projectBdp = bdpApiAccessWrapper.getProjectInfo(bdpId);
        if (projectBdp == null) {
            logger.error("Unable to get Project from BDP Server named: " + bdpId);
            return Optional.empty();
        }

        String bdpName = projectBdp.getProjectName();
        if (getProjectId(bdpId, bdpName, user).isPresent()) {
            logger.error("Project already in database: " + bdpId);
            return Optional.empty();
        }

        Set<String> releaseIds = getOrCreateComponents(projectBdp, user);

        Project projectSW360 = new BdpProjectInfoToSw360ProjectTranslator().apply(projectBdp);
        projectSW360.setProjectResponsible(user.getEmail());
        projectSW360.setReleaseIdToUsage(releaseIds.stream()
                .collect(Collectors.toMap(Function.identity(), e -> "contained")));

        return Optional.ofNullable(thriftExchange.addProject(projectSW360, user));
    }

    protected Optional<String> getProjectId(String bdpId, String bdpName, User user) throws TException  {

        Project existingProject = thriftExchange.getAccessibleProjectByBdpId(bdpId, user);
        if (existingProject != null) {
            logger.info("Project to import was already imported with bdpId: " + bdpId);
            return Optional.ofNullable(existingProject.getId());
        }
        existingProject = thriftExchange.getAccessibleProject(bdpName, user);
        if (existingProject != null) {
            logger.info("Project to import already exists in the DB with name: " + bdpName);
            return Optional.ofNullable(existingProject.getId());
        }
        return Optional.empty();
    }

    public ImportStatus importBdpProjects(Collection<String> bdpProjectIds, User user) {
        List<String> failedIds = new ArrayList<>();
        List<String> successfulIds = new ArrayList<>();
        ImportStatus bdpImportStatus = new ImportStatus().setRequestStatus(RequestStatus.SUCCESS);

        for (String bdpId : bdpProjectIds) {
            Optional<String> projectId = Optional.empty();
            try{
                projectId = createProject(bdpId, user);
            } catch (TException e){
                logger.error("Error when creating the project", e);
                bdpImportStatus.setRequestStatus(RequestStatus.FAILURE);
                return bdpImportStatus;
            }
            if (!projectId.isPresent()) {
                logger.error("Could not import project with bdpId: " + bdpId);
                failedIds.add(bdpId);
            } else {
                successfulIds.add(bdpId);
            }
        }
        return bdpImportStatus
                .setFailedIds(failedIds)
                .setSuccessfulIds(successfulIds);
    }
}
