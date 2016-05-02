/*
 * Copyright Siemens AG, 2016. Part of the SW360 Portal Project.
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

import com.siemens.sw360.datahandler.thrift.attachments.Attachment;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentContent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.siemens.sw360.datahandler.common.Duration.durationOf;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AttachmentConnectorTest {

    @Mock
    DatabaseConnector connector;

    AttachmentConnector attachmentConnector;

    @Before
    public void setUp() throws Exception {
        attachmentConnector = new AttachmentConnector(connector, durationOf(5, TimeUnit.SECONDS));
    }

    @Test
    public void testDeleteAttachmentsDifference() throws Exception {
        Attachment a1 = mock(Attachment.class);
        when(a1.getAttachmentContentId()).thenReturn("a1cid");
        when(a1.getSha1()).thenReturn(null);
        when(a1.isSetSha1()).thenReturn(false);
        Attachment a2 = mock(Attachment.class);
        when(a2.getAttachmentContentId()).thenReturn("a2cid");
        when(a2.getSha1()).thenReturn(null);
        when(a2.isSetSha1()).thenReturn(false);

        Set<Attachment> before = new HashSet<>();
        before.add(a1);
        before.add(a2);

        Attachment a3 = mock(Attachment.class);
        when(a3.getAttachmentContentId()).thenReturn("a1cid");
        when(a3.getSha1()).thenReturn("sha1");
        when(a3.isSetSha1()).thenReturn(true);

        Set<Attachment> after = new HashSet<>();
        after.add(a3);

        Set<String> deletedIds = new HashSet<>();
        deletedIds.add("a2cid");

        attachmentConnector.deleteAttachmentDifference(before, after);
        verify(connector).deleteIds(deletedIds, AttachmentContent.class);
    }


}
