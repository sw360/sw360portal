/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
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
package com.siemens.sw360.datahandler.entitlement;

import com.siemens.sw360.datahandler.common.Moderator;
import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.ThriftClients;
import com.siemens.sw360.datahandler.thrift.licenses.License;
import com.siemens.sw360.datahandler.thrift.licenses.Todo;
import com.siemens.sw360.datahandler.thrift.moderation.ModerationService;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.users.User;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import java.util.List;

/**
 * Moderation for the license service
 *
 * @author birgit.heydenreich@tngtech.com
 *
 */
public class LicenseModerator extends Moderator {

    private static final Logger log = Logger.getLogger(LicenseModerator.class);


    public LicenseModerator(ThriftClients thriftClients) {
        super(thriftClients);
    }

    public LicenseModerator(){
        super(new ThriftClients());
    }

    public RequestStatus updateLicense(License license, User user) {

        try {
            ModerationService.Iface client = thriftClients.makeModerationClient();
            client.createLicenseRequest(license, user);
            return RequestStatus.SENT_TO_MODERATOR;
        } catch (TException e) {
            log.error("Could not moderate license " + license.getId() + " for User " + user.getEmail(), e);
            return  RequestStatus.FAILURE;
        }
    }

    public RequestStatus updateLicense(License license, List<Todo> todos, User user) {

        try {
            ModerationService.Iface client = thriftClients.makeModerationClient();
            client.createLicenseRequest(license, todos, user);
            return RequestStatus.SENT_TO_MODERATOR;
        } catch (TException e) {
            log.error("Could not moderate license " + license.getId() + " for User " + user.getEmail(), e);
            return  RequestStatus.FAILURE;
        }
    }
}
