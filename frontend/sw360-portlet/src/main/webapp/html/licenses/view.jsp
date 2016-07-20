<%--
  ~ Copyright Siemens AG, 2013-2016. Part of the SW360 Portal Project.
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
  --%>

<%@include file="/html/init.jsp"%>

<%@ page import="com.siemens.sw360.portal.common.PortalConstants" %>
<%@ page import="javax.portlet.PortletRequest" %>
<%@ page import="com.liferay.portlet.PortletURLFactoryUtil" %>

<portlet:defineObjects />
<liferay-theme:defineObjects />


<%@ taglib prefix="sw360" uri="/WEB-INF/customTags.tld" %>

<jsp:useBean id="isUserAtLeastClearingAdmin" class="java.lang.String" scope="request" />
<jsp:useBean id="licenseList" type="java.util.List<com.siemens.sw360.datahandler.thrift.licenses.License>"
             scope="request"/>

<portlet:resourceURL var="exportLicensesURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value="<%=PortalConstants.EXPORT_TO_EXCEL%>"/>
</portlet:resourceURL>

<portlet:renderURL var="addLicenseURL">
    <portlet:param name="<%=PortalConstants.PAGENAME%>" value="<%=PortalConstants.PAGENAME_EDIT%>"/>
</portlet:renderURL>

<div id="header"></div>
<p class="pageHeader">
    <span class="pageHeaderBigSpan">Licenses</span> <span class="pageHeaderSmallSpan">(${licenseList.size()})</span>
    <span class="pull-right">
        <input type="button" class="addButton" onclick="window.location.href='<%=exportLicensesURL%>'"
               value="Export Licenses">
        <core_rt:if test="${isUserAtLeastClearingAdmin == 'Yes'}">
            <input type="button" class="addButton" onclick="window.location.href='<%=addLicenseURL%>'" value="Add License">
        </core_rt:if>
    </span>
</p>


<div id="searchInput" class="content1">
        <table style="width: 90%; margin-left:3%;border:1px solid #cccccc;">
            <thead class="infoheading">
            <tr>
                <th>
                    Keyword Search
                </th>
            </tr>
            </thead>
            <tbody style="background-color: #f8f7f7; border: none;">
            <tr>
                <td>
                    <input type="text" maxlength="100" style="width: 200px; padding: 5px; color: gray;height:20px;"
                           id="keywordsearchinput" value="" onkeyup="useSearch('keywordsearchinput')">
                    <br/>
                    <input style="padding: 5px 20px 5px 20px; border: none; font-weight:bold; align:center"
                           type="button" name="searchBtn" value="Search" onclick="useSearch('keywordsearchinput')">
                </td>
            </tr>
            </tbody>
        </table>
</div>
<div id="licensesTableDiv" class="content2">
    <table id="licensesTable" cellpadding="0" cellspacing="0" border="0" class="display">
        <tfoot>
        <tr>
            <th style="width:30%;"></th>
            <th style="width:40%;"></th>
            <th style="width:30%;"></th>
        </tr>
        </tfoot>
    </table>
</div>

<script type="text/javascript" src="<%=request.getContextPath()%>/js/external/jquery-1.11.1.min.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/external/jquery.dataTables-1.9.4.js"></script>


<link rel="stylesheet" href="<%=request.getContextPath()%>/css/dataTable_Siemens.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/licenses.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">

<script>

    var oTable;

    //This can not be document ready function as liferay definitions need to be loaded first
    $(window).load(function() {
        createLicenseTable();
    });

    function createLicenseTable() {
        var result = [];

    <core_rt:forEach items="${licenseList}" var="license">
        result.push({
            <%-- "DT_RowId": '${license.id}',--%>
            "0": "<sw360:DisplayLicenseLink licenseId="${license.id}"/>",
            "1": '<sw360:out value="${license.fullname}"/>',
            "2": '<sw360:out value="${license.licenseType.licenseType}" default="--"/>'
        });
    </core_rt:forEach>

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
            "aaData": result,
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

    function useSearch(searchFieldId) {
        oTable.fnFilter( $('#'+searchFieldId).val());
    }

</script>


