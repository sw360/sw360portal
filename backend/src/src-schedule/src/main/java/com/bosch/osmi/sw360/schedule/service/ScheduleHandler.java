/*
 * Copyright (c) Bosch Software Innovations GmbH 2016.
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
package com.bosch.osmi.sw360.schedule.service;

import com.bosch.osmi.sw360.schedule.timer.Scheduler;
import com.google.common.collect.ImmutableList;
import com.siemens.sw360.datahandler.common.SW360Utils;
import com.siemens.sw360.datahandler.permissions.PermissionUtils;
import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.RequestSummary;
import com.siemens.sw360.datahandler.thrift.ThriftClients;
import com.siemens.sw360.datahandler.thrift.schedule.ScheduleService;
import com.siemens.sw360.datahandler.thrift.users.User;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import java.util.function.Supplier;

public class ScheduleHandler implements ScheduleService.Iface {

    ThriftClients thriftClients;
    Logger log;

    public ScheduleHandler() {
        thriftClients = new ThriftClients();
        log = Logger.getLogger(ScheduleHandler.class);
    }

    @Override
    public RequestSummary scheduleService(String serviceName, User user) throws TException {
        if (!PermissionUtils.isAdmin(user)){
            return new RequestSummary(RequestStatus.FAILURE);
        }

        Scheduler.cancelAllSyncJobsOfService(serviceName);

        Supplier<RequestStatus> updateMethod = null;

        switch (serviceName) {
            case ThriftClients.CVESEARCH_SERVICE:
                updateMethod = () -> {
                    try {
                        return thriftClients.makeCvesearchClient().update();
                    } catch (TException e) {
                        log.error(e);
                        return RequestStatus.FAILURE;
                    }
                };
                break;
            default:
        }
        boolean successSync = Scheduler.scheduleNextSync(updateMethod, serviceName);

        if (successSync){
            RequestSummary summary = new RequestSummary(RequestStatus.SUCCESS);
            summary.setMessage(SW360Utils.getDateTimeString(Scheduler.getNextSync()));
            return summary;
        } else {
            return new RequestSummary(RequestStatus.FAILURE);
        }
    }

    @Override
    public RequestStatus unscheduleService(String serviceName, User user) throws TException {
        if (!PermissionUtils.isAdmin(user)) {
            return RequestStatus.FAILURE;
        }
        return Scheduler.cancelAllSyncJobsOfService(serviceName);
    }

    @Override
    public RequestStatus unscheduleAllServices(User user) throws TException {
        if (!PermissionUtils.isAdmin(user)) {
            return RequestStatus.FAILURE;
        }
        return Scheduler.cancelAllSyncJobs();
    }
}
