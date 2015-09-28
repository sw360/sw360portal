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
package com.siemens.sw360.commonIO;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by bodet on 18/11/14.
 *
 * @author cedric.bodet@tngtech.com
 */
public class ConvertUtil {

    private ConvertUtil() {
        // Utility class with only static functions
    }

    public static String parseDate(String input) {
        if ("NULL".equals(input)) {
            return null;
        }

        if (input.length() == 24 && input.startsWith("CAST")) {
            String text = input.substring(7, 7 + 8);

            StringBuilder invertedBuilder =  new StringBuilder();
            for (int i = 3; i >= 0; i--) {
                invertedBuilder.append(text.substring(2 * i, 2 * i + 2) );
            }

            long daysSinceRataDieEpoch = Long.parseLong(invertedBuilder.toString(), 16);

            long daysSinceLinuxEpoch = daysSinceRataDieEpoch - 719163L; // Epoch is 1.1.1 for rata die, 1970-01-01 corresponds to 719163L

            long miliSecondsSinceLinuxEpoch = daysSinceLinuxEpoch * 24 * 3600 * 1000;
            Date date = new Date(miliSecondsSinceLinuxEpoch);
            return new SimpleDateFormat("yyyy-MM-dd").format(date);
        }

        else {
            return input;
        }
    }

}
