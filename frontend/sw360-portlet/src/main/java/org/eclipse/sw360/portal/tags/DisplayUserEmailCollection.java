/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.portal.tags;

import com.google.common.base.Strings;
import org.eclipse.sw360.datahandler.common.CommonUtils;
import org.eclipse.sw360.datahandler.thrift.ThriftClients;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.datahandler.thrift.users.UserService;
import org.eclipse.sw360.portal.users.UserUtils;
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
