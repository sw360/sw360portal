/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.datahandler.permissions.jgivens;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author johannes.najjar@tngtech.com
 */
public class ThenVisible extends Stage<ThenVisible> {

    @ExpectedScenarioState
    Boolean isVisible;

    public ThenVisible the_visibility_should_be(Boolean i) {
        assertThat(isVisible, is(i));
        return self();
    }
}
