/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.bosch.osmi.sw360.bdp.entitytranslation;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class BdpLicenseToSw360LicenseTranslatorTest {

	private com.siemens.sw360.datahandler.thrift.licenses.License license;
	private com.bosch.osmi.bdp.access.api.model.License licenseBdp;
	
	@Before
	public void setUp() {
		licenseBdp = new com.bosch.osmi.bdp.access.api.model.License() {
			
			@Override
			public String getText() {
				return "bdpLicenseText";
			}
			
			@Override
			public String getName() {
				return "bdpLicenseName";
			}
			
			@Override
			public String getId() {
				return "bdpLicenseId";
			}

			@Override
			public boolean hasLicenseTextAttached() {
				return false;
			}
		};
		license = new BdpLicenseToSw360LicenseTranslator().apply(licenseBdp);
		
		assertThat(license, is(notNullValue()));
	}

	@Test
	public void testGetFullName() {
		assertThat(license.getFullname(), is(licenseBdp.getName()));
	}
	
	@Test
	public void testShortName() {
		assertThat(license.getShortname(), is(licenseBdp.getId()));
	}
	
	@Test
	public void testGetText() {
		assertThat(license.getText(), is(licenseBdp.getText()));
	}

}
