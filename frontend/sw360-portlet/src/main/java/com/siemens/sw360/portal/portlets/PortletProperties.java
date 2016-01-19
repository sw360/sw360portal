/*
 * Copyright Siemens AG, 2015. Part of the SW360 Portal Project.
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

import com.siemens.sw360.portal.portlets.components.ComponentPortlet;
import com.siemens.sw360.portal.portlets.licenses.LicensesPortlet;
import com.siemens.sw360.portal.portlets.projects.ProjectPortlet;

/**
 * @author daniele.fognini@tngtech.com
 */
public enum PortletProperties {
    COMPONENTS(ComponentPortlet.class, "components_WAR_sw360portlet"),
    PROJECTS(ProjectPortlet.class, "projects_WAR_sw360portlet"),
    LICENSES(LicensesPortlet.class, "licenses_WAR_sw360portlet");

    private final Class<? extends Sw360Portlet> portletClass;
    private final String portletName;

    PortletProperties(Class<? extends Sw360Portlet> portletClass, String portletName) {
        this.portletClass = portletClass;
        this.portletName = portletName;
    }

    public String portletName() {
        return portletName;
    }

    public long findPlid() {
        return Sw360Portlet.getPlid(portletClass);
    }
}
