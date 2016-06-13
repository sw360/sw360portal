/*
 * Copyright Siemens AG, 2016. Part of the SW360 Portal Project.
 * With modifications from Bosch Software Innovations GmbH, 2016.
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
package com.bosch.osmi.sw360.schedule.timer;

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
    private static final ConcurrentHashMap<String, TimerTask> scheduledJobs = new ConcurrentHashMap<>();

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
        try {
            if (timer == null) {
                timer = new Timer();
            }
            ScheduleSyncTask syncTask = new ScheduleSyncTask(body, serviceName);

            GregorianCalendar calendar = getNextRun(syncFirstRunOffset, syncInterval);

            nextSync = calendar.getTime();
            timer.scheduleAtFixedRate(syncTask, nextSync, syncInterval * 1000);
            scheduledJobs.put(syncTask.getId(), syncTask);
            log.info("New task scheduled. Interval=" + syncInterval + "sec " + syncTask.toString());
            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    private static GregorianCalendar getNextRun(int firstRunOffset, int interval) {
        long now = new Date().getTime();

        GregorianCalendar calendar = new GregorianCalendar(); // use today 00:00:00.000 as base date
        calendar.set(GregorianCalendar.HOUR_OF_DAY, 0);
        calendar.set(GregorianCalendar.MINUTE, 0);
        calendar.set(GregorianCalendar.SECOND, 0);
        calendar.set(GregorianCalendar.MILLISECOND, 0);

        calendar.add(GregorianCalendar.SECOND, firstRunOffset);//today with offset time as specified

        // ensure that "missed" task will not be executed at once
        while (calendar.getTime().getTime() < now) {
            calendar.add(GregorianCalendar.SECOND, interval);
        };

        // if firstRunOffset is in the past compute next run - does not work
       /* if(calendar.getTime().getTime() < now) {
            long timeLeftToNextRunInMilliSeconds = (now-calendar.getTime().getTime()) % (interval*1000);
            calendar.getTime().setTime(now + timeLeftToNextRunInMilliSeconds);
        };*/
        return calendar;
    }

    public static synchronized RequestStatus cancelAllSyncJobs() {
        try {
            for (TimerTask job : scheduledJobs.values()) {
                if (job instanceof SW360Task) {
                    cancelJob((SW360Task) job);
                }
            }
            return RequestStatus.SUCCESS;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return RequestStatus.FAILURE;
        }
    }

    public static synchronized RequestStatus cancelAllSyncJobsOfService(String serviceName) {
        try {
            for (TimerTask job : scheduledJobs.values()) {
                if (job instanceof SW360Task && serviceName.equals(((SW360Task) job).getName()) ){
                    cancelJob((SW360Task) job);
                }
            }
            return RequestStatus.SUCCESS;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return RequestStatus.FAILURE;
        }
    }

    private static synchronized void cancelJob(SW360Task job){
        long executionTime = job.scheduledExecutionTime();
        job.cancel();
        scheduledJobs.remove(job.getId());
        log.info("Task " + job.getClass().getSimpleName() + " for " + SW360Utils.getDateTimeString(new Date(executionTime)) + " cancelled. " + job.toString());
    }
}
