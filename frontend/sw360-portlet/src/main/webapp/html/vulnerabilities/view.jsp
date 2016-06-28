<%--
  ~ Copyright (c) Bosch Software Innovations GmbH 2016.
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
<%@ page import="com.siemens.sw360.datahandler.thrift.vulnerabilities.VulnerabilityDTO" %>
<%@ page import="com.siemens.sw360.portal.common.PortalConstants" %>
<%@ page import="javax.portlet.PortletRequest" %>
<%@ page import="com.siemens.sw360.portal.common.JsonHelpers" %>
<%@ page import="com.siemens.sw360.portal.common.ThriftJsonSerializer" %>

<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<jsp:useBean id="vulnerabilityList" type="java.util.List<com.siemens.sw360.datahandler.thrift.vulnerabilities.Vulnerability>"
             scope="request"/>

<portlet:resourceURL var="vulnerabilityListAjaxURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value='<%=PortalConstants.VULNERABILITY_LIST%>'/>
</portlet:resourceURL>

<div id="header"></div>
<p class="pageHeader">
    <span class="pageHeaderBigSpan">Vulnerabilities</span>
</p>

<div id="vulnerabilitiesTableDiv" class="content2">
    <table id="vulnerabilitiesTable" cellpadding="0" cellspacing="0" border="0" class="display">
        <tfoot>
        <tr>
            <th colspan="6"></th>
        </tr>
        </tfoot>
    </table>
</div>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/external/jquery-ui.css">
<script src="<%=request.getContextPath()%>/js/external/jquery-1.11.1.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery-ui.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery.dataTables.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/loadTags.js"></script>

<script>
    var oTable;

    var PortletURL;
    AUI().use('liferay-portlet-url', function (A) {
        PortletURL = Liferay.PortletURL;
        load();
    });

    function load() {
        createVulnerabilityTable();

    }

    function createVulnerabilityTable() {
        var result = [];

        <core_rt:forEach items="${vulnerabilityList}" var="vulnerability">
        result.push({
            "DT_RowId": "${vulnerability.id}",
            "0": "${vulnerability.externalId}",
            "1": "<span title='<sw360:out value='${vulnerability.description}'/>'><sw360:out value='${vulnerability.title}'/></span>",
            "2": "<span title='<sw360:out value='${vulnerability.priorityText}'/>'><sw360:out value='${vulnerability.priority}'/><img class='infopic' src='/sw360-portlet/images/ic_info.png'/></span>",
            "3": "<sw360:out value='${vulnerability.publishDate}'/>",
            "4": "<sw360:out value='${vulnerability.lastExternalUpdate}'/>",
            "5":"<div class='dataTables_cell_nowrap'><sw360:out value='${vulnerability.action}'/></div>"
        });
        </core_rt:forEach>

        oTable = $('#vulnerabilitiesTable').dataTable({
            "sPaginationType": "full_numbers",
            "aaData": result,
            "aoColumns": [
                {"sTitle": "External Id"},
                {"sTitle": "Title"},
                {"sTitle": "Priority"},
                {"sTitle": "Publish date"},
                {"sTitle": "Last update"},
                {"sTitle": "Action"}
            ],
            "order": [[2, 'asc'], [3, 'desc']]
        });
        oTable.$('td').tooltip({"delay": 0, "track": true, "fade": 250});

        $('#vulnerabilitiesTable_first').hide();
        $('#vulnerabilitiesTable_last').hide();
    }

    function makeVulnerabilityUrl(vulnerabilityId, page) {
        var portletURL = PortletURL.createURL('<%= PortletURLFactoryUtil.create(request, portletDisplay.getId(), themeDisplay.getPlid(), PortletRequest.RENDER_PHASE) %>')
                .setParameter('<%=PortalConstants.PAGENAME%>', page)
                .setParameter('<%=PortalConstants.VULNERABILITY_ID%>', vulnerabilityId);
        return portletURL.toString();
    }

    function renderVulnerabilityNameLink(name, type, row) {
        return renderLinkTo(makeVulnerabilityUrl(row.id, '<%=PortalConstants.PAGENAME_DETAIL%>'), name);
    }

    function createUrl_comp(paramId, paramVal) {
         var portletURL = PortletURL.createURL('<%= PortletURLFactoryUtil.create(request, portletDisplay.getId(), themeDisplay.getPlid(), PortletRequest.RENDER_PHASE) %>')
                 .setParameter('<%=PortalConstants.PAGENAME%>', '<%=PortalConstants.PAGENAME_DETAIL%>').setParameter(paramId, paramVal);
         return portletURL.toString();
    }

    function createDetailURLfromVulnerabilityId(paramVal) {
         return createUrl_comp('<%=PortalConstants.VULNERABILITY_ID%>', paramVal);
    }

 </script>

 <link rel="stylesheet" href="<%=request.getContextPath()%>/css/dataTable_Siemens.css">
 <link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">
