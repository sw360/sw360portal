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

import com.bosch.osmi.bdp.access.api.model.License;
import com.siemens.sw360.datahandler.thrift.components.Release;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class BdpComponentToSw360ReleaseTranslatorTest {

	private com.siemens.sw360.datahandler.thrift.components.Component component;
	private com.siemens.sw360.datahandler.thrift.components.Release release;

	private com.bosch.osmi.bdp.access.api.model.Component componentBdp;

	com.bosch.osmi.bdp.access.api.model.Component genTestComponent(String name, String version, String homepage, String releaseDate) {
		return new com.bosch.osmi.bdp.access.api.model.Component() {

			@Override
			public String getName() { return name; }

			@Override
			public License getLicense() { return null; }

			@Override
			public String getComponentVersion() { return version; }

			@Override
			public String getComponentHomePage() { return null; }

			@Override
			public String getComponentComment() { return homepage; }

			@Override
			public String getUsageLevel() { return null; }

			@Override
			public String getComponentKey(){ return null; }

			@Override
			public String getApprovalState(){ return null; }

			@Override
			public String getApprovedBy(){ return null; }

			@Override
			public String getReleaseDate(){ return releaseDate; }
		};
	}

	@Before
	public void setUp() {
		componentBdp = genTestComponent("bdpComponentName", "1.1", "http://component.homepage.org","2014-05-11");

		release = new BdpComponentToSw360ReleaseTranslator().apply(componentBdp);
		component = new BdpComponentToSw360ComponentTranslator().apply(componentBdp);

		assertThat(component, is(notNullValue()));
		assertThat(release, is(notNullValue()));
	}

	@Test
	public void testGetNameComponent() {
		assertThat(component.getName(), is(componentBdp.getName()));
	}

	@Test
	public void testGetNameRelease() {
		assertThat(release.getName(), is(componentBdp.getName()));
	}

	@Test
	public void testGetCreatedBy() {
		assertThat(release.getCreatedBy(), is(nullValue()));
	}
	
	@Test
	public void testGetCreatedOn() {
		assertThat(release.getCreatedOn(), is(nullValue()));
	}

	@Test
	public void testGetVersionNull() {
		com.bosch.osmi.bdp.access.api.model.Component componentBdp2 = genTestComponent("",null,"","");
		Release release2 = new BdpComponentToSw360ReleaseTranslator().apply(componentBdp2);
		assertThat(release2.getVersion(), is("UNKNOWN"));
	}

	@Test
	public void testGetVersion() {
		assertThat(release.getVersion(), is(componentBdp.getComponentVersion()));
	}

	@Test
	public void testGetHomepage() {
		assertThat(component.getHomepage(), is(componentBdp.getComponentHomePage()));
	}

	@Test
	public void testGetBdpId() {
		assertThat(release.getBdpId(), is(componentBdp.getComponentKey()));
	}

	@Test
	public void testGetReleaseDate() {
		assertThat(release.getReleaseDate(), is(componentBdp.getReleaseDate()));
	}

	@Test
	public void testGetLicense() {
		assertThat("Has to be attached afterwards", componentBdp.getLicense(), is(nullValue()));
	}

}
