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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.siemens.sw360.datahandler.db.ReleaseRepository;
import com.siemens.sw360.datahandler.db.VendorRepository;
import com.siemens.sw360.datahandler.thrift.ThriftUtils;
import com.siemens.sw360.datahandler.thrift.components.Component;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.vendors.Vendor;

import java.util.*;

import static com.siemens.sw360.datahandler.thrift.ThriftUtils.copyField;

/**
 * Created by bodet on 17/02/15.
 *
 * @author cedric.bodet@tngtech.com
 */
public class ComponentSummary extends DocumentSummary<Component> {

    private final ReleaseRepository releaseRepository;
    private final VendorRepository vendorRepository;

    public ComponentSummary() {
        // Create summary without database connection
        this(null, null);
    }

    public ComponentSummary(ReleaseRepository releaseRepository, VendorRepository vendorRepository) {
        this.releaseRepository = releaseRepository;
        this.vendorRepository = vendorRepository;
    }

    @Override
    public List<Component> makeSummary(SummaryType type, Collection<Component> fullDocuments) {
        if (fullDocuments == null) return Collections.emptyList();
        List<Component> documents = new ArrayList<>(fullDocuments.size());
        for (Component fullDocument : fullDocuments) {
            Component document = summary(type, fullDocument);
            if (document != null) documents.add(document);
        }
        return documents;
    }

    @Override
    protected Component summary(SummaryType type, Component document) {

        Component copy = new Component();
        if (type == SummaryType.EXPORT_SUMMARY) {
            ImmutableListMultimap<String, Release> fullReleases = releaseRepository.getFullReleases();
            return makeExportSummary(document, fullReleases);
        } else if (type == SummaryType.DETAILED_EXPORT_SUMMARY) {
                ImmutableListMultimap<String, Release> fullReleases = releaseRepository.getFullReleases();

                final Map<String, Vendor> vendorsById = ThriftUtils.getIdMap(vendorRepository.getAll());

                for (Release release : fullReleases.values()) {
                    if (!release.isSetVendor() && release.isSetVendorId()) {
                        release.setVendor(vendorsById.get(release.getVendorId()));
                    }
                }

                return makeDetailedExportSummary(document, fullReleases);
            }


        copyField(document, copy, Component._Fields.ID);
        copyField(document, copy, Component._Fields.NAME);
        copyField(document, copy, Component._Fields.VENDOR_NAMES);
        copyField(document, copy, Component._Fields.COMPONENT_TYPE);


        if (type == SummaryType.SUMMARY) {
            copyField(document, copy, Component._Fields.DESCRIPTION);
            copyField(document, copy, Component._Fields.CATEGORIES);
        }

        return copy;
    }

    private Component makeDetailedExportSummary(Component document, ImmutableListMultimap<String, Release> fullReleases) {

        final ImmutableList<Release> releases = fullReleases.get(document.getId());
        document.setReleases(releases);

        return document;
    }

    private Component makeExportSummary(Component document, ImmutableListMultimap<String, Release> fullReleases) {

        if (releaseRepository == null) {
            throw new IllegalStateException("Cannot make export summary without database connection!");
        }

        Component copy = new Component();

        copyField(document, copy, Component._Fields.ID);
        copyField(document, copy, Component._Fields.NAME);
        copyField(document, copy, Component._Fields.LANGUAGES);
        copyField(document, copy, Component._Fields.OPERATING_SYSTEMS);
        copyField(document, copy, Component._Fields.SOFTWARE_PLATFORMS);
        copyField(document, copy, Component._Fields.CREATED_BY);
        copyField(document, copy, Component._Fields.CREATED_ON);
        copyField(document, copy, Component._Fields.VENDOR_NAMES);


        final ImmutableList<Release> releases = fullReleases.get(document.getId());

        for (Release release : releases) {
            Release exportRelease = new Release();
            copyField(release, exportRelease, Release._Fields.NAME);
            copyField(release, exportRelease, Release._Fields.VERSION);
            exportRelease.setComponentId("");
            copy.addToReleases(exportRelease);
        }

        return copy;

    }


}
