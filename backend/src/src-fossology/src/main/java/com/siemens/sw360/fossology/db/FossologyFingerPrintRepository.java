/*
 * Copyright Siemens AG, 2014-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.fossology.db;

import com.siemens.sw360.datahandler.couchdb.DatabaseConnector;
import com.siemens.sw360.datahandler.couchdb.DatabaseRepository;
import com.siemens.sw360.datahandler.thrift.fossology.FossologyHostFingerPrint;

/**
 * @author daniele.fognini@tngtech.com
 */
public class FossologyFingerPrintRepository extends DatabaseRepository<FossologyHostFingerPrint> {
    public FossologyFingerPrintRepository(Class<FossologyHostFingerPrint> type, DatabaseConnector fossologyFingerPrintDatabaseConnector) {
        super(type, fossologyFingerPrintDatabaseConnector);
    }
}
