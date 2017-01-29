/*
 * Copyright Siemens AG, 2013-2016. Part of the SW360 Portal Project.
 * With contributions by Bosch Software Innovations GmbH, 2016.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.portal.common;

import org.eclipse.sw360.datahandler.common.CommonUtils;

import java.util.Properties;
import java.util.Set;

/**
 * Constants definitions for portlets
 *
 * @author cedric.bodet@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 * @author gerrit.grenzebach@tngtech.com
 * @author andreas.reichel@tngtech.com
 * @author stefan.jaeger@evosoft.com
 */
public class PortalConstants {

    public static final String PROPERTIES_FILE_PATH = "/sw360.properties";
    public static final String PROGRAMMING_LANGUAGES;
    public static final String SOFTWARE_PLATFORMS;
    public static final String OPERATING_SYSTEMS;
    public static final Set<String> SET_CLEARING_TEAMS_STRING;
    public static final String LICENSE_IDENTIFIERS;

    //! Role names
    public static final String ROLENAME_ADMIN = "Administrator";
    public static final String ROLENAME_CLEARING_ADMIN = "Clearing Admin";

    //! Standard keys for Lists and their size
    public static final String KEY_SUMMARY = "documents";

    public static final String KEY_LIST_SIZE = "documentssize";

    public static final String NO_FILTER = "noFilter";
    public static final String KEY_SEARCH_TEXT = "searchtext";
    public static final String KEY_SEARCH_FILTER_TEXT = "searchfilter";
    public static final String DOCUMENT_ID = "documentID";
    public static final String PAGENAME = "pagename";
    public static final String PAGENAME_DETAIL = "detail";
    public static final String PAGENAME_VIEW = "view";

    public static final String PAGENAME_IMPORT = "import";
    public static final String PAGENAME_EDIT = "edit";
    public static final String PAGENAME_ACTION = "action";
    public static final String PAGENAME_DUPLICATE = "duplicate";
    public static final String SELECTED_TAB = "selectedTab";
    public static final String IS_USER_AT_LEAST_CLEARING_ADMIN = "isUserAtLeastClearingAdmin";
    public static final String DOCUMENT_TYPE = "documentType";

    //! Specialized keys for licenses
    public static final String KEY_LICENSE_DETAIL = "licenseDetail";
    public static final String KEY_OBLIGATION_LIST = "obligationList";
    public static final String LICENSE_ID = "licenseid";
    public static final String LICENSE_TEXT = "licenseText";
    public static final String LICENSE_LIST = "licenseList";
    public static final String ACTUAL_LICENSE = "actual_license";
    public static final String ADDED_TODOS_FROM_MODERATION_REQUEST = "added_todos_from_moderation_request";
    public static final String DB_TODOS_FROM_MODERATION_REQUEST = "db_todos_from_moderation_request";
    public static final String MODERATION_LICENSE_DETAIL = "moderationLicenseDetail";
    public static final String LICENSE_TYPE_CHOICE = "licenseTypeChoice";

    //! Specialized keys for moderation
    public static final String MODERATION_ID = "moderationId";
    public static final String MODERATION_REQUEST = "moderationRequest";
    public static final String MODERATION_REQUESTS = "moderationRequests";
    public static final String DELETE_MODERATION_REQUEST = "deleteModerationRequest";

    //! Specialized keys for components
    public static final String COMPONENT_ID = "componentid";
    public static final String COMPONENT = "component";
    public static final String ACTUAL_COMPONENT = "actual_component";
    public static final String COMPONENT_LIST = "componentList";
    public static final String TYPE_MASK = "typeMask";
    public static final String COMPONENT_TYPE_LIST = "componentTypeList";
    public static final String COMPONENT_CATEGORIES;

    //! Specialized keys for releases
    public static final String RELEASE_ID = "releaseId";
    public static final String CLEARING_TEAM = "clearingTeam";
    public static final String RELEASE = "release";
    public static final String ACTUAL_RELEASE = "actual_release";
    public static final String PAGENAME_RELEASE_DETAIL = "detailRelease";
    public static final String PAGENAME_EDIT_RELEASE = "editRelease";
    public static final String PAGENAME_DUPLICATE_RELEASE = "duplicateRelease";

    //! Specialized keys for vendors
    public static final String VENDOR = "vendor";
    public static final String VENDOR_ID = "vendorId";
    public static final String VENDOR_LIST = "vendorList";

    //! Specialized keys for attachments
    public static final String ATTACHMENTS = "attachments";
    public static final String ADDED_ATTACHMENTS = "added_attachments";
    public static final String REMOVED_ATTACHMENTS = "removed_attachments";
    public static final String ATTACHMENT_ID = "attachmentId";

    //! Specialized keys for projects
    public static final String PROJECT_ID = "projectid";
    public static final String LINKED_PROJECT_ID = "linkedProjectId";
    public static final String PROJECT = "project";
    public static final String ACTUAL_PROJECT = "actual_project";
    public static final String USING_PROJECTS = "usingProjects";
    public static final String USING_COMPONENTS = "usingComponents";
    public static final String PROJECT_LIST = "projectList";
    public static final String RELEASE_LIST = "releaseList";
    public static final String PROJECT_SEARCH = "projectSearch";
    public static final String RELEASE_SEARCH = "releaseSearch";
    public static final String RELEASE_SEARCH_BY_VENDOR = "releaseSearchByVendor";
    public static final String RELEASE_LIST_FROM_LINKED_PROJECTS = "releaseListFromLinkedProjects";
    public static final String STATE;
    public static final String PROJECT_TYPE;
    public static final String EXTENDED_EXCEL_EXPORT = "extendedExcelExport";
    public static final String PROJECT_NOT_FOUND = "projectNotFound";
    public static final String PAGENAME_LICENSE_INFO = "generateLicenseInfo";
    public static final String PAGENAME_SOURCE_CODE_BUNDLE = "generateSourceCodeBundle";

    public static final String FOSSOLOGY_FINGER_PRINTS = "fingerPrints";
    public static final String USER_LIST = "userList";
    public static final String MISSING_USER_LIST = "missingUserList";

    //! Specialized keys for database Sanitation
    public static final String DUPLICATE_RELEASES = "duplicateReleases";
    public static final String DUPLICATE_RELEASE_SOURCES = "duplicateReleaseSources";
    public static final String DUPLICATE_COMPONENTS = "duplicateComponents";
    public static final String DUPLICATE_PROJECTS = "duplicateProjects";
    public static final String ACTION_DELETE_ALL_LICENSE_INFORMATION = "deleteAllLicenseInformation";


    //! Specialized keys for vulnerability management
    public static final String VULNERABILITY = "vulnerability";
    public static final String VULNERABILITY_LIST = "vulnerabilityList";
    public static final String VULNERABILITY_RATINGS = "vulnerabilityRatings";
    public static final String VULNERABILITY_RATINGS_EDITABLE = "vulnerabilityRatingsEditable";
    public static final String VULNERABILITY_ID = "vulnerabilityId";
    public static final String VULNERABILITY_RATING_VALUE = "vulnerabilityRatingValue";
    public static final String VULNERABILITY_RATING_COMMENT = "vulnerabilityRatingComment";
    public static final String NUMBER_OF_VULNERABILITIES = "numberOfVulnerabilities";
    public static final String NUMBER_OF_UNCHECKED_VULNERABILITIES = "numberOfUncheckedVulnerabilities";
    public static final String NUMBER_OF_INCORRECT_VULNERABILITIES = "numberOfIncorrectVulnerabilities";
    public static final String NUMBER_OF_CHECKED_OR_UNCHECKED_VULNERABILITIES = "numberOfCheckedOrUncheckedVulnerabilities";
    public static final String VULNERABILITY_CHECKSTATUS_TOOLTIPS = "vulnerabilityCheckstatusTooltips";
    public static final String VULNERABILITY_VERIFICATION_VALUE = "vulnerabilityVerificationValue";
    public static final String VULNERABILITY_VERIFICATION_COMMENT = "vulnerabilityVerificationComment";
    public static final String VULNERABILITY_VERIFICATION_EDITABLE = "vulnerabilityVerificationEditable";
    public static final String VULNERABILITY_VERIFICATION_TOOLTIPS = "vulnerabilityVerificationTooltips";
    public static final String VULNERABILITY_VERIFICATIONS = "vulnerabilityVerifications";
    public static final String VULNERABILITY_MATCHED_BY_HISTOGRAM = "vulnerabilityMatchedByHistogram";

    //! Specialized keys for account sign-up
    public static final String PASSWORD = "password";
    public static final String PASSWORD_REPEAT = "password_repeat";
    public static final String USER_GROUPS = "usergroups";
    public static final String USER = "newuser";
    public static final String ORGANIZATIONS = "organizations";
    public static final String PAGENAME_SUCCESS = "success";

    //! Specialized keys for users
    public static final String CUSTOM_FIELD_PROJECT_GROUP_FILTER = "ProjectGroupFilter";

    //! Specialized keys for scheduling
    public static final String CVESEARCH_IS_SCHEDULED = "cveSearchIsScheduled";
    public static final String ANY_SERVICE_IS_SCHEDULED = "anyServiceIsScheduled";
    public static final String CVESEARCH_OFFSET = "cvesearchOffset";
    public static final String CVESEARCH_INTERVAL = "cvesearchInterval";
    public static final String CVESEARCH_NEXT_SYNC = "cvesearchNextSync";
    
    //! Specialized keys for licenseInfo
    public static final String LICENSE_INFO_OUTPUT_FORMATS = "licenseInfoOutputFormats";
    public static final String LICENSE_INFO_SELECTED_OUTPUT_FORMAT = "licenseInfoSelectedOutputFormat";
    public static final String LICENSE_INFO_RELEASE_TO_ATTACHMENT = "licenseInfoAttachmentSelected";
    public static final String SW360_USER = "sw360User";

    //! Serve resource generic keywords
    public static final String ACTION = "action";
    public static final String ACTION_CANCEL = "action_cancel";
    public static final String ACTION_ACCEPT = "action_accept";
    public static final String ACTION_POSTPONE = "action_postpone";
    public static final String ACTION_DECLINE = "action_decline";
    public static final String ACTION_REMOVEME = "action_removeme";
    public static final String ACTION_RENDER_NEXT_AFTER_UNSUBSCRIBE = "action_render_next";
    public static final String WHAT = "what";

    public static final String WHERE = "where";
    public static final String WHERE_ARRAY = "where[]";
    public static final String HOW = "how";

    //! Serve resource keywords

    //! Actions
    // attachment actions
    public static final String ATTACHMENT_PREFIX = "Attachment";
    public static final String ATTACHMENT_UNLINK_AND_DELETE = ATTACHMENT_PREFIX + "Delete";
    public static final String ATTACHMENT_CANCEL = ATTACHMENT_PREFIX + "Cancel";
    public static final String ATTACHMENT_UPLOAD = ATTACHMENT_PREFIX + "Upload";
    public static final String ATTACHMENT_RESERVE_ID = ATTACHMENT_PREFIX + "Create";
    public static final String ATTACHMENT_LIST = ATTACHMENT_PREFIX + "List";
    public static final String ATTACHMENT_LINK_TO = ATTACHMENT_PREFIX + "LinkTo";
    public static final String ATTACHMENT_DOWNLOAD = ATTACHMENT_PREFIX + "Download";

    public static final String ATTACHMENT_DELETE_ON_CANCEL = "attachmentDeleteOnCancel";

    public static final String CLEANUP = "Cleanup";
    public static final String DUPLICATES = "Duplicates";
    public static final String DOWNLOAD = "Download";
    public static final String DOWNLOAD_SAMPLE = "DownloadSample";
    public static final String DOWNLOAD_ATTACHMENT_INFO = "DownloadAttachmentInfo";
    public static final String DOWNLOAD_SAMPLE_ATTACHMENT_INFO = "DownloadSampleAttachmentInfo";
    public static final String DOWNLOAD_SAMPLE_RELEASE_LINK_INFO = "DownloadSampleReleaseLinkInfo";
    public static final String DOWNLOAD_RELEASE_LINK_INFO = "DownloadReleaseLinkInfo";
    public static final String DOWNLOAD_LICENSE_BACKUP = "DownloadLicenseBackup";

    // project actions
    public static final String VIEW_LINKED_PROJECTS = "view_linked_projects";
    public static final String REMOVE_PROJECT = "remove_projects";
    public static final String LIST_NEW_LINKED_PROJECTS = "add_to_linked_projects";
    public static final String VIEW_LINKED_RELEASES = "view_linked_releases";
    public static final String LIST_NEW_LINKED_RELEASES = "add_to_linked_releases";
    public static final String DOWNLOAD_LICENSE_INFO = "DownloadLicenseInfo";
    public static final String DOWNLOAD_SOURCE_CODE_BUNDLE = "DownloadSourceCodeBundle";

    //component actions
    public static final String ADD_VENDOR = "add_vendor";
    public static final String VIEW_VENDOR = "view_vendor";
    public static final String DELETE_COMPONENT = "delete_component";
    public static final String DELETE_RELEASE = "delete_release";
    public static final String SUBSCRIBE = "subscribe";
    public static final String SUBSCRIBE_RELEASE = "subscribe_release";
    public static final String UNSUBSCRIBE = "unsubscribe";
    public static final String UNSUBSCRIBE_RELEASE = "unsubscribe_release";

    // fossology actions
    public static final String FOSSOLOGY_PREFIX = "fossology";
    public static final String FINGER_PRINTS = "fingerPrints";
    public static final String FOSSOLOGY_GET_PUBKEY = FOSSOLOGY_PREFIX + "get_pub";
    public static final String FOSSOLOGY_DEPLOY_SCRIPTS = FOSSOLOGY_PREFIX + "deploy_scripts";
    public static final String FOSSOLOGY_CHECK_CONNECTION = FOSSOLOGY_PREFIX + "check_connection";
    public static final String FOSSOLOGY_SEND = FOSSOLOGY_PREFIX + "send";
    public static final String FOSSOLOGY_GET_SENDABLE = FOSSOLOGY_PREFIX + "get_sendable";
    public static final String FOSSOLOGY_GET_STATUS = FOSSOLOGY_PREFIX + "get_status";

    public static final String RELEASES_AND_PROJECTS = "releasesAndProjects";

    // vendor actions
    public static final String REMOVE_VENDOR = "remove_vendor";

    // user actions
    public static final String USER_PREFIX = "user";
    public static final String USER_SEARCH = USER_PREFIX + "search";

    // license actions
    public static final String LICENSE_PREFIX = "license";
    public static final String LICENSE_SEARCH = LICENSE_PREFIX + "search";

    //vulnerability actions
    public static  final String UPDATE_VULNERABILITIES_RELEASE = "updateVulnerabilitiesRelease";
    public static  final String UPDATE_VULNERABILITIES_COMPONENT = "updateVulnerabilitiesComponent";
    public static  final String UPDATE_ALL_VULNERABILITIES = "updateAllVulnerabilities";
    public static  final String UPDATE_VULNERABILITIES_PROJECT = "updateVulnerabilitiesProject";
    public static  final String UPDATE_VULNERABILITY_RATINGS = "updateVulnerabilityRatings";
    public static  final String UPDATE_VULNERABILITY_VERIFICATION = "updateVulnerabilityVerification";

    // Excel export
    public static final String EXPORT_TO_EXCEL = "export_to_excel";
    public static final String EXPORT_ID = "export_id";

    // Import Projects
    public static final String CHECKED_PROJECT = "checked_project";

    // Import credentials
    public static final String SESSION_IMPORT_USER = "importProject_import_user";
    public static final String SESSION_IMPORT_DB = "importProject_import_db";
    public static final String SESSION_IMPORT_PASS = "importProject_import_pass";
    public static final String SESSION_IMPORT_URL = "importProject_import_url";

    // Import Actions
    public static final String IMPORT_USER_ACTION__IMPORTBDP = "importProject_importBdpData";

    public static final String IMPORT_USER_ACTION = "importProject_import_action";
    public static final String IMPORT_USER_ACTION__DISCONNECT = "importProject_disconnect";
    public static final String IMPORT_USER_ACTION__NEWIMPORTSOURCE = "importProject_select_new_import_source";
    public static final String IMPORT_USER_ACTION__UPDATEIMPORTABLES = "importProject_update_importables";

    // Import Status
    public static final String IMPORT_RESPONSE__IMPORT_BDP_SUCCESS = "importProject_response_bdp_import_success";
    public static final String IMPORT_RESPONSE__IMPORT_BDP_FAILURE = "importProject_response_bdp_import_failure";
    public static final String IMPORT_RESPONSE__IMPORT_BDP_GENERAL_FAILURE = "importProject_response_bdp_import_general_failure";
    public static final String RESPONSE__IMPORT_GENERAL_FAILURE = "response_import_general_failure";


    public static final String IMPORT_RESPONSE__STATUS = "importProject_response_status";
    public static final String IMPORT_RESPONSE__DBURL = "importProject_response_dburl";
    public static final String IMPORT_RESPONSE__DB_CHANGED = "importProject_response_db_changed";
    public static final String IMPORT_RESPONSE__DB_URL_NOTSET = "importProject_response_db_url_notset";
    public static final String IMPORT_RESPONSE__UNAUTHORIZED = "importProject_response_unauthorized";
    public static final String IMPORT_RESPONSE__DB_CONNECT_ERROR = "importProject_response_db_connect_error";
    public static final String IMPORT_RESPONSE__NEW_IMPORTABLES = "importProject_response_new_importables";

    public static final String IMPORT_RESPONSE__SUCCESSFUL_IDS = "importProject_response_successfulIds";
    public static final String IMPORT_RESPONSE__FAILED_IDS = "importProject_response_failedIds";
    public static final String UPDATE_VULNERABILITIES__FAILED_IDS = "updateVulnerabilities_failedIds";
    public static final String UPDATE_VULNERABILITIES__NEW_IDS = "updateVulnerabilities_newIds";
    public static final String UPDATE_VULNERABILITIES__UPDATED_IDS = "updateVulnerabilities_updatedIds";

    //! request status
    public static final String REQUEST_STATUS = "request_status";

    static {
        Properties props = CommonUtils.loadProperties(PortalConstants.class, PROPERTIES_FILE_PATH);

        PROGRAMMING_LANGUAGES = props.getProperty("programming.languages", "[ \"ActionScript\", \"AppleScript\", \"Asp\",\"Bash\", \"BASIC\", \"C\", \"C++\", \"C#\", \"Cocoa\", \"Clojure\",\"COBOL\",\"ColdFusion\", \"D\", \"Delphi\", \"Erlang\", \"Fortran\", \"Go\", \"Groovy\",\"Haskell\", \"JSP\", \"Java\",\"JavaScript\", \"Objective-C\", \"Ocaml\",\"Lisp\", \"Perl\", \"PHP\", \"Python\", \"Ruby\", \"SQL\", \"SVG\",\"Scala\",\"SmallTalk\", \"Scheme\", \"Tcl\", \"XML\", \"Node.js\", \"JSON\" ]");
        SOFTWARE_PLATFORMS = props.getProperty("software.platforms", "[ \"Adobe AIR\", \"Adobe Flash\", \"Adobe Shockwave\", \"Binary Runtime Environment for Wireless\", \"Cocoa (API)\", \"Cocoa Touch\", \"Java (software platform)|Java platform\", \"Java Platform, Micro Edition\", \"Java Platform, Standard Edition\", \"Java Platform, Enterprise Edition\", \"JavaFX\", \"JavaFX Mobile\", \"Microsoft XNA\", \"Mono (software)|Mono\", \"Mozilla Prism\", \".NET Framework\", \"Silverlight\", \"Open Web Platform\", \"Oracle Database\", \"Qt (framework)|Qt\", \"SAP NetWeaver\", \"Smartface\", \"Vexi\", \"Windows Runtime\" ]");
        OPERATING_SYSTEMS = props.getProperty("operating.systems", "[ \"Android\", \"BSD\", \"iOS\", \"Linux\", \"OS X\", \"QNX\", \"Microsoft Windows\", \"Windows Phone\", \"IBM z/OS\"]");
        SET_CLEARING_TEAMS_STRING = CommonUtils.splitToSet(props.getProperty("clearing.teams", "org1,org2,org3"));
        STATE = props.getProperty("state","[ \"Active\", \"Phase out\", \"Unknown\"]");
        PROJECT_TYPE = props.getProperty("project.type","[ \"Customer Project\", \"Internal Project\", \"Product\", \"Service\"]");
        LICENSE_IDENTIFIERS = props.getProperty("license.identifiers", "[]");
        COMPONENT_CATEGORIES = props.getProperty("component.categories", "[ \"framework\", \"SDK\", \"big-data\", \"build-management\", \"cloud\", \"content\", \"database\", \"graphics\", \"http\", \"javaee\", \"library\", \"mail\", \"mobile\", \"security\", \"testing\", \"virtual-machine\", \"web-framework\", \"xml\"]");
    }

    private PortalConstants() {
        // Utility class with only static functions
    }
}
