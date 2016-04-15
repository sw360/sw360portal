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

package com.siemens.sw360.portal.tags;

import com.liferay.portal.kernel.servlet.taglib.TagSupport;
import com.liferay.portal.kernel.util.JavaConstants;
import com.siemens.sw360.datahandler.thrift.ThriftClients;
import com.siemens.sw360.datahandler.thrift.components.ComponentService;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.thrift.users.UserService;
import org.apache.thrift.TException;

import javax.portlet.PortletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Heavily inspired by com.liferay.taglib.portlet.NamespaceTag
 *
 * @author Daniele.Fognini@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 */
public class NameSpaceAwareTag extends TagSupport {

    protected String getNamespace() throws JspException {
        try {
            HttpServletRequest request =
                    (HttpServletRequest) pageContext.getRequest();

            PortletResponse portletResponse =
                    (PortletResponse) request.getAttribute(
                            JavaConstants.JAVAX_PORTLET_RESPONSE);

            if (portletResponse != null) {
                return  portletResponse.getNamespace();
            } else {
                throw new JspException("Not in a portlet?");
            }
        } catch (Exception e) {
            throw new JspException(e);
        }
    }

}
