/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.bosch.osmi.bdp.access.test;

import com.bosch.osmi.bdp.access.impl.BdpApiAccessImpl;
import com.bosch.osmi.bdp.access.impl.util.Util;
import org.junit.Ignore;

import java.io.*;
import java.util.Properties;


/**
 * Created by johannes.kristan@bosch-si.com on 11/17/15.
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
