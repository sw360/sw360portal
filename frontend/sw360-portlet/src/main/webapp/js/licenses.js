/*
 * Copyright Siemens AG, 2013-2016. Part of the SW360 Portal Project.
 *
 * SPDX-License-Identifier: EPL-1.0
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

var licensesTable;

function createUrl( paramId, paramVal) {
    var portletURL = Liferay.PortletURL.createURL( baseUrl ).setParameter(pageName,pageDetail).setParameter(paramId,paramVal);
    return portletURL.toString();
}

function createDetailURLfromLicenseId (paramVal) {
    return createUrl(licenseIdInURL,paramVal );
}

//This can not be document ready function as liferay definitions need to be loaded first
$(window).load(function() {
    var data = parseAllLicensesList(allLicensesString);
     createLicenseTable(data);
});

function parseAllLicensesList(allLicensesList ){
    var result = [];

    for (var i=0; i<allLicensesList.length; ++i) {
        var id = allLicensesList[i].id;
        var row = {
            "DT_RowId": id,
            "0": "<a href='"+createDetailURLfromLicenseId(id)+"' target='_self'>"+allLicensesList[i].id+"</a>",
            "1": allLicensesList[i].fullname,
            "2": getLicenseType(allLicensesList[i])
         };
        result.push(row);
    }

    return result;
}

function getLicenseType(lic) {

    var ltype = lic.licenceType;
    var type;
    if(ltype){
        type=ltype.type;
    }
    if(!type){
        type = "--";
    }

    return type;
}

function createLicenseTable(data) {

    licensesTable = $('#licensesTable').dataTable({
        "sPaginationType": "full_numbers",
        "iDisplayLength": 10,
        "oLanguage": {
            "sLengthMenu": 'Display <select>\
                <option value="5">5</option>\
                <option value="10">10</option>\
                <option value="20">20</option>\
                <option value="50">50</option>\
                <option value="100">100</option>\
                </select> licenses'
        },
        "aaData": data,
        "aoColumns": [
            { "sTitle": "License Shortname" },
            { "sTitle": "License Fullname" },
            { "sTitle": "License Type" }
        ]
    });

    $('#licensesTable_filter').hide();
    $('#licensesTable_first').hide();
    $('#licensesTable_last').hide();

}

function licenseSearch(searchFieldId) {
    licensesTable.fnFilter( $('#'+searchFieldId).val());
}