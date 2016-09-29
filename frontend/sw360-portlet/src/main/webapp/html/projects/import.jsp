<%--
  ~ Copyright (c) Bosch Software Innovations GmbH 2015.
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
  --%>

<%@ taglib prefix="min-width" uri="http://alloy.liferay.com/tld/aui" %>
<%@ taglib prefix="min-height" uri="http://alloy.liferay.com/tld/aui" %>

<%@include file="/html/init.jsp" %>
<%-- the following is needed by liferay to display error messages--%>
<%@include file="/html/utils/includes/errorKeyToMessage.jspf"%>
<%@ page import="com.liferay.portlet.PortletURLFactoryUtil" %>
<%@ page import="com.siemens.sw360.portal.common.PortalConstants" %>
<%@ page import="javax.portlet.PortletRequest" %>
<%@ page import="com.siemens.sw360.datahandler.thrift.datasources.SourceDatabase" %>
<%@ page import="com.siemens.sw360.datahandler.thrift.datasources.SourceDatabaseSelector" %>

<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<jsp:useBean id="importables" type="java.util.List<com.siemens.sw360.datahandler.thrift.projects.Project>"
             scope="request"/>
<jsp:useBean id="idName" type="java.lang.String" scope="request"/>
<jsp:useBean id="loggedIn" type="java.lang.Boolean" scope="request" />
<jsp:useBean id="loggedInServer" type="java.lang.String" scope="request" />

<portlet:resourceURL var="ajaxURL" id="import.jsp"></portlet:resourceURL>

<div id="header"></div>
<p class="pageHeader">
    <span class="pageHeaderBigSpan">Projects</span>
    <span class="pull-right">
    </span>
</p>

<div id="import-Project-SelectSource" class="content1">
    <form id="remoteLoginForm">
        <div class='form-group'>
            <label class="control-label textlabel stackedLabel" for="datasource-dropdown">Select data source:</label>
            <div class='controls'>
                <select class="form-control toplabelledInput" id="datasource-dropdown"
                    name="<portlet:namespace/><%=SourceDatabaseSelector._Fields.SDB%>"
                    style="min-width:162px; min-height:28px">
                <sw360:DisplayEnumOptions type="<%=SourceDatabase.class%>"/>
                </select>
            </div>
        </div>
        <div class='form-group'>
            <label class="control-label textlabel stackedLabel" for="input-dataserver-url">Server URL:</label>
            <div class='controls'>
                <input class="form-control field-required toplabelledInput" id="input-dataserver-url"
                   name="<portlet:namespace/><%=SourceDatabaseSelector._Fields.SERVER_URL%>"
                   style="min-width:162px; min-height:28px" autofocus/>
            </div>
        </div>
        <div class='form-group'>
            <label class="control-label textlabel stackedLabel" for="input-dataserver-user">Server user:</label>
            <div class='controls'>
                <input class="form-control toplabelledInput" id="input-dataserver-user"
                   name="<portlet:namespace/><%=SourceDatabaseSelector._Fields.SERVER_USR%>"
                   style="min-width:162px; min-height:28px" />
            </div>
        </div>
        <div class='form-group'>
            <label class="control-label textlabel stackedLabel" for="input-dataserver-pw">Password:</label>
            <div class='controls'>
                <input class="form-control toplabelledInput" type="password" id="input-dataserver-pw"
                   name="<portlet:namespace/><%=SourceDatabaseSelector._Fields.SERVER_PW%>"
                   style="min-width:162px; min-height:28px" />
            </div>
        </div>
        <input type="button" onclick="updateDataSource()" value="Connect" id="buttonConnect"/>
    </form>
    <input type="button" onclick="disconnectDataSource()" value="Disconnect" class="hidden" id="buttonDisconnect"/>

</div>
<div id="importProject" class="content2">
    <form>
        <table id="dataSourceTable" cellpadding="0" cellspacing="0" border="0" class="display">
            <tfoot>
            <tr>
                <th colspan="2"></th>
            </tr>
            </tfoot>
        </table>

        <input type="button" value="Import" onclick="showImportProjectsPopup()"/>
    </form>

    <div class="sw360modal" id="importOverview"/>

</div>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/external/jquery-ui.css">
<script src="<%=request.getContextPath()%>/js/external/jquery-1.11.1.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery-ui.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery.dataTables.js" type="text/javascript"></script>

<script>
    var oTable;
    var PortletURL;
    AUI().use('liferay-portlet-url', function (A) {
        PortletURL = Liferay.PortletURL;
        createDataSourceTable();
    });

    AUI().use(
            'aui-form-validator',
            function(Y) {
                new Y.FormValidator(
                    {
                        boundingBox: '#remoteLoginForm'
                    }
                );
            }
    );

    function createDataSourceTable() {
        var result = [];

        <core_rt:forEach items="${importables}" var="importable">

        var checkedProjectId = '<%=PortalConstants.CHECKED_PROJECT%>${importable.externalIds.get(idName)}';
        result.push({
            "DT_RowId": "${importable.externalIds.get(idName)}",
            "0": '<span id="' + checkedProjectId + '"><sw360:out value="${importable.externalIds.get(idName)}"/></span>',
            "1": '<span id="' + checkedProjectId + 'Name"><sw360:out value="${importable.name}"/></span>'
        });
        </core_rt:forEach>

        oTable = $('#dataSourceTable').DataTable({
            "sPaginationType": "full_numbers",
            "aaData": result,
            "aoColumns": [
                {"sTitle": "Project id in selected data source"},
                {"sTitle": "Project name"}
            ]
        });

        $('#dataSourceTable tbody').on( 'click', 'tr', function() {
            $(this).toggleClass('selected');
        });
    }

    function showImportProjectsPopup() {
        var headerContent = "The following projects will be imported:",
            bodyContent = "<ol>",
            selectedProjects = [];

        oTable.rows('.selected').data().each(function(e) {
            chtml = $("<div/>").html(e[0]).contents();

            selectedProjects.push({
                id: chtml.attr('id'),
                text: chtml.text()
            });
        });

        selectedProjects.forEach(function(e) {
            bodyContent += "<li>" + e.text + "</li>";
        });
        bodyContent += "</ol>";

        var modal = createModal('#importOverview',
                {
                    bodyContent: bodyContent
                });
        modal.setHeader(headerContent);
        modal.addToolbar(
            [
                {
                    label: 'Import',
                    on: {
                        click: function () {
                            importProjectsData(modal);
                        }
                    }
                },
                {
                    label: 'Cancel',
                    on: {
                        click: function() {
                            modal.hide();
                        }
                    }
                }
            ]);
        modal.render();
    }

    function importProjectsData(modal) {
        var checked = [];

        oTable.rows('.selected').data().each(function(e) {
            var chtml = $("<div/>").html(e[0]).contents();
            checked.push(chtml.attr('id'));
        });
        modal.setHeader("Please wait while importing");
        modal.addToolbar();
        $.ajax({
            url: '<%=ajaxURL%>',
            type: 'POST',
            dataType: 'json',
            data: {
                <portlet:namespace/>checked: checked,
                <portlet:namespace/><%=PortalConstants.IMPORT_USER_ACTION%>:
                    '<%=PortalConstants.IMPORT_USER_ACTION__IMPORTBDP%>'
            },
            success: function(response) {
                var header = "Import result";
                switch(response.<%=PortalConstants.IMPORT_RESPONSE__STATUS%>) {
                    case '<%=PortalConstants.IMPORT_RESPONSE__IMPORT_BDP_SUCCESS%>':
                        showStatusPopup('Projects imported successfully.', modal, header);
                        break;
                    case '<%=PortalConstants.IMPORT_RESPONSE__IMPORT_BDP_FAILURE%>':
                        var failedIdsList = "<div>" + response.<%=PortalConstants.IMPORT_RESPONSE__FAILED_IDS%> + "</div>";
                        showStatusPopup('Some projects failed to import:' + failedIdsList, modal, header);
                        break;
                    case '<%=PortalConstants.IMPORT_RESPONSE__IMPORT_BDP_GENERAL_FAILURE%>':
                        showStatusPopup('Import failed.', modal, header);
                        break;
                    default:
                }
            }
        });
    }

    function connectDBRequestSuccess(response, serverURL) {

        var responseCode = response.<%=PortalConstants.IMPORT_RESPONSE__STATUS%>;
        cleanMessages();

        switch(responseCode) {
            case '<%=PortalConstants.IMPORT_RESPONSE__DB_CHANGED%>':
                var data = new Object();
                data["<portlet:namespace/><%=PortalConstants.IMPORT_USER_ACTION%>"] = "<%=PortalConstants.IMPORT_USER_ACTION__UPDATEIMPORTABLES%>";
                $.ajax({
                    url: '<%=ajaxURL%>',
                    type: 'POST',
                    dataType: 'json',
                    data: data,
                    success: function(response) {
                        importUpdateProjectTable(response);
                    }
                });
                showLogin(serverURL);
                break;
            case '<%=PortalConstants.IMPORT_RESPONSE__DB_CONNECT_ERROR%>':
                flashErrorMessage('Could not connect to DB.');
                break;
            case '<%=PortalConstants.IMPORT_RESPONSE__DB_URL_NOTSET%>':
                flashErrorMessage('Please enter a server URL');
                break;
            case '<%=PortalConstants.IMPORT_RESPONSE__UNAUTHORIZED%>':
                flashErrorMessage('Unable to authenticate with this username/password.');
                break;
            default:
            break;
        }
    }

    function importUpdateProjectTable(response) {
        var importables = response.<%=PortalConstants.IMPORT_RESPONSE__NEW_IMPORTABLES%>;
        var projectList = [];

        importables.forEach(function(el) {
            el = JSON.parse(el);
            var checkedProjectId = '<%=PortalConstants.CHECKED_PROJECT%>' + el.externalId;
            projectList.push({
                "DT_RowId": el.externalId,
                "0": '<span id="' + checkedProjectId + '">' + el.externalId + '</span>',
                "1": '<span id="' + checkedProjectId + 'Name">' + el.name + '</span>'
            });
        });

        oTable = $('#dataSourceTable').DataTable({
            "bDestroy": true,
            "sPaginationType": "full_numbers",
            "aoColumns": [
                {"sTitle": "Project id in selected data source"},
                {"sTitle": "Project name"}
            ],
            "aaData": projectList
        });
    }

    function updateDataSource() {
        if ($('#remoteLoginForm [role=alert]').length > 0) {
            cleanMessages();
            flashErrorMessage("Please correct the login data.");
            return;
        }
        var serverUrl = $("#input-dataserver-url").val(),
                data = new Object();
        data["<portlet:namespace/><%=PortalConstants.IMPORT_USER_ACTION%>"] = "<%=PortalConstants.IMPORT_USER_ACTION__NEWIMPORTSOURCE%>";
        data["<portlet:namespace/><%=PortalConstants.SESSION_IMPORT_URL%>"] = serverUrl;
        data["<portlet:namespace/><%=PortalConstants.SESSION_IMPORT_USER%>"] = $("#input-dataserver-user").val();
        data["<portlet:namespace/><%=PortalConstants.SESSION_IMPORT_PASS%>"] = $("#input-dataserver-pw").val();

        $.ajax({
            url: '<%=ajaxURL%>',
            type: 'POST',
            dataType: 'json',
            data: data,
            success: function(response) {
                connectDBRequestSuccess(response, serverUrl);
            }
        });
    }

    function disconnectDataSource() {
        var data = new Object();
        data["<portlet:namespace/><%=PortalConstants.IMPORT_USER_ACTION%>"] = "<%=PortalConstants.IMPORT_USER_ACTION__DISCONNECT%>";
        $.ajax({
            url: '<%=ajaxURL%>',
            type: 'POST',
            data: data,
            success: function(response) {
                oTable = $('#dataSourceTable').DataTable({
                    "bDestroy": true,
                    "sPaginationType": "full_numbers",
                    "aoColumns": [
                         {"sTitle": "Project id in selected data source"},
                        {"sTitle": "Project name"}
                    ],
                    "aaData": []
                });
                showLogout();
            }
        });
    }

    function makeProjectUrl(projectId, page) {
        var portletURL = PortletURL.createURL('<%= PortletURLFactoryUtil.create(request, portletDisplay.getId(), themeDisplay.getPlid(), PortletRequest.RENDER_PHASE) %>')
                .setParameter('<%=PortalConstants.PAGENAME%>', page)
                .setParameter('<%=PortalConstants.PROJECT_ID%>', projectId);
        return portletURL.toString();
    }

    function showLogin(serverURL) {
        displayLoginForm(true);
        flashSuccessMessage('You are logged in to ' + serverURL + '.');
    }

    function showLogout() {
        displayLoginForm(false);
        flashSuccessMessage('You are logged out.');
    }

    function displayLoginForm(loggedIn) {
        cleanMessages();
        if (loggedIn) {
            $('#remoteLoginForm').addClass('hidden');
            $('#buttonDisconnect').removeClass('hidden');
        } else {
            $('#remoteLoginForm').removeClass('hidden');
            $('#buttonDisconnect').addClass('hidden');
        }
    }
</script>

<%@include file="/html/utils/includes/modal.jspf" %>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/dataTable_Siemens.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">
