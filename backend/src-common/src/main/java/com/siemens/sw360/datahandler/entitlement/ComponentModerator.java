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
import com.siemens.sw360.datahandler.thrift.components.Component;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.moderation.ModerationService;
import com.siemens.sw360.datahandler.thrift.users.User;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

/**
 * Moderation for the component service
 *
 * @author cedric.bodet@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 */
public class ComponentModerator extends Moderator {

    private static final Logger log = Logger.getLogger(ComponentModerator.class);

    public ComponentModerator(ThriftClients thriftClients) {
        super(thriftClients);
    }

    public ComponentModerator() {
        super(new ThriftClients());
    }

    public RequestStatus updateRelease(Release release, User user) {

        try {
            ModerationService.Iface client = thriftClients.makeModerationClient();
            client.createReleaseRequest(release, user);
            return RequestStatus.SENT_TO_MODERATOR;
        } catch (TException e) {
            log.error("Could not moderate release " + release.getId() + " for User " + user.getEmail(), e);
            return RequestStatus.FAILURE;
        }
    }

    public RequestStatus deleteRelease(Release release, User user) {
        try {
            ModerationService.Iface client = thriftClients.makeModerationClient();
            client.createReleaseDeleteRequest(release, user);
            return RequestStatus.SENT_TO_MODERATOR;
        } catch (TException e) {
            log.error("Could not moderate delete release " + release.getId() + " for User " + user.getEmail(), e);
            return RequestStatus.FAILURE;
        }
    }

    public RequestStatus updateComponent(Component component, User user) {

        try {
            ModerationService.Iface client = thriftClients.makeModerationClient();
            client.createComponentRequest(component, user);
            return RequestStatus.SENT_TO_MODERATOR;
        } catch (TException e) {
            log.error("Could not moderate component " + component.getId() + " for User " + user.getEmail(), e);
            return RequestStatus.FAILURE;
        }
    }

    public RequestStatus deleteComponent(Component component, User user) {
        try {
            ModerationService.Iface client = thriftClients.makeModerationClient();
            client.createComponentDeleteRequest(component, user);
            return RequestStatus.SENT_TO_MODERATOR;
        } catch (TException e) {
            log.error("Could not moderate delete component " + component.getId() + " for User " + user.getEmail(), e);
            return RequestStatus.FAILURE;
        }
    }

}
