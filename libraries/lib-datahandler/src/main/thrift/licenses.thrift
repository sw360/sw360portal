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
include "users.thrift"
include "sw360.thrift"

namespace java com.siemens.sw360.datahandler.thrift.licenses
namespace php sw360.thrift.licenses

typedef users.User User
typedef users.RequestedAction RequestedAction
typedef sw360.RequestStatus RequestStatus
typedef sw360.DocumentState DocumentState

struct Obligation {
	1: optional string id,
    2: optional string revision
    3: optional string type = "obligation",
    5: required string name,
    6: required i32 obligationId,
}

struct Todo {
    1: optional string id,
    2: optional string revision,
    3: optional string type = "todo",
    4: required string text,
    5: optional set<string> whitelist,
    6: optional bool development,
    7: optional bool distribution,
    8: optional list<Obligation> obligations,
    9: optional set<string> obligationDatabaseIds,
    10: required i32 todoId,

    // These two are a quick fix to receiving booleans in PHP not working at the moment
    15: optional string developmentString,
    16: optional string distributionString,
}

struct RiskCategory {
    1: optional string id,
    2: optional string revision,
    3: optional string type = "riskCategory",
    5: required i32 riskCategoryId,
    6: required string text
}

struct Risk {
    1: optional string id,
    2: optional string revision,
    3: optional string type = "risk",
    5: required i32 riskId,
    6: required string text,
    7: optional RiskCategory category,
    8: optional string riskCategoryDatabaseId,
}

struct LicenseType {
    1: optional string id,
    2: optional string revision,
    3: optional string type = "licenseType",
	5: required i32 licenseTypeId,
    6: required string licenseType;
}

struct License {
	 1: optional string id,
	 2: optional string revision,
	 3: optional string type = "license",
	 4: required string shortname, // Short name of the license
	 5: required string fullname,
	 6: optional LicenseType licenseType,
	 7: optional string licenseTypeDatabaseId,
	 10: optional bool GPLv2Compat,
	 11: optional bool GPLv3Compat,
	 12: optional string reviewdate,
    20: optional list<Todo> todos,
    21: optional set<string> todoDatabaseIds,
	22: optional list<Risk> risks,
	23: optional set<string> riskDatabaseIds,
    25: optional string text,

    90: optional DocumentState documentState,

	200: optional map<RequestedAction, bool> permissions,
}

service LicenseService {

    // Get an map of id/identifier/fullname for all licenses. The other fields will be set to null.
    list<License> getLicenseSummary();

    // Get a list of all obligations
    list<Obligation> getAllObligations();


    // Get a single license by providing its ID, with todos filtered for the given organisation
    License getByID(1:string id, 2: string organisation);
    //Get a single license by providing its ID, todos filtered for organisation, user's
    //moderation request with status pending or in progress applied
    License getByIDWithOwnModerationRequests(1:string id, 2: string organisation, 3: User user);

    list<License> getByIds(1:set<string> ids, 2: string organisation);

    // Add a new todo object
    string addTodo(1:Todo todo),
    // Add an existing todo do a license
    RequestStatus addTodoToLicense(1: Todo todo, 2: string licenseId, 3: User user);
    //Update given license, user must have permission to do so,
    // requestingUser could be same as user or the requesting user from a moderation request
    RequestStatus updateLicense(1: License license, 2: User user, 3: User requestingUser);
    // Update the whitelisted todos for an organisation
    RequestStatus updateWhitelist(1: string licenceId, 2: set<string> todoDatabaseIds, 3: User user);

    // Get an map of id/identifier/fullname for all licenses. The other fields will be set to null.
    list<License> getLicenseSummaryForExport();
    list<License> getDetailedLicenseSummaryForExport(1: string organisation);
    list<License> getDetailedLicenseSummary(1: string organisation, 2: list<string> identifiers);
    list<LicenseType> getLicenseTypeSummaryForExport();


    //Bulk adds
    list<RiskCategory> addRiskCategories(1: list <RiskCategory> riskCategories);
    list<Risk> addRisks(1: list <Risk> risks);
    list<Obligation> addObligations(1: list <Obligation> obligations);
    list<LicenseType> addLicenseTypes(1: list <LicenseType> licenseTypes);
    list<License> addLicenses(1: list <License> licenses);
    list<Todo> addTodos(1: list <Todo> todos);

    //Complete List getters
    list<RiskCategory> getRiskCategories();
    list<Risk> getRisks();
    list<LicenseType> getLicenseTypes();
    list<License> getLicenses();
    list<Todo> getTodos();

    list<Risk> getRisksByIds( 1: list<string> ids);
    list<RiskCategory> getRiskCategoriesByIds( 1: list<string> ids);
    list<Obligation> getObligationsByIds( 1: list<string> ids);
    list<LicenseType> getLicenseTypesByIds( 1: list<string> ids);
    list<Todo> getTodosByIds( 1: list<string> ids);

    Risk getRiskById( 1: string id);
    RiskCategory getRiskCategoryById( 1: string id);
    Obligation getObligationById( 1: string id);
    LicenseType getLicenseTypeById( 1: string id);
    Todo getTodoById( 1: string id);
}
