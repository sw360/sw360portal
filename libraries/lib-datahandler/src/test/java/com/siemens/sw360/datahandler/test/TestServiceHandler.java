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

package com.siemens.sw360.datahandler.test;

import com.siemens.sw360.testthrift.TestObject;
import com.siemens.sw360.testthrift.TestService;
import org.apache.thrift.TException;

/**
 * Created by bodet on 19/09/14.
 */
public class TestServiceHandler implements TestService.Iface {

    public static final String testText = "This is some nice text!";

    @Override
    public TestObject test(TestObject user) throws TException {
        TestObject copy = new TestObject(user);
        copy.setText(testText);
        return copy;
    }
}
