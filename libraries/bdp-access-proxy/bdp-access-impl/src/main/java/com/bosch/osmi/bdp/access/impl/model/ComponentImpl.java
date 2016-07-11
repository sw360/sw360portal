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
import com.blackducksoftware.sdk.protex.common.ApprovalState;
import com.blackducksoftware.sdk.protex.common.ComponentKey;
import com.blackducksoftware.sdk.protex.common.UsageLevel;
import com.blackducksoftware.sdk.protex.component.ComponentApi;
import com.blackducksoftware.sdk.protex.project.bom.BomComponent;
import com.blackducksoftware.sdk.protex.project.bom.BomComponentType;
import com.bosch.osmi.bdp.access.api.model.Component;
import com.bosch.osmi.bdp.access.api.model.License;
import com.bosch.osmi.bdp.access.impl.BdpApiAccessImpl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author johannes.kristan@bosch-si.com
 * @since 11/18/15.
 */
public class ComponentImpl implements Component {
    private static final Logger LOGGER = LogManager.getLogger(ComponentImpl.class);
    private static final String UNSPECIFIED = "Unspecified";
    private static final String EMPTY = "";

    private final BomComponent source;
    private final String projectId;
    private final BdpApiAccessImpl access;

    public ComponentImpl(BomComponent source, String projectId, BdpApiAccessImpl access) {
        this.source = source;
        this.projectId = projectId;
        this.access = access;
    }

    @Override
    public String getName() {
        return source.getComponentName();
    }

    @Override
    public License getLicense() {
        License result = new LicenseImpl(source.getLicenseInfo(), access);
        return result;
    }

    @Override
    public String getComponentVersion() {
        if(source.getBomVersionName() == null){
            return EMPTY;
        }else if (source.getBomVersionName().equals(UNSPECIFIED)) {
            return EMPTY;
        } else {
            return source.getBomVersionName();
        }
    }

    @Override
    public String getComponentHomePage() {
        try {
            String homepage = EMPTY;
            if (source.getType().equals(BomComponentType.PUBLIC)) {
                ComponentKey componentKey = source.getComponentKey();
                homepage = access.getComponentApi().getComponentByKey(componentKey).getHomePage();
                if(homepage == null) {
                    homepage = EMPTY;
                }
            }
            return homepage;
        } catch (SdkFault sdkFault) {
            LOGGER.error("Unable to retrieve project data. Reason \n" + sdkFault.getMessage());
            return EMPTY;
        }
    }

    @Override
    public String getComponentComment() {
        try {
            ComponentKey key = source.getComponentKey();
            String result = access.getBomApi().getComponentComment(projectId, key);
            if(result == null){
                result = EMPTY;
            }
            return result;
        } catch (SdkFault sdkFault) {
            LOGGER.error("Unable to retrieve project data. Reason \n" + sdkFault.getMessage());
            return EMPTY;
        }
    }

    @Override
    public String getUsageLevel() {
        String usageLevel = EMPTY;
        List<UsageLevel> usage = source.getUsageLevels();
        if(!usage.isEmpty()){
            usageLevel = usage.get(0).name();
            if(usage.size() > 1){
                LOGGER.error("Component " + this.getName() + " has more than one usage level but should have only one.");
            }
        }else{
            LOGGER.debug("No usage level present at component " + this.getName() + " but should.");
        }
        return usageLevel;
    }

    @Override
    public String getComponentKey(){
        String result = "";
        ComponentKey key = source.getComponentKey();
        if(key != null){
           result = key.getComponentId();
        }
        return result;
    }


    @Override
    public String getApprovalState(){
        String result = "";
        ApprovalState state = source.getApprovalState();
        if(state != null){
            result = state.name();
        }
        return result;
    }

    @Override
    public String getApprovedBy(){
        String result = source.getApprovedBy();
        if(result == null){
            result = "";
        }
        return result;
    }

    @Override
    public String getReleaseDate(){
        ComponentApi componentApi = access.getComponentApi();
        try {
            com.blackducksoftware.sdk.protex.component.Component bdpComponent =
                    componentApi.getComponentByKey(source.getComponentKey());
            Date date = bdpComponent.getReleaseDate();
            if (date == null) {
                return null;
            }
            return new SimpleDateFormat("y-MM-dd").format(date);
        } catch (SdkFault sdkFault) {
            LOGGER.error("Unable to retrieve component data. Reason \n" + sdkFault.getMessage());
            // In case the sdk api cannot find a component details it throws an exception. However,
            // we would not consider that as an exception just as a case where not more information
            // is available.
            return null;
        }
    }
}
