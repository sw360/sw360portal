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

package com.siemens.sw360.moderation.testutil;

import com.siemens.sw360.datahandler.couchdb.DatabaseConnector;
import com.siemens.sw360.datahandler.thrift.moderation.DocumentType;
import com.siemens.sw360.datahandler.thrift.moderation.ModerationRequest;
import com.siemens.sw360.datahandler.thrift.projects.Project;

import java.net.MalformedURLException;

/**
 * Created by GerritGrenzebachTNG on 09.02.15.
 */
public class DatabaseTestSetup {


    public static void main(String[] args) throws MalformedURLException {

        //DatabaseTestSetup dbSetup = new DatabaseTestSetup("http://localhost:5984", "sw360db");

        DatabaseConnector db = new DatabaseConnector("http://localhost:5984", "sw360db");

        Project project = new Project().setName("Test Project");
        project.addToModerators("user1");

        db.add(project);

        ModerationRequest moderationRequest = new ModerationRequest();
        moderationRequest.setDocumentId(project.id).setDocumentType(DocumentType.PROJECT);
        moderationRequest.setRequestingUser("cedric.bodet@tngtech.com");
        moderationRequest.addToModerators("cedric.bodet@tngtech.com");

        db.add(moderationRequest);

    }

}
