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


import com.siemens.sw360.datahandler.common.CommonUtils;
import org.apache.log4j.Logger;

import java.util.Properties;

import static org.apache.log4j.Logger.getLogger;

/**
 * @author stefan.jaeger@evosoft.com
 */
public class ScheduleConstants {
    private static final Logger log = getLogger(ScheduleConstants.class);

    private ScheduleConstants(){}

    public static final String PROPERTIES_FILE_PATH = "/sw360.properties";

    // scheduler properties
    public static final String SYNC_FIRST_RUN_OFFSET_SEC;
    public static final String SYNC_INTERVAL_SEC;

    static {
        Properties props = CommonUtils.loadProperties(ScheduleConstants.class, PROPERTIES_FILE_PATH);

        SYNC_FIRST_RUN_OFFSET_SEC  = props.getProperty("schedule.firstOffset.seconds", (0*60*60)+""); // default 00:00 am
        SYNC_INTERVAL_SEC  = props.getProperty("schedule.interval.seconds", (24*60*60)+""); // default 24h
    }

}
