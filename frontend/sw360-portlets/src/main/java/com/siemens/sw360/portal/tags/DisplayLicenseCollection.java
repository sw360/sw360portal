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

import com.google.common.base.Joiner;
import com.siemens.sw360.datahandler.common.SW360Utils;
import com.siemens.sw360.portal.users.UserCacheHolder;
import org.apache.thrift.TException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * This displays collection of Licenses
 *
 * @author Johannes.Najjar@tngtech.com
 * @author daniele.fognini@tngtech.com
 */
public class DisplayLicenseCollection extends SimpleTagSupport {

    private Collection<String> ids;
    private String userEmail = null;

    public void setLicenseIds(Collection<String> ids) {
        this.ids = ids;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void doTag() throws JspException, IOException {
        if (null != ids && !ids.isEmpty()) {
            String department = UserCacheHolder.getUserFromEmail(userEmail).getDepartment();
            List<String> resultList;
            try {
               resultList= SW360Utils.getLicenseNamesFromIds(ids, department);
            } catch (TException e) {
                throw new JspException(e);
            }
            getJspContext().getOut().print(Joiner.on(", ").join(resultList));
        }
    }
}
