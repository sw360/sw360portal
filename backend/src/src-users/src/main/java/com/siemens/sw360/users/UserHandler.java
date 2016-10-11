/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.users;

import com.siemens.sw360.datahandler.common.DatabaseSettings;
import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.thrift.users.UserService;
import com.siemens.sw360.users.db.UserDatabaseHandler;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import java.net.MalformedURLException;
import java.util.List;

import static com.siemens.sw360.datahandler.common.SW360Assert.assertNotEmpty;
import static com.siemens.sw360.datahandler.common.SW360Assert.assertNotNull;

/**
 * Implementation of the Thrift service
 *
 * @author cedric.bodet@tngtech.com
 */
public class UserHandler implements UserService.Iface {

    private static final Logger log = Logger.getLogger(UserHandler.class);

    UserDatabaseHandler db;

    public UserHandler() throws MalformedURLException {
        db = new UserDatabaseHandler(DatabaseSettings.getConfiguredHttpClient(), DatabaseSettings.COUCH_DB_USERS);
    }

    @Override
    public User getByEmail(String email) throws TException {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
        assertNotEmpty(email, "Invalid empty email " + stackTraceElement.getFileName() + ": "  + stackTraceElement.getLineNumber());

        if (log.isTraceEnabled()) log.trace("getByEmail: " + email);

        // Get user from database
        User user = db.getByEmail(email);
        if (user == null) {
            log.info("User does not exist in DB");
        }
        return user;
    }

    @Override
    public List<User> searchUsers(String name) throws TException {
        return db.searchUsers(name);
    }

    @Override
    public List<User> getAllUsers() throws TException {
        return db.getAll();
    }

    @Override
    public RequestStatus addUser(User user) throws TException {
        assertNotNull(user);
        assertNotNull(user.getEmail());
        return db.addUser(user);
    }

    @Override
    public RequestStatus updateUser(User user) throws TException {
        assertNotNull(user);
        assertNotNull(user.getEmail());
        return db.updateUser(user);
    }

    @Override
    public RequestStatus deleteUser(User user, User adminUser) throws TException {
        assertNotNull(user);
        assertNotNull(user.getEmail());
        return db.deleteUser(user, adminUser);
    }

    public RequestStatus sendMailForAcceptedModerationRequest(String userEmail) throws TException {
        assertNotNull(userEmail);
        return db.sendMailForAcceptedModerationRequest(userEmail);
    }

    @Override
    public String getDepartmentByEmail(String email) throws TException {
        User user = getByEmail(email);
        return user != null ? user.getDepartment() : null;
    }
}
