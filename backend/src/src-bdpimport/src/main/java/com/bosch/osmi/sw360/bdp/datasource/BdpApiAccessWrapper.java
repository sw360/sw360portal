/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.bosch.osmi.sw360.bdp.datasource;

import com.bosch.osmi.bdp.access.api.model.Component;
import com.bosch.osmi.bdp.access.api.model.License;
import com.bosch.osmi.bdp.access.api.model.ProjectInfo;

import java.util.Collection;
import java.util.Map;

public interface BdpApiAccessWrapper {

    boolean validateCredentials();

    String getEmailAddress();

    Collection<ProjectInfo> getUserProjectInfos();

    Map<ProjectInfo, Collection<Component>> getProjectInfoMapComponents();

    Map<Component, License> getComponentMapLicense(Collection<Component> allComponent);

    ProjectInfo getProjectInfo(String bdpId);
}
