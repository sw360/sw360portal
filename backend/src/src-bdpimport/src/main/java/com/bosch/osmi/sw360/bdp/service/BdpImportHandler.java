/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
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
