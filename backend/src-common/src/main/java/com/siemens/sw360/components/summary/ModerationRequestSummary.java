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
package com.siemens.sw360.components.summary;

import com.siemens.sw360.datahandler.thrift.moderation.ModerationRequest;

import static com.siemens.sw360.datahandler.thrift.ThriftUtils.copyField;

/**
 * Created by bodet on 17/02/15.
 *
 * @author cedric.bodet@tngtech.com
 */
public class ModerationRequestSummary extends DocumentSummary<ModerationRequest> {

    @Override
    protected ModerationRequest summary(SummaryType type, ModerationRequest document) {
        ModerationRequest copy = new ModerationRequest();

        copyField(document, copy, ModerationRequest._Fields.ID);
        copyField(document, copy, ModerationRequest._Fields.DOCUMENT_ID);
        copyField(document, copy, ModerationRequest._Fields.DOCUMENT_TYPE);
        copyField(document, copy, ModerationRequest._Fields.DOCUMENT_NAME);
        copyField(document, copy, ModerationRequest._Fields.MODERATION_STATE);
        copyField(document, copy, ModerationRequest._Fields.REQUESTING_USER);
        copyField(document, copy, ModerationRequest._Fields.MODERATORS);
        copyField(document, copy, ModerationRequest._Fields.TIMESTAMP);

        return copy;
    }

}
