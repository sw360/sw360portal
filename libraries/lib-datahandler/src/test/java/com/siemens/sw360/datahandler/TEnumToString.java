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
package com.siemens.sw360.datahandler;

import com.siemens.sw360.datahandler.common.ThriftEnumUtils;
import com.tngtech.jgiven.annotation.Format;
import com.tngtech.jgiven.format.ArgumentFormatter;
import org.apache.thrift.TEnum;

import java.lang.annotation.*;
/**
 * @author daniele.fognini@tngtech.com
 */
@Documented
@Format(value = TEnumToString.EnumFormatter.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
public @interface TEnumToString {

    class EnumFormatter implements ArgumentFormatter<TEnum> {

        @Override
        public String format(TEnum o, String... args) {
            return "\"" + ThriftEnumUtils.enumToString(o) + "\"";
        }
    }
}
