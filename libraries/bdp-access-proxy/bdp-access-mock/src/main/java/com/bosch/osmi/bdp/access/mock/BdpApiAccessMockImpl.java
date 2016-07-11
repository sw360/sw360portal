/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.bosch.osmi.bdp.access.mock;

import com.bosch.osmi.bdp.access.api.BdpApiAccess;
import com.bosch.osmi.bdp.access.api.model.User;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;


/**
 * Allows an access to the Bdp APIs. An access has to be initialized with a username and a password for which
 * a user object can be retrieved. The user object is the actual entry point into the whole object net retrieved
 * from Bdp.
 *
 * @author johannes.kristan@bosch-si.com
 * @since 11/16/15.
 */
public class BdpApiAccessMockImpl implements BdpApiAccess {

    public static final String MOCKDATA_CLASSPATH_LOCATION = "/mockdata.json";
    private static final Logger LOGGER = LogManager.getLogger(BdpApiAccessMockImpl.class);
    private JsonObject sourceFile;

    public BdpApiAccessMockImpl() {
        LOGGER.info("Initialize mock implementation with data from classpath.");
        InputStream inputStream = BdpApiAccessMockImpl.class.getResourceAsStream(MOCKDATA_CLASSPATH_LOCATION);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        sourceFile = readJsonFile(inputStreamReader);
    }

    public BdpApiAccessMockImpl(String jsonFilePath) throws FileNotFoundException {
        LOGGER.info("Initialize mock implementation with data from " + jsonFilePath + ".");
        sourceFile = readJsonFile(new FileReader(jsonFilePath));

    }

    @Override
    public boolean validateCredentials() {
        return true;
    }

    public User retrieveUser() {
        JsonInvocationHandler handler = new JsonInvocationHandler(sourceFile);
        return (User) Proxy.newProxyInstance(
                User.class.getClassLoader(),
                new Class[]{User.class},
                handler);
    }

    private JsonObject readJsonFile(Reader jsonReader) {
        // Read from File to String
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(jsonReader);
        return jsonElement.getAsJsonObject();
    }


    /**
     * InvocationHandler that handles incoming method requests to api methods and forwards them to the respective entry
     * in a json object with the data. The json-object is retrieved when an object of the host class is created.  The
     * invocation handler is registered at a proxy object that acts as an implementation of the bdp api.
     *
     */
    private static class JsonInvocationHandler implements InvocationHandler {
        // http://tutorials.jenkov.com/java-reflection/dynamic-proxies.html

        private JsonObject context = null;

        JsonInvocationHandler(JsonObject context) {
            this.context = context;
        }

        /**
         * called every time a method on the api is called.
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // With the type- and method name we search the corresponding entries in the json document
            String typeName = method.getDeclaringClass().getCanonicalName();
            String methodName = method.getName();

            if(Object.class == method.getDeclaringClass()) {
                if("equals".equals(methodName)) {
                    return proxy == args[0];
                } else if("hashCode".equals(methodName)) {
                    return System.identityHashCode(proxy);
                } else if("toString".equals(methodName)) {
                    return proxy.getClass().getName() + "@" +
                            Integer.toHexString(System.identityHashCode(proxy)) +
                            ", with InvocationHandler " + this;
                } else {
                    throw new IllegalStateException(String.valueOf(method));
                }
            }

            // We search on the Json document that is stored in context. Every JsonInvocationHandler
            JsonElement jsonElement = context.get(typeName);
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            JsonElement element = jsonObject.get(methodName);

            if (element.isJsonPrimitive()) {
                // Maybe add some return type check at method object
                return element.getAsString();
            } else if (element.isJsonArray()) {
                JsonArray content = element.getAsJsonArray();
                return constructCollection(content);
            } else if (element.isJsonObject()) {
                JsonObject content = element.getAsJsonObject();
                return constructObject(content);
            } else {
                throw new IllegalStateException("No proper way to handle " + element.getClass().getCanonicalName() +
                        " implemented");
            }
        }


        private Collection<Object> constructCollection(JsonArray array) throws ClassNotFoundException {
            Collection<Object> result = new ArrayList<>();
            for (JsonElement element : array) {
                if (element.isJsonPrimitive()) {
                    result.add(element.getAsString());
                } else if (element.isJsonObject()) {
                    JsonObject jsonObject = element.getAsJsonObject();
                    Object proxy = constructObject(jsonObject);
                    result.add(proxy);
                } else {
                    throw new IllegalStateException("Illegal entry in json document: " + element);
                }
            }

            return result;
        }

        private Object constructObject(JsonObject jsonObject) throws ClassNotFoundException {
            Map.Entry<String, JsonElement> entry = jsonObject.entrySet().iterator().next();
            String typeName = entry.getKey();
            Class<?> type = loadClass(typeName);
            JsonInvocationHandler handler = new JsonInvocationHandler(jsonObject);
            return Proxy.newProxyInstance(
                    JsonInvocationHandler.class.getClassLoader(),
                    new Class[]{type}, handler);
        }

        private Class<?> loadClass(String typeName) throws ClassNotFoundException {
            // http://tutorials.jenkov.com/java-reflection/dynamic-class-loading-reloading.html
            ClassLoader classLoader = JsonInvocationHandler.class.getClassLoader();
            return classLoader.loadClass(typeName);
        }
    }
}
