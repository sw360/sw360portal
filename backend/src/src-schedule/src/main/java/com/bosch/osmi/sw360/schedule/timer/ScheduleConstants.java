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
