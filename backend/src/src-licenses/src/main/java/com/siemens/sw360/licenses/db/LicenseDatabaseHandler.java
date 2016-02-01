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
package com.siemens.sw360.licenses.db;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.siemens.sw360.components.summary.SummaryType;
import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.common.SW360Utils;
import com.siemens.sw360.datahandler.couchdb.DatabaseConnector;
import com.siemens.sw360.datahandler.entitlement.LicenseModerator;
import com.siemens.sw360.datahandler.permissions.PermissionUtils;
import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.SW360Exception;
import com.siemens.sw360.datahandler.thrift.ThriftUtils;
import com.siemens.sw360.datahandler.thrift.licenses.*;
import com.siemens.sw360.datahandler.thrift.users.RequestedAction;
import com.siemens.sw360.datahandler.thrift.users.User;
import org.ektorp.DocumentOperationResult;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.util.*;

import static com.siemens.sw360.datahandler.common.SW360Assert.assertNotNull;
import static com.siemens.sw360.datahandler.permissions.PermissionUtils.makePermission;
import static com.siemens.sw360.datahandler.thrift.ThriftValidate.*;

/**
 * Class for accessing the CouchDB database
 *
 * @author cedric.bodet@tngtech.com
 */
public class LicenseDatabaseHandler {

    /**
     * Connection to the couchDB database
     */
    private final DatabaseConnector db;

    /**
     * License Repository
     */
    private final LicenseRepository licenseRepository;
    private final ObligationRepository obligationRepository;
    private final TodoRepository todoRepository;
    private final RiskRepository riskRepository;
    private final RiskCategoryRepository riskCategoryRepository;
    private final LicenseTypeRepository licenseTypeRepository;
    private final LicenseModerator moderator;

    public LicenseDatabaseHandler(String url, String dbName) throws MalformedURLException {
        // Create the connector
        db = new DatabaseConnector(url, dbName);

        // Create the repository
        licenseRepository = new LicenseRepository(db);
        obligationRepository = new ObligationRepository(db);
        todoRepository = new TodoRepository(db);
        riskRepository = new RiskRepository(db);
        riskCategoryRepository = new RiskCategoryRepository(db);
        licenseTypeRepository = new LicenseTypeRepository(db);

        moderator = new LicenseModerator();
    }


    /////////////////////
    // SUMMARY GETTERS //
    /////////////////////

    /**
     * Get all obligations from database
     */
    public List<Obligation> getAllObligations() {
        return obligationRepository.getAll();
    }

    /**
     * Get a summary of all licenses from the database
     */
    public List<License> getLicenseSummary() {
        final List<License> licenses = licenseRepository.getAll();
        final List<LicenseType> licenseTypes = licenseTypeRepository.getAll();
        putLicenseTypesInLicenses(licenses, licenseTypes);
        /*Note that risks are not set here*/
        return licenseRepository.makeSummaryFromFullDocs(SummaryType.SUMMARY, licenses);

    }

    /**
     * Get a summary of all licenses from the database for Excel export
     */
    public List<License> getLicenseSummaryForExport() {
        return licenseRepository.getLicenseSummaryForExport();
    }

    ////////////////////////////
    // GET INDIVIDUAL OBJECTS //
    ////////////////////////////

    /**
     * Get license from the database and fill its obligations
     */
    public License getLicense(String id, String organisation) throws SW360Exception {
        License license = licenseRepository.get(id);

        assertNotNull(license);

        fillLicense(organisation, license);

        return license;
    }

    private void fillLicense(String organisation, License license) {
        if (license.isSetTodoDatabaseIds()) {
            license.setTodos(getTodosByIds(license.todoDatabaseIds));
            license.unsetTodoDatabaseIds();
        }

        if (license.isSetTodos()) {
            for (Todo todo : license.getTodos()) {
                todo.setWhitelist(SW360Utils.filterBUSet(organisation, todo.whitelist));
            }
        }

        if (license.isSetLicenseTypeDatabaseId()) {
            final LicenseType licenseType = licenseTypeRepository.get(license.getLicenseTypeDatabaseId());
            license.setLicenseType(licenseType);
        }

    }

    ////////////////////
    // BUSINESS LOGIC //
    ////////////////////

    /**
     * Adds a new todo to the database.
     *
     * @return ID of the added todo.
     */
    public String addTodo(@NotNull Todo todo) throws SW360Exception {
        prepareTodo(todo);
        todoRepository.add(todo);

        return todo.getId();
    }

    /**
     * Add todo id to a given license
     */
    public RequestStatus addTodoToLicense(String todoId, String licenseId, User user) throws SW360Exception {
        // Get objects from database
        License license = licenseRepository.get(licenseId);
        Todo todo = todoRepository.get(todoId);

        assertNotNull(license);
        assertNotNull(todo);

        List<Todo> Todos = license.getTodos();
        if (Todos != null){
            for (Todo todoAlreadyInLicense : Todos) {
                if (todoAlreadyInLicense.whitelist == null) {
                    todo.setWhitelist(Collections.emptySet());
                }
            }
        }
        license.addToTodoDatabaseIds(todoId);

        if (makePermission(license, user).isActionAllowed(RequestedAction.WRITE)) {
            licenseRepository.update(license);
            return RequestStatus.SUCCESS;
        } else {
            if(todo.getObligations()==null) {
                todo.setObligations(Collections.emptyList());
            }
            return moderator.updateLicense(license, user); // Only moderators can change licenses!
        }
    }

    /**
     * Update the whitelisted todos for an organisation
     */
    public RequestStatus updateWhitelist(String licenseId, Set<String> whitelistTodos, User user) throws SW360Exception {
        License license = licenseRepository.get(licenseId);
        assertNotNull(license);

        if (makePermission(license, user).isActionAllowed(RequestedAction.WRITE)) {

            String organisation = user.getDepartment();

            String bu = SW360Utils.getBUFromOrganisation(organisation);

            List<Todo> todos = todoRepository.get(license.todoDatabaseIds);
            for (Todo todo : todos) {
                String todoId = todo.getId();
                Set<String> currentWhitelist = CommonUtils.nullToEmptySet(todo.whitelist);

                // Add to whitelist if necessary
                if (whitelistTodos.contains(todoId) && !currentWhitelist.contains(bu)) {
                    todo.addToWhitelist(bu);
                    todoRepository.update(todo);
                }

                // Remove from whitelist if necessary
                if (!whitelistTodos.contains(todoId) && currentWhitelist.contains(bu)) {
                    currentWhitelist.remove(bu);
                    todoRepository.update(todo);
                }

                // In the other cases, no doBulk necessary
            }
            return RequestStatus.SUCCESS;
        } else {
            return RequestStatus.SENT_TO_MODERATOR; // Only moderators can delete!
        }
    }

    public List<License> getLicenses(Set<String> ids, String organisation) {
        final List<License> licenses = licenseRepository.get(ids);
        final List<Todo> todosFromLicenses = getTodosFromLicenses(licenses);
        final List<LicenseType> licenseTypes = getLicenseTypesFromLicenses(licenses);
        final List<Risk> risks = getRisksFromLicenses(licenses);
        filterTodoWhiteListAndFillTodosRisksAndLicenseTypeInLicense(organisation, licenses, todosFromLicenses, risks, licenseTypes);
        return licenses;
    }

    public List<License> getDetailedLicenseSummaryForExport(String organisation) {

        final List<License> licenses = licenseRepository.getAll();
        final List<Todo> todos = todoRepository.getAll();
        final List<LicenseType> licenseTypes = licenseTypeRepository.getAll();
        final List<Risk> risks = riskRepository.getAll();
        return filterTodoWhiteListAndFillTodosRisksAndLicenseTypeInLicense(organisation, licenses, todos, risks, licenseTypes);
    }

    @NotNull
    private List<License> filterTodoWhiteListAndFillTodosRisksAndLicenseTypeInLicense(String organisation, List<License> licenses, List<Todo> todos, List<Risk> risks, List<LicenseType> licenseTypes) {
        filterTodoWhiteList(organisation, todos);
        fillTodosRisksAndLicenseTypes(licenses, todos, risks, licenseTypes);
        return licenses;
    }

    private void putLicenseTypesInLicenses(List<License> licenses, List<LicenseType> licenseTypes) {
        final Map<String, LicenseType> licenseTypesById = ThriftUtils.getIdMap(licenseTypes);

        for (License license : licenses) {
            license.setLicenseType(licenseTypesById.get(license.getLicenseTypeDatabaseId()));
            license.unsetLicenseTypeDatabaseId();
        }
    }

    private void putTodosInLicenses(List<License> licenses, List<Todo> todos) {
        final Map<String, Todo> todosById = ThriftUtils.getIdMap(todos);

        for (License license : licenses) {
            license.setTodos(getEntriesFromIds(todosById, CommonUtils.nullToEmptySet(license.getTodoDatabaseIds())));
            license.unsetTodoDatabaseIds();
        }
    }

    private void putRisksInLicenses(List<License> licenses, List<Risk> risks) {
        final Map<String, Risk> risksById = ThriftUtils.getIdMap(risks);

        for (License license : licenses) {
            license.setRisks(getEntriesFromIds(risksById, CommonUtils.nullToEmptySet(license.getRiskDatabaseIds())));
            license.unsetRiskDatabaseIds();
        }
    }

    private void filterTodoWhiteList(String organisation, List<Todo> todos) {
        for (Todo todo : todos) {
            todo.setWhitelist(SW360Utils.filterBUSet(organisation, todo.getWhitelist()));
        }
    }


    public static <T> List<T> getEntriesFromIds(final Map<String, T> map, Set<String> ids) {
        return FluentIterable.from(ids).transform(new Function<String, T>() {
            @Override
            public T apply(String input) {
                return map.get(input);
            }
        }).filter(Predicates.notNull()).toList();
    }

    public RequestStatus updateLicense(License license, User user) {
        if (PermissionUtils.isAdmin(user)) {
            licenseRepository.update(license);
            return RequestStatus.SUCCESS;
        }
        return RequestStatus.FAILURE;
    }

    public License getById(String id) {
        return licenseRepository.get(id);
    }

    public List<License> getDetailedLicenseSummaryForExport(String organisation, List<String> identifiers) {
        final List<License> licenses = CommonUtils.nullToEmptyList(licenseRepository.searchByShortName(identifiers));
        List<Todo> todos = getTodosFromLicenses(licenses);
        final List<LicenseType> licenseTypes = getLicenseTypesFromLicenses(licenses);
        final List<Risk> risks = getRisksFromLicenses(licenses);
        return filterTodoWhiteListAndFillTodosRisksAndLicenseTypeInLicense(organisation, licenses, todos, risks, licenseTypes);
    }

    private List<Todo> getTodosFromLicenses(List<License> licenses) {
        List<Todo> todos;
        final Set<String> todoIds = new HashSet<>();
        for (License license : licenses) {
            todoIds.addAll(CommonUtils.nullToEmptySet(license.getTodoDatabaseIds()));
        }

        if (todoIds.isEmpty()) {
            todos = Collections.emptyList();
        } else {
            todos = CommonUtils.nullToEmptyList(getTodosByIds(todoIds));
        }
        return todos;
    }

    private List<Risk> getRisksFromLicenses(List<License> licenses) {
        List<Risk> risks;
        final Set<String> riskIds = new HashSet<>();
        for (License license : licenses) {
            riskIds.addAll(CommonUtils.nullToEmptySet(license.getRiskDatabaseIds()));
        }

        if (riskIds.isEmpty()) {
            risks = Collections.emptyList();
        } else {
            risks = CommonUtils.nullToEmptyList(getRisksByIds(riskIds));
        }
        return risks;
    }

    private List<LicenseType> getLicenseTypesFromLicenses(List<License> licenses) {
        List<LicenseType> licenseTypes;
        final Set<String> licenseTypeIds = new HashSet<>();
        for (License license : licenses) {
            if (license.isSetLicenseTypeDatabaseId()) {
                licenseTypeIds.add(license.getLicenseTypeDatabaseId());
            }
        }

        if (licenseTypeIds.isEmpty()) {
            licenseTypes = Collections.emptyList();
        } else {
            licenseTypes = CommonUtils.nullToEmptyList(getLicenseTypesByIds(licenseTypeIds));
        }
        return licenseTypes;
    }

    public List<RiskCategory> addRiskCategories(List<RiskCategory> riskCategories) throws SW360Exception {
        for (RiskCategory riskCategory : riskCategories) {
            prepareRiskCategory(riskCategory);
        }

        final List<DocumentOperationResult> documentOperationResults = riskCategoryRepository.executeBulk(riskCategories);
        if (documentOperationResults.isEmpty()) {
            return riskCategories;
        } else return null;
    }

    public List<Risk> addRisks(List<Risk> risks) throws SW360Exception {
        for (Risk risk : risks) {
            prepareRisk(risk);
        }

        final List<DocumentOperationResult> documentOperationResults = riskRepository.executeBulk(risks);
        if (documentOperationResults.isEmpty()) {
            return risks;
        } else return null;
    }

    public List<LicenseType> addLicenseTypes(List<LicenseType> licenseTypes) {
        final List<DocumentOperationResult> documentOperationResults = licenseTypeRepository.executeBulk(licenseTypes);
        if (documentOperationResults.isEmpty()) {
            return licenseTypes;
        } else return null;
    }

    public List<License> addLicenses(List<License> licenses) throws SW360Exception {
        for (License license : licenses) {
            prepareLicense(license);
        }

        final List<DocumentOperationResult> documentOperationResults = licenseRepository.executeBulk(licenses);
        if (documentOperationResults.isEmpty()) {
            return licenses;
        } else return null;
    }

    public List<Obligation> addObligations(List<Obligation> obligations) throws SW360Exception {
        for (Obligation obligation : obligations) {
            prepareObligation(obligation);
        }

        final List<DocumentOperationResult> documentOperationResults = obligationRepository.executeBulk(obligations);
        if (documentOperationResults.isEmpty()) {
            return obligations;
        } else return null;
    }

    public List<Todo> addTodos(List<Todo> todos) throws SW360Exception {
        for (Todo todo : todos) {
            prepareTodo(todo);
        }

        final List<DocumentOperationResult> documentOperationResults = todoRepository.executeBulk(todos);
        if (documentOperationResults.isEmpty()) {
            return todos;
        } else return null;
    }

    public List<License> getLicenses() {
        final List<License> licenses = licenseRepository.getAll();
        final List<Todo> todos = getTodosFromLicenses(licenses);
        final List<LicenseType> licenseTypes = getLicenseTypesFromLicenses(licenses);
        final List<Risk> risks = getRisksFromLicenses(licenses);
        fillTodosRisksAndLicenseTypes(licenses, todos, risks, licenseTypes);
        return licenses;
    }

    private void fillTodosRisksAndLicenseTypes(List<License> licenses, List<Todo> todos, List<Risk> risks, List<LicenseType> licenseTypes) {
        putTodosInLicenses(licenses, todos);
        putRisksInLicenses(licenses, risks);
        putLicenseTypesInLicenses(licenses, licenseTypes);
    }

    public List<LicenseType> getLicenseTypes() {
        return licenseTypeRepository.getAll();
    }

    public List<Risk> getRisks() {
        final List<Risk> risks = riskRepository.getAll();
        fillRisks(risks);
        return risks;
    }

    public List<RiskCategory> getRiskCategories() {
        return riskCategoryRepository.getAll();
    }


    public List<Todo> getTodos() {
        final List<Todo> todos = todoRepository.getAll();
        fillTodos(todos);
        return todos;
    }

    public List<Risk> getRisksByIds(Collection<String> ids) {
        final List<Risk> risks = riskRepository.get(ids);
        fillRisks(risks);
        return risks;
    }

    private void fillRisks(List<Risk> risks) {
        final List<RiskCategory> riskCategories = riskCategoryRepository.get(FluentIterable.from(risks).transform(new Function<Risk, String>() {
            @Override
            public String apply(Risk input) {
                return input.getRiskCategoryDatabaseId();
            }
        }).filter(Predicates.notNull()).toList());

        final Map<String, RiskCategory> idMap = ThriftUtils.getIdMap(riskCategories);
        for (Risk risk : risks) {
            if (risk.isSetRiskCategoryDatabaseId()) {
                risk.setCategory(idMap.get(risk.getRiskCategoryDatabaseId()));
                risk.unsetRiskCategoryDatabaseId();
            }
        }
    }

    public List<RiskCategory> getRiskCategoriesByIds(Collection<String> ids) {
        return riskCategoryRepository.get(ids);
    }

    public List<Obligation> getObligationsByIds(Collection<String> ids) {
        return obligationRepository.get(ids);
    }

    public List<LicenseType> getLicenseTypesByIds(Collection<String> ids) {
        return licenseTypeRepository.get(ids);
    }

    public List<Todo> getTodosByIds(Collection<String> ids) {
        final List<Todo> todos = todoRepository.get(ids);
        fillTodos(todos);
        return todos;
    }

    private void fillTodos(List<Todo> todos) {
        Set<String> obligationIdsToFetch = new HashSet<>();
        for (Todo todo : todos) {
            obligationIdsToFetch.addAll(CommonUtils.nullToEmptySet(todo.getObligationDatabaseIds()));
        }

        Map<String, Obligation> obligationIdMap = null;
        if (!obligationIdsToFetch.isEmpty()) {
            obligationIdMap = ThriftUtils.getIdMap(getObligationsByIds(obligationIdsToFetch));
        }
        if (obligationIdMap == null) {
            obligationIdMap = Collections.emptyMap();
        }

        for (Todo todo : todos) {
            if (todo.isSetObligationDatabaseIds()) {
                for (String id : todo.getObligationDatabaseIds()) {
                    final Obligation obligation = obligationIdMap.get(id);
                    if (obligation != null) {
                        todo.addToObligations(obligation);
                    }
                }
            }
            todo.setDevelopmentString(todo.isDevelopement()?"True":"False");
            todo.setDistributionString(todo.isDistribution()?"True":"False");
            todo.unsetObligationDatabaseIds();
        }
    }

    public Risk getRiskById(String id) {
        final Risk risk = riskRepository.get(id);
        fillRisk(risk);
        return risk;
    }

    private void fillRisk(Risk risk) {
        if (risk.isSetRiskCategoryDatabaseId()) {
            final RiskCategory riskCategory = riskCategoryRepository.get(risk.getRiskCategoryDatabaseId());
            risk.setCategory(riskCategory);
            risk.unsetRiskCategoryDatabaseId();
        }
    }

    public RiskCategory getRiskCategoryById(String id) {
        return riskCategoryRepository.get(id);
    }

    public Obligation getObligationById(String id) {
        return obligationRepository.get(id);
    }

    public LicenseType getLicenseTypeById(String id) {
        return licenseTypeRepository.get(id);
    }

    public Todo getTodoById(String id) {
        final Todo todo = todoRepository.get(id);

        fillTodo(todo);

        return todo;
    }

    private void fillTodo(Todo todo) {
        if (todo.isSetObligationDatabaseIds()) {
            final List<Obligation> obligations = obligationRepository.get(todo.getObligationDatabaseIds());
            todo.setObligations(obligations);
            todo.unsetObligationDatabaseIds();

            todo.setDevelopmentString(todo.isDevelopement()?"True":"False");
            todo.setDistributionString(todo.isDistribution()?"True":"False");
        }
    }
}
