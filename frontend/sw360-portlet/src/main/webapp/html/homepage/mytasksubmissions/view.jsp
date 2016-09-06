<%--
  ~ Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
  --%>
<%@ page import="com.siemens.sw360.portal.common.PortalConstants" %>
<%@include file="/html/init.jsp" %>
<portlet:defineObjects/>
<liferay-theme:defineObjects/>
<%-- Note that the necessary includes are in liferay-portlet.xml --%>
<% assert ("moderationRequests".equals(PortalConstants.MODERATION_REQUESTS)); %>

<jsp:useBean id="moderationRequests"
             type="java.util.List<com.siemens.sw360.datahandler.thrift.moderation.ModerationRequest>"
             class="java.util.ArrayList" scope="request"/>

<portlet:resourceURL var="deleteAjaxURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value='<%=PortalConstants.DELETE_MODERATION_REQUEST%>'/>
</portlet:resourceURL>

<br>
<br>

<div class="homepageheading">
    My Task Submissions
</div>
<div id="tasksubmissionDiv" class="homepageListingTable">
    <table id="tasksubmissionTable" cellpadding="0" cellspacing="0" border="0" class="display">
    </table>
</div>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/external/jquery-confirm.min.css">
<script src="<%=request.getContextPath()%>/js/external/jquery-confirm.min.js" type="text/javascript"></script>
<script>
    var oTable;
    var oTable;
    AUI().use('liferay-portlet-url', function (A) {
        <portlet:namespace/>load();
    });

    //This can not be document ready function as liferay definitions need to be loaded first
   function <portlet:namespace/>load() {
        var result = [];

        <core_rt:forEach items="${moderationRequests}" var="moderation">
        result.push({
            "DT_RowId": "${moderation.id}",
            "0": '<sw360:out value="${moderation.documentName}"/>',
            "1": '<sw360:out value="${moderation.moderationState}"/>',
            "2": "<img src='<%=request.getContextPath()%>/images/Trash.png' onclick=\"deleteModerationRequest('${moderation.id}','<b>${moderation.documentName}</b>')\"  alt='Delete' title='Delete'>"
        });
        </core_rt:forEach>

        oTable = $('#tasksubmissionTable').DataTable({
            pagingType: "full_numbers",
            data: result,
            "iDisplayLength": 10,
            columns: [
                {"title": "Document Name"},
                {"title": "Status"},
                {"title": "Actions"}
            ]
        });

        $('#tasksubmissionTable_filter').hide();
        $('#tasksubmissionTable_first').hide();
        $('#tasksubmissionTable_last').hide();
        $('#tasksubmissionTable_length').hide();
    }
    function deleteModerationRequest(id, docName) {

        function deleteModerationRequestInternal() {
            jQuery.ajax({
                type: 'POST',
                url: '<%=deleteAjaxURL%>',
                cache: false,
                data: {
                    <portlet:namespace/>moderationId: id
                },
                success: function (data) {
                    if (data.result == 'SUCCESS') {
                        oTable.row('#' + id).remove().draw();
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

        deleteConfirmed("Do you really want to delete the moderation request for " + docName + " ?", deleteModerationRequestInternal);
    }


</script>


