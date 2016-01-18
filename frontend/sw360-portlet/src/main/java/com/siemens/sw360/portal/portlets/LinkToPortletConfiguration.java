/*
 * Copyright Siemens AG, 2015. Part of the SW360 Portal Project,
 * Copyright Bosch Software Innovations GmbH 2016.
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
package com.siemens.sw360.portal.portlets;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Layout;
import com.liferay.portal.model.Portlet;
import com.liferay.portal.service.LayoutLocalServiceUtil;
import com.liferay.portal.service.PortletLocalServiceUtil;
import com.siemens.sw360.portal.portlets.components.ComponentPortlet;
import com.siemens.sw360.portal.portlets.licenses.LicensesPortlet;
import com.siemens.sw360.portal.portlets.projects.ProjectPortlet;

import java.util.Optional;

/**
 * Maps Portlet classes to sw360 internal portlet names and layouts.
 * Portlet names are created internally
 * by concatenating data from the xml configuration file and deployment data.
 * To avoid update efforts (e.g. after increasing the version number of the war file, this is handled here.
 * If portletGroupId is not null,
 * findPlid searches for a lower case version of the enum instance name to identify a Layout.
 * @author daniele.fognini@tngtech.com
 */
public enum LinkToPortletConfiguration {
    COMPONENTS(ComponentPortlet.class),
    PROJECTS(ProjectPortlet.class),
    LICENSES(LicensesPortlet.class);

    private final Class<? extends Sw360Portlet> portletClass;

    LinkToPortletConfiguration(Class<? extends Sw360Portlet> portletClass) {
        this.portletClass = portletClass;
    }

    public Portlet findPortlet() {
        Optional<Portlet> portlet = PortletLocalServiceUtil.getPortlets().stream()
                .filter(p -> p.getPortletClass().equals(portletClass.getName())).findFirst();
        if (portlet.isPresent()) {
            return portlet.get();
        }
        throw new IllegalArgumentException("Could not find portlet name for " + this.portletClass);
    }

    public String portletName() {
        return findPortlet().getPortletId();
    }

    public long findPlid(Long portletGroupId) {
        if (portletGroupId == null ) {
            return 0;
        }
        try {
            Optional<Layout> layout = LayoutLocalServiceUtil.getLayouts(portletGroupId, true).stream()
            .filter(l -> ("/"+name().toLowerCase()).equals(l.getFriendlyURL()))
            .findFirst();
            if (layout.isPresent()) {
                return layout.get().getPlid();
            }
        } catch (SystemException e) {
            throw new IllegalStateException("Could not get layout for portlet " + portletClass, e);
        }
        return 0;
    }
}
