/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.bosch.osmi.sw360.bdp.service;

import com.bosch.osmi.sw360.bdp.datasink.thrift.ThriftApiSimple;
import com.bosch.osmi.sw360.bdp.datasink.thrift.ThriftExchange;
import com.bosch.osmi.sw360.bdp.datasink.thrift.ThriftUploader;
import com.bosch.osmi.sw360.bdp.datasource.BdpApiAccessWrapperSimple;
import com.bosch.osmi.sw360.bdp.entitytranslation.BdpProjectInfoToSw360ProjectTranslator;
import com.bosch.osmi.sw360.bdp.entitytranslation.TranslationConstants;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.bdpimport.BdpImportService;
import com.siemens.sw360.datahandler.thrift.bdpimport.RemoteCredentials;
import com.siemens.sw360.datahandler.thrift.bdpimportstatus.BdpImportStatus;
import com.siemens.sw360.datahandler.thrift.users.User;
import org.apache.thrift.TException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by maximilian.huber@tngtech.com on 12/2/15.
 *
 * @author maximilian.huber@tngtech.com
 * @author andreas.reichel@tngtech.com
 */
public class BdpImportHandler implements BdpImportService.Iface {

    @Override
    public synchronized BdpImportStatus importDatasources(List<String> bdpProjectIds, User user, RemoteCredentials remoteCredentials) throws TException {
        BdpApiAccessWrapperSimple bdpApiAccessWrapper = new BdpApiAccessWrapperSimple(remoteCredentials);
        ThriftUploader thriftUploader = new ThriftUploader(new ThriftExchange(new ThriftApiSimple()), bdpApiAccessWrapper);

        return thriftUploader.importBdpProjects(bdpProjectIds, user);
    }

    @Override
    public boolean validateCredentials(RemoteCredentials credentials) throws TException {
        return new BdpApiAccessWrapperSimple(credentials).validateCredentials();
    }

    @Override
    public List<Project> loadImportables(RemoteCredentials remoteCredentials) {
        return new BdpApiAccessWrapperSimple(remoteCredentials).getUserProjectInfos()
                .stream()
                .map(new BdpProjectInfoToSw360ProjectTranslator())
                .collect(Collectors.toList());
    }

    @Override
    public String getIdName(){
        return TranslationConstants.BDP_ID;
    }

}
