<%--
  ~ Copyright Siemens AG, 2013-2017. Part of the SW360 Portal Project.
  ~
  ~ SPDX-License-Identifier: EPL-1.0
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
  --%>
<%@ page import="org.eclipse.sw360.portal.common.PortalConstants" %>

<%@ taglib prefix="sw360" uri="/WEB-INF/customTags.tld" %>

<%@ include file="/html/init.jsp" %>
<%-- the following is needed by liferay to display error messages--%>
<%@ include file="/html/utils/includes/errorKeyToMessage.jspf"%>


<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<portlet:resourceURL var="deleteModerationRequestAjaxURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value='<%=PortalConstants.DELETE_MODERATION_REQUEST%>'/>
</portlet:resourceURL>

<jsp:useBean id="moderationRequests" type="java.util.List<org.eclipse.sw360.datahandler.thrift.moderation.ModerationRequest>"
             scope="request"/>
<jsp:useBean id="closedModerationRequests" type="java.util.List<org.eclipse.sw360.datahandler.thrift.moderation.ModerationRequest>"
             scope="request"/>
<jsp:useBean id="isUserAtLeastClearingAdmin" class="java.lang.String" scope="request" />


<link rel="stylesheet" href="<%=request.getContextPath()%>/webjars/jquery-ui/1.12.1/jquery-ui.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/webjars/github-com-craftpip-jquery-confirm/3.0.1/jquery-confirm.min.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/dataTable_Siemens.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">

<div id="header"></div>
<p class="pageHeader">
    <span class="pageHeaderBigSpan">Moderations</span> <span class="pageHeaderSmallSpan" title="Count of open/closed moderation requests">(${moderationRequests.size()}/${closedModerationRequests.size()})</span>
</p>

<div id="content">
    <div class="container-fluid">
        <div id="myTab" class="row-fluid">
            <ul class="nav nav-tabs span2">
                <div id="searchInput">
                    <%@ include file="/html/utils/includes/quickfilter.jspf" %>
                </div>
                <br/>
                <li class="active"><a href="#tab-Open">Open</a></li>
                <li><a href="#tab-Closed">Closed</a></li>
            </ul>
            <div class="tab-content span10">
                <div id="tab-Open" class="tab-pane">
                    <table id="moderationsTable" cellpadding="0" cellspacing="0" border="0" class="display">
                        <tfoot>
                        <tr>
                            <th colspan="5"></th>
                        </tr>
                        </tfoot>
                    </table>
                </div>
                <div id="tab-Closed">
                    <table id="closedModerationsTable" cellpadding="0" cellspacing="0" border="0" class="display">
                        <tfoot>
                        <tr>
                            <th colspan="5"></th>
                        </tr>
                        </tfoot>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<%--for javascript library loading --%>
<%@ include file="/html/utils/includes/requirejs.jspf" %>
<script>
    YUI().use('aui-tabview', function (Y) {
           new Y.TabView({
            srcNode: '#myTab',
            stacked: true,
            type: 'tab'
        }).render();
    });

    require(['jquery', 'utils/includes/quickfilter', 'modules/confirm', /* jquery-plugins: */ 'datatables', 'jquery-confirm'], function($, quickfilter, confirm) {
        var moderationsDataTable,
            closedModerationsDataTable;

        // add event handler
        $(window).load(function () {
            moderationsDataTable = createModerationsTable("#moderationsTable", prepareModerationsData());
            closedModerationsDataTable = createModerationsTable("#closedModerationsTable", prepareClosedModerationsData());

            quickfilter.addTable(moderationsDataTable);
            quickfilter.addTable(closedModerationsDataTable);
        });
        $('#closedModerationsTable').on('click', 'img.delete', function(event) {
            var data = $(event.currentTarget).data();
            deleteModerationRequest(data.moderationRequest, data.documentName);
        });

        // helper functions
        function prepareModerationsData() {
            var result = [];
            <core_rt:forEach items="${moderationRequests}" var="moderation">
                result.push({
                    "DT_RowId": "${moderation.id}",
                    "0": "<sw360:DisplayModerationRequestLink moderationRequest="${moderation}"/>",
                    "1": '<sw360:DisplayUserEmail email="${moderation.requestingUser}" bare="true"/>',
                    "2": '<sw360:DisplayUserEmailCollection value="${moderation.moderators}" bare="true"/>',
                    "3": '<sw360:DisplayEnum value="${moderation.moderationState}"/>',
                    "4": 'TODO'
                });
            </core_rt:forEach>
            return result;
        }

        function prepareClosedModerationsData() {
            var result = [];
            <core_rt:forEach items="${closedModerationRequests}" var="moderation">
                result.push({
                    "DT_RowId": "${moderation.id}",
                    "0": "<sw360:DisplayModerationRequestLink moderationRequest="${moderation}"/>",
                    "1": '<sw360:DisplayUserEmail email="${moderation.requestingUser}" bare="true"/>',
                    "2": '<sw360:DisplayUserEmailCollection value="${moderation.moderators}" bare="true"/>',
                    "3": '<sw360:DisplayEnum value="${moderation.moderationState}"/>',
                    <core_rt:if test="${isUserAtLeastClearingAdmin == 'Yes'}">
                    "4": "<img class='delete' src='<%=request.getContextPath()%>/images/Trash.png' data-moderation-request='${moderation.id}' data-document-name='${moderation.documentName}' alt='Delete' title='Delete'>"
                    </core_rt:if>
                    <core_rt:if test="${isUserAtLeastClearingAdmin != 'Yes'}">
                    "4": "READY"
                    </core_rt:if>

                });
            </core_rt:forEach>
            return result;

        }

        function createModerationsTable(tableId, tableData) {
            var tbl = $(tableId).DataTable({
                pagingType: "simple_numbers",
                dom: "lrtip",
                data: tableData,
                columns: [
                    {"title": "Document Name"},
                    {"title": "Requesting User"},
                    {"title": "Moderators"},
                    {"title": "State"},
                    {"title": "Actions"}
                ]
            });

            return tbl;
        }

        function deleteModerationRequest(id, docName) {
            function deleteModerationRequestInternal() {
                jQuery.ajax({
                    type: 'POST',
                    url: '<%=deleteModerationRequestAjaxURL%>',
                    cache: false,
                    data: {
                        <portlet:namespace/>moderationId: id
                    },
                    success: function (data) {
                        if (data.result == 'SUCCESS') {
                            closedModerationsDataTable.row('#' + id).remove().draw(false);
                        }
                        else {
                            $.alert("I could not delete the moderation request!");
                        }
                    },
                    error: function () {
                        $.alert("I could not delete the moderation request!");
                    }
                });
            }

            confirm.confirmDeletion("Do you really want to delete the moderation request for <b>" + docName + "</b> ?", deleteModerationRequestInternal);
        }
    });
</script>
