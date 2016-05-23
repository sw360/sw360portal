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
package com.bosch.osmi.sw360.bdp.service;

import com.bosch.osmi.sw360.bdp.datasource.Assertions;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Attention:
 * In order to run test sw360 back-end has to be online.
 * Leaving dirty context on DB (should be re-factored).
 */
public class BdpImportHandlerTest {

	private BdpImportHandler bdpImportHandler;

	@Before
	public void setUp() throws Exception {
		// TODO: use `TestUtils` to generate a testing environment. See ProjectHandlerTest

		bdpImportHandler = new BdpImportHandler();
	}

	@Ignore("This test modifies the database and would run against a production version if a config file exists.")
	@Test
	public void testLoadImportables() {
		List<Project> loadImportables = bdpImportHandler.loadImportables(null);
		Collections.sort(loadImportables, Comparator.comparing(Project::getName).reversed());
		assertThat(loadImportables.size(), is(2));
		
		Project project = Assertions.firstOf(loadImportables);
		assertThat(project.getName(), is("bdp-api-access"));
	}

	@Ignore ("Does not work, if no user is registered in DB. How to get/create a test user?")
	@Test
	public void testImportDatasources() throws TException {
		bdpImportHandler.importDatasources(Arrays.asList("INST-ESY-zlib-test-projectname"), null, null);
	}

}
