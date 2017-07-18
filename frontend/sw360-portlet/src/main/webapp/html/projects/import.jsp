<%--
  ~ Copyright (c) Bosch Software Innovations GmbH 2015-2016.
  ~
  ~ SPDX-License-Identifier: EPL-1.0
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
<%@ page import="org.eclipse.sw360.portal.common.PortalConstants" %>
<%@ page import="javax.portlet.PortletRequest" %>
<%@ page import="org.eclipse.sw360.datahandler.thrift.bdpimport.RemoteCredentials" %>

<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<jsp:useBean id="importables" type="java.util.List<org.eclipse.sw360.datahandler.thrift.projects.Project>"
             scope="request"/>
<jsp:useBean id="idName" type="java.lang.String" scope="request"/>
<jsp:useBean id="loggedIn" type="java.lang.Boolean" scope="request" />
<jsp:useBean id="loggedInServer" type="java.lang.String" scope="request" />

<core_rt:set var="hosts" value="<%=PortalConstants.PROJECTIMPORT_HOSTS%>"/>

<portlet:resourceURL var="ajaxURL" id="import.jsp"></portlet:resourceURL>

<div id="header"></div>
<p class="pageHeader">
    <span class="pageHeaderBigSpan">Project import</span>
    <span class="pull-right">
    </span>
</p>

<div id="import-Project-SelectSource" class="content1">
    <div id="remoteLoginForm">
        <div class='form-group'>
            <label class="control-label textlabel stackedLabel" for="input-dataserver-url">Server URL:</label>
<core_rt:if test="${empty hosts}">
            <div class='controls'>
                <input class="form-control field-required toplabelledInput" id="input-dataserver-url"
                   name="<portlet:namespace/><%=RemoteCredentials._Fields.SERVER_URL%>"
                   style="min-width:162px; min-height:28px" autofocus/>
            </div>
</core_rt:if>
<core_rt:if test="${not empty hosts}">
           <select class="toplabelledInput" id="input-dataserver-url"
                   name="<portlet:namespace/><%=RemoteCredentials._Fields.SERVER_URL%>" >
               <core_rt:forEach items="${hosts}" var="host">
                   <option value="${host}" class="textlabel stackedLabel" <core_rt:if
                           test='${loggedInServer == host}'>selected="selected"</core_rt:if>> ${host} </option>
               </core_rt:forEach>
           </select>
</core_rt:if>
        </div>
        <div id="remoteLoginFormHidingPart">
            <div class='form-group'>
                <label class="control-label textlabel stackedLabel" for="input-dataserver-user">Server user:</label>
                <div class='controls'>
                    <input class="form-control toplabelledInput" id="input-dataserver-user"
                       name="<portlet:namespace/><%=RemoteCredentials._Fields.USERNAME%>"
                       style="min-width:162px; min-height:28px" />
                </div>
            </div>
            <div class='form-group'>
                <label class="control-label textlabel stackedLabel" for="input-dataserver-pw">Password:</label>
                <div class='controls'>
                    <input class="form-control toplabelledInput" type="password" id="input-dataserver-pw"
                       name="<portlet:namespace/><%=RemoteCredentials._Fields.PASSWORD%>"
                       style="min-width:162px; min-height:28px" />
                </div>
            </div>
            <input type="button" onclick="updateDataSource()" value="Connect" id="buttonConnect"/>
        </div>
    </div>
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
</div>

<link rel="stylesheet" href="<%=request.getContextPath()%>/webjars/jquery-ui/1.12.1/jquery-ui.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/webjars/github-com-craftpip-jquery-confirm/3.0.1/jquery-confirm.min.css">
<script src="<%=request.getContextPath()%>/webjars/jquery/1.12.4/jquery.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/webjars/jquery-ui/1.12.1/jquery-ui.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/webjars/datatables/1.10.7/js/jquery.dataTables.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/webjars/github-com-craftpip-jquery-confirm/3.0.1/jquery-confirm.min.js" type="text/javascript"></script>

<script>
    var dataSourceTable;
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

        dataSourceTable = $('#dataSourceTable').DataTable({
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
        var bodyContent = "<ol>",
            selectedProjects = [];

        dataSourceTable.rows('.selected').data().each(function(e) {
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

        $.confirm({
            title: 'The following projects will be imported:',
            content: bodyContent,
            confirmButtonClass: 'btn-info',
            cancelButtonClass: 'btn-danger',
            buttons: {
                import: function() {
                    importProjectsData(selectedProjects);
                },
                cancel: function () {
                    //close
                },
            }
        });
    }

    function importProjectsData(selectedProjects) {
        $.confirm({
            title: "Import",
            content: function () {
                var self = this;
                return $.ajax({
                    url: '<%=ajaxURL%>',
                    type: 'POST',
                    cache: false,
                    dataType: 'json',
                    data: {
                        "<portlet:namespace/>checked":
                            selectedProjects.map(function (o) { return o["id"]; }),
                        "<portlet:namespace/><%=PortalConstants.IMPORT_USER_ACTION%>":
                            '<%=PortalConstants.IMPORT_USER_ACTION__IMPORTBDP%>'
                    }
                }).done(function (response) {
                    self.setTitle("Import result");
                    switch(response.<%=PortalConstants.IMPORT_RESPONSE__STATUS%>) {
                        case '<%=PortalConstants.IMPORT_RESPONSE__IMPORT_BDP_SUCCESS%>':
                            self.setContent('Projects imported successfully.');
                            break;
                        case '<%=PortalConstants.IMPORT_RESPONSE__IMPORT_BDP_FAILURE%>':
                            var failedIdsList = "<div>" + response.<%=PortalConstants.IMPORT_RESPONSE__FAILED_IDS%> + "</div>";
                            self.setContent('Some projects failed to import:' + failedIdsList);
                            break;
                        case '<%=PortalConstants.IMPORT_RESPONSE__IMPORT_BDP_GENERAL_FAILURE%>':
                            flashErrorMessage('Could not import the projects.');
                            self.setContent('Import failed.');
                            break;
                        default:
                    }
                }).fail(function(){
                    flashErrorMessage('Could not import the projects.');
                    self.close();
                });
            }
        });
    }

    function connectDBRequestSuccess(response, serverURL) {

        var responseCode = response.<%=PortalConstants.IMPORT_RESPONSE__STATUS%>;
        cleanMessages();

        switch(responseCode) {
            case '<%=PortalConstants.IMPORT_RESPONSE__DB_CHANGED%>':
                showLogin(serverURL);
                $.confirm({
                    title: "Fetch projects",
                    content: function () {
                        var self = this;
                        return $.ajax({
                            url: '<%=ajaxURL%>',
                            type: 'POST',
                            cache: false,
                            dataType: 'json',
                            data: {
                                "<portlet:namespace/><%=PortalConstants.IMPORT_USER_ACTION%>":
                                    "<%=PortalConstants.IMPORT_USER_ACTION__UPDATEIMPORTABLES%>"
                            }
                        }).done(function (response) {
                            importUpdateProjectTable(response);
                            self.close();
                        }).fail(function(){
                            flashErrorMessage('Could not get the projects.');
                            self.close();
                        });
                    }
                });
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

        dataSourceTable = $('#dataSourceTable').DataTable({
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

        $.confirm({
            title: "Authenticate",
            content: function () {
                var self = this;
                return $.ajax({
                    url: '<%=ajaxURL%>',
                    type: 'POST',
                    cache: false,
                    dataType: 'json',
                    data: data
                }).done(function (response) {
                    self.close();
                    connectDBRequestSuccess(response, serverUrl);
                }).fail(function(){
                    flashErrorMessage('Could not connect to server.');
                    self.close();
                });
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
                dataSourceTable = $('#dataSourceTable').DataTable({
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
            $('#remoteLoginFormHidingPart').addClass('hidden');
            $('#input-dataserver-url').prop('disabled', true);
            $('#buttonDisconnect').removeClass('hidden');
        } else {
            $('#remoteLoginFormHidingPart').removeClass('hidden');
            $('#input-dataserver-url').prop('disabled', false);
            $('#buttonDisconnect').addClass('hidden');
        }
    }

<core_rt:if test="${loggedIn}">
    $( document ).ready(function() {
        displayLoginForm(true);
    });
</core_rt:if>
</script>

<%@include file="/html/utils/includes/modal.jspf" %>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/dataTable_Siemens.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">
