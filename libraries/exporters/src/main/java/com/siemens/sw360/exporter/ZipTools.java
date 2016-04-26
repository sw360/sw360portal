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

import com.google.common.collect.ImmutableSet;

import java.io.*;
import java.util.HashMap;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author johannes.najjar@tngtech.com
 */
public class ZipTools {

    public static final String RISK_CATEGORY_FILE = "dbo.Riskcategory.csv";
    public static final String RISK_FILE = "dbo.Risk.csv";
    public static final String OBLIGATION_FILE = "dbo.Obligation.csv";
    public static final String OBLIGATION_TODO_FILE = "dbo.obligationtodo.csv";
    public static final String TODO_FILE = "dbo.Todo.csv";
    public static final String LICENSETYPE_FILE = "dbo.Licensetype.csv";
    public static final String LICENSE_TODO_FILE = "dbo.licensetodo.csv";
    public static final String LICENSE_RISK_FILE = "dbo.licenserisk.csv";
    public static final String LICENSE_FILE = "dbo.License.csv";
    public static final Set<String> licenseFileNames =  ImmutableSet.<String>builder()
                                                                    .add(RISK_CATEGORY_FILE).add(RISK_FILE)
                                                                    .add(OBLIGATION_FILE).add(OBLIGATION_TODO_FILE)
                                                                    .add(TODO_FILE).add(LICENSETYPE_FILE)
                                                                    .add(LICENSE_TODO_FILE).add(LICENSE_RISK_FILE)
                                                                    .add(LICENSE_FILE).build();

    public static boolean isValidLicenseArchive(HashMap<String, InputStream> inputMap) {
        if(inputMap==null) return false;
        boolean isValidLicenseArchive = inputMap.size() == licenseFileNames.size();
        for (String licenseFileName : licenseFileNames) {
            isValidLicenseArchive &= inputMap.containsKey(licenseFileName);
        }
        return isValidLicenseArchive;
    }

    public static void addToZip(ZipOutputStream zipOutputStream, String filename, InputStream bit) throws IOException {
        zipOutputStream.putNextEntry(new ZipEntry(filename));
        byte[] buffer = new byte[1024];
        int length;
        while ((length = bit.read(buffer)) > 0) {
            zipOutputStream.write(buffer, 0, length);
        }
        zipOutputStream.closeEntry();
    }

    public static void extractZipToInputStreamMap(InputStream in, HashMap<String, InputStream> inputMap) throws IOException {
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(in));
        /** I assume that the license Zip will not be so big and I can keep it in memory with impunity**/
        /** Nevertheless I do not want to go through the file twice, so read first and then check :) **/
        final int BUFFER = 2048;
        byte data[] = new byte[BUFFER];
        ZipEntry entry;
        int count;
        while ((entry = zis.getNextEntry()) != null) {
            final ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
            while ((count = zis.read(data, 0, BUFFER)) != -1) {
                bufferStream.write(data, 0, count);
            }
            bufferStream.flush();
            bufferStream.close();

            final ByteArrayInputStream inputStream = new ByteArrayInputStream(bufferStream.toByteArray());

            inputMap.put(entry.getName(), inputStream);
        }
    }


}
