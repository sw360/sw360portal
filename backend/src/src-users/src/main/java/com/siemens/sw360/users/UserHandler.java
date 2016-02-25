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
        db = new UserDatabaseHandler(DatabaseSettings.COUCH_DB_URL, DatabaseSettings.COUCH_DB_USERS);
    }

    @Override
    public User getByEmail(String email) throws TException {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
        assertNotEmpty(email, "Invalid empty email " + stackTraceElement.getFileName() + ": "  + stackTraceElement.getLineNumber());

        if (log.isTraceEnabled()) log.trace("getByEmail: " + email);

        // Get user from database
        User user = db.getByEmail(email);
        if (user == null) {
            log.error("Non existing user");
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

    public RequestStatus sendMailForAcceptedModerationRequest(String userEmail) throws TException {
        assertNotNull(userEmail);
        return db.sendMailForAcceptedModerationRequest(userEmail);
    }

}
