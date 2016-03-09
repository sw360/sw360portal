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

import com.siemens.sw360.datahandler.thrift.attachments.AttachmentContent;

public class AttachmentContentWrapperTest extends DocumentWrapperTest<AttachmentContentWrapper, AttachmentContent, AttachmentContent._Fields> {

    public void testUpdateNonMetadataTouchesAllFields() throws Exception {
        AttachmentContent source;
        source = new AttachmentContent();
        source.setFilename("a");
        source.setType("b");
        source.setContentType("v");
        source.setPartsCount("1");
        source.setRemoteUrl("uskt"); //TODO this is not required !

        AttachmentContentWrapper attachmentContentWrapper = new AttachmentContentWrapper();
        attachmentContentWrapper.updateNonMetadata(source);

        assertTFields(source, attachmentContentWrapper, AttachmentContentWrapper.class, AttachmentContent._Fields.class);
    }
}
