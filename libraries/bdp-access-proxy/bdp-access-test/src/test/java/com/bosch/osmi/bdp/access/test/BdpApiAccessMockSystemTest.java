/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.bosch.osmi.bdp.access.test;

import com.bosch.osmi.bdp.access.mock.BdpApiAccessMockImpl;

import java.io.FileNotFoundException;

/**
 * @author johannes.kristan@bosch-si.com
 * @since 11/20/15.
 */
public class BdpApiAccessMockSystemTest extends BdpApiAccessSystemTest{


    @org.junit.Before
    public void setup() throws FileNotFoundException {
        System.out.println("Run tests in " + this.getClass().getCanonicalName());
        access = new BdpApiAccessMockImpl(/*System.getProperty("user.home") + File.separator + ".bdp-access-files" + File.separator + "mockdata.json"*/);
    }

    @org.junit.Test
    public void testGetProjectHandles() throws Exception {
        super.testGetProjectHandles();
    }
}