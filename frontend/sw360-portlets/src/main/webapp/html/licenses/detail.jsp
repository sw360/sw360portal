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
<div id="content" >
    <div class="container-fluid">
        <div id="myTab" class="row-fluid">
            <ul class="nav nav-tabs span2">
                <li <core_rt:if test="${selectedTab == 'Details' || empty selectedTab}"> class="active" </core_rt:if> ><a href="#tab-Details">Details</a></li>
                <li <core_rt:if test="${selectedTab == 'LicenseText'}"> class="active" </core_rt:if> ><a href="#tab-LicenseText">Text</a></li>
                <li <core_rt:if test="${selectedTab == 'Risks'}"> class="active" </core_rt:if> ><a href="#tab-Risks">Risks</a></li>
                <li <core_rt:if test="${selectedTab == 'Todos'}"> class="active" </core_rt:if> ><a href="#tab-TodosAndObligations">TODOs and Obligations</a></li>
                <li <core_rt:if test="${selectedTab == 'AddTodo'}"> class="active" </core_rt:if> ><a href="#tab-AddTodo">Add a Todo</a></li>
            </ul>
            <div class="tab-content span10">
                <div id="tab-Details" class="tab-pane" >
                    <%@include file="/html/licenses/includes/detailSummary.jspf" %>
                </div>
                <div id="tab-LicenseText" class="tab-pane" >
                    <%@include file="/html/licenses/includes/detailText.jspf" %>
                </div>
                <div id="tab-Risks">
                    <%@include file="/html/licenses/includes/detailRisks.jspf" %>
                </div>
                <div id="tab-TodosAndObligations" >
                    <%@include file="/html/licenses/includes/detailTodos.jspf" %>
                </div>
                <div id="tab-AddTodo" class="tab-pane">
                    <%@include file="/html/licenses/includes/detailAddTodo.jspf" %>
                </div>
            </div>
        </div>
    </div>
</div>
<div class="yui3-skin-sam">
    <div id="modal"></div>
</div>

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
            }

    );

    function showWhiteListOptions() {
        Y.all('td.addToWhiteListCheckboxes').show();
        Y.all('tr.dependentOnWhiteList').show();
        Y.one('#EditWhitelist').hide();
//        Y.one('#showAddTodoModal').hide();
        Y.one('#SubmitWhitelist').show();
    }


</script>




<link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">