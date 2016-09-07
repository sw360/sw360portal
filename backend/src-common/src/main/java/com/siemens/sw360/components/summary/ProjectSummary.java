/*
 * Copyright Siemens AG, 2014-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
        copyField(document, copy, _Fields.DESCRIPTION);
        copyField(document, copy, _Fields.VERSION);
        copyField(document, copy, _Fields.CLEARING_TEAM);

        switch (type) {
            case SUMMARY:
                setSummaryFields(document, copy);
            default:
                break;
        }

        return copy;
    }

    protected static void setSummaryFields(Project document, Project copy) {

        for (_Fields field : Project.metaDataMap.keySet()) {
            switch (field) {
                case RELEASE_IDS:
                    if (document.isSetReleaseIdToUsage()) {
                        copy.setReleaseIds(document.releaseIdToUsage.keySet());
                    }
                    break;
                default:
                    copyField(document, copy, field);
            }
        }
    }
}
