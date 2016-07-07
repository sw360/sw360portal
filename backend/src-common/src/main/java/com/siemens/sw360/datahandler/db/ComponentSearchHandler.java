/*
 * Copyright Siemens AG, 2013-2016. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.datahandler.db;

import com.siemens.sw360.datahandler.common.DatabaseSettings;
import com.siemens.sw360.datahandler.couchdb.lucene.LuceneAwareDatabaseConnector;
import com.siemens.sw360.datahandler.couchdb.lucene.LuceneSearchView;
import com.siemens.sw360.datahandler.thrift.components.Component;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class for accessing the Lucene connector on the CouchDB database
 *
 * @author cedric.bodet@tngtech.com
 */
public class ComponentSearchHandler {

    private static final Logger log = Logger.getLogger(ComponentSearchHandler.class);

    private static final LuceneSearchView luceneSearchView = new LuceneSearchView("lucene", "components",
            "function(doc) {" +
                    "    var ret = new Document();" +
                    "    if(!doc.type) return ret;" +
                    "    if(doc.type != 'component') return ret;" +
                    "    function idx(obj) {" +
                    "        for (var key in obj) {" +
                    "            switch (typeof obj[key]) {" +
                    "                case 'object':" +
                    "                    idx(obj[key]);" +
                    "                    break;" +
                    "                case 'function':" +
                    "                    break;" +
                    "                default:" +
                    "                    ret.add(obj[key]);" +
                    "                    break;" +
                    "            }" +
                    "        }" +
                    "    };" +
                    "    idx(doc);" +
                    "    for(var i in doc.categories) {" +
                    "      ret.add(doc.categories[i], {\"field\": \"categories\"} );" +
                    "    }" +
                    "    for(var i in doc.languages) {" +
                    "      ret.add(doc.languages[i], {\"field\": \"languages\"} );" +
                    "    }" +
                    "    for(var i in doc.softwarePlatforms) {" +
                    "      ret.add(doc.softwarePlatforms[i], {\"field\": \"softwarePlatforms\"} );" +
                    "    }" +
                    "    for(var i in doc.operatingSystems) {" +
                    "      ret.add(doc.operatingSystems[i], {\"field\": \"operatingSystems\"} );" +
                    "    }" +
                    "    for(var i in doc.vendorNames) {" +
                    "      ret.add(doc.vendorNames[i], {\"field\": \"vendorNames\"} );" +
                    "    }" +
                    "    for(var i in doc.mainLicenseIds) {" +
                    "      ret.add(doc.mainLicenseIds[i], {\"field\": \"mainLicenseIds\"} );" +
                    "    }" +
                    "        ret.add(doc.componentType, {\"field\": \"componentType\"} );" +
                    "    return ret;" +
                    "}");


    private final LuceneAwareDatabaseConnector connector;

    public ComponentSearchHandler(String url, String dbName) throws IOException {
        connector = new LuceneAwareDatabaseConnector(url, dbName);
        connector.addView(luceneSearchView);
        connector.setResultLimit(DatabaseSettings.LUCENE_SEARCH_LIMIT);
    }

    public List<Component> search(String text, final Map<String , Set<String > > subQueryRestrictions ){
        return connector.searchViewWithRestrictions(Component.class, luceneSearchView, text, subQueryRestrictions);
    }

}
