/*
 * Copyright Siemens AG, 2014-2015. Part of the SW360 Portal Project.
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
package com.siemens.sw360.datahandler.couchdb.lucene;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 *
 * @author cedric.bodet@tngtech.com
 */
public class LuceneSearchView {

    final String searchView;
    final String searchFunction;
    final String searchBody;

    public LuceneSearchView(String searchView, String searchFunction, String searchBody) {
        if (isNullOrEmpty(searchView) || isNullOrEmpty(searchFunction) || isNullOrEmpty(searchBody)) {
            throw new IllegalArgumentException("Invalid search functions, provided strings cannot be empty!");
        }

        this.searchView = searchView;
        this.searchFunction = searchFunction;
        this.searchBody = searchBody;
    }

}
