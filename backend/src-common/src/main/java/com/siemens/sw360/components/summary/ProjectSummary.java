/*
 * Copyright Siemens AG, 2014-2015. Part of the SW360 Portal Project.
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

import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.projects.Project._Fields;

import static com.siemens.sw360.datahandler.thrift.ThriftUtils.copyField;

/**
 * Created by bodet on 17/02/15.
 *
 * @author cedric.bodet@tngtech.com
 */
public class ProjectSummary extends DocumentSummary<Project> {

    @Override
    protected Project summary(SummaryType type, Project document) {
        // Copy required details
        Project copy = new Project();
        copyField(document, copy, _Fields.ID);
        copyField(document, copy, _Fields.NAME);
        copyField(document, copy, _Fields.VERSION);

        switch (type) {
            case EXPORT_SUMMARY:
                setExportSummaryFields(document, copy);
            case SUMMARY:
                setSummaryFields(document, copy);
            default:
                break;
        }

        return copy;
    }

    private static void setSummaryFields(Project document, Project copy) {
        copyField(document, copy, _Fields.DESCRIPTION);
        copyField(document, copy, _Fields.STATE);
        copyField(document, copy, _Fields.PROJECT_RESPONSIBLE);
        // Add release ids
        if (document.isSetReleaseIdToUsage())
            copy.setReleaseIds(document.releaseIdToUsage.keySet());
    }

    private static void setExportSummaryFields(Project document, Project copy) {
        copyField(document, copy, _Fields.CREATED_ON);
        copyField(document, copy, _Fields.CREATED_BY);
        copyField(document, copy, _Fields.LEAD_ARCHITECT);
        copyField(document, copy, _Fields.BUSINESS_UNIT);
    }

}
