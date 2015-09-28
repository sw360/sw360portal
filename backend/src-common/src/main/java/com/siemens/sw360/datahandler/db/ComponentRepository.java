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
package com.siemens.sw360.datahandler.db;

import com.siemens.sw360.components.summary.ComponentSummary;
import com.siemens.sw360.components.summary.SummaryType;
import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.couchdb.DatabaseConnector;
import com.siemens.sw360.datahandler.couchdb.SummaryAwareRepository;
import com.siemens.sw360.datahandler.thrift.components.Component;
import com.siemens.sw360.datahandler.thrift.users.User;
import org.ektorp.ViewQuery;
import org.ektorp.support.View;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * CRUD access for the Component class
 *
 * @author cedric.bodet@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 */
@View(name = "all", map = "function(doc) { if (doc.type == 'component') emit(null, doc._id) }")
public class ComponentRepository extends SummaryAwareRepository<Component> {

    private static final String RECENT_VIEW = "function(doc) { if(doc.type == 'component') { emit(doc.createdOn, doc._id) } }";

    private static final String SUBSCRIBERS_VIEW =
            "function(doc) {" +
                    "  if (doc.type == 'component') {" +
                    "    for(var i in doc.subscribers) {" +
                    "      emit(doc.subscribers[i], doc._id);" +
                    "    }" +
                    "  }" +
                    "}";

    private static final String MY_COMPONENTS_VIEW =
            "function(doc) {" +
                    "  if (doc.type == 'component') {" +
                    "    emit(doc.createdBy, doc._id);" +
                    "  } " +
                    "}";

    private static final String BY_NAME_VIEW =
            "function(doc) {" +
                    "  if (doc.type == 'component') {" +
                    "    emit(doc.name, doc._id);" +
                    "  } " +
                    "}";

    private static final String FULL_BY_NAME_VIEW =
            "function(doc) {" +
                    "  if (doc.type == 'component') {" +
                    "    emit(doc.name, doc);" +
                    "  } " +
                    "}";

    private static final String BY_FOSSOLOGY_ID =
            "function(doc) {  " +
                    "if (doc.type == 'release') { " +
                    "   if(doc.fossologyId) {    " +
                    "       emit(doc.fossologyId, doc.componentId);  " +
                    "   } " +
                    "} " +
                    "}";

    private static final String ALL_COMPONENTS =
            "function(doc) {" +
                    "  if (doc.type == 'component') {" +
                    "    emit(doc.id, doc);" +
                    "  } " +
                    "}";

    private static final String BY_LINKING_RELEASE_ID_VIEW =
            "function(doc) {" +
                    "  if (doc.type == 'release') {" +
                    "    for(var i in doc.releaseIdToRelationship) {" +
                    "      emit(i, doc.componentId);" +
                    "    }" +
                    "  }" +
                    "}";

    private static final String USED_ATTACHMENT_CONTENT_IDS =
            "function(doc) { " +
                    "        if(doc.type == 'release' || doc.type == 'component' || doc.type == 'project') {" +
                    "            for(var i in doc.attachments){" +
                    "                emit(null, doc.attachments[i].attachmentContentId);" +
                    "            }" +
                    "        }" +
                    "    }";


    public ComponentRepository(DatabaseConnector db, ReleaseRepository releaseRepository, VendorRepository vendorRepository) {
        super(Component.class, db, new ComponentSummary(releaseRepository, vendorRepository));

        initStandardDesignDocument();
    }

    @View(name = "recent", map = RECENT_VIEW)
    public List<Component> getRecentComponents() {
        ViewQuery query = createQuery("recent");
        // Get the 5 last documents
        query.limit(5).descending(true).includeDocs(false);
        return makeSummary(SummaryType.SHORT, queryForIds(query));
    }

    @View(name = "usedAttachmentContents", map = USED_ATTACHMENT_CONTENT_IDS)
    public Set<String> getUsedAttachmentContents() {
        return queryForIdsAsValue(createQuery("usedAttachmentContents"));
    }

    @View(name = "mycomponents", map = MY_COMPONENTS_VIEW)
    public Set<String> getMyComponentIds(String user) {
        return queryForIdsAsValue("mycomponents", user);
    }

    @View(name = "subscribers", map = SUBSCRIBERS_VIEW)
    public List<Component> getSubscribedComponents(String user) {
        Set<String> ids = queryForIds("subscribers", user);
        return makeSummary(SummaryType.SHORT, ids);
    }

    @View(name = "allDocs", map = ALL_COMPONENTS)
    public List<Component> getSummaryForExport() {
        final List<Component> componentList = queryView("allDocs");
        return makeSummaryFromFullDocs(SummaryType.EXPORT_SUMMARY, componentList);
    }

    public List<Component> getDetailedSummaryForExport() {
        final List<Component> componentList = queryView("allDocs");
        return makeSummaryFromFullDocs(SummaryType.DETAILED_EXPORT_SUMMARY, componentList);
    }

    public List<Component> getComponentSummary(User user) {
        final List<Component> componentList = queryView("allDocs");
        return makeSummaryWithPermissionsFromFullDocs(SummaryType.SUMMARY, componentList, user);
    }

    @View(name = "byname", map = BY_NAME_VIEW)
    public Set<String> getMyComponentIdsByName(String name) {
        return queryForIdsAsValue("byname", name);
    }

    @View(name = "fullbyname", map = FULL_BY_NAME_VIEW)
    public List<Component> searchByNameForExport(String name) {
        final List<Component> componentList = queryByPrefix("fullbyname", name);
        return makeSummaryFromFullDocs(SummaryType.EXPORT_SUMMARY, componentList);
    }

    @View(name = "byLinkingRelease", map = BY_LINKING_RELEASE_ID_VIEW)
    public Set<Component> getUsingComponents(String releaseId) {
        final Set<String> componentIdsByLinkingRelease = queryForIdsAsValue("byLinkingRelease", releaseId);
        return new HashSet<>(get(componentIdsByLinkingRelease));
    }

    @View(name = "byFossologyId", map = BY_FOSSOLOGY_ID)
    public Component getComponentFromFossologyUploadId(String fossologyUploadId) {
        final Set<String> componentIdList = queryForIdsAsValue("byFossologyId", fossologyUploadId);
        if (componentIdList != null && componentIdList.size() > 0)
            return get(CommonUtils.getFirst(componentIdList));
        return null;
    }

    public Set<Component> getUsingComponents(Set<String> releaseIds) {
        final Set<String> componentIdsByLinkingRelease = queryForIdsAsValue("byLinkingRelease", releaseIds);
        return new HashSet<>(get(componentIdsByLinkingRelease));
    }
}
