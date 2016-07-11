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

import com.siemens.sw360.datahandler.thrift.components.Component;

public class BdpComponentToSw360ComponentTranslator implements EntityTranslator<com.bosch.osmi.bdp.access.api.model.Component, Component> {

    @Override
    public Component apply(com.bosch.osmi.bdp.access.api.model.Component componentBdp) {
        Component componentSW360 = new Component(componentBdp.getName());
        componentSW360.setHomepage(componentBdp.getComponentHomePage());

// Not yet used:
// componentBdp.getComponentComment();
// componentBdp.getUsageLevel();

        return componentSW360;
    }

}
