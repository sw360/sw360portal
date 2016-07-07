/*
 * Copyright Siemens AG, 2015. Part of the SW360 Portal Project.
 * With contributions by Bosch Software Innovations GmbH, 2016.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.portal.tags.links;

import com.siemens.sw360.datahandler.thrift.components.Component;
import com.siemens.sw360.portal.common.PortalConstants;
import com.siemens.sw360.portal.common.page.PortletDefaultPage;
import com.siemens.sw360.portal.portlets.LinkToPortletConfiguration;

import javax.servlet.jsp.JspException;

import static com.siemens.sw360.datahandler.common.SW360Utils.printName;
import static com.siemens.sw360.portal.tags.urlutils.UrlWriterImpl.renderUrl;

/**
 * @author daniele.fognini@tngtech.com
 */
public class DisplayLinkToComponent extends DisplayLinkAbstract {
    private Component component;
    private Boolean showName = true;
    private String componentId;

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public void setComponent(Component component) {
        this.component = component;
        componentId = component.getId();
    }

    public void setShowName(Boolean showName) {
        this.showName = showName;
    }

    @Override
    protected String getTextDisplay() {
        return showName ? printName(component) : null;
    }

    @Override
    protected void writeUrl() throws JspException {
        renderUrl(pageContext)
                .toPortlet(LinkToPortletConfiguration.COMPONENTS, scopeGroupId)
                .toPage(PortletDefaultPage.DETAIL)
                .withParam(PortalConstants.COMPONENT_ID, componentId)
                .writeUrlToJspWriter();
    }
}
