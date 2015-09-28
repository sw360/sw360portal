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

package com.siemens.sw360.importer;

import com.siemens.sw360.datahandler.common.ImportCSV;
import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Testing the CSV import format
 *
 * @author cedric.bodet@tngtech.com
 * @author manuel.wickmann@tngtech.com
 */
public class ImportCSVTest {

    private static final int NUMBER_OF_LINES = 3;
    private static final int NUMBER_OF_COLUMNS = 2;

    List<CSVRecord> records;

    @Before
    public void setUp() throws Exception {
        records = ImportCSV.readAsCSVRecords(getClass().getResource("/riskcategory.csv").openStream());
    }

    @Test
    public void testReadCSV() throws Exception {
        assertEquals(NUMBER_OF_LINES, records.size());
    }

    @Test
    public void testReadAsCSVRecords() throws Exception {
        assertEquals(NUMBER_OF_LINES, records.size());
        for (CSVRecord record : records) {
            assertEquals(NUMBER_OF_COLUMNS, record.size());
        }
    }

}