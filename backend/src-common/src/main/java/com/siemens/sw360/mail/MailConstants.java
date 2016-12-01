/*
 * Copyright Siemens AG, 2016. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.mail;

import org.eclipse.sw360.datahandler.common.CommonUtils;

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
    public static final String SUBJECT_FOR_DECLINED_USER_MODERATION_REQUEST = "subjectForDeclinedUserModerationRequest";

    public static final String TEXT_FOR_NEW_MODERATION_REQUEST = "textForNewModerationRequest";
    public static final String TEXT_FOR_UPDATE_MODERATION_REQUEST = "textForUpdateModerationRequest";
    public static final String TEXT_FOR_ACCEPTED_MODERATION_REQUEST = "textForAcceptedModerationRequest";
    public static final String TEXT_FOR_DECLINED_MODERATION_REQUEST = "textForDeclinedModerationRequest";
    public static final String TEXT_FOR_DECLINED_USER_MODERATION_REQUEST = "textForDeclinedUserModerationRequest";

    private MailConstants() {
        // Utility class with only static functions
    }

}
