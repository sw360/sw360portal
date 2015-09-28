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

import com.siemens.sw360.datahandler.thrift.attachments.AttachmentType;
import com.siemens.sw360.datahandler.thrift.components.RepositoryType;
import org.apache.thrift.TEnum;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class ThriftEnumUtilsTest {

    @Test
    public void testToString() {
        assertThat(ThriftEnumUtils.enumToString(AttachmentType.DESIGN), is("Design Document"));
        assertThat(ThriftEnumUtils.enumToString(RepositoryType.GIT), is("Git"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAllMaps() throws Exception {
        for (Map.Entry<Class<? extends TEnum>, Map<? extends TEnum, String>> mapEntry : ThriftEnumUtils.MAP_ENUMTYPE_MAP.entrySet()) {
            Map<TEnum, String> value = (Map<TEnum, String>) mapEntry.getValue();
            testGenericMap((Class<TEnum>) mapEntry.getKey(), value);
        }
    }

    private <T extends TEnum> void testGenericMap(Class<T> type, Map<T, String> input) {
        for (T val : type.getEnumConstants()) {
            assertNotNull(type.getSimpleName() + "." + val.toString() + " [" + val.getValue() + "] has no string associated", input.get(val));
        }
    }
}