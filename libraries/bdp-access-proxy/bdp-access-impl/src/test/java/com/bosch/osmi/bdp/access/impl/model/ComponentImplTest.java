/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
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