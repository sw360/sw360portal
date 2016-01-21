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

var oTable;

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
            "0": "<a href='"+createDetailURLfromLicenseId(id)+"' target='_self'>"+allLicensesList[i].shortname+"</a>",
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

    oTable = $('#licensesTable').dataTable({
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
    oTable.fnFilter( $('#'+searchFieldId).val());
}