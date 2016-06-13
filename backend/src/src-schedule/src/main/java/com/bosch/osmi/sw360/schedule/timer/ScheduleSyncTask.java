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

        import com.siemens.sw360.datahandler.thrift.RequestStatus;
        import com.siemens.sw360.datahandler.thrift.RequestSummary;
        import org.apache.log4j.Logger;

        import java.util.function.Supplier;

        import static org.apache.log4j.Logger.getLogger;

/**
 * creates new {@link java.util.TimerTask} which will be executed on the next scheduled time
 *
 * @author stefan.jaeger@evosoft.com
 * @author birgit.heydenreich@tngtech.com
 */
public class ScheduleSyncTask extends SW360Task {
    private static final Logger log = getLogger(ScheduleSyncTask.class);
    private final Supplier<RequestStatus> body;

    public ScheduleSyncTask(Supplier<RequestStatus> body, String name) {
        super(name);
        this.body = body;
    }

    @Override
    public void run() {
        RequestStatus requestStatus = body.get();
        if (RequestStatus.SUCCESS.equals(requestStatus)) {
            log.info("Successfully finished ScheduleSyncTask " + getId() + ".");
        } else {
            log.error("ScheduleSyncTask " + getId() + " failed.");
        }
    }
}
