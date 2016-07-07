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
import com.siemens.sw360.datahandler.thrift.licenses.RiskCategory;
import org.ektorp.support.View;

/**
 * @author johannes.najjar@tngtech.com
 */
@View(name = "all", map = "function(doc) { if (doc.type == 'riskCategory') emit(null, doc._id) }")
public class RiskCategoryRepository extends DatabaseRepository<RiskCategory> {

    public RiskCategoryRepository(DatabaseConnector db) {
        super(RiskCategory.class, db);

        initStandardDesignDocument();
    }
}
