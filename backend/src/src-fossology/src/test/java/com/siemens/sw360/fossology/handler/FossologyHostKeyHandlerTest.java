/*
 * Copyright Siemens AG, 2015. Part of the SW360 Portal Project.
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
package com.siemens.sw360.fossology.handler;

import com.siemens.sw360.datahandler.thrift.fossology.FossologyHostFingerPrint;
import com.siemens.sw360.fossology.db.FossologyFingerPrintRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FossologyHostKeyHandlerTest {

    FossologyHostKeyHandler fossologyHostKeyHandler;

    @Mock
    FossologyFingerPrintRepository fossologyHostKeyConnector;

    @Before
    public void setUp() {
        fossologyHostKeyHandler = new FossologyHostKeyHandler(fossologyHostKeyConnector);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(fossologyHostKeyConnector);
    }

    @Test
    public void testGetFingerPrints() throws Exception {
        List<FossologyHostFingerPrint> fingerPrints = Collections.emptyList();
        when(fossologyHostKeyConnector.getAll()).thenReturn(fingerPrints);

        assertThat(fossologyHostKeyHandler.getFingerPrints(), sameInstance(fingerPrints));

        verify(fossologyHostKeyConnector).getAll();
    }

    @Test
    public void testGetFingerPrintsReturnEmptyOnFailure() throws Exception {

        when(fossologyHostKeyConnector.getAll()).thenReturn(null);

        assertThat(fossologyHostKeyHandler.getFingerPrints(),
                is(emptyCollectionOf(FossologyHostFingerPrint.class)));

        verify(fossologyHostKeyConnector).getAll();
    }

    @Test
    public void testSetFingerPrints() throws Exception {
        List<FossologyHostFingerPrint> fingerPrints = Collections.emptyList();
        fossologyHostKeyHandler.setFingerPrints(fingerPrints);

        verify(fossologyHostKeyConnector).executeBulk(fingerPrints);
    }
}