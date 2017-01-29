/*
 * Copyright Siemens AG, 2014-2016. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.datahandler.common;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.eclipse.sw360.datahandler.thrift.attachments.AttachmentType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Sets.newHashSet;

/**
 * Utility class with definitions for the CouchDB connection
 *
 * @author cedric.bodet@tngtech.com
 * @author alex.borodin@evosoft.com
 */
public class SW360Constants {

    public static final String KEY_ID = "_id";
    public static final String KEY_REV = "_rev";

    public static final String PROJECT_VULNERABILITY_RATING_ID_PREFIX = "pvrating_";
    // Proper values of the "type" member to deserialize to CouchDB
    public static final String TYPE_OBLIGATION = "obligation";
    public static final String TYPE_TODO = "todo";
    public static final String TYPE_RISKCATEGORY = "riskCategory";
    public static final String TYPE_RISK = "risk";
    public static final String TYPE_LICENSETYPE = "licenseType";
    public static final String TYPE_LICENSE = "license";
    public static final String TYPE_VENDOR = "vendor";
    public static final String TYPE_USER = "user";
    public static final String TYPE_COMPONENT = "component";
    public static final String TYPE_RELEASE = "release";
    public static final String TYPE_ATTACHMENT = "attachment";
    public static final String TYPE_PROJECT = "project";
    public static final String TYPE_MODERATION = "moderation";

    /**
     * Hashmap containing the name field for each type.
     * Used by the search service to fill the search results
     */
    public static final Map<String, String> MAP_FULLTEXT_SEARCH_NAME =
            ImmutableMap.<String, String>builder()
                    .put(TYPE_LICENSE, "fullname")
                    .put(TYPE_TODO, "text")
                    .put(TYPE_OBLIGATION, "name")
                    .put(TYPE_USER, "email")
                    .put(TYPE_VENDOR, "fullname")
                    .put(TYPE_COMPONENT, "name")
                    .put(TYPE_RELEASE, "name version")
                    .put(TYPE_PROJECT, "name")
                    .build();

    public static final Collection<AttachmentType> LICENSE_INFO_ATTACHMENT_TYPES = Arrays.asList(AttachmentType.COMPONENT_LICENSE_INFO_XML, AttachmentType.COMPONENT_LICENSE_INFO_COMBINED);
    public static final Collection<AttachmentType> SOURCE_CODE_ATTACHMENT_TYPES = Arrays.asList(AttachmentType.SOURCE, AttachmentType.SOURCE_SELF);

    public static Collection<AttachmentType> allowedAttachmentTypes(String documentType) {
        Set<AttachmentType> types = newHashSet(AttachmentType.values());

        if (TYPE_COMPONENT.equals(documentType)) {
            return Sets.filter(types, not(equalTo(AttachmentType.CLEARING_REPORT)));
        } else {
            return types;
        }
    }

    private SW360Constants() {
        // Utility class with only static functions
    }

}
