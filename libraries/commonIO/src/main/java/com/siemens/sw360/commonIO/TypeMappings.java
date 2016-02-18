/*
 * Copyright Siemens AG, 2014-2015. Part of the SW360 Portal Project.
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
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.common.ImportCSV;
import com.siemens.sw360.datahandler.thrift.SW360Exception;
import com.siemens.sw360.datahandler.thrift.licenses.*;
import org.apache.commons.csv.CSVRecord;
import org.apache.thrift.TException;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.siemens.sw360.commonIO.ConvertRecord.putToTodos;

/**
 * @author johannes.najjar@tngtech.com
 */
public class TypeMappings {
    @NotNull
    public static <T> Predicate<T> containedWithAddIn(final Set<T> knownIds) {
        return new Predicate<T>() {
                            @Override
                            public boolean apply(T input) {
                                return knownIds.add(input);
                            }
                        };
    }

    public  static <T,U> ImmutableList<T> getElementsWithIdentifiersNotInMap(Function<T, U> getIdentifier, Map<U, T> theMap, List<T> candidates) {
        return FluentIterable.from(candidates).filter(
                CommonUtils.afterFunction(getIdentifier).is(containedWithAddIn(Sets.newHashSet(theMap.keySet())))).toList();
    }

    public  static <T,U> ImmutableList<T> getElementsWithIdentifiersNotInSet(Function<T, U> getIdentifier, Set<U> theSet, List<T> candidates) {
        return FluentIterable.from(candidates).filter(
                CommonUtils.afterFunction(getIdentifier).is(containedWithAddIn(theSet))).toList();
    }

    @NotNull
    public static Function<License, String> getLicenseIdentifier() {
        return new Function<License, String>() {
            @Override
            public String apply(License input) {
                return input.getShortname();
            }
        };
    }

    @NotNull
    public static Function<Todo, Integer> getTodoIdentifier() {
        return new Function<Todo, Integer>() {
            @Override
            public Integer apply(Todo input) {
                return input.getTodoId();
            }
        };
    }

    @NotNull
    public static Function<Obligation, Integer> getObligationIdentifier() {
        return new Function<Obligation, Integer>() {
            @Override
            public Integer apply(Obligation input) {
                return input.getObligationId();
            }
        };
    }

    @NotNull
    public static Function<LicenseType, Integer> getLicenseTypeIdentifier() {
        return new Function<LicenseType, Integer>() {
            @Override
            public Integer apply(LicenseType input) {
                return input.getLicenseTypeId();
            }
        };
    }

    @NotNull
    public static Function<RiskCategory, Integer> getRiskCategoryIdentifier() {
        return new Function<RiskCategory, Integer>() {
            @Override
            public Integer apply(RiskCategory input) {
                return input.getRiskCategoryId();
            }
        };
    }

    @NotNull
    public static Function<Risk, Integer> getRiskIdentifier() {
        return new Function<Risk, Integer>() {
            @Override
            public Integer apply(Risk input) {
                return input.getRiskId();
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <T, U> Function<T, U> getIdentifier(Class<T> clazz, @SuppressWarnings("unused") Class<U> uClass /*used to infer type*/) throws SW360Exception {
        if (clazz.equals(LicenseType.class)) {
            return (Function<T, U>) getLicenseTypeIdentifier();
        } else if (clazz.equals(Obligation.class)) {
            return (Function<T, U>) getObligationIdentifier();
        } else if (clazz.equals(RiskCategory.class)) {
            return (Function<T, U>) getRiskCategoryIdentifier();
        }

        throw new SW360Exception("Unknown Type requested");
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> getAllFromDB(LicenseService.Iface licenseClient, Class<T> clazz) throws TException {
        if (clazz.equals(LicenseType.class)) {
            return (List<T>) licenseClient.getLicenseTypes();
        } else if (clazz.equals(Obligation.class)) {
            return (List<T>) licenseClient.getObligations();
        } else if (clazz.equals(RiskCategory.class)) {
            return (List<T>) licenseClient.getRiskCategories();
        }
        throw new SW360Exception("Unknown Type requested");
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> simpleConvert(List<CSVRecord> records, Class<T> clazz) throws SW360Exception {
        if (clazz.equals(LicenseType.class)) {
            return (List<T>) ConvertRecord.convertLicenseTypes(records);
        } else if (clazz.equals(Obligation.class)) {
            return (List<T>) ConvertRecord.convertObligation(records);
        } else if (clazz.equals(RiskCategory.class)) {
            return (List<T>) ConvertRecord.convertRiskCategories(records);
        }
        throw new SW360Exception("Unknown Type requested");
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> addAlltoDB(LicenseService.Iface licenseClient, Class<T> clazz, List<T> candidates) throws TException {
        if (candidates != null && !candidates.isEmpty()) {
            if (clazz.equals(LicenseType.class)) {
                return (List<T>) licenseClient.addLicenseTypes((List<LicenseType>) candidates);
            } else if (clazz.equals(Obligation.class)) {
                return (List<T>) licenseClient.addObligations((List<Obligation>) candidates);
            } else if (clazz.equals(RiskCategory.class)) {
                return (List<T>) licenseClient.addRiskCategories((List<RiskCategory>) candidates);
            }
        }
        throw new SW360Exception("Unknown Type requested");
    }

    @NotNull
    public static <T, U> Map<U, T> getIdentifierToTypeMapAndWriteMissingToDatabase(LicenseService.Iface licenseClient, InputStream in, Class<T> clazz, Class<U> uClass) throws TException {
        Map<U, T> typeMap;
        List<CSVRecord> records = ImportCSV.readAsCSVRecords(in);
        final List<T> recordsToAdd = simpleConvert(records, clazz);
        final List<T> fullList = CommonUtils.nullToEmptyList(getAllFromDB(licenseClient, clazz));
        typeMap = Maps.newHashMap(Maps.uniqueIndex(fullList, getIdentifier(clazz, uClass)));
        final ImmutableList<T> filteredList = getElementsWithIdentifiersNotInMap(getIdentifier(clazz, uClass), typeMap, recordsToAdd);
        List<T> added = null;
        if (filteredList.size() > 0) {
            added = addAlltoDB(licenseClient, clazz, filteredList);
        }
        if (added != null)
            typeMap.putAll(Maps.uniqueIndex(added, getIdentifier(clazz, uClass)));
        return typeMap;
    }

    @NotNull
    public static Map<Integer, Risk> getIntegerRiskMap(LicenseService.Iface licenseClient, Map<Integer, RiskCategory> riskCategoryMap, InputStream in) throws TException {
        List<CSVRecord> riskRecords = ImportCSV.readAsCSVRecords(in);
        final List<Risk> risksToAdd = ConvertRecord.convertRisks(riskRecords, riskCategoryMap);
        final List<Risk> risks = CommonUtils.nullToEmptyList(licenseClient.getRisks());
        Map<Integer, Risk> riskMap = Maps.newHashMap(Maps.uniqueIndex(risks, getRiskIdentifier()));
        final ImmutableList<Risk> filteredList = getElementsWithIdentifiersNotInMap(getRiskIdentifier(), riskMap, risksToAdd);
        List<Risk> addedRisks = null;
        if (filteredList.size() > 0) {
            addedRisks = licenseClient.addRisks(filteredList);
        }
        if (addedRisks != null)
            riskMap.putAll(Maps.uniqueIndex(addedRisks, getRiskIdentifier()));
        return riskMap;
    }

    @NotNull
    public static Map<Integer, Todo> getTodoMap(LicenseService.Iface licenseClient, Map<Integer, Obligation> obligationMap, Map<Integer, Set<Integer>> obligationTodoMapping, InputStream in) throws TException {
        List<CSVRecord> todoRecords = ImportCSV.readAsCSVRecords(in);
        final List<Todo> todos = CommonUtils.nullToEmptyList(licenseClient.getTodos());
        Map<Integer, Todo> todoMap = Maps.newHashMap(Maps.uniqueIndex(todos, getTodoIdentifier()));
        final List<Todo> todosToAdd = ConvertRecord.convertTodos(todoRecords);
        final ImmutableList<Todo> filteredTodos = getElementsWithIdentifiersNotInMap(getTodoIdentifier(), todoMap, todosToAdd);
        final ImmutableMap<Integer, Todo> filteredMap = Maps.uniqueIndex(filteredTodos, getTodoIdentifier());
        putToTodos(obligationMap, filteredMap, obligationTodoMapping);

        if (filteredTodos.size() > 0) {
            final List<Todo> addedTodos = licenseClient.addTodos(filteredTodos);
            if (addedTodos != null) {
                final ImmutableMap<Integer, Todo> addedTodoMap = Maps.uniqueIndex(addedTodos, getTodoIdentifier());
                todoMap.putAll(addedTodoMap);
            }
        }
        return todoMap;
    }
}
