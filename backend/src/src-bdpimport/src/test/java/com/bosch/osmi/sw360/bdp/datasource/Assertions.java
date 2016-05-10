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

import com.bosch.osmi.bdp.access.api.model.Component;
import com.bosch.osmi.bdp.access.api.model.License;
import com.bosch.osmi.bdp.access.api.model.Project;
import com.bosch.osmi.bdp.access.api.model.ProjectInfo;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Contains common assertion for BDP Api Tests.
 */
public class Assertions {

	public static void assertProjectInfo(ProjectInfo projectInfo, String projectIdExpected, String projectNameExpected) {
		assertThat(projectInfo, is(notNullValue()));
		assertThat(projectInfo.getProjectId(), is(projectIdExpected));
		assertThat(projectInfo.getProjectName(), is(projectNameExpected));
	}
	
	public static void assertProjectInfo(ProjectInfo projectInfo) {
		assertProjectInfo(projectInfo, "c_bdp-api-access_8104", "bdp-api-access");
	}

	public static void assertProject(ProjectInfo projectInfo, String projectNameExpected) {
		assertThat(projectInfo, is(notNullValue()));
		assertThat(projectInfo.getProjectName(), is(projectNameExpected));
	}
	
	public static void assertProject(ProjectInfo projectInfo) {
		assertProject(projectInfo, "bdp-api-access");
	}

	public static void assertComponent(Component component, String componentNameExpected) {
		assertThat(component, is(notNullValue()));
		assertThat(component.getName(), is(componentNameExpected));
	}

	public static void assertComponent(Component component) {
		assertComponent(component, "sails-hook-permit-actions");
	}
	
	public static void assertLicense(License license) {
		assertThat(license, is(notNullValue()));
		assertThat(license.getId(), is("mit2"));
		assertThat(license.getName(), is("MIT License"));
		assertThat(license.getText(), is(notNullValue()));
	}
		
	public static <T> T firstOf(Collection<T> elements) {
		@SuppressWarnings("unchecked")
		List<T> allElements = Arrays.asList(elements.toArray((T[])new Object[0]));
		T element = allElements.get(0);
		return element;
	}

}
