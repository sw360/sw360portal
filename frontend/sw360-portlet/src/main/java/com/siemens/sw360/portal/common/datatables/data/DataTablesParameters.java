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
package com.siemens.sw360.portal.common.datatables.data;

import java.util.List;

/**
 * @author daniele.fognini@tngtech.com
 */
public class DataTablesParameters {
    private final int draw;
    private final int length;
    private final int start;

    private final List<DataTablesOrder> orders;

    private final List<DataTablesColumn> columns;
    private final DataTablesSearch search;

    public DataTablesParameters(int draw, int length, int start, List<DataTablesOrder> orders, List<DataTablesColumn> columns, DataTablesSearch search) {
        this.draw = draw;
        this.length = length;
        this.start = start;
        this.orders = orders;
        this.columns = columns;
        this.search = search;
    }

    public int getDraw() {
        return draw;
    }

    public int getLength() {
        return length;
    }

    public int getStart() {
        return start;
    }

    public List<DataTablesOrder> getOrders() {
        return orders;
    }

    public List<DataTablesColumn> getColumns() {
        return columns;
    }

    public DataTablesSearch getSearch() {
        return search;
    }
}
