/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
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
