/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.bosch.osmi.bdp.access.mock.generator;

import com.bosch.osmi.bdp.access.api.model.BdpEntity;
import com.bosch.osmi.bdp.access.api.model.User;
import com.bosch.osmi.bdp.access.impl.BdpApiAccessImpl;
import com.bosch.osmi.bdp.access.impl.util.Util;
import com.google.gson.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Properties;

/**
 * The MockDataGenerator iterates over all public methods of all api entities and stores the return values in a json
 * file. It uses the Java Reflection API to inspect the bdp-access-api and google.gson to serialize the result.
 *
 * @author johannes.kristan@bosch-si.com
 * @since 11/19/15.
 */
public class MockDataGenerator {

    private static final Logger LOGGER = LogManager.getLogger(MockDataGenerator.class);

    private String userName;
    private String password;
    private String serverUrl;

    public static void main(String[] args) throws IOException, InvocationTargetException, IllegalAccessException {
        MockDataGenerator generator = new MockDataGenerator();
        generator.generateMockData();
    }

    public MockDataGenerator() throws IOException {
        readProperties();


    }

    private void readProperties() throws IOException {
        Properties props = Util.readBdpAccessImplProperties();
        userName = props.getProperty("user");
        password = props.getProperty("password");
        serverUrl= props.getProperty("bdp.url");
    }

    public void generateMockData() throws IOException, InvocationTargetException, IllegalAccessException {
        LOGGER.info("Start mock data generation ...");
        LOGGER.info("Try to initialize API access for user "+ userName + " at server " + serverUrl +".");
        LOGGER.info("Initialize API access.");
        BdpApiAccessImpl access = new BdpApiAccessImpl(serverUrl, userName, password);

        LOGGER.info("Retrieve user " + userName);
        User user = access.retrieveUser();
        JsonObject resultStore = new JsonObject();
        LOGGER.info("Start bdp-access-api evaluation");
        evaluate(user, resultStore);

        writeToJsonFile(resultStore);
    }

    private void writeToJsonFile(JsonObject resultStore) throws IOException {
        String path = Util.getHomeDir() + File.separator + ".bdp-access-files" + File.separator + "mockdata.json";
        File file = new File(path);
        boolean dirCreated = file.getParentFile().mkdirs();
        if(dirCreated){
            LOGGER.debug("Unable to create directory " + file.getParentFile().getAbsolutePath() + ". Maybe it already exists.");
        }

        try (Writer writer = new FileWriter(file)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
            gson.toJson(resultStore, writer);
        }
    }

    private void evaluateElements(Collection<?> collection, JsonArray parent) throws InvocationTargetException, IllegalAccessException {
        for(Object object : collection) {
            JsonObject jsonObject = new JsonObject();
            parent.add(jsonObject);
            evaluate(object, jsonObject);
        }
    }

    private void evaluate(Object object, JsonObject parent) throws InvocationTargetException, IllegalAccessException {
        Class<?>[] types = object.getClass().getInterfaces();
        if(types != null && types.length > 0){
            // TODO proably check if the interface is annotated with a marker that says that this is actually part of the Bdp API
            for(Class<?> type : types){
                LOGGER.info("Evaluate methods of type " + type.getName());
                JsonObject jsonObject = new JsonObject();
                parent.add(type.getCanonicalName(), jsonObject);
                executeMethodsOfType(type, object, jsonObject);
            }
        }
    }

    private void executeMethodsOfType(Class<?> type, Object object, JsonObject parent) throws InvocationTargetException, IllegalAccessException {
        Method[] methods = type.getMethods();
        for (Method method : methods) {
            LOGGER.info("Evaluate method " + method.getName());
            Object result = method.invoke(object);
            if(result == null){
                System.out.println();
                throw new IllegalStateException("Return value is null. Cannot be processed.");
            }
            if (result instanceof String) {
                parent.addProperty(method.getName(), (String) result);
            } else  if(result instanceof Boolean){
                parent.addProperty(method.getName(), (Boolean) result);
            } else if (result instanceof Collection<?>) {
                JsonArray entries = new JsonArray();
                parent.add(method.getName(), entries);
                evaluateElements((Collection<?>) result, entries);
            } else if(result instanceof BdpEntity){
                JsonObject jsonObject = new JsonObject();
                parent.add(method.getName(), jsonObject);
                evaluate(result, jsonObject);
            } else {
                throw new IllegalStateException("Return value not of any processable type " + result.getClass().getCanonicalName());
            }
        }
    }
}