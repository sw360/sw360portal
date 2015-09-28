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
package com.siemens.sw360.fossology.config;

import com.jcraft.jsch.JSch;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class FossologySettingsTest {

    FossologySettings fossologySettings;

    @Before
    public void setUp() throws Exception {
        fossologySettings = new FossologySettings();
    }

    @Test
    public void testGetFossologyConnectionTimeout() throws Exception {
        final long connectionTimeout = fossologySettings.getFossologyConnectionTimeout();
        assertThat(connectionTimeout, is(greaterThan(100L)));
        assertThat(connectionTimeout, is(lessThan(100000L)));
    }

    @Test
    public void testGetFossologyExecutionTimeout() throws Exception {
        final long executionTimeout = fossologySettings.getFossologyExecutionTimeout();
        assertThat(executionTimeout, is(greaterThan(1000L)));
        assertThat(executionTimeout, is(lessThan(1000000L)));
    }

    @Test
    public void testGetFossologyHost() throws Exception {
        final String fossologyHost = fossologySettings.getFossologyHost();
        assertThat(fossologyHost, not(isEmptyOrNullString()));
    }

    @Test
    public void testGetFossologyPort() throws Exception {
        final int fossologySshUsername = fossologySettings.getFossologyPort();
        assertThat(fossologySshUsername, is(greaterThan(0)));
        assertThat(fossologySshUsername, is(lessThan(65536)));
    }

    @Test
    public void testGetFossologySshUsername() throws Exception {
        final String fossologySshUsername = fossologySettings.getFossologySshUsername();
        assertThat(fossologySshUsername, not(isEmptyOrNullString()));
    }

    @Test
    public void testKeyIsAValidPrivateKey() throws Exception {
        final String msg = /* this tests that the */ "Private key defined in property files" /* is valid */;

        final byte[] fossologyPrivateKey = fossologySettings.getFossologyPrivateKey();

        assertThat(msg + "is not readable",
                fossologyPrivateKey, notNullValue());

        assertThat(msg + "is empty",
                fossologyPrivateKey.length, is(greaterThan(0)));

        try {
            final JSch jSch = new JSch();
            jSch.addIdentity("test", fossologyPrivateKey, null, null);
        } catch (Exception e) {
            fail(msg + "is not a valid private key");
        }
    }
}