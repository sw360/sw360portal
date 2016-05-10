/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 * Part of the SW360 Portal Project.
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
package com.bosch.osmi.sw360.bdp.datasink.thrift;

import com.siemens.sw360.datahandler.thrift.ThriftClients;
import com.siemens.sw360.datahandler.thrift.components.ComponentService;
import com.siemens.sw360.datahandler.thrift.licenses.LicenseService;
import com.siemens.sw360.datahandler.thrift.projects.ProjectService;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.thrift.users.UserService;
import com.siemens.sw360.datahandler.thrift.vendors.VendorService;
import org.apache.thrift.TException;

public class ThriftApiSimple implements ThriftApi {
	
	private ThriftClients thriftClients;
	private UserService.Iface userClient;
	private ComponentService.Iface componentClient;
	private VendorService.Iface vendorClient;
	private ProjectService.Iface projectClient;
	private LicenseService.Iface licenseClient;

	public ThriftApiSimple() {
		this.thriftClients = new ThriftClients();
		this.userClient = thriftClients.makeUserClient();
		this.componentClient = thriftClients.makeComponentClient();
		this.vendorClient = thriftClients.makeVendorClient();
		this.projectClient = thriftClients.makeProjectClient();
		this.licenseClient = thriftClients.makeLicenseClient();
		
		this.userClient = thriftClients.makeUserClient();
	}

	/* (non-Javadoc)
	 * @see ThriftApi#getThriftClients()
	 */
	@Override
	public ThriftClients getThriftClients() {
		return thriftClients;
	}

	/* (non-Javadoc)
	 * @see ThriftApi#getUserClient()
	 */
	@Override
	public UserService.Iface getUserClient() {
		return userClient;
	}

	/* (non-Javadoc)
	 * @see ThriftApi#getComponentClient()
	 */
	@Override
	public ComponentService.Iface getComponentClient() {
		return componentClient;
	}

	/* (non-Javadoc)
	 * @see ThriftApi#getVendorClient()
	 */
	@Override
	public VendorService.Iface getVendorClient() {
		return vendorClient;
	}

	/* (non-Javadoc)
	 * @see ThriftApi#getProjectClient()
	 */
	@Override
	public ProjectService.Iface getProjectClient() {
		return projectClient;
	}

	private UserService.Iface addUserToUserClient(User user) {
		try {
			this.userClient.addUser(user);
		} catch (TException e) {
			throw new IllegalArgumentException(e);
		}
		return this.userClient;
	}

	@Override
	public LicenseService.Iface getLicenseClient() {
		return licenseClient;
	}

}
