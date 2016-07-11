/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.bosch.osmi.bdp.access.api.model;

import java.util.Collection;

/**
 * @author johannes.kristan@bosch-si.com
 * @since 11/16/15.
 */
public interface Project extends BdpEntity {
    String getName();
    Collection<Component> getComponents();
    String getCreatedBy();
    String getFilesScanned();
    String getPendingIdentification();
}
