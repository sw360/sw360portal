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

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableListMultimap;
import com.siemens.sw360.components.summary.ReleaseSummary;
import com.siemens.sw360.components.summary.SummaryType;
import com.siemens.sw360.datahandler.couchdb.DatabaseConnector;
import com.siemens.sw360.datahandler.couchdb.SummaryAwareRepository;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.users.User;

import org.ektorp.ViewQuery;
import org.ektorp.support.View;

import java.util.List;
import java.util.Set;

/**
 * CRUD access for the Release class
 *
 * @author cedric.bodet@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 */
@View(name = "all", map = "function(doc) { if (doc.type == 'release') emit(null, doc._id) }")
public class ReleaseRepository extends SummaryAwareRepository<Release> {

    private static final String BY_NAME_VIEW = "function(doc) { if(doc.type == 'release') { emit(doc.name, doc) } }";

    private static final String RECENT_VIEW = "function(doc) { if(doc.type == 'release') { emit(doc.createdOn, doc._id) } }";

    private static final String SUBSCRIBERS_VIEW =
            "function(doc) {" +
                    " if (doc.type == 'release'){" +
                    "    for(var i in doc.subscribers) {" +
                    "      emit(doc.subscribers[i], doc._id);" +
                    "    }" +
                    "  }" +
                    "}";

    private static final String RELEASE_BY_VENDORID_VIEW =
            "function(doc) {" +
                    " if (doc.type == 'release'){" +
                    "     emit(doc.vendorId, doc);" +
                    "  }" +
                    "}";

    private static final String RELEASES_BY_COMPONENT_ID =
            "function(doc) {" +
                    " if (doc.type == 'release'){" +
                    "      emit(doc.componentId, doc);" +
                    "  }" +
                    "}";

    private static final String RELEASE_IDS_BY_LICENSE_ID =
            "function(doc) {" +
                      "  if (doc.type == 'release'){" +
                      "    for(var i in doc.mainLicenseIds) {" +
                      "      emit(doc.mainLicenseIds[i], doc);" +
                      "    }" +
                      "  }" +
                      "}";

    private static final String MY_COMPONENTS_VIEW =
            "function(doc) {" +
                    " if (doc.type == 'release'){" +
                    "    emit(doc.createdBy, doc.componentId);" +
                    "    for(var i in doc.contacts) {" +
                    "      emit(doc.contacts[i], doc.componentId);" +
                    "    }" +
                    "    for(var i in doc.moderators) {" +
                    "      emit(doc.moderators[i], doc.componentId);" +
                    "    }" +
                    "  }" +
                    "}";

    private static final String FULL_RELEASE_BY_COMPONENTID_VIEW =
            "function(doc) {" +
                    " if (doc.type == 'release'){" +
                    "     emit(doc.componentId, doc);" +
                    "  }" +
                    "}";

    public ReleaseRepository(DatabaseConnector db, VendorRepository vendorRepository) {
        super(Release.class, db, new ReleaseSummary(vendorRepository));

        initStandardDesignDocument();
    }

    @View(name = "byname", map = BY_NAME_VIEW)
    public List<Release> searchByName(String name) {
        return makeSummary(SummaryType.SHORT, queryForIdsByPrefix("byname", name));
    }

    public List<Release> getReleaseSummary() {
        return makeSummary(SummaryType.SUMMARY, getAllIds());
    }

    @View(name = "recent", map = RECENT_VIEW)
    public List<Release> getRecentReleases() {
        ViewQuery query = createQuery("recent");
        // Get the 5 last documents
        query.limit(5).descending(true).includeDocs(false);
        return makeSummary(SummaryType.SHORT, queryForIds(query));
    }

    @View(name = "subscribers", map = SUBSCRIBERS_VIEW)
    public List<Release> getSubscribedReleases(String email) {
        Set<String> ids = queryForIds("subscribers", email);
        return makeSummary(SummaryType.SHORT, ids);
    }

    @View(name = "releaseIdByVendorId", map = RELEASE_BY_VENDORID_VIEW)
    public List<Release> getReleasesFromVendorId(String id, User user) {
        return  makeSummaryWithPermissionsFromFullDocs(SummaryType.SUMMARY, queryView("releaseIdByVendorId", id), user);
    }

    @View(name = "releasesByComponentId", map = RELEASES_BY_COMPONENT_ID)
    public List<Release> getReleasesFromComponentId(String id, User user) {
        return makeSummaryWithPermissionsFromFullDocs(SummaryType.SUMMARY, queryView("releasesByComponentId", id), user);
    }

    @View(name = "mycomponents", map = MY_COMPONENTS_VIEW)
    public Set<String> getMyComponentIds(String user) {
        return queryForIdsAsValue("mycomponents", user);
    }

    @View(name = "fullbycomponentId", map = FULL_RELEASE_BY_COMPONENTID_VIEW)
    public List<Release> getFullReleasesByComponentid(String componentId) {
        return queryView("fullbycomponentId", componentId);
    }

    public ImmutableListMultimap<String, Release> getFullReleases() {
        return FluentIterable.from(queryView("fullbycomponentId")).index(Release::getComponentId);
    }

    public List<Release> getReleasesFromVendorIds(Set<String> ids) {

        return makeSummaryFromFullDocs(SummaryType.SHORT, queryByIds("releaseIdByVendorId", ids));
    }

    @View(name = "releaseIdsByLicenseId", map = RELEASE_IDS_BY_LICENSE_ID)
    public List<Release> searchReleasesByUsingLicenseId(String licenseId) {

        return queryView("releaseIdsByLicenseId", licenseId);
    }
}
