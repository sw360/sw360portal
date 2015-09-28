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
package com.siemens.sw360.datahandler.thrift;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.siemens.sw360.datahandler.thrift.attachments.Attachment;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author daniele.fognini@tngtech.com
 */
public class ThriftUtilsTest {


    private Function<Attachment, Object> objectIdExtractor;
    private Function<Attachment, String> stringIdExtractor;

    @Before
    public void setUp() throws Exception {
        objectIdExtractor = ThriftUtils.extractField(Attachment._Fields.ATTACHMENT_CONTENT_ID);
        stringIdExtractor = ThriftUtils.extractField(Attachment._Fields.ATTACHMENT_CONTENT_ID, String.class);
    }

    @Test
    public void testExtractId() throws Exception {
        String contentId = "42";
        Attachment attachment = getAttachment(contentId);

        assertThat(objectIdExtractor.apply(attachment), is((Object) contentId));
        assertThat(stringIdExtractor.apply(attachment), is(contentId));
    }

    @Test
    public void testExtractIdTransformer() throws Exception {
        String contentId = "42";
        String contentId1 = "44";
        List<Attachment> input = ImmutableList.of(getAttachment(contentId), getAttachment(contentId1));
        List<String> expected = ImmutableList.of(contentId, contentId1);

        assertThat(Lists.transform(input, stringIdExtractor), is(expected));
    }

    private static Attachment getAttachment(String contentId) {
        return new Attachment().setAttachmentContentId(contentId);
    }
}