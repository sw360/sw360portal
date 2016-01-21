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
package com.siemens.sw360.projects;

import com.siemens.sw360.datahandler.thrift.projects.ProjectService;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;

import java.io.IOException;

/**
 * Small client for testing a service
 *
 * @author cedric.bodet@tngtech.com
 */
public class TestProjectClient {

    public static void main(String[] args) throws TException, IOException {
        THttpClient thriftClient = new THttpClient("http://127.0.0.1:8080/projects/thrift");
        TProtocol protocol = new TCompactProtocol(thriftClient);
        ProjectService.Iface client = new ProjectService.Client(protocol);

//        User cedric = new User().setEmail("cedric.bodet@tngtech.com").setDepartment("CT BE OSS TNG CB");
//        Project myProject = new Project().setName("First project").setDescription("My first project");
//        client.addProject(myProject, cedric);

    //    List<Project> projects = client.getBUProjectsSummary("CT BE OSS");

//        System.out.println("Fetched " + projects.size() + " from project service");
//        for (Project project : projects) {
//            System.out.println(project.toString());
//        }
    }

}
