/*
 * Copyright Siemens AG, 2015. Part of the SW360 Portal Project.
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
package com.siemens.sw360.fossology.config;

import com.siemens.sw360.datahandler.common.Duration;
import com.siemens.sw360.datahandler.couchdb.AttachmentConnector;
import com.siemens.sw360.datahandler.couchdb.DatabaseConnector;
import com.siemens.sw360.datahandler.thrift.ThriftClients;
import com.siemens.sw360.datahandler.thrift.fossology.FossologyHostFingerPrint;
import com.siemens.sw360.fossology.db.FossologyFingerPrintRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;

import static com.siemens.sw360.datahandler.common.DatabaseSettings.*;
import static com.siemens.sw360.datahandler.common.Duration.durationOf;

@Configuration
@ComponentScan({"com.siemens.sw360.fossology"})
public class FossologyConfig {
    // TODO get from a config class
    private final Duration downloadTimeout = durationOf(2, TimeUnit.MINUTES);

    @Bean
    public FossologyFingerPrintRepository fossologyFingerPrintRepository() throws MalformedURLException {
        DatabaseConnector fossologyFingerPrintDatabaseConnector = new DatabaseConnector(COUCH_DB_URL, COUCH_DB_FOSSOLOGY);
        return new FossologyFingerPrintRepository(FossologyHostFingerPrint.class, fossologyFingerPrintDatabaseConnector);
    }

    @Bean
    public AttachmentConnector attachmentConnector() throws MalformedURLException {
        return new AttachmentConnector(COUCH_DB_URL, COUCH_DB_ATTACHMENTS, downloadTimeout);
    }

    @Bean
    public ThriftClients thriftClients() {
        return new ThriftClients();
    }

}
