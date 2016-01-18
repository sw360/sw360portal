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

import com.liferay.taglib.portlet.ActionURLTag;
import com.liferay.taglib.portlet.RenderURLTag;
import com.liferay.taglib.portlet.ResourceURLTag;
import com.siemens.sw360.portal.common.PortalConstants;
import com.siemens.sw360.portal.common.page.PortletPage;
import com.siemens.sw360.portal.portlets.PortletProperties;
import com.siemens.sw360.portal.portlets.LinkToPortletConfiguration;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * @author daniele.fognini@tngtech.com
 */
public class UrlWriterImpl implements UrlWriter {

    private final ActionURLTag urlTag;
    private boolean done = false;

    private UrlWriterImpl(PageContext pageContext, ActionURLTag urlTag) {
        this.urlTag = urlTag;
        this.urlTag.setPageContext(pageContext);
    }

    @Override
    public UrlWriter withParam(String name, String value) throws JspException {
        checkNotDone();
        urlTag.addParam(name, value);
        return this;
    }

    @Override
    public UrlWriter toPortlet(LinkToPortletConfiguration portlet, Long portletGroupId) throws JspException {
        checkNotDone();
        urlTag.setPortletName(portlet.portletName());
        urlTag.setPlid(portlet.findPlid(portletGroupId));
        return this;
    }

    @Override
    public UrlWriter toPage(PortletPage page) throws JspException {
        return withParam(PortalConstants.PAGENAME, page.pagename());
    }

    @Override
    public void writeUrlToJspWriter() throws JspException {
        checkNotDone();
        urlTag.doStartTag();
        urlTag.doEndTag();
        done = true;
    }

    private void checkNotDone() throws JspException {
        if (done) {
            throw new JspException("this url writer has already been written");
        }
    }

    public static UrlWriter resourceUrl(PageContext pageContext) {
        return new UrlWriterImpl(pageContext, new ResourceURLTag());
    }

    public static UrlWriter renderUrl(PageContext pageContext) {
        return new UrlWriterImpl(pageContext, new RenderURLTag());
    }
}
