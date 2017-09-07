<%--
  ~ Copyright Siemens AG, 2013-2017. Part of the SW360 Portal Project.
  ~ With modifications by Bosch Software Innovations GmbH, 2016.
  ~
  ~ SPDX-License-Identifier: EPL-1.0
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
  --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ include file="/html/init.jsp" %>
<%-- the following is needed by liferay to display error messages--%>
<%@ include file="/html/utils/includes/errorKeyToMessage.jspf"%>


<portlet:defineObjects/>
<liferay-theme:defineObjects/>

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
    <jsp:useBean id="vulnerabilityVerificationEditable" type="java.lang.Boolean" scope="request"/>
    <core_rt:if test="${vulnerabilityVerificationEditable}">
        <jsp:useBean id="numberOfIncorrectVulnerabilities" type="java.lang.Long" scope="request"/>
    </core_rt:if>
    <jsp:useBean id="numberOfCheckedOrUncheckedVulnerabilities" type="java.lang.Long" scope="request"/>
 </c:catch>
<%@include file="/html/utils/includes/logError.jspf" %>
<core_rt:if test="${empty attributeNotFoundException}">

    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/webjars/jquery-ui/1.12.1/jquery-ui.css">
    <script src="<%=request.getContextPath()%>/webjars/jquery/1.12.4/jquery.min.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/webjars/jquery-validation/1.15.1/jquery.validate.min.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/webjars/jquery-validation/1.15.1/additional-methods.min.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/webjars/jquery-ui/1.12.1/jquery-ui.min.js"></script>

    <div id="header"></div>
    <p class="pageHeader"><span class="pageHeaderBigSpan">Component: ${component.name}</span>
        <span class="pull-right">
        <input type="button" onclick="editComponent()" id="edit" value="Edit" class="addButton">
        <sw360:DisplaySubscribeButton email="<%=themeDisplay.getUser().getEmailAddress()%>" object="${component}"
                                      id="SubscribeButton" onclick="subscribeComponent('SubscribeButton')"  altonclick="unsubscribeComponent('SubscribeButton')" />
    </span>
    </p>
    <core_rt:set var="inComponentDetailsContext" value="true" scope="request"/>
    <%@include file="/html/components/includes/components/detailOverview.jspf"%>
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
