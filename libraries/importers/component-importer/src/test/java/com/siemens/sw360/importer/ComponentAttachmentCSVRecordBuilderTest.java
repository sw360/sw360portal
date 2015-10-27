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

package com.siemens.sw360.importer;

import com.siemens.sw360.datahandler.common.SW360Utils;
import com.siemens.sw360.datahandler.thrift.attachments.Attachment;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentType;
import com.siemens.sw360.datahandler.thrift.components.Component;
import com.siemens.sw360.datahandler.thrift.components.Release;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author johannes.najjar@tngtech.com
 */
public class ComponentAttachmentCSVRecordBuilderTest {

    @Test
    public void testFillComponent() throws Exception {
        final String componentName = "myCompo";

        final Component component = new Component();
        component.setName(componentName);

        final ComponentAttachmentCSVRecordBuilder componentAttachmentCSVRecordBuilder = new ComponentAttachmentCSVRecordBuilder();
        componentAttachmentCSVRecordBuilder.fill(component);

        final ComponentAttachmentCSVRecord filledRecord = componentAttachmentCSVRecordBuilder.build();

        assertThat(filledRecord.getComponentName(), is(componentName));
    }

    @Test
    public void testFillRelease() throws Exception {
        final String releaseName =  "myRelease";
        final String releaseVersion =  "1.862b";

        final Release release = new Release();
        release.setName(releaseName).setVersion(releaseVersion);

        final ComponentAttachmentCSVRecordBuilder componentAttachmentCSVRecordBuilder =  new ComponentAttachmentCSVRecordBuilder();
        componentAttachmentCSVRecordBuilder.fill(release);

        final ComponentAttachmentCSVRecord filledRecord = componentAttachmentCSVRecordBuilder.build();

        assertThat(filledRecord.getReleaseIdentifier(), is(SW360Utils.getVersionedName(releaseName, releaseVersion)));
    }

    @Test
    public void testFillAttachment() throws Exception {
        final String attachmentContentID =  "asda823123123";
        final String fileName = "My.tar.gz";
        final String comment = "blabla";
        final AttachmentType attachmentType = AttachmentType.CLEARING_REPORT;
        final String createdBy = "Me";
        final String createdOn =  "Now";


        final Attachment attachment = new Attachment();
        attachment.setFilename(fileName)
                  .setAttachmentContentId(attachmentContentID)
                .setCreatedComment(comment)
                .setAttachmentType(attachmentType)
                .setCreatedBy(createdBy)
                .setCreatedOn(createdOn);


        final ComponentAttachmentCSVRecordBuilder componentAttachmentCSVRecordBuilder =  new ComponentAttachmentCSVRecordBuilder();
        componentAttachmentCSVRecordBuilder.fill(attachment);

        final ComponentAttachmentCSVRecord filledRecord = componentAttachmentCSVRecordBuilder.build();

        assertThat(filledRecord.getAttachment(), is(attachment));


    }
}