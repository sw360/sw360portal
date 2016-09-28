/*
 * Copyright Siemens AG, 2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
        DatabaseConnector fossologyFingerPrintDatabaseConnector = new DatabaseConnector(getConfiguredHttpClient(), COUCH_DB_FOSSOLOGY);
        return new FossologyFingerPrintRepository(FossologyHostFingerPrint.class, fossologyFingerPrintDatabaseConnector);
    }

    @Bean
    public AttachmentConnector attachmentConnector() throws MalformedURLException {
        return new AttachmentConnector(getConfiguredHttpClient(), COUCH_DB_ATTACHMENTS, downloadTimeout);
    }

    @Bean
    public ThriftClients thriftClients() {
        return new ThriftClients();
    }

}
