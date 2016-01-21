<%@ page import="com.siemens.sw360.portal.common.PortalConstants" %>
<%--
  ~ Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
  ~
  ~ This program is free software; you can redistribute it and/or modify it under
  ~ the terms of the GNU General Public License Version 2.0 as published by the
  ~ Free Software Foundation with classpath exception.
  ~
  ~ This program is distributed in the hope that it will be useful, but WITHOUT
  ~ ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  ~ FOR A PARTICULAR PURPOSE. See the GNU General Public License version 2.0 for
  ~ more details.
  ~
  ~ You should have received a copy of the GNU General Public License along with
  ~ this program (please see the COPYING file); if not, write to the Free
  ~ Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
  ~ 02110-1301, USA.
  --%>

<%@include file="/html/init.jsp" %>
<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<jsp:useBean id="userList" type="java.util.List<com.liferay.portal.model.User>" scope="request"/>
<jsp:useBean id="missingUserList" type="java.util.List<com.siemens.sw360.datahandler.thrift.users.User>"
             scope="request"/>
<portlet:actionURL var="updateLifeRayUsers" name="updateUsers">
</portlet:actionURL>

<div id="header"></div>
<p class="pageHeader"><span class="pageHeaderBigSpan">Liferay Users</span> <span
        class="pageHeaderSmallSpan">(${userList.size()}) </span></p>


<div id="searchTableDiv" class="SW360full">
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
    document.getElementById("<portlet:namespace/>userFileUploadInput").onchange = function () {
        if (this.value) {
            document.getElementById("<portlet:namespace/>userCSV-Submit").disabled = false;
        }
    }
</script>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/dataTable_Siemens.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/search.css">



