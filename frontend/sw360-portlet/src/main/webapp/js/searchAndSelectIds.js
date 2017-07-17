/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * SPDX-License-Identifier: EPL-1.0
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

var SearchAndSelectIds = function (options) {

    var opts = {
        $addButton: options.$addButton || $('#search-add-button'),
        $searchButton: options.$searchButton || $('#search-button'),
        $searchInput: options.$searchInput || $('#search-text'),
        $tableBody: options.$tableBody || $('#search-result-table'),
        ajaxSearch: options.ajaxSearch
    };

    var currentState = {
        multi: false,
        $resultInput: false,
        $resultInputDisplay: false
    };

    var pr = {
        resetSearchTable: function (data) {
            var tableData = "";

            var idList = currentState.$resultInput.val().split(",");
            if (idList.length == 0 || idList == "" || idList[0] == "") {
                tableData = '<tr class="trbodyClass"><td colspan="2"></td></tr>';
            } else {
                var idDisplayList = currentState.$resultInputDisplay.val().split(",");

                for (var i in idList) {
                    if (idList.hasOwnProperty(i)) {
                        tableData += '<tr><td><input type="' + ( currentState.multi ? 'checkbox' : 'radio' )
                            + '" checked="checked" value="' + idList[i] + ', ' + idDisplayList[i] + '" name="id"/>' +
                            '</td><td>' + idDisplayList[i] + '</td></tr>';
                    }
                }
            }

            tableData += data;
            opts.$tableBody.html(tableData);
        },
        setOutput: function () {
            var selected = opts.$tableBody.find(':checked').map(
                function (index, obj) {
                    return obj.value;
                }
            );

            var idsAndDisplayIds = pr.unique(selected.sort());
            var ids = [];
            var displayIds = [];

            for (var i in idsAndDisplayIds) {
                if (idsAndDisplayIds.hasOwnProperty(i)) {
                    var tmp = idsAndDisplayIds[i].split(",");
                    ids.push(tmp[0].trim());
                    displayIds.push(tmp[1].trim());
                }
            }

            ids = pr.unique(ids);
            displayIds = pr.unique(displayIds);

            currentState.$resultInput.val(ids.join(", "));
            currentState.$resultInputDisplay.val(displayIds.join(", "));
        },
        unique: function (input) {
            var o = {}, i, l = input.length, r = [];
            for (i = 0; i < l; i += 1)
                o[input[i]] = input[i];
            for (i in o) {
                if (o.hasOwnProperty(i)) {
                    r.push(o[i]);
                }
            }
            return r;
        },
        doSearch: function () {
            opts.ajaxSearch(
                opts.$searchInput.val(),
                currentState.multi
            ).done(
                pr.resetSearchTable
            );
        },
        doAdd: function () {
            pr.setOutput();
            closeOpenDialogs();
        }
    };

    bindkeyPressToClick(opts.$searchInput, opts.$searchButton);

    return {
        open: function (multi, resultInputId) {
            closeOpenDialogs();

            currentState.$resultInput = $('#' + resultInputId);
            currentState.$resultInputDisplay = $('#' + resultInputId + 'Display');
            currentState.multi = multi;
            pr.resetSearchTable("");

            opts.$addButton.off('click');
            opts.$addButton.on('click', pr.doAdd);
            opts.$searchButton.off('click');
            opts.$searchButton.on('click', pr.doSearch);

            openDialog('search-div', opts.$searchInput);
        }
    }
};