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

package com.bosch.osmi.sw360.bdp.datasource;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

@Ignore ("System test, works only after specific setup")
public class BdpSettingsTest {

    @Before
    public void testBDP_FILE() {
        assertThat(BdpSettings.BDP_FILE, is("/etc/sw360/bdp.properties"));
    }

    @Test
    public void testBDP_USER_NAME() {
        assertThat(BdpSettings.BDP_USER_NAME, is("username"));
    }

    @Test
    public void testBDP_SERVER_NAME() {
        assertThat(BdpSettings.BDP_SERVER_NAME, is("https://url.to.server.example"));
    }

    @Test
    public void testBDP_PASSWORD() {
        assertThat(BdpSettings.BDP_PASSWORD, is("password"));
    }

}
