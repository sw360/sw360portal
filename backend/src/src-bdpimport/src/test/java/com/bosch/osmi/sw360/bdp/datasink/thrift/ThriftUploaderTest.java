/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.bosch.osmi.sw360.bdp.datasink.thrift;

import com.bosch.osmi.bdp.access.api.model.License;
import com.bosch.osmi.bdp.access.api.model.ProjectInfo;
import com.bosch.osmi.sw360.bdp.datasource.BdpApiAccessWrapper;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.users.User;
import org.apache.thrift.TException;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ThriftUploaderTest {

    public static final String LICENSE_ID = "lic";
    private final String PROJECT_ID = "theprojectid";

    private ThriftUploader thriftUploader;
    private User user;

    @Mock
    private ThriftExchange exchange;

    @Mock
    private BdpApiAccessWrapper bdp;

    private Project project;

    @Before
    public void setUp() throws Exception {
        thriftUploader = new ThriftUploader(exchange, bdp);
        user = new User().setId("userId").setEmail("anemail");
        project = new Project().setId(PROJECT_ID);
    }

    @Test
    public void testCreateProjectIdHandlesFailureTalkingToBdp() throws TException {
        when(bdp.getProjectInfo(anyString())).thenReturn(null);
        assertThat(thriftUploader.createProject("c_bdp-api-access_8104", user), is(Optional.empty()));
    }

    @Test
    public void testCreateProjectIdReturnsEmptyInCaseOfNameDuplication() throws TException {
        ProjectInfo bdpProject = new ProjectInfo() {
            @Override
            public String getProjectName() {
                return "bdpName";
            }

            @Override
            public String getProjectId() {
                return "bdpId";
            }

            @Override
            public com.bosch.osmi.bdp.access.api.model.Project getProject() {
                return null;
            }
        };

        when(bdp.getProjectInfo(anyString())).thenReturn(bdpProject);
        when(exchange.getAccessibleProject(anyString(), any(User.class))).thenReturn(project);
        assertThat(thriftUploader.createProject("asasdf", user), is(Optional.empty()));
        verify(exchange, never()).addProject(any(Project.class), any(User.class));
    }

    @Test
    public void testGetOrCreateLicenceIdCreatesNewLicense() {
        License license = createLicense();
        when(exchange.searchLicenseByBdpId(anyString())).thenReturn(Optional.empty());
        when(exchange.addLicense(any(com.siemens.sw360.datahandler.thrift.licenses.License.class), any(User.class))).thenReturn("newId");
        assertThat(thriftUploader.getOrCreateLicenseId(license, user), is("newId"));
    }

    @Test
    public void testGetOrCreateLicenceIdFindsDuplicatesByBdpId() {
        License license = createLicense();
        com.siemens.sw360.datahandler.thrift.licenses.License existingLicense = new com.siemens.sw360.datahandler.thrift.licenses.License().setId("42");
        when(exchange.searchLicenseByBdpId(anyString())).thenReturn(Optional.of(Collections.singletonList(existingLicense)));
        assertThat(thriftUploader.getOrCreateLicenseId(license, user), is("42"));
    }

    @NotNull
    private License createLicense() {
        return new License() {
            @Override
            public String getName() {
                return LICENSE_ID;
            }

            @Override
            public String getId() {
                return "anId";
            }

            @Override
            public boolean hasLicenseTextAttached() {
                return false;
            }

            @Override
            public String getText() {
                return null;
            }
        };
    }

    @Test
    public void testIfIsAlreadyInDbWrapperEmpty() {
        Optional<String> toCheck = thriftUploader.searchExistingEntityId(Optional.empty(),
                Object::toString,
                "");
        assertThat(toCheck, is(notNullValue()));
        assertThat(toCheck.isPresent(), is(false));
    }

    @Test
    public void testIfIsAlreadyInDbWrapperEmptyList() {
        Optional<String> toCheck = thriftUploader.searchExistingEntityId(Optional.of(Collections.EMPTY_LIST),
                Object::toString,
                "");
        assertThat(toCheck, is(notNullValue()));
        assertThat(toCheck.isPresent(), is(false));
    }

    @Test
    public void testIfIsAlreadyInDbWrapperWithSingleId() {
        Optional<String> toCheck = thriftUploader.searchExistingEntityId(Optional.of(Collections.singletonList("Id1")),
                p -> p.toUpperCase(),
                "");
        assertThat(toCheck, is(notNullValue()));
        assertThat(toCheck.isPresent(), is(true));
        assertThat(toCheck.get(), is("ID1"));
    }

    @Test
    public void testIfIsAlreadyInDbWrapperWithMultipleIds() {
        Optional<String> toCheck = thriftUploader.searchExistingEntityId(Optional.of(Arrays.asList("Id1", "Id2", "Id3")),
                p -> p.toUpperCase(),
                "");
        assertThat(toCheck, is(notNullValue()));
        assertThat(toCheck.isPresent(), is(true));
        assertThat(toCheck.get(), is("ID1"));
    }

}
