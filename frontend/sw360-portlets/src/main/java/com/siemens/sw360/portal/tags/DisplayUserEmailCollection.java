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

import com.google.common.base.Strings;
import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.thrift.ThriftClients;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.thrift.users.UserService;
import com.siemens.sw360.portal.users.UserUtils;
import org.apache.thrift.TException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This displays collection of users
 *
 * @author Johannes.Najjar@tngtech.com
 */
public class DisplayUserEmailCollection extends SimpleTagSupport {

    private Collection<String> value;

    public void setValue(Collection<String> value) {
        this.value = value;
    }

    public void doTag() throws JspException, IOException {
        if (null != value && !value.isEmpty()) {
            List<String> valueList = new ArrayList<>(value);
            Collections.sort(valueList, String.CASE_INSENSITIVE_ORDER);

            List<String> resultList = new ArrayList<>();

            UserService.Iface client = new ThriftClients().makeUserClient();

            if (client != null) {

                for (String email : valueList) {
                    User user = null;
                    try {
                        if (!Strings.isNullOrEmpty(email)) {
                            user = client.getByEmail(email);
                        }
                    } catch (TException e) {
                        user = null;
                    }
                    if (user != null || !Strings.isNullOrEmpty(email))
                        resultList.add(UserUtils.displayUser(email, user));
                }
            }
            getJspContext().getOut().print(CommonUtils.COMMA_JOINER.join(resultList));
        }
    }

}
