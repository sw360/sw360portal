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

//This can not be document ready function as liferay definitions need to be loaded first
$(window).load(function() {
    var data = parseList(allSearchResultsString);

    createSearchTable(data);
});

function parseList(listString ){
    var result = [];

    for (var i=0; i<listString.length; ++i) {
        var id = listString[i].id;
        var row = {
            "DT_RowId": id,
            "0": listString[i].type,
            "1": listString[i].name
         };
        result.push(row);
    }

    return result;
}

function createSearchTable(data) {
    $('#searchTable').dataTable({
        "sPaginationType": "full_numbers",
        "aaData": data,
        "aoColumns": [
            { "sTitle": "Type",
                "mRender": function ( data, type, full ) {
                    return typeColumn( data, type, full );
                }
            },
            { "sTitle": "Text" }
        ]
    });
    $('#searchTable_filter').hide();
    $('#searchTable_first').hide();
    $('#searchTable_last').hide();
}