/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 * With modifications by Bosch Software Innovations GmbH, 2016.
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

var openDialogs = [];

function openDialog(id, focusId, heightPerc, widthPerc) {
    heightPerc = (heightPerc !== undefined) ? heightPerc : .40;
    widthPerc = (widthPerc !== undefined) ? widthPerc : .55;

    var dlgHeight = Math.max((window.innerHeight * heightPerc), 400);
    var dlgWidth = Math.max((window.innerWidth * widthPerc), 600);

    var $dialogForm = $("#" + id);

    if (!contains(openDialogs, id)) {
        openDialogs.push(id);
    }

    $dialogForm.css('visibility', 'visible');
    $dialogForm.dialog({
        height: dlgHeight,
        width: dlgWidth,
        modal: true,
        resizable: false
    });

    toJQuery(focusId).focus();
}

function openDialogAbsolute(id, focusId, widthPx, heightPx) {
    heightPx = (heightPx !== undefined) ? heightPx : 256;
    widthPx = (widthPx !== undefined) ? widthPx: 256;

    var $dialogForm = $("#" + id);

    if (!contains(openDialogs, id)) {
        openDialogs.push(id);
    }

    $dialogForm.css('visibility', 'visible');
    $dialogForm.dialog({
        height: heightPx,
        width: widthPx,
        modal: true,
        resizable: false
    });

    toJQuery(focusId).focus();
}

function contains(a, obj) {
    return $.inArray(obj, a) !== -1;
}

function closeOpenDialogs() {
    $.each(openDialogs, function (index, id) {
        $("#" + id).dialog("close");
    });

    openDialogs = [];
}

function toJQuery(idOrElement) {
    if (typeof idOrElement == "string") {
        return $('#' + idOrElement);
    } else {
        return idOrElement;
    }
}

function bindkeyPressToClick(keyed, clicked) {
    var $keyed = toJQuery(keyed);
    var $clicked = toJQuery(clicked);

    $keyed.keypress(function (e) {
        if (e.which == 13) {
            e.preventDefault();
            $clicked.click();
        }
    });

}

function choosePane($pane) {
    if (typeof tabView != "undefined" && typeof tabView.getTabs == "function") {
        var indexOfErrorPane = $pane.parent().children().index($pane[0]);
        try {
            tabView.getTabs().item(indexOfErrorPane).simulate('click');
            return;
        } catch (e) {
            console.log("cannot select tab using alloy UI " + indexOfErrorPane + ": " + e);
        }
    }

    $pane.siblings().removeClass("active");
    $pane.addClass("active");
}

function invalidHandlerShowErrorTab(event, validator) {
    var errorList = validator.errorList;
    if (errorList.length > 0) {
        var errorElement = errorList[0].element;
        if (errorElement) {
            var $errorPane = $(errorElement).parents('.tab-pane');
            choosePane($errorPane);
        }
    }
}

function replaceSingleQuote(str) {
    return str.replace(/'/g,"\\\'").replace(/"/g, "&quot;");
}

function renderLinkTo(url, content, htmlContent) {
    var $link = $("<a href='" + encodeURI(url) + "'/>");
    if (typeof htmlContent == 'string' && htmlContent) {
        $link.html(htmlContent);
    } else if (typeof content == 'string' && content) {
        $link.text(content);
    } else {
        $link.text(url);
    }

    return $link[0].outerHTML;
}

function displayEscaped(text) {
    return $('<span>').text(text).html();
}

function renderUserEmail(user) {
    if (typeof user == 'string') {
        return renderLinkTo("mailto:" + user, user);
    }

    if (typeof user == 'object' && user.hasOwnProperty("email") && user.hasOwnProperty("givenname") && user.hasOwnProperty("lastname")) {
        return renderLinkTo("mailto:" + user.email, user.givenname + " " + user.lastname);
    } else {
        return "N.A.";
    }
}

function objectNamespacerOf(prefix) {
    return function(data) {
        var result = {};
        for (var i in data) {
            if (data.hasOwnProperty(i)) {
                result[prefix + i] = data[i];
            }
        }
        return result;
    }
}

var renderVersion = {
    display: "display",
    filter: function(version) {
        return version.name + " " + version.version;
    }
};

function numberCmp(a, b) {
    var aN = Number(a);
    var bN = Number(b);

    return (aN > bN) ? 1 : ((aN < bN) ? -1 : 0);
}

function versionCmp(a, b) {
    var nameCmp = a.name.localeCompare(b.name);
    if (nameCmp != 0) {
        return nameCmp;
    }

    var aVer = a.version.split('.');
    var bVer = b.version.split('.');

    var i;
    for (i = 0; i < aVer.length; i++) {
        if (i >= bVer.length)
            return 1;

        var aPart = aVer[i];
        var bPart = bVer[i];

        var numCmp = numberCmp(aPart, bPart);

        if (numCmp != 0) {
            return numCmp;
        }
        if (aPart !== bPart) {
            return aPart.localeCompare(bPart);
        }
    }

    return (i < bVer.length) ? -1 : 0;
}

function loadDataTableExtension() {
    $.extend($.fn.dataTableExt.oSort, {
        "version-asc": function (a, b) {
            return versionCmp(a, b);
        },

        "version-desc": function (a, b) {
            return versionCmp(b, a);
        }
    });
}

function cleanMessages() {
    $('.portlet-body .alert').remove();
}

function flashSuccessMessage(content) {
    flashMessage(content, 'alert-success');
}

function flashErrorMessage(content) {
    flashMessage(content, 'alert-error');
}

function flashMessage(content, styleClass) {
    var target = $('.portlet-body');

    var node = $('<div/>');
    node.addClass('alert ' + styleClass).text(content).prependTo(target);
}

