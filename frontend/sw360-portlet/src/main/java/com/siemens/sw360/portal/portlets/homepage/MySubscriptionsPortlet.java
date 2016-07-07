/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.portal.portlets.homepage;

import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.thrift.components.Component;
import com.siemens.sw360.datahandler.thrift.components.ComponentService;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.portal.common.PortalConstants;
import com.siemens.sw360.portal.portlets.Sw360Portlet;
import com.siemens.sw360.portal.users.UserCacheHolder;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;
import java.util.List;

import static org.apache.log4j.Logger.getLogger;

/**
 * Small homepage portlet
 *
 * @author cedric.bodet@tngtech.com
 * @author gerrit.grenzebach@tngtech.com
 */
public class MySubscriptionsPortlet extends Sw360Portlet {

    private static final Logger log = getLogger(MySubscriptionsPortlet.class);

    @Override
    public void doView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        List<Component> components=null;
        List<Release> releases=null;

        try {
            final User user = UserCacheHolder.getUserFromRequest(request);
            ComponentService.Iface componentClient = thriftClients.makeComponentClient();
            components = componentClient.getSubscribedComponents(user);
            releases  = componentClient.getSubscribedReleases(user);
        } catch (TException e) {
            log.error("Could not fetch your subscriptions from backend", e);
        }

        request.setAttribute(PortalConstants.COMPONENT_LIST, CommonUtils.nullToEmptyList(components));
        request.setAttribute(PortalConstants.RELEASE_LIST, CommonUtils.nullToEmptyList( releases));

        super.doView(request, response);
    }
}
