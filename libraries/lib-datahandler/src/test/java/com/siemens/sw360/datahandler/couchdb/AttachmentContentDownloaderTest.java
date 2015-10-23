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

import com.google.common.io.CharStreams;
import com.siemens.sw360.datahandler.common.Duration;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentContent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.*;

import static com.siemens.sw360.datahandler.TestUtils.*;
import static com.siemens.sw360.datahandler.common.Duration.durationOf;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author daniele.fognini@tngtech.com
 */
@RunWith(MockitoJUnitRunner.class)
public class AttachmentContentDownloaderTest {
    private final Duration downloadTimeout = durationOf(2, TimeUnit.SECONDS);
    private AttachmentContentDownloader attachmentContentDownloader;

    @Before
    public void setUp() throws Exception {
        attachmentContentDownloader = new AttachmentContentDownloader();
    }

    @Test
    public void testTheCouchDbUrl() throws Exception {
        AttachmentContent attachmentContent = mock(AttachmentContent.class);

        when(attachmentContent.getRemoteUrl()).thenReturn(DatabaseTestProperties.COUCH_DB_URL);

        try (InputStream download = attachmentContentDownloader.download(attachmentContent, downloadTimeout)) {
            String read = CharStreams.toString(new InputStreamReader(download));
            assertThat(read, is(not(nullOrEmpty())));
            assertThat(read, containsString("couchdb"));
        }
    }

    @Test
    public void testABlackHoleUrl() throws Exception {
        assumeThat(getAvailableNetworkInterface(), isAvailable());

        Callable<String> downloadAttempt = new Callable<String>() {
            @Override
            public String call() throws Exception {
                AttachmentContent attachmentContent = mock(AttachmentContent.class);
                when(attachmentContent.getRemoteUrl()).thenReturn("http://" + BLACK_HOLE_ADDRESS + "/filename");

                try (InputStream download = attachmentContentDownloader
                        .download(attachmentContent, downloadTimeout)) {
                    return CharStreams.toString(new InputStreamReader(download));
                }
            }
        };

        ExecutorService executor = newSingleThreadExecutor();
        try {
            Future<String> future = executor.submit(downloadAttempt);

            try {
                try {
                    String read = future.get(1, TimeUnit.MINUTES);
                    fail("downloader managed to escape with '" + read + "' from the black hole! " +
                            "Try this test again with a more massive black hole");
                } catch (ExecutionException e) {
                    Throwable futureException = e.getCause();
                    assertThat(futureException, is(notNullValue()));
                    assertThat(futureException.getMessage(), either(containsString("timed out")).or(containsString("403")));
                }
            } catch (TimeoutException e) {
                fail("downloader got stuck on a black hole");
                throw e; // unreachable
            }
        } finally {
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MINUTES);
        }
    }
}