/*
 * Copyright Siemens AG, 2013-2016. Part of the SW360 Portal Project.
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
package com.siemens.sw360.commonIO;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.thrift.licenses.*;
import org.apache.commons.csv.CSVRecord;
import org.apache.thrift.TException;
import org.jetbrains.annotations.NotNull;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Convert CSV Record to license objects
 *
 * @author cedric.bodet@tngtech.com
 * @author manuel.wickmann@tngtech.com
 */
public class ConvertRecord {

    private ConvertRecord() {
        // Utility class with only static functions
    }

    public static List<RiskCategory> convertRiskCategories(List<CSVRecord> records) {
        ArrayList<RiskCategory> list = new ArrayList<>(records.size());

        for (CSVRecord record : records) {
            int id = Integer.parseInt(record.get(0));
            String text = record.get(1);

            RiskCategory category = new RiskCategory().setRiskCategoryId(id).setText(text);
            list.add(category);
        }

        return list;
    }

    public static Serializer<RiskCategory> riskCategorySerializer() {
        return new Serializer<RiskCategory>() {
            @Override
            public Function<RiskCategory, List<String>> transformer() {
                return new Function<RiskCategory, List<String>>() {
                    @Override
                    public List<String> apply(RiskCategory in) {
                        final ArrayList<String> out = new ArrayList<>(2);
                        out.add(((Integer)in.getRiskCategoryId()).toString());
                        out.add(in.getText());
                        return out;
                    }
                };
            }

            @Override
            public List<String> headers() {
                return ImmutableList.of("ID", "Category");
            }
        };
    }

    public static List<Risk> convertRisks(List<CSVRecord> records, Map<Integer, RiskCategory> categories) {
        List<Risk> list = new ArrayList<>(records.size());

        for (CSVRecord record : records) {
            int id = Integer.parseInt(record.get(0));
            int catId = Integer.parseInt(record.get(1));
            String text = record.get(2);

            Risk risk = new Risk().setRiskId(id).setText(text);
            risk.setCategory(categories.get(catId));

            list.add(risk);
        }

        return list;
    }

    public static Serializer<Risk> riskSerializer() {
        return new Serializer<Risk>() {
            @Override
            public Function<Risk, List<String>> transformer() {
                return new Function<Risk, List<String>>() {
                    @Override
                    public List<String> apply(Risk in) {
                        final ArrayList<String> out = new ArrayList<>(3);

                        out.add(((Integer) in.getRiskId()).toString());
                        out.add(((Integer) in.getCategory().getRiskCategoryId()).toString());
                        out.add(in.getText());
                        return out;
                    }
                };
            }

            @Override
            public List<String> headers() {
                return ImmutableList.of("ID", "Category_ID", "Text");
            }
        };
    }

    public static List<Todo> convertTodos(List<CSVRecord> records) {
        List<Todo> list = new ArrayList<>(records.size());

        for (CSVRecord record : records) {
            String id = record.get(0);
            String text = record.get(1);

            Todo todo = new Todo().setTodoId(Integer.parseInt(id)).setText(text);

            // Parse boolean values

            String developmentString = record.get(2);
            if (!"NULL".equals(developmentString)) {
                boolean development = parseBoolean(developmentString);
                todo.setDevelopment(development);
            }

            String distributionString = record.get(3);
            if (!"NULL".equals(distributionString)) {
                boolean distribution = parseBoolean(distributionString);
                todo.setDistribution(distribution);
            }
            list.add(todo);
        }
        return list;
    }

    public static Serializer<Todo> todoSerializer() {
        return new Serializer<Todo>() {
            @Override
            public Function<Todo, List<String>> transformer() {
                return new Function<Todo, List<String>>() {
                    @Override
                    public List<String> apply(Todo in) {
                        final ArrayList<String> out = new ArrayList<>(4);

                        out.add(((Integer) in.getTodoId()).toString());
                        out.add(in.getText());
                        out.add(((Boolean) in.isDevelopment()).toString());
                        out.add(((Boolean) in.isDistribution()).toString());
                        return out;
                    }
                };
            }

            @Override
            public List<String> headers() {
                return ImmutableList.of("ID", "Text", "Development", "Distribution");
            }
        };
    }


    public static List<Obligation> convertObligation(List<CSVRecord> records) {
        List<Obligation> list = new ArrayList<>(records.size());

        for (CSVRecord record : records) {
            String id = record.get(0);
            String name = record.get(1);

            Obligation obligation = new Obligation().setObligationId(Integer.parseInt(id)).setName(name);
            list.add(obligation);
        }

        return list;
    }

    public static Serializer<Obligation> obligationSerializer() {
        return new Serializer<Obligation>() {
            @Override
            public Function<Obligation, List<String>> transformer() {
                return new Function<Obligation, List<String>>() {
                    @Override
                    public List<String> apply(Obligation in) {
                        final ArrayList<String> out = new ArrayList<>(2);

                        out.add(((Integer) in.getObligationId()).toString());
                        out.add(in.getName());
                        return out;
                    }
                };
            }

            @Override
            public List<String> headers() {
                return ImmutableList.of("ID", "Name");
            }
        };
    }


    public static void putToTodos(Map<Integer, Obligation> obligations, Map<Integer, Todo> todos, Map<Integer, Set<Integer>> obligationTodo) {
        Map<Integer, Set<Integer>> todoObligation = invertRelation(obligationTodo);


        for (Map.Entry<Integer, Set<Integer>> entry : todoObligation.entrySet()) {
            Todo todo = todos.get(entry.getKey());

            if (todo != null) {
                fillTodo(obligations, entry.getValue(), todo);
            }

        }
    }

    private static void fillTodo(Map<Integer, Obligation> obligations, Set<Integer> values, Todo todo) {
        for (int obligationId : values) {
            Obligation obligation = obligations.get(obligationId);

            if (obligation != null) {
                todo.addToObligations(obligation);
            }
        }
    }

    public static List<LicenseType> convertLicenseTypes(List<CSVRecord> records) {
        List<LicenseType> list = new ArrayList<>(records.size());

        for (CSVRecord record : records) {
            int id = Integer.parseInt(record.get(0));
            String text = record.get(1);

            LicenseType type = new LicenseType().setLicenseTypeId(id).setLicenseType(text);
            list.add(type);
        }

        return list;
    }

    public static Serializer<LicenseType> licenseTypeSerializer() {
        return new Serializer<LicenseType>() {
            @Override
            public Function<LicenseType, List<String>> transformer() {
                return new Function<LicenseType, List<String>>() {
                    @Override
                    public List<String> apply(LicenseType in) {
                        final ArrayList<String> out = new ArrayList<>(2);
                        out.add(((Integer) in.getLicenseTypeId()).toString());
                        out.add(in.getLicenseType());
                        return out;
                    }
                };
            }

            @Override
            public List<String> headers() {
                return ImmutableList.of("ID", "Type");
            }
        };
    }


    public static List<License> fillLicenses(List<CSVRecord> records, Map<Integer, LicenseType> licenseTypeMap, Map<Integer, Todo> todoMap, Map<Integer, Risk> riskMap, Map<String, Set<Integer>> licenseTodo, Map<String, Set<Integer>> licenseRisk) {
        List<License> licenses = new ArrayList<>(records.size());

        for (CSVRecord record : records) {
            String identifier = record.get(0);
            String fullname = record.get(1);

            License license = new License().setId(identifier).setFullname(fullname);


            String typeString = record.get(2);
            if (!Strings.isNullOrEmpty(typeString) && !"NULL".equals(typeString)) {
                Integer typeId = Integer.parseInt(typeString);
                LicenseType licenseType = licenseTypeMap.get(typeId);
                license.setLicenseType(licenseType);
            }

            String gplv2CompatString = record.get(3);
            if (!Strings.isNullOrEmpty(typeString) && !"NULL".equals(gplv2CompatString)) {
                boolean gplv2Compat = parseBoolean(gplv2CompatString);
                license.setGPLv2Compat(gplv2Compat);
            }

            String gplv3CompatString = record.get(4);
            if (!Strings.isNullOrEmpty(typeString) && !"NULL".equals(gplv3CompatString)) {
                boolean gplv3Compat = parseBoolean(gplv3CompatString);
                license.setGPLv3Compat(gplv3Compat);
            }

            String reviewdate = record.get(5);
            license.setReviewdate(ConvertUtil.parseDate(reviewdate));

            String text = record.get(6);
            license.setText(text);

            // Add all risks
            Set<Integer> riskIds = licenseRisk.get(identifier);
            if (riskIds != null) {
                for (int riskId : riskIds) {
                    Risk risk = riskMap.get(riskId);
                    if (risk != null) {
                        license.addToRiskDatabaseIds(risk.getId());
                    }
                }
            }

            // Add all todos
            Set<Integer> todoIds = licenseTodo.get(identifier);
            if (todoIds != null) {
                for (int todoId : todoIds) {
                    Todo todo = todoMap.get(todoId);
                    if (todo != null) {
                        license.addToTodoDatabaseIds(todo.getId());
                    }
                }
            }

            licenses.add(license);
        }

        return licenses;
    }

    public static Serializer<License> licenseSerializer() {
        return new Serializer<License>() {
            @Override
            public Function<License, List<String>> transformer() {
                return new Function<License, List<String>>() {
                    @Override
                    public List<String> apply(License in) {
                        final ArrayList<String> out = new ArrayList<>(7);
                        out.add(CommonUtils.nullToEmptyString(in.getId()));
                        out.add(CommonUtils.nullToEmptyString(in.getFullname()));
                        out.add(in.isSetLicenseType()?((Integer) in.getLicenseType().getLicenseTypeId()).toString():"");
                        out.add(in.isSetGPLv2Compat()?((Boolean) in.isGPLv2Compat()).toString():"");
                        out.add(in.isSetGPLv3Compat() ? ((Boolean) in.isGPLv3Compat()).toString() : "");
                        out.add(CommonUtils.nullToEmptyString(in.getReviewdate()));
                        out.add(CommonUtils.nullToEmptyString(in.getText()));
                        return out;
                    }
                };
            }

            @Override
            public List<String> headers() {
                return ImmutableList.of("Identifier", "Fullname", "Type", "Gplv2compat", "Gplv3compat", "reviewdate", "Text");
            }
        };
    }

    public static Map<String, Set<Integer>> convertRelationalTable(List<CSVRecord> records) {
        Map<String, Set<Integer>> map = new HashMap<>(records.size());

        for (CSVRecord record : records) {
            String mainId = record.get(0);
            int otherId = Integer.parseInt(record.get(1));

            if (map.containsKey(mainId)) {
                map.get(mainId).add(otherId);
            } else {
                Set<Integer> ids = new HashSet<>();
                ids.add(otherId);
                map.put(mainId, ids);
            }

        }

        return map;
    }

    public static Map<Integer, Set<Integer>> convertObligationTodo(List<CSVRecord> records) {
        Map<String, Set<Integer>> stringMap = convertRelationalTable(records);
        Map<Integer, Set<Integer>> intMap = new HashMap<>();

        for (Map.Entry<String, Set<Integer>> entry : stringMap.entrySet()) {
            intMap.put(Integer.parseInt(entry.getKey()), entry.getValue());
        }

        return intMap;
    }


    private static Map<Integer, Set<Integer>> invertRelation(Map<Integer, Set<Integer>> obligationTodo) {
        Map<Integer, Set<Integer>> todoObligation = new HashMap<>();

        for (Map.Entry<Integer, Set<Integer>> entry : obligationTodo.entrySet()) {
            int obligationId = entry.getKey();
            for (int todoId : entry.getValue()) {
                if (todoObligation.containsKey(todoId)) {
                    todoObligation.get(todoId).add(obligationId);
                } else {
                    Set<Integer> obligationIds = new HashSet<>();
                    obligationIds.add(obligationId);
                    todoObligation.put(todoId, obligationIds);
                }
            }
        }

        return todoObligation;
    }


    private static boolean parseBoolean(String string) {

        if(string==null) return false;

        try {
            int value = Integer.parseInt(string);

            if (value != 0 && value != 1) {
                throw new IllegalArgumentException("Invalid string for boolean: " + string);
            }

            return value == 1;
        } catch (NumberFormatException e) {

         return Boolean.parseBoolean(string);
        }
    }


    @NotNull
    public static SetMultimap<Integer, Integer> getTodoToObligationMap(List<Todo> todos) {
        SetMultimap<Integer, Integer> obligationTodo = HashMultimap.create();

        for (Todo todo : todos) {
            if (todo.isSetObligations()) {
                for (Obligation obligation : todo.getObligations()) {
                    obligationTodo.put(obligation.getObligationId(), todo.getTodoId());
                }
            }
        }
        return obligationTodo;
    }

    @NotNull
    public static SetMultimap<String, Integer> getLicenseToTodoMap(List<License> licenses) {
        SetMultimap<String, Integer> licenseToTodo = HashMultimap.create();

        for (License license : licenses) {
            if (license.isSetTodos()) {
                for (Todo todo : license.getTodos()) {
                    licenseToTodo.put(license.getId(), todo.getTodoId());
                }
            }
        }
        return licenseToTodo;
    }

    @NotNull
    public static SetMultimap<String, Integer> getLicenseToRiskMap(List<License> licenses) {
        SetMultimap<String, Integer> licenseToRisk = HashMultimap.create();

        for (License license : licenses) {
            if (license.isSetRisks()) {
                for (Risk risk : license.getRisks()) {
                    licenseToRisk.put(license.getId(), risk.getRiskId());
                }
            }
        }
        return licenseToRisk;
    }

    public static void addLicenses(LicenseService.Iface licenseClient, List<License> licensesToAdd, Logger log) {
        try {
            final List<License> licenses = licenseClient.getLicenses();
            final Set<String> knownLicenseNames = Sets.newHashSet(FluentIterable.from(licenses).transform(TypeMappings.getLicenseIdentifier()));
            final ImmutableList<License> filteredLicenses = TypeMappings.getElementsWithIdentifiersNotInSet(TypeMappings.getLicenseIdentifier(), knownLicenseNames, licensesToAdd);

            log.debug("Sending " + filteredLicenses.size() + " Licenses to the database!");
            final List<License> addedLicenses = licenseClient.addLicenses(filteredLicenses);

            if (addedLicenses == null) {
                log.debug("There were errors.");
            } else {
                log.debug("Everything went fine.");
            }

        } catch (TException e) {
            log.error("Error getting licenses from DB", e);
        }
    }


    interface Serializer<T> {
        Function<T, List<String>> transformer();

        List<String> headers();
    }

    public static <T> List<List<String>> serialize(List<T> in, Serializer<T> serializer) {
        return serialize(in, serializer.headers(), serializer.transformer());
    }

    public static <T> List<List<String>> serialize(List<T> in, List<String> headers, Function<T, List<String>> function) {
        final ArrayList<List<String>> out = new ArrayList<>(in.size() + 1);
        out.add(headers);
        for (T t : in) {
            out.add(function.apply(t));
        }
        return out;
    }

    @NotNull
    public static <T, U> List<List<String>> serialize(SetMultimap<T, U> aToB, List<String> headers) {
        final List<List<String>> mapEntryList = new ArrayList<>(aToB.size() + 1);

        mapEntryList.add(headers);

        for (Map.Entry<T, U> mapEntry : aToB.entries()) {
            final ArrayList<String> entry = new ArrayList<>(2);
            entry.add(mapEntry.getKey().toString());
            entry.add(mapEntry.getValue().toString());
            mapEntryList.add(entry);
        }
        return mapEntryList;
    }

}
