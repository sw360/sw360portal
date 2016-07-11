/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.bosch.osmi.bdp.access.test;

import com.bosch.osmi.bdp.access.api.BdpApiAccess;
import com.bosch.osmi.bdp.access.api.model.*;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;

import java.util.Collection;

import static org.hamcrest.Matchers.greaterThan;

/**
 * @author johannes.kristan@bosch-si.com
 * @since 11/20/15.
 */
public abstract class BdpApiAccessSystemTest {

    protected BdpApiAccess access;

    public void testGetProjectHandles() throws Exception {
        User user = access.retrieveUser();

        Collection<ProjectInfo> result =  user.getProjectInfos();
        Assert.assertThat(result.size(), greaterThan(1));

        boolean contains = false;
        Project project = null;
        for(ProjectInfo projectInfo : result){
            contains = projectInfo.getProjectName().equals("bdp-api-access");
            if(contains){
                project = projectInfo.getProject();
                break;
            }
        }
        Assert.assertTrue(contains);

        String projectName = project.getName();
        Assert.assertThat(projectName, CoreMatchers.is("bdp-api-access"));

        String createdBy = project.getCreatedBy();
        Assert.assertThat(createdBy, CoreMatchers.is("john@example.com"));
        String filesScanned = project.getFilesScanned();
        Assert.assertThat(filesScanned, CoreMatchers.is("162"));
        String pendingIdentification = project.getPendingIdentification();
        Assert.assertThat(pendingIdentification, CoreMatchers.is("3"));

        Collection<Component> components = project.getComponents();
        Assert.assertThat(components.size(), CoreMatchers.is(7));

        Component component = components.iterator().next();

        String componentComment = component.getComponentComment();
        Assert.assertThat(componentComment, CoreMatchers.is(""));
        String componentHomepage = component.getComponentHomePage();
        Assert.assertThat(componentHomepage, CoreMatchers.is("https://www.npmjs.org/package/homunculus"));
        String componentVersion = component.getComponentVersion();
        Assert.assertThat(componentVersion, CoreMatchers.is(""));
        String componentUsageLevel = component.getUsageLevel();
        Assert.assertThat(componentUsageLevel, CoreMatchers.is("FILE"));
        String componentName = component.getName();
        Assert.assertThat(componentName, CoreMatchers.is("homunculus"));

        License license = component.getLicense();
        String licenseName = license.getName();
        String licenseId = license.getId();
        String licenseText = license.getText();
        Assert.assertThat(licenseName, CoreMatchers.is("MIT License"));
        Assert.assertThat(licenseId, CoreMatchers.is("mit2"));
        Assert.assertThat(licenseText, CoreMatchers.is(""));
    }
}
