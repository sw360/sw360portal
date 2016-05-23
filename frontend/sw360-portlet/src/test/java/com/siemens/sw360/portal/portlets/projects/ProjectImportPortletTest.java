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
package com.siemens.sw360.portal.portlets.projects;

import com.liferay.portal.kernel.json.JSONObject;
import com.siemens.sw360.datahandler.thrift.bdpimport.RemoteCredentials;
import com.siemens.sw360.portal.common.PortalConstants;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.portlet.PortletSession;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class ProjectImportPortletTest extends TestCase {
    private final String name = "name";
    private final String password = "password";
    private final String newURL = "newURL";

    @Mock
    private ResourceRequest request;

    @Mock
    private ResourceResponse response;

    @Mock
    private PortletSession session;

    @Mock
    private JSONObject responseData;

    @Before
    public void before() {
        when(request.getPortletSession()).thenReturn(session);
    }

    @Test
    public void testUpdateInputSourceWithoutUrl() throws Exception {

        ProjectImportPortlet.LoginState loginState = new ProjectImportPortlet.LoginState();
        RemoteCredentials remoteCredentials = new RemoteCredentials();

        new ProjectImportPortlet().setNewImportSource(
                remoteCredentials,
                session,
                responseData,
                loginState);
        verify(responseData).put(PortalConstants.IMPORT_RESPONSE__STATUS, PortalConstants.IMPORT_RESPONSE__DB_URL_NOTSET);
    }

    @Test
    public void testUpdateInputSourceWithUrlUpdatesResponse() throws Exception {

        ProjectImportPortlet.LoginState loginState = new ProjectImportPortlet.LoginState();
        RemoteCredentials remoteCredentials = new RemoteCredentials();
        remoteCredentials.setUsername(name);
        remoteCredentials.setPassword(password);
        remoteCredentials.setServerUrl(newURL);

        new ProjectImportPortlet().setNewImportSource(
                remoteCredentials,
                session,
                responseData,
                loginState);

        verify(responseData).put(PortalConstants.IMPORT_RESPONSE__STATUS, PortalConstants.IMPORT_RESPONSE__DB_CHANGED);
        verify(responseData).put(PortalConstants.IMPORT_RESPONSE__DBURL, newURL);
    }

    @Test
    public void testUpdateInputSourceWithUrlUpdatesSession() throws Exception {

        ProjectImportPortlet.LoginState loginState = new ProjectImportPortlet.LoginState();
        RemoteCredentials remoteCredentials = new RemoteCredentials();
        remoteCredentials.setUsername(name);
        remoteCredentials.setPassword(password);
        remoteCredentials.setServerUrl(newURL);

        new ProjectImportPortlet().setNewImportSource(
                remoteCredentials,
                session,
                responseData,
                loginState);

        verify(session).setAttribute(PortalConstants.SESSION_IMPORT_USER, name);
        verify(session).setAttribute(PortalConstants.SESSION_IMPORT_PASS, password);
        verify(session).setAttribute(PortalConstants.SESSION_IMPORT_URL, newURL);
    }

}
