<%--
  ~ Copyright (c) Bosch Software Innovations GmbH 2016.
  ~ Copyright (c) Siemens AG 2016. Part of the SW360 Portal Project.
  ~
  ~ SPDX-License-Identifier: EPL-1.0
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
  --%>

<%@include file="/html/init.jsp" %>

<%@ page import="com.liferay.portlet.PortletURLFactoryUtil" %>
<%@ page import="org.eclipse.sw360.portal.common.PortalConstants" %>
<%@ page import="javax.portlet.PortletRequest" %>

<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<jsp:useBean id="vulnerabilityList" type="java.util.List<org.eclipse.sw360.datahandler.thrift.vulnerabilities.Vulnerability>"
             scope="request"/>

<portlet:resourceURL var="vulnerabilityListAjaxURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value='<%=PortalConstants.VULNERABILITY_LIST%>'/>
</portlet:resourceURL>

<div id="header"></div>
<p class="pageHeader">
    <span class="pageHeaderBigSpan">Vulnerabilities</span>
</p>
<div id="searchInput" class="content1">
    <table>
        <thead>
        <tr>
            <th class="infoheading">
                Display Filter
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
<div id="vulnerabilitiesTableDiv" class="content2">
    <table id="vulnerabilitiesTable" cellpadding="0" cellspacing="0" border="0" class="display">
        <tfoot>
        <tr>
            <th colspan="5"></th>
        </tr>
        </tfoot>
    </table>
</div>

<link rel="stylesheet" href="<%=request.getContextPath()%>/webjars/jquery-ui/1.12.1/jquery-ui.css">
<script src="<%=request.getContextPath()%>/webjars/jquery/1.12.4/jquery.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/webjars/jquery-ui/1.12.1/jquery-ui.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/webjars/datatables/1.10.7/js/jquery.dataTables.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/loadTags.js"></script>

<script>
    var vulnerabilityTable;

    var PortletURL;
    AUI().use('liferay-portlet-url', function (A) {
        PortletURL = Liferay.PortletURL;
        load();
    });

    function load() {
        createVulnerabilityTable();

    }

    function useSearch(searchFieldId) {
        vulnerabilityTable.fnFilter( $('#'+searchFieldId).val());
    }

    function createVulnerabilityTable() {
        var result = [];

        <core_rt:forEach items="${vulnerabilityList}" var="vulnerability">
        result.push({
            "DT_RowId": "${vulnerability.externalId}",
            "0": "<a href='" + createDetailURLFromVulnerabilityId("${vulnerability.externalId}") + "' target='_self'><sw360:out value="${vulnerability.externalId}"/></a>",
            "1": "<span title='<sw360:out value='${vulnerability.description}'/>'><sw360:out value='${vulnerability.title}'/></span>",
            "2": <core_rt:if test="${vulnerability.isSetCvss}">
                    "<div>cvss: <sw360:out value="${vulnerability.cvss}"/>"+
                        <core_rt:if test="${not empty vulnerability.cvssTime}">
                            " (as of: <sw360:out value="${vulnerability.cvssTime}" default="not set"/>)"+
                        </core_rt:if>
                    "</div>"+
                 </core_rt:if>
                 <core_rt:if test="${not empty vulnerability.priority}">
                    "<div title='<sw360:out value='${vulnerability.priorityText}'/>'>priority: <sw360:out value='${vulnerability.priority}'/><img class='infopic' src='/sw360-portlet/images/ic_info.png'/></div>"+
                 </core_rt:if>
                    "",
            "3": "<sw360:out value='${vulnerability.publishDate}'/>",
            "4": "<sw360:out value='${vulnerability.lastExternalUpdate}'/>"
        });
        </core_rt:forEach>

        vulnerabilityTable = $('#vulnerabilitiesTable').dataTable({
            pagingType: "simple_numbers",
            dom: "lrtip",
            "data": result,
            "columns": [
                {"title": "External Id"},
                {"title": "Title", "width": "30%"},
                {"title": "Weighting"},
                {"title": "Publish date"},
                {"title": "Last update"}
            ],
            "order": [[2, 'asc'], [3, 'desc']]
        });
        vulnerabilityTable.$('td').tooltip({"delay": 0, "track": true, "fade": 250});

    }

    function createDetailURLFromVulnerabilityId(paramVal) {
        return createCompositeUrl('<%=PortalConstants.VULNERABILITY_ID%>', paramVal);
    }

    function createCompositeUrl(paramId, paramVal) {
         var portletURL = PortletURL.createURL('<%= PortletURLFactoryUtil.create(request, portletDisplay.getId(), themeDisplay.getPlid(), PortletRequest.RENDER_PHASE) %>')
                 .setParameter('<%=PortalConstants.PAGENAME%>', '<%=PortalConstants.PAGENAME_DETAIL%>').setParameter(paramId, paramVal);
         return portletURL.toString();
    }

 </script>

 <link rel="stylesheet" href="<%=request.getContextPath()%>/css/dataTable_Siemens.css">
 <link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">
