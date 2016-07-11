/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.bosch.osmi.bdp.access.impl.model;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.license.GlobalLicense;
import com.blackducksoftware.sdk.protex.license.LicenseInfo;
import com.bosch.osmi.bdp.access.api.model.License;
import com.bosch.osmi.bdp.access.impl.BdpApiAccessImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author johannes.kristan@bosch-si.com
 * @since 11/18/15
 */
public class LicenseImpl implements License {
    private static final Logger LOGGER = LogManager.getLogger(LicenseImpl.class);
    public static final String EMPTY = "";

    private final LicenseInfo licenseInfo;
    private final BdpApiAccessImpl access;

    public LicenseImpl(LicenseInfo licenseInfo, BdpApiAccessImpl access) {
        this.licenseInfo = licenseInfo;
        this.access = access;
    }

    @Override
    public String getName() {
        return licenseInfo.getName();
    }

    @Override
    public String getId(){
        return licenseInfo.getLicenseId();
    }

    @Override
    public boolean hasLicenseTextAttached(){
        try {
            GlobalLicense globalLicense = access.getLicenseApi().getLicenseById(getId());
             return globalLicense.getText() != null;
        } catch (SdkFault sdkFault) {
            LOGGER.error("Unable to get license text from Bdp server. Reason: \n" + sdkFault.getMessage());
            LOGGER.debug("License ID: " + getId());
            return false;
        }
    }

    @Override
    public String getText(){
        try {
            GlobalLicense globalLicense = access.getLicenseApi().getLicenseById(getId());
            byte[] licenseText = globalLicense.getText();
            String licenseTextAsString = EMPTY;
            if(licenseText != null){
                licenseTextAsString = new String(licenseText);
            }
            return licenseTextAsString;

        } catch (SdkFault sdkFault) {
            LOGGER.error("Unable to get license text from Bdp server. Reason: \n" + sdkFault.getMessage());
            LOGGER.debug("License ID: " + getId());
            return EMPTY;
        }
    }
}
