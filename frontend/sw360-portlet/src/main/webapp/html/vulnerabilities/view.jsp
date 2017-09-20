<%--
  ~ Copyright (c) Bosch Software Innovations GmbH 2016.
  ~ Copyright (c) Siemens AG 2016-2017. Part of the SW360 Portal Project.
  ~
  ~ SPDX-License-Identifier: EPL-1.0
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
  --%>
<%@ page import="com.liferay.portlet.PortletURLFactoryUtil" %>
<%@ page import="org.eclipse.sw360.portal.common.PortalConstants" %>
<%@ page import="javax.portlet.PortletRequest" %>

<%@ include file="/html/init.jsp" %>

<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<jsp:useBean id="vulnerabilityList" type="java.util.List<org.eclipse.sw360.datahandler.thrift.vulnerabilities.Vulnerability>"
             scope="request"/>

<portlet:resourceURL var="vulnerabilityListAjaxURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value='<%=PortalConstants.VULNERABILITY_LIST%>'/>
</portlet:resourceURL>


<link rel="stylesheet" href="<%=request.getContextPath()%>/webjars/jquery-ui/1.12.1/jquery-ui.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/webjars/datatables.net-buttons-dt/1.1.2/css/buttons.dataTables.min.css"/>
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/dataTable_Siemens.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">

<div id="header"></div>
<p class="pageHeader">
    <span class="pageHeaderBigSpan">Vulnerabilities</span>
</p>
<div id="searchInput" class="content1">
    <%@ include file="/html/utils/includes/quickfilter.jspf" %>
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


<%--for javascript library loading --%>
<%@ include file="/html/utils/includes/requirejs.jspf" %>
<script>
    AUI().use('liferay-portlet-url', function () {
        var PortletURL = Liferay.PortletURL;

        require(['jquery', 'utils/includes/quickfilter', /* jquery-plugins: */ 'datatables', 'datatables_buttons', 'buttons.print', 'jquery-ui'], function($, quickfilter) {
            var vulnerabilityTable;

            // initializing
            load();

             // helper functions
            function load() {
                vulnerabilityTable = createVulnerabilityTable();
                quickfilter.addTable(vulnerabilityTable);
            }

            // catch ctrl+p and print dataTable
            $(document).on('keydown', function(e){
                if(e.ctrlKey && e.which === 80){
                    e.preventDefault();
                    vulnerabilityTable.buttons('.custom-print-button').trigger();
                }
            });

            function createVulnerabilityTable() {
                var vulnerabilityTable,
                    result = [];

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
                    "pagingType": "simple_numbers",
                    "dom": "lBrtip",
                    "buttons": [
                        {
                            extend: 'print',
                            text: 'Print',
                            autoPrint: true,
                            className: 'custom-print-button'
                        }
                    ],
                    "data": result,
                    "columns": [
                        {"title": "External Id"},
                        {"title": "Title", "width": "30%"},
                        {"title": "Weighting"},
                        {"title": "Publish date"},
                        {"title": "Last update"}
                    ],
                    "order": [[2, 'asc'], [3, 'desc']],
                    "autoWidth": false
                });
                vulnerabilityTable.$('td').tooltip({"delay": 0, "track": true, "fade": 250});

                return vulnerabilityTable;
            }

            function createDetailURLFromVulnerabilityId(paramVal) {
                var portletURL = PortletURL.createURL('<%= PortletURLFactoryUtil.create(request, portletDisplay.getId(), themeDisplay.getPlid(), PortletRequest.RENDER_PHASE) %>')
                        .setParameter('<%=PortalConstants.PAGENAME%>', '<%=PortalConstants.PAGENAME_DETAIL%>').setParameter('<%=PortalConstants.VULNERABILITY_ID%>', paramVal);
                return portletURL.toString();
            }
        });
    });
 </script>
