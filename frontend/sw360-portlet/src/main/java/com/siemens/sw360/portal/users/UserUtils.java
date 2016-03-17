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
package com.siemens.sw360.portal.users;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Organization;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.RoleConstants;
import com.liferay.portal.model.User;
import com.siemens.sw360.datahandler.thrift.ThriftClients;
import com.siemens.sw360.datahandler.thrift.users.UserGroup;
import com.siemens.sw360.datahandler.thrift.users.UserService;
import com.siemens.sw360.portal.common.PortalConstants;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import java.util.ArrayList;
import java.util.List;

import static com.siemens.sw360.datahandler.common.SW360Constants.TYPE_USER;

/**
 * Class with helper utils to convert Liferay users to Thrift users
 *
 * @author johannes.najjar@tngtech.com
 */
public class UserUtils {

    private static final Logger log = Logger.getLogger(UserUtils.class);
    private final ThriftClients thriftClients;

    public UserUtils(ThriftClients thriftClients) {
        this.thriftClients = thriftClients;
    }

    public UserUtils() {
        thriftClients = new ThriftClients();
    }

    public static void synchronizeUserWithDatabase(User user, ThriftClients thriftClients) {
        UserService.Iface client = thriftClients.makeUserClient();

        com.siemens.sw360.datahandler.thrift.users.User thriftUser = null;

        try {
            thriftUser = client.getByEmail(user.getEmailAddress());
        } catch (TException e) {
            //This occurs for every new user, so there is not necessarily something wrong
            log.trace("Thrift exception when getting the user", e);
        }

        try {
            if (thriftUser == null) {
                //we have a new user
                thriftUser = new com.siemens.sw360.datahandler.thrift.users.User();
                fillThriftUserFromLiferayUser(thriftUser, user);
                client.addUser(thriftUser);
            } else {
                fillThriftUserFromLiferayUser(thriftUser, user);
                client.updateUser(thriftUser);
            }
        } catch (TException e) {
            log.error("Thrift exception when saving the user", e);
        }
    }

    public static void synchronizeUserWithDatabase(UserCSV userCsv, ThriftClients thriftClients) {
        UserService.Iface client = thriftClients.makeUserClient();

        com.siemens.sw360.datahandler.thrift.users.User thriftUser = null;

        try {
            thriftUser = client.getByEmail(userCsv.getEmail());
        } catch (TException e) {
            //This occurs for every new user, so there is not necessarily something wrong
            log.trace("User does not exist in DB yet.");
        }

        try {
            if (thriftUser == null) {
                log.info("Create new user.");
                thriftUser = new com.siemens.sw360.datahandler.thrift.users.User();
                fillThriftUserFromUserCSV(thriftUser, userCsv);
                client.addUser(thriftUser);
            } else {
                fillThriftUserFromUserCSV(thriftUser, userCsv);
                client.updateUser(thriftUser);
            }
        } catch (TException e) {
            log.error("Thrift exception when saving the user", e);
        }
    }

    public static String displayUser(String email, com.siemens.sw360.datahandler.thrift.users.User user) {
        String userString;
        if (user != null) {
            userString = "<a href=\"mailto:" + user.getEmail() + "\">" + user.getGivenname() + " " + user.getLastname() + "</a>";
        } else {
            userString = "<a href=\"mailto:" + email + "\">" + email + "</a>";
        }
        return userString;
    }

    public void synchronizeUserWithDatabase(User user) {
        String userEmailAddress = user.getEmailAddress();

        com.siemens.sw360.datahandler.thrift.users.User refreshed = UserCacheHolder.getRefreshedUserFromEmail(userEmailAddress);
        if (!equivalent(refreshed, user)) {
            synchronizeUserWithDatabase(user, thriftClients);
            UserCacheHolder.getRefreshedUserFromEmail(userEmailAddress);
        }
    }

    private boolean equivalent(com.siemens.sw360.datahandler.thrift.users.User refreshed, User user) {
        final com.siemens.sw360.datahandler.thrift.users.User thriftUser = new com.siemens.sw360.datahandler.thrift.users.User();
        fillThriftUserFromLiferayUser(thriftUser, user);
        return thriftUser.equals(refreshed);
    }

    public static void fillThriftUserFromUserCSV(final com.siemens.sw360.datahandler.thrift.users.User thriftUser, final UserCSV userCsv) {
        thriftUser.setEmail(userCsv.getEmail());
        thriftUser.setId(userCsv.getEmail());
        thriftUser.setType(TYPE_USER);
        thriftUser.setUserGroup(UserGroup.valueOf(userCsv.getGroup()));
        thriftUser.setExternalid(userCsv.getGid());
        thriftUser.setFullname(userCsv.getGivenname()+" "+userCsv.getLastname());
        thriftUser.setGivenname(userCsv.getGivenname());
        thriftUser.setLastname(userCsv.getLastname());
        thriftUser.setDepartment(userCsv.getDepartment());
        thriftUser.setWantsMailNotification(userCsv.wantsMailNotification());
    }

    public static void fillThriftUserFromLiferayUser(final com.siemens.sw360.datahandler.thrift.users.User thriftUser, final User user) {
        thriftUser.setEmail(user.getEmailAddress());
        thriftUser.setId(user.getEmailAddress());
        thriftUser.setType(TYPE_USER);
        thriftUser.setUserGroup(getUserGroupFromLiferayUser(user));
        thriftUser.setExternalid(user.getOpenId());
        thriftUser.setFullname(user.getFullName());
        thriftUser.setGivenname(user.getFirstName());
        thriftUser.setLastname(user.getLastName());
        thriftUser.setDepartment(getDepartment(user));
    }

    public static UserGroup getUserGroupFromLiferayUser(com.liferay.portal.model.User user) {

        try {
            List<Role> roles = user.getRoles();

            List<String> roleNames = new ArrayList<>();

            for (Role role : roles) {
                roleNames.add(role.getName());
            }

            if (roleNames.contains(PortalConstants.ROLENAME_ADMIN)) return UserGroup.ADMIN;
            else if (roleNames.contains(PortalConstants.ROLENAME_CLEARING_ADMIN)) return UserGroup.CLEARING_ADMIN;

        } catch (SystemException e) {
            log.error("Problem retrieving UserGroup", e);
        }
        return UserGroup.USER;
    }

    public static String getDepartment(User user) {
        String department = "";
        try {
            List<Organization> organizations = user.getOrganizations();
            if (!organizations.isEmpty()) {
                Organization organization = organizations.get(0);
                department = organization.getName();
            }
        } catch (PortalException | SystemException e) {
            log.error("Error getting department", e);
        }
        return department;
    }

    public static String getRoleConstantFromUserGroup(UserGroup group) {
        switch (group) {
            case USER:
                return RoleConstants.USER;
            case CLEARING_ADMIN:
                return RoleConstants.ORGANIZATION_ADMINISTRATOR;
            case ADMIN:
                return RoleConstants.ADMINISTRATOR;
        }
        return RoleConstants.USER;
    }

    public static UserGroup userGroupFromString(String s) {
        try {
            return UserGroup.valueOf(s);
        } catch (IllegalArgumentException e) {
            log.error("Illegal Argument Exception from " + s, e);
            return UserGroup.USER;
        }
    }
}
