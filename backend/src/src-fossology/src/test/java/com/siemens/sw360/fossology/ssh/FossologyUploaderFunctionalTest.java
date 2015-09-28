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
package com.siemens.sw360.fossology.ssh;

import com.google.common.collect.ImmutableList;
import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.components.FossologyStatus;
import com.siemens.sw360.datahandler.thrift.fossology.FossologyHostFingerPrint;
import com.siemens.sw360.fossology.config.TestConfig;
import com.siemens.sw360.fossology.db.FossologyFingerPrintRepository;
import com.siemens.sw360.fossology.handler.FossologyScriptsHandler;
import com.siemens.sw360.fossology.ssh.FossologyUploader.CapturerOutputStream;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.lang.StrictMath.max;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;


/**
 * N.B. this tests will try to connect to the FOSSology installation on the other side if correctly configured
 */
public class FossologyUploaderFunctionalTest {

    private static final String GROUP = "sw360";

    private FossologySshConnector fossologySshConnector;
    private FossologyUploader fossologyUploader;
    private ApplicationContext appContext;

    private static boolean trySimpleConnection(FossologySshConnector fossologySshConnector) {
        final int exitStatus = fossologySshConnector.runInFossologyViaSsh("exit 1");
        return exitStatus == 1;
    }

    @Before
    public void setUp() {
        appContext = new AnnotationConfigApplicationContext(TestConfig.class);

        fossologySshConnector = appContext.getBean(FossologySshConnector.class);
        fossologyUploader = appContext.getBean(FossologyUploader.class);
    }

    private void setUpAutoTrustOnSecondTry() {
        final FossologyFingerPrintRepository fossologyFingerPrintRepository = appContext.getBean(FossologyFingerPrintRepository.class);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                final FossologyHostFingerPrint finger = (FossologyHostFingerPrint) invocation.getArguments()[0];
                FossologyHostFingerPrint trustedFingerPrint = finger.deepCopy().setTrusted(true);

                doReturn(ImmutableList.of(trustedFingerPrint))
                        .when(fossologyFingerPrintRepository)
                        .getAll();
                return null;
            }
        }).when(fossologyFingerPrintRepository).add(any(FossologyHostFingerPrint.class));
    }

    @Test
    public void testTrustingAFingerPrintWorks() {
        RealSshConnectionUtils.assumeCanOpenSshSessions();

        setUpAutoTrustOnSecondTry();
        assertFalse(trySimpleConnection(fossologySshConnector));
        assertTrue(trySimpleConnection(fossologySshConnector));
    }

    @Test
    public void testNotTrustingByDefault() {
        RealSshConnectionUtils.assumeCanOpenSshSessions();

        assertFalse(trySimpleConnection(fossologySshConnector));
        assertFalse(trySimpleConnection(fossologySshConnector));
    }

    @Test
    public void testNotLosingBytes() {
        RealSshConnectionUtils.assumeCanOpenSshSessions();

        final long count = 5000000L;
        CapturerOutputStream capturer = new CapturerOutputStream();
        InputStream inputStream = new RepeaterInputStream("\n", count);

        doFirstConnectionAndTrust();

        assertThat(fossologySshConnector.runInFossologyViaSsh("wc -l", inputStream, capturer), is(0));
        assertThat(capturer.getContent(), startsWith("" + count));
    }

    @Test
    public void testDeployScripts() throws Exception {
        RealSshConnectionUtils.assumeCanOpenSshSessions();

        FossologyScriptsHandler fossologyScriptsHandler = appContext.getBean(FossologyScriptsHandler.class);

        doFirstConnectionAndTrust();
        assertThat(fossologyScriptsHandler.deployScripts(), is(RequestStatus.SUCCESS));
    }

    private void doFirstConnectionAndTrust() {
        setUpAutoTrustOnSecondTry();
        trySimpleConnection(fossologySshConnector);
    }

    @Ignore
    @Test
    public void testRealUpload() {
        doFirstConnectionAndTrust();

        String test = "this is the content\n";
        InputStream inputStream = new ByteArrayInputStream(test.getBytes());
        final CapturerOutputStream outputStream = new CapturerOutputStream();

        final String command = String.format(FossologyUploader.FOSSOLOGY_COMMAND_UPLOAD, "id", GROUP, "project");
        final int exitCode = fossologySshConnector.runInFossologyViaSsh(command, inputStream, outputStream);

        final long uploadId = FossologyUploader.parseResultUploadId(outputStream.getContent());

        System.out.println("uploaded as " + uploadId);

        assertThat("Send to fossology failed: " + outputStream.getContent(), exitCode, is(0));
        assertThat("bad uploadId received: " + outputStream.getContent(), uploadId, is(greaterThan(0L)));

        int i = 0;
        while (++i < 60) {
            ByteArrayOutputStream osget = new ByteArrayOutputStream();
            final String commandGet = String.format(FossologyUploader.FOSSOLOGY_COMMAND_GET_STATUS, uploadId, GROUP);
            final int exitCodeGet = fossologySshConnector.runInFossologyViaSsh(commandGet, osget);
            if (exitCodeGet != 0) {
                fail("check status script is broken");
                break;
            }
            final String outputget = new String(osget.toByteArray());

            final FossologyStatus status = fossologyUploader.parseResultStatus(outputget);
            System.out.println("status is " + status + ", try changing it from FOSSology");
            if (status.equals(FossologyStatus.CLOSED)) {
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                fail("interrupted");
            }
        }
    }

    private static class RepeaterInputStream extends InputStream {
        final long count;
        final byte[] bytes;

        long currentCount = 0;
        int bytePosition = 0;

        private RepeaterInputStream(String line, long count) {
            this.count = max(count, 0);
            this.bytes = line.getBytes();
        }

        @Override
        public int read() throws IOException {
            if (bytes.length == 0 || currentCount >= count) {
                return -1;
            }
            bytePosition = (bytePosition + 1) % bytes.length;
            if (bytePosition == 0) {
                currentCount++;
            }

            return bytes[bytePosition];
        }
    }

}