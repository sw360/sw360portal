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
package com.siemens.sw360.licenses;


import com.siemens.sw360.datahandler.common.DatabaseSettings;
import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.licenses.*;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.licenses.db.LicenseDatabaseHandler;
import org.apache.thrift.TException;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Set;

import static com.siemens.sw360.datahandler.common.SW360Assert.*;

/**
 * Implementation of the Thrift service
 *
 * @author cedric.bodet@tngtech.com
 */
public class LicenseHandler implements LicenseService.Iface {

    LicenseDatabaseHandler handler;

    LicenseHandler() throws MalformedURLException {
        handler = new LicenseDatabaseHandler(DatabaseSettings.COUCH_DB_URL, DatabaseSettings.COUCH_DB_DATABASE);
    }

    /////////////////////
    // SUMMARY GETTERS //
    /////////////////////

    /**
     * Get a list of all obligations
     */
    @Override
    public List<Obligation> getAllObligations() throws TException {
        return handler.getAllObligations();
    }

    /**
     * Get an list of id/identifier/fullname for all licenses. The other fields will be set to null.
     */
    @Override
    public List<License> getLicenseSummary() throws TException {
        return handler.getLicenseSummary();
    }

    /**
     * Get an list of license details for Excel export.
     */
    @Override
    public List<License> getLicenseSummaryForExport() throws TException {
        return handler.getLicenseSummaryForExport();
    }

    @Override
    public List<License> getDetailedLicenseSummaryForExport(String organisation) throws TException {
        return handler.getDetailedLicenseSummaryForExport(organisation);
    }

    @Override
    public List<License> getDetailedLicenseSummary(String organisation, List<String> identifiers) throws TException {
        return handler.getDetailedLicenseSummaryForExport(organisation, identifiers);
    }

    @Override
    public List<RiskCategory> addRiskCategories(List<RiskCategory> riskCategories) throws TException {
        return handler.addRiskCategories(riskCategories);
    }

    @Override
    public List<Risk> addRisks(List<Risk> risks) throws TException {
        return handler.addRisks(risks);
    }

    @Override
    public List<Obligation> addObligations(List<Obligation> obligations) throws TException {
        return handler.addObligations(obligations);

    }

    @Override
    public List<LicenseType> addLicenseTypes(List<LicenseType> licenseTypes) throws TException {
        return handler.addLicenseTypes(licenseTypes);
    }

    @Override
    public List<License> addLicenses(List<License> licenses) throws TException {
        return handler.addLicenses(licenses);
    }

    @Override
    public List<Todo> addTodos(List<Todo> todos) throws TException {
        return handler.addTodos(todos);

    }

    @Override
    public List<RiskCategory> getRiskCategories() throws TException {
        return handler.getRiskCategories() ;
    }

    @Override
    public List<Risk> getRisks() throws TException {
        return handler.getRisks();
    }

    @Override
    public List<LicenseType> getLicenseTypes() throws TException {
        return handler.getLicenseTypes();
    }

    @Override
    public List<License> getLicenses() throws TException {
        return handler.getLicenses();
    }

    @Override
    public List<Todo> getTodos() throws TException {
        return handler.getTodos();
    }

    @Override
    public List<Risk> getRisksByIds(List<String> ids) throws TException {
        assertNotEmpty(ids);
        return handler.getRisksByIds(ids);
    }

    @Override
    public List<RiskCategory> getRiskCategoriesByIds(List<String> ids) throws TException {
        assertNotEmpty(ids);
        return handler.getRiskCategoriesByIds(ids);
    }

    @Override
    public List<Obligation> getObligationsByIds(List<String> ids) throws TException {
        assertNotEmpty(ids);
        return handler.getObligationsByIds(ids);
    }

    @Override
    public List<LicenseType> getLicenseTypesByIds(List<String> ids) throws TException {
        assertNotEmpty(ids);
        return handler.getLicenseTypesByIds(ids);
    }

    @Override
    public List<Todo> getTodosByIds(List<String> ids) throws TException {
        assertNotEmpty(ids);
        return handler.getTodosByIds(ids);
    }



    ////////////////////////////
    // GET INDIVIDUAL OBJECTS //
    ////////////////////////////

    /**
     * Get a single license by providing its ID, with todos filtered for the given organisation
     */
    @Override
    public License getByID(String id, String organisation) throws TException {
        assertNotEmpty(id);
        assertNotEmpty(organisation);

        return handler.getLicenseForOrganisation(id, organisation);
    }

    @Override
    public License getFromID(String id) throws TException {
        assertNotEmpty(id);
        return handler.getById(id);
    }

    @Override
    public List<License> getByIds(Set<String> ids, String organisation) throws TException {
        assertNotNull(ids);
        assertNotEmpty(organisation);

        return handler.getLicenses(ids, organisation);
    }

    @Override
    public Risk getRiskById(String id) throws TException {
        assertNotEmpty(id);
        return handler.getRiskById(id);
    }

    @Override
    public RiskCategory getRiskCategoryById(String id) throws TException {
        assertNotEmpty(id);
        return handler.getRiskCategoryById(id);
    }

    @Override
    public Obligation getObligationById(String id) throws TException {
        assertNotEmpty(id);
        return handler.getObligationById(id);
    }

    @Override
    public LicenseType getLicenseTypeById(String id) throws TException {
        assertNotEmpty(id);
        return handler.getLicenseTypeById(id);
    }

    @Override
    public Todo getTodoById(String id) throws TException {
        assertNotEmpty(id);
        return handler.getTodoById(id);
    }

    ////////////////////
    // BUSINESS LOGIC //
    ////////////////////

    /**
     * Add a new todo object
     */
    @Override
    public String addTodo(Todo todo) throws TException {
        assertNotNull(todo);
        assertIdUnset(todo.getId());

        return handler.addTodo(todo);
    }

    /**
     * Add an existing todo to a license
     */
    @Override
    public RequestStatus addTodoToLicense(Todo todo, String licenseId, User user) throws TException {
        //final String todoId = addTodo(todo);
        //assertNotEmpty(todoId);
        assertNotEmpty(licenseId);
       return  handler.addTodoToLicense(todo, licenseId, user);
    }

    @Override
    public RequestStatus updateLicense(License license, User user) throws TException {
        return handler.updateLicense(license, user);
    }

    @Override
    public RequestStatus updateWhitelist(String licenceId, Set<String> whitelist, User user) throws TException {
        assertNotEmpty(licenceId);
        assertUser(user);

        return handler.updateWhitelist(licenceId, whitelist, user);
    }

}
