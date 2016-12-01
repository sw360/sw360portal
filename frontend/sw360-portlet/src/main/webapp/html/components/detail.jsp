<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  ~ Copyright Siemens AG, 2013-2016. Part of the SW360 Portal Project.
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
  --%>

<%@include file="/html/init.jsp" %>
<%-- the following is needed by liferay to display error messages--%>
<%@include file="/html/utils/includes/errorKeyToMessage.jspf"%>
<portlet:defineObjects/>
<liferay-theme:defineObjects/>
<%@ page import="org.eclipse.sw360.portal.common.PortalConstants" %>

<portlet:resourceURL var="subscribeComponentURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value="<%=PortalConstants.SUBSCRIBE%>"/>
    <portlet:param name="<%=PortalConstants.COMPONENT_ID%>" value="${component.id}"/>
</portlet:resourceURL>


<portlet:resourceURL var="unsubscribeComponentURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value="<%=PortalConstants.UNSUBSCRIBE%>"/>
    <portlet:param name="<%=PortalConstants.COMPONENT_ID%>" value="${component.id}"/>
</portlet:resourceURL>

<c:catch var="attributeNotFoundException">
    <jsp:useBean id="component" class="org.eclipse.sw360.datahandler.thrift.components.Component" scope="request"/>
    <jsp:useBean id="selectedTab" class="java.lang.String" scope="request"/>
    <jsp:useBean id="usingProjects" type="java.util.Set<org.eclipse.sw360.datahandler.thrift.projects.Project>" scope="request"/>
    <jsp:useBean id="usingComponents" type="java.util.Set<org.eclipse.sw360.datahandler.thrift.components.Component>" scope="request"/>
    <jsp:useBean id="documentType" class="java.lang.String" scope="request"/>
</c:catch>
<core_rt:if test="${empty attributeNotFoundException}">

    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/external/jquery-ui.css">
    <script src="<%=request.getContextPath()%>/js/external/jquery-1.11.1.min.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/js/external/jquery.validate.min.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/js/external/additional-methods.min.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/js/external/jquery-ui.min.js"></script>

    <jsp:include page="/html/utils/includes/attachmentsDelete.jsp"/>

    <div id="header"></div>
    <p class="pageHeader"><span class="pageHeaderBigSpan">Component: ${component.name}</span>
        <span class="pull-right">
        <input type="button" onclick="editComponent()" id="edit" value="Edit" class="addButton">
        <sw360:DisplaySubscribeButton email="<%=themeDisplay.getUser().getEmailAddress()%>" object="${component}"
                                      id="SubscribeButton" onclick="subscribeComponent('SubscribeButton')"  altonclick="unsubscribeComponent('SubscribeButton')" />
    </span>
    </p>
    <div id="content">
        <div class="container-fluid">
            <div id="myTab" class="row-fluid">
                <ul class="nav nav-tabs span2">
                    <li <core_rt:if test="${selectedTab == 'Summary' || empty selectedTab}"> class="active" </core_rt:if> ><a href="#tab-Summary">Summary</a></li>
                    <li <core_rt:if test="${selectedTab == 'Clearing'}"> class="active" </core_rt:if>><a href="#tab-ClearingStatus">Release Overview</a></li>
                    <li <core_rt:if test="${selectedTab == 'Attachments'}"> class="active" </core_rt:if>><a href="#tab-Attachments">Attachments</a></li>
                    <li <core_rt:if test="${selectedTab == 'Vulnerabilities'}"> class="active" </core_rt:if>><a href="#tab-Vulnerabilities">Vulnerabilities</a></li>
                </ul>
                <div class="tab-content span10">
                    <div id="tab-Summary" class="tab-pane">
                        <%@include file="/html/components/includes/components/summary.jspf" %>
                        <core_rt:set var="documentName"><sw360:out value='${component.name}'/></core_rt:set>
                        <%@include file="/html/utils/includes/usingProjectsTable.jspf" %>
                        <%@include file="/html/utils/includes/usingComponentsTable.jspf"%>
                    </div>
                    <div id="tab-ClearingStatus">
                        <%@include file="/html/components/includes/components/clearingStatus.jspf" %>
                    </div>
                    <div id="tab-Attachments">
                        <jsp:include page="/html/utils/includes/attachmentsDetail.jsp" />
                    </div>
                    <div id="tab-Vulnerabilities">
                        <%@include file="/html/components/includes/components/vulnerabilities.jspf" %>
                    </div>
                </div>
            </div>
        </div>
    </div>
</core_rt:if>

<script>
    var tabView;
    var Y = YUI().use(
            'aui-tabview',
            function (Y) {
                tabView = new Y.TabView(
                        {
                            srcNode: '#myTab',
                            stacked: true,
                            type: 'tab'
                        }
                ).render();
            }
    );

    function editComponent() {
        window.location ='<portlet:renderURL ><portlet:param name="<%=PortalConstants.COMPONENT_ID%>" value="${component.id}"/><portlet:param name="<%=PortalConstants.PAGENAME%>" value="<%=PortalConstants.PAGENAME_EDIT%>"/></portlet:renderURL>'
    }

    function doAjax(url,spanId , successCallback) {
        var $resultElement = $('#' + spanId);
        $resultElement.val("...");
        $.ajax({
            type: 'POST',
            url: url,
            success: function (data) {
                successCallback(spanId, data);
            },
            error: function () {
                $resultElement.val("error");
            }
        });
    }

    function subscribed(spanId, data) {
        var $resultElement = $('#' + spanId);
        var msg = data.result == "SUCCESS" ? "Unsubscribe" : data.result;
        $resultElement.val(msg);
        $resultElement.addClass("subscribed");
        $resultElement.attr("onclick","unsubscribeComponent('"+spanId+"')");
    }

    function unsubscribed(spanId, data) {
        var $resultElement = $('#' + spanId);
        var msg = data.result == "SUCCESS" ? "Subscribe" : data.result;
        $resultElement.val(msg);
        $resultElement.removeClass("subscribed");
        $resultElement.attr("onclick","subscribeComponent('"+spanId+"')");
    }

    function subscribeComponent(spanId) {
        doAjax('<%=subscribeComponentURL%>',spanId , subscribed);
    }

    function unsubscribeComponent(spanId) {
        doAjax('<%=unsubscribeComponentURL%>',spanId , unsubscribed);
    }

</script>
