/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.sw360.datahandler.permissions.jgivens;

import org.eclipse.sw360.datahandler.thrift.users.RequestedAction;
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
