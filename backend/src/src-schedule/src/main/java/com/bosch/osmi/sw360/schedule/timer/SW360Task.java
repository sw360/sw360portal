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
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import java.util.Date;
import java.util.TimerTask;
import java.util.UUID;

import static org.apache.log4j.Logger.getLogger;

/**
 * creates new {@link TimerTask} which will be executed on the scheduled execution time
 *
 * @author stefan.jaeger@evosoft.com
 * @author birgit.heydenreich@tngtech.com
 */
public abstract class SW360Task extends TimerTask {
    private static final Logger log = getLogger(SW360Task.class);

    private String id = UUID.randomUUID().toString();
    private String name;

    public SW360Task (){
        this("");
    }

    public SW360Task (String name){
        this.name = name;
    }

    public String getId() {
        return this.id;
    }
    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("SW360Task{");
        sb.append("name='").append(name).append('\'');
        sb.append("id='").append(id).append('\'');
        sb.append("scheduledExecutionTime='").append(SW360Utils.getDateTimeString(new Date(this.scheduledExecutionTime()))).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
