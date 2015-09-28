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

package com.siemens.sw360.datahandler.permissions.jgivens;

import com.siemens.sw360.datahandler.thrift.users.RequestedAction;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author johannes.najjar@tngtech.com
 */
public class ThenHighestAllowedAction  extends Stage<ThenHighestAllowedAction> {

    @ExpectedScenarioState
    RequestedAction highestAllowedAction;


    public ThenHighestAllowedAction the_highest_allowed_action_should_be(RequestedAction i) {
        assertThat(highestAllowedAction, is(i));
        return self();
    }
}
