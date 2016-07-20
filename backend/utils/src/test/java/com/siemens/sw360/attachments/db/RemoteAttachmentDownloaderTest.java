/*
 * Copyright Siemens AG, 2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.attachments.db;

import com.siemens.sw360.datahandler.TestUtils;
import com.siemens.sw360.datahandler.common.DatabaseSettings;
import com.siemens.sw360.datahandler.common.Duration;
import com.siemens.sw360.datahandler.couchdb.AttachmentConnector;
import com.siemens.sw360.datahandler.couchdb.DatabaseConnector;
import com.siemens.sw360.datahandler.db.AttachmentRepository;
import com.siemens.sw360.datahandler.thrift.SW360Exception;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentContent;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static com.siemens.sw360.attachments.db.RemoteAttachmentDownloader.length;
import static com.siemens.sw360.attachments.db.RemoteAttachmentDownloader.retrieveRemoteAttachments;
import static com.siemens.sw360.datahandler.TestUtils.*;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;

/**
 * @author daniele.fognini@tngtech.com
 */
public class RemoteAttachmentDownloaderTest {

    private static final String url = DatabaseSettings.COUCH_DB_URL;
    private static final String dbName = DatabaseSettings.COUCH_DB_ATTACHMENTS;

    private AttachmentConnector attachmentConnector;
    private AttachmentRepository repository;

    private List<String> garbage;
    private Duration downloadTimeout = Duration.durationOf(5, TimeUnit.SECONDS);

    @BeforeClass
    public static void setUpClass() throws Exception {
        assertTestString(dbName);
        deleteDatabase(url, dbName);
    }

    @Before
    public void setUp() throws Exception {
        DatabaseConnector databaseConnector = new DatabaseConnector(url, dbName);
        attachmentConnector = new AttachmentConnector(url, dbName, downloadTimeout);
        repository = new AttachmentRepository(databaseConnector);

        garbage = new ArrayList<>();
    }

    @After
    public void tearDown() throws Exception {
        for (String id : garbage) {
            repository.remove(id);
        }
    }

    @Test
    public void testIntegration() throws Exception {
        AttachmentContent attachmentContent = saveRemoteAttachment(url);

        assertThat(retrieveRemoteAttachments(url, dbName, downloadTimeout), is(1));

        assertThat(attachmentConnector.getAttachmentStream(attachmentContent), hasLength(greaterThan(0l)));

        assertThat(retrieveRemoteAttachments(url, dbName, downloadTimeout), is(0));
    }

    @Test
    public void testWithBlackHole() throws Exception {
        assumeThat(getAvailableNetworkInterface(), isAvailable());

        saveRemoteAttachment("http://" + BLACK_HOLE_ADDRESS + "/filename");
        assertThat(repository.getOnlyRemoteAttachments(), hasSize(1));

        ExecutorService executor = newSingleThreadExecutor();

        try {
            Future<Integer> future = executor.submit(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    return retrieveRemoteAttachments(url, dbName, downloadTimeout);
                }
            });

            try {
                Integer downloadedSuccessfully = future.get(1, TimeUnit.MINUTES);
                assertThat(downloadedSuccessfully, is(0));
            } catch (TimeoutException e) {
                fail("retriever got stuck on a black hole");
                throw e; // unreachable
            }
        } finally {
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MINUTES);
        }

        assertThat(repository.getOnlyRemoteAttachments(), hasSize(1));
    }

    @Test
    public void testWithBrokenURL() throws Exception {
        AttachmentContent attachmentContent = saveRemoteAttachment(TestUtils.BLACK_HOLE_ADDRESS);
        AttachmentContent attachmentGood = saveRemoteAttachment(url);

        assertThat(repository.getOnlyRemoteAttachments(), hasSize(2));
        assertThat(retrieveRemoteAttachments(url, dbName, downloadTimeout), is(1));
        assertThat(repository.getOnlyRemoteAttachments(), hasSize(1));

        assertThat(attachmentConnector.getAttachmentStream(attachmentGood), hasLength(greaterThan(0l)));

        assertThat(repository.getOnlyRemoteAttachments(), hasSize(1));
        assertThat(retrieveRemoteAttachments(url, dbName, downloadTimeout), is(0));
        assertThat(repository.getOnlyRemoteAttachments(), hasSize(1));

        try {
            assertThat(attachmentConnector.getAttachmentStream(attachmentContent), hasLength(greaterThan(0l)));
            fail("expected exception not thrown");
        } catch (SW360Exception e) {
            assertThat(e.getWhy(), containsString(attachmentContent.getId()));
        }
    }

    private AttachmentContent saveRemoteAttachment(String remoteUrl) {
        AttachmentContent attachmentContent = new AttachmentContent()
                .setFilename("testfile")
                .setContentType("text")
                .setOnlyRemote(true)
                .setRemoteUrl(remoteUrl);

        repository.add(attachmentContent);

        garbage.add(attachmentContent.getId());

        return attachmentContent;
    }

    private static Matcher<InputStream> hasLength(final Matcher<Long> lengthMatcher) {
        return new TypeSafeMatcher<InputStream>() {
            @Override
            protected boolean matchesSafely(InputStream item) {
                try {
                    return lengthMatcher.matches(length(item));
                } catch (IOException e) {
                    return false;
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("has Length: ");
                lengthMatcher.describeTo(description);
            }
        };
    }

}