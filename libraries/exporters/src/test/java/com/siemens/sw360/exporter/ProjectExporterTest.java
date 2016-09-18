/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.exporter;

import com.siemens.sw360.datahandler.thrift.components.ComponentService;
import com.siemens.sw360.datahandler.thrift.projects.ProjectService;
import com.siemens.sw360.datahandler.thrift.users.User;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by heydenrb on 06.11.15.
 */
public class ProjectExporterTest {
    @Mock
    ComponentService.Iface componentClient;

    @Mock
    ProjectService.Iface projectClient;

    @Mock
    User user;

    @Test
    public void testEveryRenderedProjectFieldHasAHeader() throws Exception {
        ProjectExporter exporter = new ProjectExporter(componentClient,
                projectClient, user, false);
        assertThat(exporter.PROJECT_RENDERED_FIELDS.size(), is(exporter.HEADERS.size()));
    }
}
