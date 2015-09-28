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

<%@ page import="com.liferay.portlet.PortletURLFactoryUtil" %>
<%@ page import="com.siemens.sw360.portal.common.PortalConstants" %>
<%@ page import="javax.portlet.PortletRequest" %>

<portlet:defineObjects/>
<liferay-theme:defineObjects/>


<%@ taglib prefix="sw360" uri="/WEB-INF/customTags.tld" %>


<jsp:useBean id="moderationRequests" type="java.util.List<com.siemens.sw360.datahandler.thrift.moderation.ModerationRequest>"
             scope="request"/>

<div id="header"></div>
<p class="pageHeader">
    <span class="pageHeaderBigSpan">Moderations</span> <span class="pageHeaderSmallSpan">(${moderationRequests.size()})</span>
</p>


<div id="searchInput" class="content1">
    <table >
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
                <input type="text" class="searchbar"
                       id="keywordsearchinput" value="" onkeyup="useSearch('keywordsearchinput')">
                <br/>
                <input class="searchbutton" type="button"
                       name="searchBtn" value="Search" onclick="useSearch('keywordsearchinput')">
            </td>
        </tr>
        </tbody>
    </table>
</div>
<div id="moderationsTableDiv" class="content2">
    <table id="moderationsTable" cellpadding="0" cellspacing="0" border="0" class="display">
        <tfoot>
        <tr>
            <th colspan="5"></th>
        </tr>
        </tfoot>
    </table>
</div>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/external/jquery-ui.css">
<script src="<%=request.getContextPath()%>/js/external/jquery-1.11.1.min.js"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery-ui.min.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/external/jquery.dataTables.js"></script>

<script>
    var oTable;

    //This can not be document ready function as liferay definitions need to be loaded first
    $(window).load(function () {
        createModerationsTable();
    });

    function useSearch(searchFieldId) {
        oTable.fnFilter( $('#'+searchFieldId).val());
    }

    function createModerationsTable() {

        var result = [];

        <core_rt:forEach items="${moderationRequests}" var="moderation">
        result.push({
            "DT_RowId": "${moderation.id}",
            "0": '<a href=\'<portlet:renderURL ><portlet:param name="<%=PortalConstants.MODERATION_ID%>" value="${moderation.id}"/><portlet:param name="<%=PortalConstants.PAGENAME%>" value="<%=PortalConstants.PAGENAME_EDIT%>"/></portlet:renderURL>\' target=\'_self\'><sw360:out value="${moderation.documentName}"/></a>',
            "1": '<sw360:DisplayUserEmail email="${moderation.requestingUser}"/>',
            "2": '<sw360:DisplayUserEmailCollection value="${moderation.moderators}"/>',
            "3": '<sw360:DisplayEnum value="${moderation.moderationState}"/>',
            "4": 'TODO'
        });
        </core_rt:forEach>

        oTable = $('#moderationsTable').dataTable({
            pagingType: "full_numbers",
            data: result,
            columns: [
                {"title": "Document Name"},
                {"title": "Requesting User"},
                {"title": "Moderators"},
                {"title": "State"},
                {"title": "Actions"}
            ]
        });

        $('#moderationsTable_filter').hide();
        $('#moderationsTable_first').hide();
        $('#moderationsTable_last').hide();

    }

</script>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/dataTable_Siemens.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">
