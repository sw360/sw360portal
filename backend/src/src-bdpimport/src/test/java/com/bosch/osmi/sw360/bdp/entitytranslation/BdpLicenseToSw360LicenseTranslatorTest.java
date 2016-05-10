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
