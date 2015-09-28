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

import org.apache.commons.csv.CSVRecord;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author johannes.najjar@tngtech.com
 */
public abstract  class  CustomizedCSVRecordBuilder  <T> {
    public static String alternative(String left, String right) {
        return isNullOrEmpty(left)?right:left;
    }

    CustomizedCSVRecordBuilder(CSVRecord record){
        //parse CSV Record
        //        int i = 0;
        //        member = record.get(i++);
    }

    CustomizedCSVRecordBuilder(){
        //set all members null
    }

    public abstract T build();
}
