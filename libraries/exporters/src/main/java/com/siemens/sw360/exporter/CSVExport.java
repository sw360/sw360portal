/*
 * Copyright Siemens AG, 2014-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
