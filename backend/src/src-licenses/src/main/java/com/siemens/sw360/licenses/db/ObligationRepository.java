/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.licenses.db;

import com.siemens.sw360.datahandler.couchdb.DatabaseConnector;
import com.siemens.sw360.datahandler.couchdb.DatabaseRepository;
import com.siemens.sw360.datahandler.thrift.licenses.Obligation;
import org.ektorp.support.View;

/**
 * CRUD access for the Obligation class
 *
 * @author cedric.bodet@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 */
@View(name = "all", map = "function(doc) { if (doc.type == 'obligation') emit(null, doc._id) }")
public class ObligationRepository extends DatabaseRepository<Obligation> {

    public ObligationRepository(DatabaseConnector db) {
        super(Obligation.class, db);

        initStandardDesignDocument();
    }

}

