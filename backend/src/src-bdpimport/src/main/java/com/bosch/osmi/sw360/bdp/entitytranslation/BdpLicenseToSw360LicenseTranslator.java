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

import com.siemens.sw360.datahandler.thrift.licenses.License;
import java.util.HashMap;

public class BdpLicenseToSw360LicenseTranslator implements EntityTranslator<com.bosch.osmi.bdp.access.api.model.License, License> {

    @Override
    public License apply(com.bosch.osmi.bdp.access.api.model.License licenseBdp) {
        com.siemens.sw360.datahandler.thrift.licenses.License licenseSW360 = new License();
        licenseSW360.setExternalIds(new HashMap<>());

        licenseSW360.setId(licenseBdp.getId());
        licenseSW360.setShortname(licenseBdp.getId());
        licenseSW360.getExternalIds().put(TranslationConstants.BDP_ID, licenseBdp.getId());
        licenseSW360.setFullname(licenseBdp.getName());
        licenseSW360.setText(licenseBdp.getText());

        return licenseSW360;
    }

}
