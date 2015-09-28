/*
 * Copyright Siemens AG, 2015. Part of the SW360 Portal Project.
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

import org.junit.Test;

import static com.siemens.sw360.datahandler.common.CommonUtils.getTargetNameOfUrl;
import static com.siemens.sw360.datahandler.common.CommonUtils.isValidUrl;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author daniele.fognini@tngtech.com
 */
public class CommonUtilsTest {


    @Test
    public void testIsUrl() throws Exception {
        assertThat(isValidUrl("http://www.google.com"), is(true));
    }

    @Test
    public void testIsUrl1() throws Exception {
        assertThat(isValidUrl("www.google.com"), is(false));
    }

    @Test
    public void testIsUrl2() throws Exception {
        assertThat(isValidUrl("ftp://www.google.com"), is(true));
    }

    @Test
    public void testIsUrl3() throws Exception {
        assertThat(isValidUrl("httpwww://www.google.com"), is(false));
    }

    @Test
    public void testIsUrl4() throws Exception {
        assertThat(isValidUrl("http://"), is(false));
    }

    @Test
    public void testIsUrl5() throws Exception {
        assertThat(isValidUrl(null), is(false));
    }

    @Test
    public void testIsUrl6() throws Exception {
        assertThat(isValidUrl(""), is(false));
    }

    @Test
    public void testNameOfUrl() throws Exception {
        assertThat(getTargetNameOfUrl("http://www.google.com"), is(""));
    }

    @Test
    public void testNameOfUrl1() throws Exception {
        assertThat(getTargetNameOfUrl("www.google.com"), is(""));
    }

    @Test
    public void testNameOfUrl2() throws Exception {
        assertThat(getTargetNameOfUrl("ftp://www.google.com"), is(""));
    }

    @Test
    public void testNameOfUrl3() throws Exception {
        assertThat(getTargetNameOfUrl("httpwww://www.google.com"), is(""));
    }

    @Test
    public void testNameOfUrl4() throws Exception {
        assertThat(getTargetNameOfUrl("http://example.com/file"), is("file"));
    }

    @Test
    public void testNameOfUrl5() throws Exception {
        assertThat(getTargetNameOfUrl("http://www.google.com?file=12"), is(""));
    }

    @Test
    public void testNameOfUrl6() throws Exception {
        assertThat(getTargetNameOfUrl("ftp://www.google.com/dir/file.xe"), is("file.xe"));
    }

    @Test
    public void testNameOfUrl7() throws Exception {
        assertThat(getTargetNameOfUrl("http://www.google.com/dir/file.ext?cookie=14&cr=345"), is("file.ext"));
    }

}