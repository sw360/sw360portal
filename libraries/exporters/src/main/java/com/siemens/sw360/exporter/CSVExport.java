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
package com.siemens.sw360.exporter;

import com.siemens.sw360.datahandler.common.CommonUtils;
import org.apache.commons.csv.CSVPrinter;
import org.jetbrains.annotations.NotNull;

import java.io.*;

/**
 * @author johannes.najjar@tngtech.com
 */
public class CSVExport {
    @NotNull
    public static ByteArrayInputStream createCSV(Iterable<String> csvHeaderIterable, Iterable<Iterable<String>> inputIterable) throws IOException {
        final ByteArrayOutputStream outB = getCSVOutputStream(csvHeaderIterable, inputIterable);

        return new ByteArrayInputStream(outB.toByteArray());
    }

    @NotNull
    private static ByteArrayOutputStream getCSVOutputStream(Iterable<String> csvHeaderIterable, Iterable<Iterable<String>> inputIterable) throws IOException {
        final ByteArrayOutputStream outB = new ByteArrayOutputStream();
        try(Writer out = new BufferedWriter(new OutputStreamWriter(outB));) {
            CSVPrinter csvPrinter = new CSVPrinter(out, CommonUtils.sw360CsvFormat);
            csvPrinter.printRecord(csvHeaderIterable);
            csvPrinter.printRecords(inputIterable);
            csvPrinter.flush();
            csvPrinter.close();
        } catch (Exception e) {
            outB.close();
            throw e;
        }

            return outB;

    }
}
