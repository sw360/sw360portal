/*
 * Copyright Siemens AG, 2014-2015. Part of the SW360 Portal Project.
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

import com.siemens.sw360.datahandler.thrift.attachments.AttachmentType;
import org.junit.Test;

import java.util.Collection;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

/**
 * @author daniele.fognini@tngtech.com
 */
public class SW360ConstantsTest {
    @Test
    public void testProjectsCanHaveAllAttachmentTypes() throws Exception {
        Collection<AttachmentType> types = SW360Constants.allowedAttachmentTypes(SW360Constants.TYPE_PROJECT);

        assertThat(types, containsInAnyOrder(AttachmentType.values()));
    }

    @Test
    public void testComponentsCanNotHaveReports() throws Exception {
        Collection<AttachmentType> types = SW360Constants.allowedAttachmentTypes(SW360Constants.TYPE_COMPONENT);

        for (AttachmentType attachmentType : AttachmentType.values()) {
            if (attachmentType == AttachmentType.CLEARING_REPORT) {
                assertThat(types, not(hasItem(equalTo(attachmentType))));
            } else {
                assertThat(types, hasItem(equalTo(attachmentType)));
            }
        }

    }
}