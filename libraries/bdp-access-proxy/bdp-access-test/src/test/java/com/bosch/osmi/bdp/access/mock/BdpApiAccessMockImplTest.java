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

package com.bosch.osmi.bdp.access.mock;

import com.bosch.osmi.bdp.access.api.model.User;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author muj1be
 * @since 11/20/15.
 */
public class BdpApiAccessMockImplTest {

    @Test
    public void defaultConstructorWorksWithResource() throws Exception {
        BdpApiAccessMockImpl access = new BdpApiAccessMockImpl();
        User user = access.retrieveUser();
        Assert.assertThat(user, CoreMatchers.notNullValue());
    }
}