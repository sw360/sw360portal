/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.components.summary;

import com.google.common.collect.ImmutableSet;
import org.eclipse.sw360.datahandler.thrift.projects.Project;
import org.eclipse.sw360.datahandler.thrift.projects.ProjectState;
import org.junit.Test;

import java.util.List;

import static org.eclipse.sw360.exporter.ProjectExporter.RENDERED_FIELDS;
import static org.junit.Assert.assertNotNull;

/**
 * Created by heydenrb on 06.11.15.
 */
public class ProjectSummaryTest {


    @Test
    public void testAllRequiredFieldsAreSet() throws Exception {
        Project project = new Project();
        Project copy = new Project();

        for (Project._Fields renderedField : RENDERED_FIELDS) {
            switch (renderedField) {
                case STATE:
                    project.state = ProjectState.ACTIVE;
                    break;
                case RELEASE_IDS:
                    project.releaseIds = ImmutableSet.of("2","3" );
                    break;
                default: //most fields are string
                    project.setFieldValue(renderedField, "asd");
                    break;
            }
        }


        ProjectSummary.setSummaryFields(project, copy);

        for (Project._Fields renderedField : RENDERED_FIELDS) {
            assertNotNull(copy.getFieldValue(renderedField));
        }


    }
}