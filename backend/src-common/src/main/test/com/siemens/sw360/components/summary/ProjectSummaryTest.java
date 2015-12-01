/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
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
package com.siemens.sw360.components.summary;

import com.google.common.collect.ImmutableSet;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.projects.ProjectState;
import org.junit.Test;

import java.util.List;

import static com.siemens.sw360.exporter.ProjectExporter.RENDERED_FIELDS;
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