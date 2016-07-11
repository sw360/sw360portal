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

import com.siemens.sw360.datahandler.thrift.components.Component;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.thrift.vendors.Vendor;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Attention:
 * In order to run test sw360 back-end has to be online.
 * Leaving dirty context on DB (should be re-factored).
 */
@Ignore("In order to run test sw360 back-end has to be online. Leaving dirty context on DB (should be re-factored).")
public class ThriftExchangeTest {
	
	private ThriftExchange thriftExchange;
	private User user;

	@Before
	public void setUp() throws Exception {
		thriftExchange = new ThriftExchange(new ThriftApiSimple());
		user = null;
	}

	@Ignore("Does not work, if no user is registered in DB. How to get/create a test user?")
	@Test
	public void testComponentSummary() {
		List<Component> componentSummary = thriftExchange.getComponentSummary(user);
		assertThat(componentSummary, is(notNullValue()));
		assertThat(componentSummary.isEmpty(), is(false));
	}
	
	@Test
	public void testGetAllVendors() {
		List<Vendor> allVendors = thriftExchange.getAllVendors();
		assertThat(allVendors, is(notNullValue()));
		assertThat(allVendors.isEmpty(), is(false));
	}

	@Ignore("Does not work, if no user is registered in DB. How to get/create a test user?")
	@Test
	public void testGetAccessibleProjectsSummary() throws TException {
		List<Project> accessibleProjectsSummary = thriftExchange.getAccessibleProjectsSummary(user);
		assertThat(accessibleProjectsSummary, is(notNullValue()));
		assertThat(accessibleProjectsSummary.isEmpty(), is(false));
	}

	@Ignore("Does not work, if no user is registered in DB. How to get/create a test user?")
	@Test
	public void testGetReleaseSummary() {
		List<Release> releaseSummary = thriftExchange.getReleaseSummary(user);
		assertThat(releaseSummary, is(notNullValue()));
		assertThat(releaseSummary.isEmpty(), is(false));
	}

	@Test
	public void testAddVendor() {
		Vendor vendor = vendorWithRequiredFields();
		String vendorId = thriftExchange.addVendor(vendor);
		assertThat(vendorId, is(notNullValue()));
		assertThat(vendorId.isEmpty(), is(false));
	}

	private Vendor vendorWithRequiredFields() {
		Vendor vendor = new Vendor();
		vendor.setFullname("<BDP Placeholder Vendor Fullname>");
		vendor.setShortname("<BDP Placeholder Vendor Shortname>");
		vendor.setUrl("http://no.data.from.source/");
		return vendor;
	}

	@Ignore("Does not work, if no user is registered in DB. How to get/create a test user?")
	@Test
	public void testAddComponent() {
		Component component = componentWithName();
		String componentId = thriftExchange.addComponent(component, user);
		assertThat(componentId, is(notNullValue()));
		assertThat(componentId.isEmpty(), is(false));
	}

	private Component componentWithName() {
		Component component = new Component();
		component.setName("<BDP Placeholder Component Name>");
		return component;
	}

	@Ignore("Does not work, if no user is registered in DB. How to get/create a test user?")
	@Test
	public void testAddRelease() {
		Release release = releaseWithRequiredFields();
		String releaseId = thriftExchange.addRelease(release, user);
		assertThat(releaseId, is(notNullValue()));
		assertThat(releaseId.isEmpty(), is(false));
	}

	private Release releaseWithRequiredFields() {
		Release release = new Release();
		release.setName("<BDP Placeholder Release Name>");
		release.setVersion("<BDP Placeholder Release Version>");
		Component component = componentWithName();
		String componentId = thriftExchange.addComponent(component, user);
		release.setComponentId(componentId);
		return release;
	}

}
