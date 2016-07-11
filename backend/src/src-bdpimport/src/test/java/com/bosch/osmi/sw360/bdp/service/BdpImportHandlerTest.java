/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
