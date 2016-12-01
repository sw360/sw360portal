/*
 * Copyright Siemens AG, 2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.datahandler.common;

import org.junit.Test;

import static org.eclipse.sw360.datahandler.common.CommonUtils.formatTime;
import static org.eclipse.sw360.datahandler.common.CommonUtils.getTargetNameOfUrl;
import static org.eclipse.sw360.datahandler.common.CommonUtils.isValidUrl;
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

    @Test
    public void testFormatTime0() throws Exception {
        assertThat(formatTime(0), is("00:00:00"));
    }

    @Test
    public void testFormatTimeSecond() throws Exception {
        assertThat(formatTime(1), is("00:00:01"));
    }

    @Test
    public void testFormatTimeMinute() throws Exception {
        assertThat(formatTime(60), is("00:01:00"));
    }

    @Test
    public void testFormatTimeHour() throws Exception {
        assertThat(formatTime(3600), is("01:00:00"));
    }

    @Test
    public void testFormatTime24Hours() throws Exception {
        assertThat(formatTime(86400), is("24:00:00"));
    }

}
