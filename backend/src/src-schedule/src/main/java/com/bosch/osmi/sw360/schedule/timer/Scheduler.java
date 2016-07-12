/*
 * Copyright Siemens AG, 2016. Part of the SW360 Portal Project.
 * With modifications from Bosch Software Innovations GmbH, 2016.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.bosch.osmi.sw360.schedule.timer;

import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.common.SW360Utils;
import com.siemens.sw360.datahandler.thrift.RequestStatus;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static org.apache.log4j.Logger.getLogger;

/**
 * creates new {@link TimerTask} which will be executed on the next valid time
 *
 * @author stefan.jaeger@evosoft.com
 */
public class Scheduler {
    private static final Logger log = getLogger(Scheduler.class);
    private static Date nextSync = null;
    private static final ConcurrentHashMap<String, SW360Task> scheduledJobs = new ConcurrentHashMap<>();

    private static Timer timer = null;
    private static final int syncFirstRunOffset = Integer.parseInt(ScheduleConstants.SYNC_FIRST_RUN_OFFSET_SEC);
    private static final int syncInterval = Integer.parseInt(ScheduleConstants.SYNC_INTERVAL_SEC);

    private Scheduler() {
        //only static member
    }

    public static Date getNextSync() {
        return nextSync;
    }

    public static synchronized boolean scheduleNextSync(Supplier<RequestStatus> body, String serviceName) {
        if (timer == null) {
            timer = new Timer();
        }
        ScheduleSyncTask syncTask = new ScheduleSyncTask(body, serviceName);
        nextSync = getNextSyncDate(syncFirstRunOffset, syncInterval);

        try {
            timer.scheduleAtFixedRate(syncTask, nextSync, syncInterval * 1000);
        } catch (IllegalStateException e) {
            log.error(e.getMessage(), e);
            return false;
        }

        scheduledJobs.put(syncTask.getId(), syncTask);
        log.info("New task scheduled. Interval=" + syncInterval + "sec " + syncTask.toString());
        return true;
    }

    private static Date getNextSyncDate(int firstRunOffset, int interval) {
        GregorianCalendar calendar = new GregorianCalendar(); // use today 00:00:00.000 as base date
        long now = calendar.getTime().getTime();
        calendar.set(GregorianCalendar.HOUR_OF_DAY, 0);
        calendar.set(GregorianCalendar.MINUTE, 0);
        calendar.set(GregorianCalendar.SECOND, 0);
        calendar.set(GregorianCalendar.MILLISECOND, 0);

        calendar.add(GregorianCalendar.SECOND, firstRunOffset);//today with offset time as specified

        // if firstRunOffset is in the past compute next run
        if(calendar.getTime().getTime() < now) {
            long timeLeftToNextRunInMilliSeconds = interval*1000 - ((now-calendar.getTime().getTime()) % (interval*1000));
            calendar.setTimeInMillis(now + timeLeftToNextRunInMilliSeconds);
        };
        return calendar.getTime();
    }

    public static synchronized RequestStatus cancelAllSyncJobs() {
        return scheduledJobs.values().stream()
                .map(Scheduler::cancelJob)
                .reduce(RequestStatus.SUCCESS, CommonUtils::reduceRequestStatus);
    }

    public static synchronized RequestStatus cancelSyncJobOfService(String serviceName) {
        return scheduledJobs.values().stream()
                .filter(job -> serviceName.equals(job.getName()))
                .map(Scheduler::cancelJob)
                .reduce(RequestStatus.SUCCESS, CommonUtils::reduceRequestStatus);
    }

    private static synchronized RequestStatus cancelJob(SW360Task job){
        long executionTime = job.scheduledExecutionTime();
        try{
            job.cancel();
        } catch (IllegalStateException e) {
            log.error(e.getMessage(), e);
            return RequestStatus.FAILURE;
        }
        scheduledJobs.remove(job.getId());
        log.info("Task " + job.getClass().getSimpleName() + " for " + SW360Utils.getDateTimeString(new Date(executionTime)) + " cancelled. " + job.toString());
        return RequestStatus.SUCCESS;
    }
}
