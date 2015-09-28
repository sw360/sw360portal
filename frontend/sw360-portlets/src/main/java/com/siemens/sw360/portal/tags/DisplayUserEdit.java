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
import com.google.common.base.Strings;
import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.thrift.ThriftClients;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.thrift.users.UserService;
import org.apache.thrift.TException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This displays a user in Edit mode
 *
 * @author Johannes.Najjar@tngtech.com
 */
public class DisplayUserEdit extends NameSpaceAwareTag {

    private List<String> emails;
    private String email;
    private String id;
    private String description;
    private Boolean multiUsers;
    private Boolean readonly = false;

    public void setMultiUsers(Boolean multiUsers) {
        this.multiUsers = multiUsers;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setEmails(Collection<String> emails) {
        this.emails = new ArrayList<>(CommonUtils.nullToEmptyCollection(emails));
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setReadonly(Boolean readonly) {
        this.readonly = readonly;
    }

    public String getString(String input) {
        if (Strings.isNullOrEmpty(input)) {
            input = "";
        }
        return input;
    }

    public int doStartTag() throws JspException {

        JspWriter jspWriter = pageContext.getOut();
        StringBuilder display = new StringBuilder();

        List<String> userList = new ArrayList<>();
        List<String> emailList = new ArrayList<>();

        List<String> emailInput;

        if (multiUsers) {
            emailInput = emails;
        } else {
            emailInput = new ArrayList<>();
            emailInput.add(email);
        }

        String namespace = getNamespace();

        try {
            UserService.Iface client = new ThriftClients().makeUserClient();

            for (String email : emailInput) {
                User user = null;
                try {
                    if (!Strings.isNullOrEmpty(email))
                        user = client.getByEmail(email);
                } catch (TException e) {
                    user = null;
                }
                if (user != null) {
                    emailList.add(email);
                    userList.add(user.getFullname());
                }
            }

            Joiner commaJoiner = Joiner.on(", ");
            String mails = getString(commaJoiner.join(emailList));
            String userNames = getString(commaJoiner.join(userList));

            display.append(String.format("<label class=\"textlabel stackedLabel\" for=\"%sDisplay\">%s</label>", id, description))
                    .append(String.format("<input type=\"hidden\" readonly=\"\" value=\"%s\"  id=\"%s\" name=\"%s%s\"/>", mails, id, namespace, id))
                    .append(String.format("<input type=\"text\" readonly=\"\" value=\"%s\" id=\"%sDisplay\" ", userNames, id));

            if (!readonly) {
                display.append(String.format(" onclick=\"showUserDialog(%s, '%s')\" ", multiUsers ? "true" : "false", id));
                display.append(" placeholder=\"Click to Edit\" class=\"clickable\" ");
            } else {
                display.append(" placeholder=\"Will be set automatically\" ");
            }

            display.append("/>");

            jspWriter.print(display.toString());
        } catch (Exception e) {
            throw new JspException(e);
        }
        return SKIP_BODY;
    }
}
