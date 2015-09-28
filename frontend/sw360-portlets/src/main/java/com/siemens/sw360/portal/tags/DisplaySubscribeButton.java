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

import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.thrift.components.Component;
import com.siemens.sw360.datahandler.thrift.components.Release;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.util.Set;

/**
 * This displays a subscribe button
 *
 * @author Johannes.Najjar@tngtech.com
 */
public class DisplaySubscribeButton extends SimpleTagSupport {

    private Object object;
    private String email;
    private String id = "SubscribeButtonID";
    private String onclick = "";
    private String altonclick = "";

    public void setOnclick(String onclick) {
        this.onclick = onclick;
    }
    public void setAltonclick(String altonclick) {
        this.altonclick = altonclick;
    }
    public void setId(String id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public void doTag() throws JspException, IOException {

        StringBuilder builder = new StringBuilder();

        Set<String> subscribers = null;

        if(object instanceof Release) {
            subscribers = ((Release) object).getSubscribers();
        } else if ( object instanceof Component) {
            subscribers = ((Component) object).getSubscribers();
        }

        subscribers= CommonUtils.nullToEmptySet(subscribers);

        if (subscribers.contains(email)) {
            builder.append(String.format("<input type=\"button\" id=\"%s\" onclick=\"%s\" value=\"Unsubscribe\" class=\"addButton subscribed\" />", id, altonclick));
        } else {
            builder.append(String.format("<input type=\"button\" id=\"%s\" onclick=\"%s\" value=\"Subscribe\" class=\"addButton\" />", id, onclick));
        }

        getJspContext().getOut().print(builder.toString());
    }
}
