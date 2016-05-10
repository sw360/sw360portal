/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
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

package com.bosch.osmi.bdp.access.impl;

import com.blackducksoftware.sdk.protex.client.util.ProtexServerProxy;
import com.blackducksoftware.sdk.protex.client.util.ServerAuthenticationException;
import com.blackducksoftware.sdk.protex.component.ComponentApi;
import com.blackducksoftware.sdk.protex.license.LicenseApi;
import com.blackducksoftware.sdk.protex.project.ProjectApi;
import com.blackducksoftware.sdk.protex.project.bom.BomApi;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeApi;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.DiscoveryApi;
import com.blackducksoftware.sdk.protex.user.UserApi;
import com.bosch.osmi.bdp.access.api.BdpApiAccess;
import com.bosch.osmi.bdp.access.api.model.User;
import com.bosch.osmi.bdp.access.impl.model.UserImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Allows an access to the Bdp APIs. An access has to be initialized with a username and a password for which
 * a user object can be retrieved. The user object is the actual entry point into the whole object net retrieved
 * from Bdp.
 *
 * @author muj1be
 * @since 11/16/15.
 */
public class BdpApiAccessImpl implements BdpApiAccess {

    private static final Logger LOGGER = LogManager.getLogger(BdpApiAccessImpl.class);

    private static final long CONNECTION_TIME_OUT = 120 * 1000L;
    private final String serverUrl;

    private ProtexServerProxy bdpServerProxy;
    
    private String userName;
    private String password;

    // TODO maybe externalize the user from this class
    User user;

    public BdpApiAccessImpl(String serverUrl, String userName, String password){
        this.serverUrl = serverUrl;
        this.userName = userName;
        this.password = password;
    }

    @Override
    public boolean validateCredentials() {
        try {
            getServerProxy().validateCredentials();
            return true;
        } catch (ServerAuthenticationException e) {
            return false;
        }
    }

    @Override
    public User retrieveUser(){
        if(user == null){
            createUser();
        }
        return user;
    }

    private void createUser(){
        user = new UserImpl(userName, this);
    }

    public ProjectApi getProjectApi(){
        LOGGER.debug("Bdp project api accessed.");
        return getServerProxy().getProjectApi(CONNECTION_TIME_OUT);
    }

    public BomApi getBomApi(){
        LOGGER.debug("Bdp BOM api accessed.");
        return getServerProxy().getBomApi(CONNECTION_TIME_OUT);
    }

    public LicenseApi getLicenseApi(){
        LOGGER.debug("Bdp license api accessed.");
        return getServerProxy().getLicenseApi(CONNECTION_TIME_OUT);
    }

    public UserApi getUserApi(){
        LOGGER.debug("Bdp user api accessed.");
        return getServerProxy().getUserApi(CONNECTION_TIME_OUT);
    }

    public ComponentApi getComponentApi() {
        LOGGER.debug("Bdp component api accessed.");
        return getServerProxy().getComponentApi(CONNECTION_TIME_OUT);
    }

    public DiscoveryApi getDiscoveryApi() {
        LOGGER.debug("Bdp discovery api accessed.");
        return getServerProxy().getDiscoveryApi(CONNECTION_TIME_OUT);
    }

    public CodeTreeApi getCodeTreeApi(){
        LOGGER.debug("Bdp code tree api accessed.");
        return getServerProxy().getCodeTreeApi(CONNECTION_TIME_OUT);
    }

    private void initializeServerProxy(){
        LOGGER.debug("Initializing bdp server proxy for server " + serverUrl);
        bdpServerProxy = new ProtexServerProxy(serverUrl, userName, password, CONNECTION_TIME_OUT);
    }

    private ProtexServerProxy getServerProxy(){
        if (bdpServerProxy == null) {
            initializeServerProxy();
        }
        return bdpServerProxy;
    }
}
