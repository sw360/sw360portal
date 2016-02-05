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
<%@include file="/html/init.jsp"%>

<portlet:defineObjects />
<liferay-theme:defineObjects />

<jsp:useBean id="selectedTab" class="java.lang.String" scope="request" />
<jsp:useBean id="licenseDetail" class="com.siemens.sw360.datahandler.thrift.licenses.License" scope="request" />
<jsp:useBean id="todos_from_moderation_request" type="java.util.List<com.siemens.sw360.datahandler.thrift.licenses.Todo>" scope="request" />
<jsp:useBean id="isAdminUser" class="java.lang.String" scope="request" />
<jsp:useBean id="obligationList" type="java.util.List<com.siemens.sw360.datahandler.thrift.licenses.Obligation>"
             scope="request"/>

<portlet:actionURL var="editLicenseTodosURL" name="updateWhiteList">
     <portlet:param name="<%=PortalConstants.LICENSE_ID%>" value="${licenseDetail.id}" />
</portlet:actionURL>

<portlet:actionURL var="addLicenseTodoURL" name="addTodo">
    <portlet:param name="<%=PortalConstants.LICENSE_ID%>" value="${licenseDetail.id}" />
</portlet:actionURL>

<portlet:actionURL var="changeLicenseTextURL" name="changeText">
    <portlet:param name="<%=PortalConstants.LICENSE_ID%>" value="${licenseDetail.id}" />
</portlet:actionURL>
<div id="header"></div>
<p class="pageHeader"><span class="pageHeaderBigSpan">License:  ${licenseDetail.fullname} (${licenseDetail.shortname})</span> </p>

<core_rt:set var="editMode" value="true" scope="request"/>
<%@include file="includes/detailOverview.jspf"%>

<script>
    var Y = YUI().use(
            'aui-tabview',
            function(Y) {
                new Y.TabView(
                        {
                            srcNode: '#myTab',
                            stacked: true,
                            type: 'tab'
                        }
                ).render();

                Y.all('td.addToWhiteListCheckboxes').hide();
                Y.all('td.addToWhiteListCheckboxesPlaceholder').show();
            }

    );

    function showWhiteListOptions() {
        Y.all('td.addToWhiteListCheckboxes').show();
        Y.all('td.addToWhiteListCheckboxesPlaceholder').hide();
        Y.all('tr.dependentOnWhiteList').show();
        Y.all('tr.todosFromModerationRequest').show();
        Y.all('table.todosFromModerationRequest').show();
        Y.one('#EditWhitelist').hide();
        Y.one('#SubmitWhitelist').show();
    }
</script>




<link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">