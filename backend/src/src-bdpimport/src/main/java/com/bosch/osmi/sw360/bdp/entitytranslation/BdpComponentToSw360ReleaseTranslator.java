/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.bosch.osmi.sw360.bdp.entitytranslation;

import com.bosch.osmi.bdp.access.api.model.Component;
import com.siemens.sw360.datahandler.thrift.components.Release;

import java.util.*;

import static com.google.common.base.Strings.isNullOrEmpty;

public class BdpComponentToSw360ReleaseTranslator implements EntityTranslator<Component, Release> {

    public static final String unknownVersionString = "UNKNOWN";

    @Override
    public Release apply(com.bosch.osmi.bdp.access.api.model.Component componentBdp) {
        Release releaseSW360 = new Release();
        releaseSW360.setExternalIds(new HashMap<>());

        releaseSW360.setName(componentBdp.getName());
        releaseSW360.getExternalIds().put(TranslationConstants.BDP_ID,componentBdp.getComponentKey());


        if (! isNullOrEmpty(componentBdp.getComponentVersion())) {
            releaseSW360.setVersion(componentBdp.getComponentVersion());
        } else {
            // this appears for example, if componentBdp.getUsageLevel() == "ORIGINAL_CODE"
            releaseSW360.setVersion(unknownVersionString);
        }

        releaseSW360.setReleaseDate(componentBdp.getReleaseDate());

        releaseSW360.setModerators(new HashSet<>());
// Problem: Can not set mail address when no corresponding user is registered
// releaseSW360.getModerators().add(componentBdp.getApprovedBy());
// Not yet used:
// componentBdp.getComponentComment();
// componentBdp.getUsageLevel();

        return releaseSW360;
    }

}
