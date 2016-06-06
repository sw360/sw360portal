/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.bosch.osmi.sw360.bdp.datasource;

import com.bosch.osmi.bdp.access.api.model.Component;
import com.bosch.osmi.bdp.access.api.model.ProjectInfo;
import com.siemens.sw360.datahandler.thrift.bdpimport.RemoteCredentials;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.Map.Entry;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BdpApiAccessWrapperSimpleTest {

    BdpApiAccessWrapper bdpApiAccessWrapper;

    @Before
    public void setUp() throws Exception {
        this.bdpApiAccessWrapper = new BdpApiAccessWrapperSimple(new RemoteCredentials()
                .setUsername("")
                .setPassword("")
                .setServerUrl("mock"));
    }

    @Test
    public void testGetEmailAddress() {
        assertThat(this.bdpApiAccessWrapper.getEmailAddress(), is("john@example.com"));
    }

    @Test
    public void testGetUserProjectInfos() {
        Collection<ProjectInfo> userProjectInfos = this.bdpApiAccessWrapper.getUserProjectInfos();
        assertThat(userProjectInfos.size(), is(3));

        ((List) userProjectInfos).remove(0);//c_bdp-api-access_8104 is third project
        ((List) userProjectInfos).remove(0);
        Assertions.assertProjectInfo(firstOf(userProjectInfos));
    }

    @Test
    public void testGetAllProject() {
        Collection<ProjectInfo> allProjectInfos = this.bdpApiAccessWrapper.getUserProjectInfos();
        TreeSet<ProjectInfo> sortedProjects = new TreeSet<>(Comparator.comparing(ProjectInfo::getProjectId).reversed());
        sortedProjects.addAll(allProjectInfos);
        assertThat(sortedProjects.size(), is(3));

        sortedProjects.pollFirst();//c_bdp-api-access_8104 is second project
        Assertions.assertProject((ProjectInfo) sortedProjects.first());
    }

    @Test
    public void testGetProjectMapComponents() {
        Map<ProjectInfo, Collection<Component>> projectInfoMapComponents = this.bdpApiAccessWrapper.getProjectInfoMapComponents();

        TreeMap<ProjectInfo, Collection<Component>> sortedProjectMapComponents = new TreeMap<>(Comparator.comparing(ProjectInfo::getProjectId).reversed());
        sortedProjectMapComponents.putAll(projectInfoMapComponents);
        assertThat(sortedProjectMapComponents.size(), is(3));

        sortedProjectMapComponents.pollFirstEntry();
        Entry<ProjectInfo, Collection<Component>> entry = firstOf(sortedProjectMapComponents);
        Assertions.assertProject(entry.getKey());

        Collection<Component> value = entry.getValue();
        assertThat(value.size(), is(7));
        TreeSet<Component> sortedComponent = new TreeSet<>(Comparator.comparing(Component::getName).reversed());
        sortedComponent.addAll(value);

        Assertions.assertComponent(firstOf(sortedComponent));
    }

    private static <T> T firstOf(Collection<T> value) {
        return Assertions.firstOf(value);
    }

    private static <K, V> Entry<K, V> firstOf(Map<K, V> map) {
        return map.entrySet().iterator().next();
    }

}
