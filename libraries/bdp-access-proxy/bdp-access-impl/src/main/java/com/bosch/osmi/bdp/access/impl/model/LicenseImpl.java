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
            throw new IllegalStateException(sdkFault.getCause());
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
