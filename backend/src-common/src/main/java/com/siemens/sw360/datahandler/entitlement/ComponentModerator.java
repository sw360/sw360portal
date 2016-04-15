/*
 * Copyright Siemens AG, 2013-2016. Part of the SW360 Portal Project.
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
 * @author birgit.heydenreich@tngtech.com
 */
public class ComponentModerator extends Moderator<Component._Fields, Component> {

    private static final Logger log = Logger.getLogger(ComponentModerator.class);

    public ComponentModerator(ThriftClients thriftClients) {
        super(thriftClients);
    }

    public ComponentModerator() {
        super(new ThriftClients());
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

    public Component updateComponentFromModerationRequest(Component component,
                                                          Component componentAdditions,
                                                          Component componentDeletions){

        for (Component._Fields field : Component._Fields.values()) {
            if(componentAdditions.getFieldValue(field) == null && componentDeletions.getFieldValue(field) == null){
                continue;
            }

            switch (field) {
                case ID:
                case REVISION:
                case TYPE:
                case CREATED_BY:
                case CREATED_ON:
                case PERMISSIONS:
                case DOCUMENT_STATE:
                    //Releases and aggregates:
                case RELEASES:
                case RELEASE_IDS:
                case MAIN_LICENSE_IDS:
                case MAIN_LICENSE_NAMES:
                case LANGUAGES:
                case OPERATING_SYSTEMS:
                case VENDOR_NAMES:
                    break;
                case ATTACHMENTS:
                    component.setAttachments( updateAttachments(
                            component.getAttachments(),
                            componentAdditions.getAttachments(),
                            componentDeletions.getAttachments()));
                    break;
                default:
                    component = updateBasicField(field, Component.metaDataMap.get(field), component, componentAdditions, componentDeletions);
            }

        }
        return component;
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
