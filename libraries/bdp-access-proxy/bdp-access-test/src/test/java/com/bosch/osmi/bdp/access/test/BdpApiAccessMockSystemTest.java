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