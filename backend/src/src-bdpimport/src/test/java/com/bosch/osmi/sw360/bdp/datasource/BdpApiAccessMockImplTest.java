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
package com.bosch.osmi.sw360.bdp.datasource;

import com.bosch.osmi.bdp.access.api.BdpApiAccess;
import com.bosch.osmi.bdp.access.api.model.*;
import com.bosch.osmi.bdp.access.mock.BdpApiAccessMockImpl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Test up data source to receive all users informations.
 */
public class BdpApiAccessMockImplTest {

	private BdpApiAccess bdpApiAccess;
	private static URL jsonFilePath;
	private User user;
	
	@BeforeClass
	public static void setUpBefore() {
		jsonFilePath = BdpApiAccessMockImplTest.class.getResource("/mockdata.json");
		assertThat("File could not be found. Put Json-File /mockdata.json into /src/test/resources and try again.", jsonFilePath, is(notNullValue()));
	}
	
	@Before
	public void setUp() throws Exception {
		bdpApiAccess = new BdpApiAccessMockImpl(jsonFilePath.getFile());	
		user = bdpApiAccess.retrieveUser();
		assertThat(user, is(notNullValue()));
	}

	@Test
	public void testGetEmailAddress() {
		assertThat(user.getEmailAddress(), is("john@example.com"));
	}
	
	@Test
	public void testProjectInfo() {
		Collection<ProjectInfo> projectInfos = user.getProjectInfos();
		assertThat(projectInfos.size(), is(2));
        TreeSet<ProjectInfo> sortedProjectInfo = new TreeSet(Comparator.comparing(ProjectInfo::getProjectName).reversed());
        sortedProjectInfo.addAll(projectInfos);

		ProjectInfo projectInfo = Assertions.firstOf(sortedProjectInfo);
		Assertions.assertProjectInfo(projectInfo);

		Project project = projectInfo.getProject();
		Assertions.assertProject(projectInfo);

		Collection<Component> components = project.getComponents();
        assertThat(components.size(), is(7));
        TreeSet<Component> sortedComponent = new TreeSet(Comparator.comparing(Component::getName).reversed());
        sortedComponent.addAll(components);

		Component component = Assertions.firstOf(sortedComponent);
		Assertions.assertComponent(component);

		License license = component.getLicense();
		Assertions.assertLicense(license);
	}

}
