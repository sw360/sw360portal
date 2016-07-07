/*
 * Copyright Siemens AG, 2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.portal.common.datatables.data;


import java.util.Optional;

/**
 * @author daniele.fognini@tngtech.com
 */
public class DataTablesColumn {

    private final Optional<DataTablesSearch> search;

    public DataTablesColumn(DataTablesSearch search) {
        this.search = Optional.of(search);
    }

    public DataTablesSearch getSearch() {
        return search.get();
    }

    public boolean isSearchable() {
        return search.isPresent();
    }
}
