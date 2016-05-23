/*
 * Copyright Siemens AG, 2015. Part of the SW360 Portal Project.
 * With contributions by Bosch Software Innovations GmbH, 2016.
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
package com.siemens.sw360.portal.tags.urlutils;

import com.siemens.sw360.portal.common.page.PortletPage;
import com.siemens.sw360.portal.portlets.LinkToPortletConfiguration;

import javax.servlet.jsp.JspException;

/**
 * @author daniele.fognini@tngtech.com
 */
public interface UrlWriter {
    UrlWriter withParam(String name, String value) throws JspException;

    UrlWriter toPortlet(LinkToPortletConfiguration portlet, Long scopeGroupId) throws JspException;

    UrlWriter toPage(PortletPage page) throws JspException;

    void writeUrlToJspWriter() throws JspException;
}
