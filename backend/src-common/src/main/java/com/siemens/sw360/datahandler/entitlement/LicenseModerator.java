/*
 * Copyright Siemens AG, 2016. Part of the SW360 Portal Project.
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

import com.google.common.collect.Maps;
import com.siemens.sw360.datahandler.common.Moderator;
import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.ThriftClients;
import com.siemens.sw360.datahandler.thrift.licenses.License;
import com.siemens.sw360.datahandler.thrift.licenses.Todo;
import com.siemens.sw360.datahandler.thrift.moderation.ModerationService;
import com.siemens.sw360.datahandler.thrift.users.User;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import static com.siemens.sw360.datahandler.common.CommonUtils.isTemporaryTodo;
import static com.siemens.sw360.datahandler.common.CommonUtils.nullToEmptyList;

/**
 * Moderation for the license service
 *
 * @author birgit.heydenreich@tngtech.com
 */
public class LicenseModerator extends Moderator<License._Fields, License> {

    private static final Logger log = Logger.getLogger(LicenseModerator.class);


    public LicenseModerator(ThriftClients thriftClients) {
        super(thriftClients);
    }

    public LicenseModerator() {
        super(new ThriftClients());
    }

    public RequestStatus updateLicense(License license, User user) {

        try {
            ModerationService.Iface client = thriftClients.makeModerationClient();
            client.createLicenseRequest(license, user);
            return RequestStatus.SENT_TO_MODERATOR;
        } catch (TException e) {
            log.error("Could not moderate license " + license.getId() + " for User " + user.getEmail(), e);
            return RequestStatus.FAILURE;
        }
    }

    public License updateLicenseFromModerationRequest(License license,
                                                      License licenseAdditions,
                                                      License licenseDeletions,
                                                      String department) {
        Map<String, Todo> actualTodoMap = Maps.uniqueIndex(nullToEmptyList(license.getTodos()), Todo::getId);

        for (Todo added : nullToEmptyList(licenseAdditions.getTodos())) {
            if (!added.isSetId()) {
                log.error("Todo id not set in licenseAdditions.");
                continue;
            }
            if (isTemporaryTodo(added)) {
                if(!license.isSetTodos()){
                    license.setTodos(new ArrayList<>());
                }
                license.getTodos().add(added);
            } else {
                Todo actual = actualTodoMap.get(added.getId());
                if (added.isSetWhitelist() && added.getWhitelist().contains(department)) {
                    if(!actual.isSetWhitelist()){
                        actual.setWhitelist(new HashSet<>());
                    }
                    actual.getWhitelist().add(department);
                }
            }
        }
        for (Todo deleted : nullToEmptyList(licenseDeletions.getTodos())) {
            if (!deleted.isSetId()) {
                log.error("Todo id is not set in licenseDeletions.");
                continue;
            }
            Todo actual = actualTodoMap.get(deleted.getId());
            if (actual == null) {
                log.info("Todo from licenseDeletions does not exist (any more) in license.");
                continue;
            }
            if (deleted.isSetWhitelist() && deleted.getWhitelist().contains(department)) {
                if (actual.isSetWhitelist() && actual.getWhitelist().contains(department)) {
                    actual.getWhitelist().remove(department);
                }
            }
        }
        return license;
    }
}
