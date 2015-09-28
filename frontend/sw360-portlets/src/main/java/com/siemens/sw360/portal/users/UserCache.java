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

import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.siemens.sw360.datahandler.thrift.ThriftClients;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.thrift.users.UserService;
import org.apache.thrift.TException;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * In-memory cache for user data
 *
 * @author cedric.bodet@tngtech.com
 */
public class UserCache {

    LoadingCache<String, User> cache;

    public UserCache() {
        // Initialize user loader
        UserLoader loader = new UserLoader();

        List<User> allUsers;
        try {
            allUsers = loader.getAllUsers();
        } catch (TException ignored) {
            allUsers= Collections.emptyList();
        }

        // Initialize user cache
        cache = CacheBuilder.newBuilder()
                .maximumSize(allUsers.size() + 100)
                .expireAfterWrite(1, TimeUnit.DAYS)
                .build(loader);

        if(allUsers.size()>0) {
            cache.putAll(Maps.uniqueIndex(allUsers, new Function<User, String>() {
                @Override
                public String apply(User input) {
                    return input.getEmail();
                }
            }));
        }
    }

    public User get(String email) throws ExecutionException {
        return cache.get(email);
    }

    public User getRefreshed (String email)  throws ExecutionException {
        cache.refresh(email);
        return cache.get(email);
    }

    private static class UserLoader extends CacheLoader<String, User> {

        @Override
        public User load(String email) throws TException {
            ThriftClients thriftClients =  new ThriftClients();
            UserService.Iface client = thriftClients.makeUserClient();
            return client.getByEmail(email);
        }

        private List<User> getAllUsers() throws TException {
            ThriftClients thriftClients =  new ThriftClients();
            UserService.Iface client = thriftClients.makeUserClient();

            return client.getAllUsers();
        }
    }
}
