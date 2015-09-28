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
<%@ page import="com.siemens.sw360.portal.common.PortalConstants" %>
<%@include file="/html/init.jsp" %>
<portlet:defineObjects/>
<liferay-theme:defineObjects/>
<%-- Note that the necessary includes are in life-ray-portlet.xml --%>
<% assert ("moderationRequests".equals(PortalConstants.MODERATION_REQUESTS)); %>

<jsp:useBean id="moderationRequests"
             type="java.util.List<com.siemens.sw360.datahandler.thrift.moderation.ModerationRequest>"
             class="java.util.ArrayList" scope="request"/>

<br>
<br>

<div class="homepageheading">
    My Task Submissions
</div>
<div id="tasksubmissionDiv" class="homepageListingTable">
    <table id="tasksubmissionTable" cellpadding="0" cellspacing="0" border="0" class="display">
    </table>
</div>


<script>

    //This can not be document ready function as liferay definitions need to be loaded first
    $(window).load(function () {
        var result = [];

        <core_rt:forEach items="${moderationRequests}" var="moderation">
        result.push({
            "DT_RowId": "${moderation.id}",
            "0": '<sw360:out value="${moderation.documentName}"/>',
            "1": '<sw360:out value="${moderation.moderationState}"/>'
        });
        </core_rt:forEach>

        $('#tasksubmissionTable').dataTable({
            pagingType: "full_numbers",
            data: result,
            "iDisplayLength": 10,
            columns: [
                {"title": "Document Name"},
                {"title": "Status"},
            ]
        });

        $('#tasksubmissionTable_filter').hide();
        $('#tasksubmissionTable_first').hide();
        $('#tasksubmissionTable_last').hide();
        $('#tasksubmissionTable_length').hide();
    });

</script>


