/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.bosch.osmi.sw360.bdp.datasink.thrift;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import com.siemens.sw360.datahandler.TestUtils;
import com.siemens.sw360.datahandler.common.DatabaseSettings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.siemens.sw360.datahandler.thrift.ThriftClients;
import com.siemens.sw360.datahandler.thrift.components.ComponentService;
import com.siemens.sw360.datahandler.thrift.users.UserService;

public class ThriftApiSimpleTest {

	private static final String dbName = DatabaseSettings.COUCH_DB_DATABASE;
	
	private ThriftApi thriftApi;

	@Before
	public void setUp() throws Exception {
		TestUtils.createDatabase(DatabaseSettings.getConfiguredHttpClient(), dbName);

		thriftApi = new ThriftApiSimple();
	}

	@After
	public void tearDown() throws Exception {
		TestUtils.deleteDatabase(DatabaseSettings.getConfiguredHttpClient(), dbName);
	}
	
	@Test
	public void testUserClient() {
		UserService.Iface userClient = thriftApi.getUserClient();
		assertThat(userClient, is(notNullValue()));
	}
	
	@Test
	public void testComponentClient() {
		ComponentService.Iface componentClient = thriftApi.getComponentClient();
		assertThat(componentClient, is(notNullValue()));
	}
	
	@Test
	public void testThriftClients() {
		ThriftClients thriftClients = thriftApi.getThriftClients();
		assertThat(thriftClients, is(notNullValue()));
	}
	
	@Test
	public void testProjectClient() {
		com.siemens.sw360.datahandler.thrift.projects.ProjectService.Iface projectClient = thriftApi.getProjectClient();
		assertThat(projectClient, is(notNullValue()));
	}
	
	@Test
	public void testVendorClient() {
		com.siemens.sw360.datahandler.thrift.vendors.VendorService.Iface vendorClient = thriftApi.getVendorClient();
		assertThat(vendorClient, is(notNullValue()));
	}

}
