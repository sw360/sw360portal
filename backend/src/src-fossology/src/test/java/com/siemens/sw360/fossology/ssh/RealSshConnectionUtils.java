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
package com.siemens.sw360.fossology.ssh;

import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.Session;
import com.siemens.sw360.fossology.config.FossologySettings;
import com.siemens.sw360.fossology.db.FossologyFingerPrintRepository;
import com.siemens.sw360.fossology.ssh.keyrepo.FossologyHostKeyRepository;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assume.assumeNoException;
import static org.junit.Assume.assumeThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author daniele.fognini@tngtech.com
 */
public class RealSshConnectionUtils {

    public static void assumeCanOpenSshSessions() {
        JSchSessionProvider trustingJSchSessionProvider = getTrustingJSchSessionFactory();
        try {
            assumeCanOpenSshSessions(trustingJSchSessionProvider);
        } finally {
            try {
                trustingJSchSessionProvider.destroy();
            } catch (Exception e) {
                assumeNoException(e);
            }
        }
    }

    static void assumeCanOpenSshSessions(JSchSessionProvider jSchSessionProvider) {
        try {
            Session session = jSchSessionProvider.getSession(2000);
            assumeThat(session, notNullValue());
            jSchSessionProvider.closeSession(session);
        } catch (Exception e) {
            assumeNoException(e);
        }
    }

    static JSchSessionProvider getTrustingJSchSessionFactory() {
        FossologyFingerPrintRepository keyConnector = mock(FossologyFingerPrintRepository.class);
        FossologyHostKeyRepository hostKeyRepository = spy(new FossologyHostKeyRepository(keyConnector));

        doReturn(HostKeyRepository.OK).when(hostKeyRepository).check(anyString(), any(byte[].class));
        return new JSchSessionProvider(new FossologySettings(), hostKeyRepository);
    }
}
