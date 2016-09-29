<%--
  ~ Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
  ~ With modifications by Bosch Software Innovations GmbH, 2016.
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
  --%>
<%@ page import="com.siemens.sw360.portal.common.PortalConstants" %>
<%@include file="/html/init.jsp" %>
<%-- the following is needed by liferay to display error messages--%>
<%@include file="/html/utils/includes/errorKeyToMessage.jspf"%>

<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<jsp:useBean id="selectedTab" class="java.lang.String" scope="request"/>
<jsp:useBean id="licenseDetail" class="com.siemens.sw360.datahandler.thrift.licenses.License" scope="request"/>
<jsp:useBean id="moderationLicenseDetail" class="com.siemens.sw360.datahandler.thrift.licenses.License"
             scope="request"/>
<jsp:useBean id="added_todos_from_moderation_request"
             type="java.util.List<com.siemens.sw360.datahandler.thrift.licenses.Todo>" scope="request"/>
<jsp:useBean id="db_todos_from_moderation_request"
             type="java.util.List<com.siemens.sw360.datahandler.thrift.licenses.Todo>" scope="request"/>
<jsp:useBean id="isUserAtLeastClearingAdmin" class="java.lang.String" scope="request"/>
<jsp:useBean id="obligationList" type="java.util.List<com.siemens.sw360.datahandler.thrift.licenses.Obligation>"
             scope="request"/>

<portlet:actionURL var="editLicenseTodosURL" name="updateWhiteList">
    <portlet:param name="<%=PortalConstants.LICENSE_ID%>" value="${licenseDetail.id}"/>
</portlet:actionURL>

<portlet:actionURL var="addLicenseTodoURL" name="addTodo">
    <portlet:param name="<%=PortalConstants.LICENSE_ID%>" value="${licenseDetail.id}"/>
</portlet:actionURL>

<portlet:actionURL var="changeLicenseTextURL" name="changeText">
    <portlet:param name="<%=PortalConstants.LICENSE_ID%>" value="${licenseDetail.id}"/>
</portlet:actionURL>

<portlet:actionURL var="editExternalLinkURL" name="editExternalLink">
    <portlet:param name="<%=PortalConstants.LICENSE_ID%>" value="${licenseDetail.id}" />
</portlet:actionURL>

<div id="header"></div>
<p class="pageHeader"><span
        class="pageHeaderBigSpan">License: ${licenseDetail.fullname} (${licenseDetail.shortname})</span>
    <core_rt:if test="${isUserAtLeastClearingAdmin == 'Yes'}">
         <span class="pull-right">
             <input type="button" onclick="editLicense()" id="edit" value="Edit License Details and Text"
                    class="addButton">
         </span>
    </core_rt:if>
</p>
<core_rt:set var="editMode" value="true" scope="request"/>
<%@include file="includes/detailOverview.jspf" %>

<script>
    var Y = YUI().use(
            'aui-tabview',
            function (Y) {
                new Y.TabView(
                        {
                            srcNode: '#myTab',
                            stacked: true,
                            type: 'tab'
                        }
                ).render();
            }
    );

    function showWhiteListOptions() {
        Y.all('table.todosFromModerationRequest').show();
        Y.all('table.db_table').hide();
    }
    function editLicense() {
        window.location = '<portlet:renderURL >'
                             +'<portlet:param name="<%=PortalConstants.LICENSE_ID%>" value="${licenseDetail.id}"/>'
                             +'<portlet:param name="<%=PortalConstants.PAGENAME%>" value="<%=PortalConstants.PAGENAME_EDIT%>"/>'
                         +'</portlet:renderURL>'
    }
</script>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">
