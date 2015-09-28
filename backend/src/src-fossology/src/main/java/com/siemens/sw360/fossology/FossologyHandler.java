/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
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
package com.siemens.sw360.fossology;

import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.fossology.FossologyHostFingerPrint;
import com.siemens.sw360.datahandler.thrift.fossology.FossologyService;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.fossology.config.FossologySettings;
import com.siemens.sw360.fossology.handler.FossologyFileHandler;
import com.siemens.sw360.fossology.handler.FossologyHostKeyHandler;
import com.siemens.sw360.fossology.handler.FossologyScriptsHandler;
import com.siemens.sw360.fossology.ssh.FossologySshConnector;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Implementation of the Thrift service
 *
 * @author cedric.bodet@tngtech.com
 * @author daniele.fognini@tngtech.com
 * @author johannes.najjar@tngtech.com
 */
@Component
public class FossologyHandler implements FossologyService.Iface {
    private final FossologyFileHandler fossologyFileHandler;
    private final FossologyHostKeyHandler fossologyHostKeyHandler;
    private final FossologySshConnector fossologySshConnector;
    private final FossologyScriptsHandler fossologyScriptsHandler;
    private final byte[] fossologyPubKey;

    @Autowired
    public FossologyHandler(FossologyFileHandler fossologyFileHandler, FossologyHostKeyHandler fossologyHostKeyHandler, FossologySshConnector fossologySshConnector, FossologyScriptsHandler fossologyScriptsHandler, FossologySettings fossologySettings) {
        this.fossologyFileHandler = fossologyFileHandler;
        this.fossologyHostKeyHandler = fossologyHostKeyHandler;
        this.fossologySshConnector = fossologySshConnector;
        this.fossologyScriptsHandler = fossologyScriptsHandler;
        this.fossologyPubKey = fossologySettings.getFossologyPublicKey();
    }

    @Override
    public RequestStatus sendToFossology(String releaseId, User user, String clearingTeam) throws TException {
        return fossologyFileHandler.sendToFossology(releaseId, user, clearingTeam);
    }

    @Override
    public RequestStatus sendReleasesToFossology(List<String> releaseIds, User user, String clearingTeam) throws TException {
        for (String releaseId : releaseIds) {
            RequestStatus requestStatus = sendToFossology(releaseId, user, clearingTeam);
            if (requestStatus != RequestStatus.SUCCESS) return requestStatus;
        }
        return RequestStatus.SUCCESS;
    }

    @Override
    public Release getStatusInFossology(String releaseId, User user, String clearingTeam) throws TException {
        return fossologyFileHandler.getStatusInFossology(releaseId, user, clearingTeam);
    }

    @Override
    public List<FossologyHostFingerPrint> getFingerPrints() throws TException {
        return fossologyHostKeyHandler.getFingerPrints();
    }

    @Override
    public RequestStatus setFingerPrints(List<FossologyHostFingerPrint> fingerPrints) throws TException {
        return fossologyHostKeyHandler.setFingerPrints(fingerPrints);
    }

    @Override
    public RequestStatus deployScripts() throws TException {
        return fossologyScriptsHandler.deployScripts();
    }

    @Override
    public RequestStatus checkConnection() throws TException {
        return fossologySshConnector.runInFossologyViaSsh("exit 2") == 2 ? RequestStatus.SUCCESS : RequestStatus.FAILURE;
    }

    @Override
    public String getPublicKey() throws TException {
        return new String(fossologyPubKey);
    }
}
