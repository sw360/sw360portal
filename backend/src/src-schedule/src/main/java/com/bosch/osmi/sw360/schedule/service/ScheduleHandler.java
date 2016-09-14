/*
 * Copyright (c) Bosch Software Innovations GmbH 2016.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.bosch.osmi.sw360.schedule.service;

import com.bosch.osmi.sw360.schedule.timer.Scheduler;
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

    @FunctionalInterface
    public interface SupplierThrowingTException {
        RequestStatus get() throws TException;
    }

    private boolean wrapSupplierException(SupplierThrowingTException body, String serviceName){
        Supplier<RequestStatus> wrappedBody = () -> {
            try {
                return body.get();
            } catch (TException e) {
                log.error("was not able to schedule sync for client with name:" + serviceName + " message:" + e.getMessage(), e);
                return RequestStatus.FAILURE;
            }
        };
        return Scheduler.scheduleNextSync(wrappedBody, serviceName);
    }

    @Override
    public RequestSummary scheduleService(String serviceName, User user) throws TException {
        if (!PermissionUtils.isAdmin(user)){
            return new RequestSummary(RequestStatus.FAILURE);
        }

        Scheduler.cancelSyncJobOfService(serviceName);

        boolean successSync = false;
        switch (serviceName) {
            case ThriftClients.CVESEARCH_SERVICE:
                successSync = wrapSupplierException(() ->thriftClients.makeCvesearchClient().update(), serviceName);
                break;
            default:
        }

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
        return Scheduler.cancelSyncJobOfService(serviceName);
    }

    @Override
    public RequestStatus unscheduleAllServices(User user) throws TException {
        if (!PermissionUtils.isAdmin(user)) {
            return RequestStatus.FAILURE;
        }
        return Scheduler.cancelAllSyncJobs();
    }
}
