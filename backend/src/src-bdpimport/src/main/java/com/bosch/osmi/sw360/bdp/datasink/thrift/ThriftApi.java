/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.bosch.osmi.sw360.bdp.datasink.thrift;

import com.siemens.sw360.datahandler.thrift.ThriftClients;
import com.siemens.sw360.datahandler.thrift.components.ComponentService;
import com.siemens.sw360.datahandler.thrift.licenses.LicenseService;
import com.siemens.sw360.datahandler.thrift.projects.ProjectService;
import com.siemens.sw360.datahandler.thrift.users.UserService;
import com.siemens.sw360.datahandler.thrift.vendors.VendorService;

/**
 * API Interface for Thrift clients. 
 */
public interface ThriftApi {

	ThriftClients getThriftClients();

	UserService.Iface getUserClient();

	ComponentService.Iface getComponentClient();

	VendorService.Iface getVendorClient();

	ProjectService.Iface getProjectClient();
	
	LicenseService.Iface getLicenseClient();

}