/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.bosch.osmi.bdp.access.impl.model;

import com.blackducksoftware.sdk.protex.common.ComponentKey;
import com.blackducksoftware.sdk.protex.common.UsageLevel;
import com.blackducksoftware.sdk.protex.component.Component;
import com.blackducksoftware.sdk.protex.component.ComponentApi;
import com.blackducksoftware.sdk.protex.project.bom.BomComponent;
import com.bosch.osmi.bdp.access.impl.BdpApiAccessImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ComponentImplTest {

    private ComponentImpl component;
    @Mock
    private BdpApiAccessImpl bdp;
    @Mock
    private BomComponent bomComponent;
    @Mock
    private ComponentApi componentApi;

    @Before
    public void setUp() throws Exception {
        when(bdp.getComponentApi()).thenReturn(componentApi);
        when(bomComponent.getComponentKey()).thenReturn(new ComponentKey());
        component = new ComponentImpl(bomComponent, "", bdp);
    }

    @Test
    public void testGetReleaseDateHandlesNullFromBdp() throws Exception {
        when(componentApi.getComponentByKey(any(ComponentKey.class))).thenReturn(new Component());
        assertThat(component.getReleaseDate(), is(nullValue()));
    }
    @Test
    public void testGetReleaseDateFormatsDateCorrectly() throws Exception {
        Component c = new Component();
        Date d = new Date(114, 2, 1);
        c.setReleaseDate(d);
        when(componentApi.getComponentByKey(any(ComponentKey.class))).thenReturn(c);
        assertThat(component.getReleaseDate(), is("2014-03-01"));
    }


    @Test
    public void testGetUsageLevel() throws Exception {
        when(bomComponent.getUsageLevels()).thenReturn(new ArrayList<UsageLevel>());
        component = new ComponentImpl(bomComponent, "", bdp);
        assertThat(component.getUsageLevel(), is(""));

        String expectedResult = "FILE";
        List<UsageLevel> usages = new ArrayList<>();
        usages.add(UsageLevel.fromValue(expectedResult));
        when(bomComponent.getUsageLevels()).thenReturn(usages);
        component = new ComponentImpl(bomComponent, "", bdp);
        assertThat(component.getUsageLevel(), is(expectedResult));

        expectedResult = "FILE";
        usages = new ArrayList<>();
        usages.add(UsageLevel.fromValue(expectedResult));
        usages.add(UsageLevel.fromValue("COMPONENT"));
        when(bomComponent.getUsageLevels()).thenReturn(usages);
        component = new ComponentImpl(bomComponent, "", bdp);
        assertThat(component.getUsageLevel(), is(expectedResult));

    }
}