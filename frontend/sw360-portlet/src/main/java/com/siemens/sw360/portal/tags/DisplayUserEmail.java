/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.portal.tags;

import com.google.common.base.Strings;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.portal.users.UserCacheHolder;
import com.siemens.sw360.portal.users.UserUtils;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

/**
 * This displays a user
 *
 * @author Johannes.Najjar@tngtech.com
 */
public class DisplayUserEmail extends SimpleTagSupport {
    private String email;

    public void setEmail(String email) {
        this.email = email;
    }

    public void doTag() throws JspException, IOException {
        User user = null;

        if (!Strings.isNullOrEmpty(email)) {
            user = UserCacheHolder.getUserFromEmail(email);
        } else {
            email = "";
        }

        getJspContext().getOut().print(UserUtils.displayUser(email, user));
    }

}
