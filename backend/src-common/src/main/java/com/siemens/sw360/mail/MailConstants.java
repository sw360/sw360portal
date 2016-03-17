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
package com.siemens.sw360.mail;

import com.siemens.sw360.datahandler.common.CommonUtils;

import java.util.Properties;

/**
 * Constants for the MailUtil class.
 *
 * @author birgit.heydenreich@tngtech.com
 */
public class MailConstants {

    public static final String MAIL_PROPERTIES_FILE_PATH = "/sw360.properties";

    public static final String DEFAULT_BEGIN = "defaultBegin";
    public static final String DEFAULT_END = "defaultEnd";
    public static final String UNSUBSCRIBE_NOTICE_BEFORE = "unsubscribeNoticeBefore";
    public static final String UNSUBSCRIBE_NOTICE_AFTER = "unsubscribeNoticeAfter";

    public static final String SUBJECT_FOR_NEW_MODERATION_REQUEST = "subjectForNewModerationRequest";
    public static final String SUBJECT_FOR_UPDATE_MODERATION_REQUEST = "subjectForUpdateModerationRequest";
    public static final String SUBJECT_FOR_ACCEPTED_MODERATION_REQUEST = "subjectForAcceptedModerationRequest";
    public static final String SUBJECT_FOR_DECLINED_MODERATION_REQUEST = "subjectForDeclinedModerationRequest";

    public static final String TEXT_FOR_NEW_MODERATION_REQUEST = "textForNewModerationRequest";
    public static final String TEXT_FOR_UPDATE_MODERATION_REQUEST = "textForUpdateModerationRequest";
    public static final String TEXT_FOR_ACCEPTED_MODERATION_REQUEST = "textForAcceptedModerationRequest";
    public static final String TEXT_FOR_DECLINED_MODERATION_REQUEST = "textForDeclinedModerationRequest";

    private MailConstants() {
        // Utility class with only static functions
    }

}
