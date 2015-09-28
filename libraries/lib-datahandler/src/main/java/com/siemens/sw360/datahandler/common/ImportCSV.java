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
package com.siemens.sw360.datahandler.common;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.List;

/**
 * Import data from CSV files
 *
 * @author cedric.bodet@tngtech.com
 * @author johannes.najjar@tngtech.com
 * @author manuel.wickmann@tngtech.com
 */
public class ImportCSV {

    private static final Logger log = Logger.getLogger(ImportCSV.class.getName());

    private ImportCSV() {
        // Utility class with only static functions
    }

    /**
     * reads a CSV file and returns its content as a list of CSVRecord
     *
     * @param in
     * @return list of records
     */
    public static List<CSVRecord> readAsCSVRecords(InputStream in) {
        List<CSVRecord> records = null;

        try (Reader reader = new InputStreamReader(in)) {
            CSVParser parser = new CSVParser(reader, CommonUtils.sw360CsvFormat);
            records = parser.getRecords();
            records.remove(0); // Remove header
        } catch (IOException e) {
            log.error("Error parsing CSV File!", e);
        }

        // To avoid returning null above
        if (records == null)
            records = Collections.emptyList();

        return records;
    }

}
