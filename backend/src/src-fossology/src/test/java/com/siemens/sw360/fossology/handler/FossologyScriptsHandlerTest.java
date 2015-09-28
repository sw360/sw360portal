/*
 * Copyright Siemens AG, 2015. Part of the SW360 Portal Project.
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
package com.siemens.sw360.fossology.handler;

import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.fossology.ssh.FossologyUploader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FossologyScriptsHandlerTest {

    FossologyScriptsHandler fossologyScriptsHandler;

    @Mock
    FossologyUploader fossologyUploader;

    @Before
    public void setUp() throws Exception {
        fossologyScriptsHandler = new FossologyScriptsHandler(fossologyUploader);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(fossologyUploader);
    }

    @Test
    public void testDeployScriptsCanOpenAllResourceFiles() throws Exception {
        when(fossologyUploader.copyToFossology(anyString(), any(InputStream.class), anyBoolean())).thenReturn(true);
        assertThat(fossologyScriptsHandler.deployScripts(), is(RequestStatus.SUCCESS));

        verify(fossologyUploader, times(5)).copyToFossology(anyString(), any(InputStream.class), anyBoolean());

    }

    @Test
    public void testDeployScriptsReportsErrorOnUploadError() throws Exception {
        when(fossologyUploader.copyToFossology(anyString(), any(InputStream.class), anyBoolean())).thenReturn(true, false);
        assertThat(fossologyScriptsHandler.deployScripts(), is(RequestStatus.FAILURE));

        verify(fossologyUploader, times(5)).copyToFossology(anyString(), any(InputStream.class), anyBoolean());

    }
}