<%@ page import="com.siemens.sw360.portal.common.PortalConstants" %>
<%--
  ~ Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
  --%>

<%@include file="/html/init.jsp" %>
<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<jsp:useBean id="userList" type="java.util.List<com.liferay.portal.model.User>" scope="request"/>
<jsp:useBean id="missingUserList" type="java.util.List<com.siemens.sw360.datahandler.thrift.users.User>"
             scope="request"/>
<portlet:actionURL var="updateLifeRayUsers" name="updateUsers">
</portlet:actionURL>

<script src="<%=request.getContextPath()%>/js/external/jquery-1.11.1.min.js"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery-ui.min.js"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery.dataTables.js"></script>

<div id="header"></div>
<p class="pageHeader"><span class="pageHeaderBigSpan">Liferay Users</span> <span
        class="pageHeaderSmallSpan">(${userList.size()}) </span></p>

<div id="searchInput" class="content1">
    <table style="width: 90%; margin-left:3%;border:1px solid #cccccc;">
        <thead>
        <tr>
            <th class="infoheading">
                Keyword Search
            </th>
        </tr>
        </thead>
        <tbody style="background-color: #f8f7f7; border: none;">
        <tr>
            <td>
                <input type="text" style="width: 90%; padding: 5px; color: gray;height:20px;"
                       id="keywordsearchinput" value="" onkeyup="useSearch('keywordsearchinput')">
                <br/>
                <input style="padding: 5px 20px 5px 20px; border: none; font-weight:bold;" type="button"
                       name="searchBtn" value="Search" onclick="useSearch('keywordsearchinput')">
            </td>
        </tr>
        </tbody>
    </table>
</div>

<div id="searchTableDiv" class="content2">
    <h4>Users already in liferay</h4>
    <table id="userTable" cellpadding="0" cellspacing="0" border="0" class="display">
        <thead>
        <tr>
            <th>Given name</th>
            <th>Last name</th>
            <th>Department</th>
            <th>User Role</th>
        </tr>
        </thead>
        <tbody>
        <core_rt:forEach var="user" items="${userList}">
            <tr>
                <td><sw360:out value="${user.firstName}"/></td>
                <td><sw360:out value="${user.lastName}"/></td>
                <td><sw360:out value="${user.getOrganizations(false).get(0).getName()}"/></td>
                <td>
                    <core_rt:forEach var="role" items="${user.roles}" varStatus="loop">
                        <sw360:out value="${role.getName()}"/>,
                    </core_rt:forEach>
                </td>
            </tr>

        </core_rt:forEach>
        </tbody>
        <tfoot>
        <tr>
            <th style="width:25%;"></th>
            <th style="width:25%;"></th>
            <th style="width:25%;"></th>
            <th style="width:25%;"></th>
        </tr>
        </tfoot>
    </table>

    <table class="info_table">
        <thead>
        <tr>
            <th colspan="2"> Downloads</th>
        </tr>
        </thead>

        <tbody>
        <tr>
            <td>Download Liferay User CSV</td>
            <td><a href="<portlet:resourceURL>
                               <portlet:param name="<%=PortalConstants.ACTION%>" value='<%=PortalConstants.USER_LIST%>'/>
                         </portlet:resourceURL>">
                <img src="<%=request.getContextPath()%>/images/downloadEnable.jpg" alt="Download">
            </a>
            </td>
        </tr>
        </tbody>
    </table>
    <br>

    <h4>Users not in liferay</h4>
    <table id="userMissingTable" cellpadding="0" cellspacing="0" border="0" class="display">
        <thead>
        <tr>
            <th>Given name</th>
            <th>Last name</th>
        </tr>
        </thead>
        <tbody>
        <core_rt:forEach var="user" items="${missingUserList}">
            <tr>
                <td><sw360:out value="${user.givenname}"/></td>
                <td><sw360:out value="${user.lastname}"/></td>
            </tr>

        </core_rt:forEach>
        </tbody>
        <tfoot>
        <tr>
            <th style="width:50%;"></th>
            <th style="width:50%;"></th>
        </tr>
        </tfoot>
    </table>

    <form id="usersForm" name="usersForm" action="<%=updateLifeRayUsers%>" method="POST" enctype="multipart/form-data">
        <div class="fileupload-buttons">
            <span class="fileinput-button">
                <span>Upload user CSV</span>
                <input id="<portlet:namespace/>userFileUploadInput" type="file" name="<portlet:namespace/>file">
            </span>
            <input type="submit" value="Update Users" class="addButton" id="<portlet:namespace/>userCSV-Submit" disabled>
        </div>
    </form>

</div>

<script>
    var PortletURL;
    AUI().use('liferay-portlet-url', function (A) {
        PortletURL = Liferay.PortletURL;
        load();
    });

    var usersTable;
    var usersMissingTable;

    //This can not be document ready function as liferay definitions need to be loaded first
    function load() {
        configureUsersTable();
        configureMissingUsersTable();
    }

    document.getElementById("<portlet:namespace/>userFileUploadInput").onchange = function () {
        if (this.value) {
            document.getElementById("<portlet:namespace/>userCSV-Submit").disabled = false;
        }
    }

    function configureUsersTable() {
        usersTable = setupPagination('#userTable');
    }

    function configureMissingUsersTable() {
        usersMissingTable = setupPagination('#userMissingTable');
    }

    function setupPagination(tableId){
        var tbl;
        if ($(tableId)){
            tbl = $(tableId).dataTable({
                "sPaginationType": "full_numbers"
            });

            $(tableId+'_filter').hide();
            $(tableId+'_first').hide();
            $(tableId+'_last').hide();
        }
        return tbl;
    }

    function useSearch( buttonId) {
        usersTable.fnFilter( $('#'+buttonId).val());
        usersMissingTable.fnFilter( $('#'+buttonId).val());
    }
</script>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/dataTable_Siemens.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">



