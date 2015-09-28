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

package com.siemens.sw360.datahandler.couchdb;

import com.google.common.collect.ImmutableList;
import junit.framework.TestCase;
import org.apache.thrift.TBase;
import org.apache.thrift.TFieldIdEnum;

import java.lang.reflect.Field;

// test that the wrapper class W correctly wraps class C, which has fields C (F is always C._Fields)
public abstract class DocumentWrapperTest<W extends DocumentWrapper<C>, C extends TBase<C, F>, F extends Enum<F> & TFieldIdEnum> extends TestCase {
    private static final ImmutableList<String> NOT_COPIED_FIELDS = ImmutableList.of("id", "revision");

    protected void assertTFields(C source, W attachmentWrapper, Class<W> wrapperClass, Class<F> fieldClass) throws IllegalAccessException {
        for (F thriftField : fieldClass.getEnumConstants()) {
            final String sourceFieldName = thriftField.getFieldName();
            final Object sourceFieldValue = source.getFieldValue(thriftField);

            if (!NOT_COPIED_FIELDS.contains(sourceFieldName))
                assertNotNull("please set the field " + sourceFieldName + " in this test", sourceFieldValue);

            final Field field = getField(wrapperClass, sourceFieldName);
            assertNotNull("field " + sourceFieldName + " is not defined in " + wrapperClass.getName(), field);

            field.setAccessible(true);
            final Object copyFiledValue = field.get(attachmentWrapper);

            if (field.getType().isPrimitive()) {
                assertEquals(copyFiledValue, sourceFieldValue);
            } else {
                assertSame(copyFiledValue, sourceFieldValue);
            }
        }
    }

    private Field getField(Class<W> wrapperClass, String sourceFieldName) {
        return getField(wrapperClass, sourceFieldName, 0);
    }

    private Field getField(Class attachmentWrapperClass, String sourceFieldName, int depth) {
        if (attachmentWrapperClass == null) {
            return null;
        }
        try {
            return attachmentWrapperClass.getDeclaredField(sourceFieldName);
        } catch (NoSuchFieldException e) {
            final Class superclass = attachmentWrapperClass.getSuperclass();
            return getField(superclass, sourceFieldName, depth + 1);
        }
    }

}