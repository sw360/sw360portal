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

package com.bosch.osmi.bdp.access.test;

import com.bosch.osmi.bdp.access.impl.BdpApiAccessImpl;
import com.bosch.osmi.bdp.access.impl.util.Util;
import org.junit.Ignore;

import java.io.*;
import java.util.Properties;


/**
 * Created by muj1be on 11/17/15.
 */
@Ignore("This Test will not run, if connection to BDP Server could not be established.")
public class BdpApiAccessImplSystemTest extends BdpApiAccessSystemTest {

    public static final String SERVER_URL = "https://url.to-bdp-server.com";
    private static String user;
    private static String password;

    @org.junit.BeforeClass
    public static void readProperties() throws IOException {
        Properties props = Util.readBdpAccessImplProperties();
        user = props.getProperty("user");
        password = props.getProperty("password");
    }

    @org.junit.Before
    public void setup(){
        System.out.println("Run tests in " + this.getClass().getCanonicalName());
        access = new BdpApiAccessImpl(SERVER_URL, user, password);
    }


    @org.junit.Test
    public void testGetProjectHandles() throws Exception {
        super.testGetProjectHandles();
    }
}
