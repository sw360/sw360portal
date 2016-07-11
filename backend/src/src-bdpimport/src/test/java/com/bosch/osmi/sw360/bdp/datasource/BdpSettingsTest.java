/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
