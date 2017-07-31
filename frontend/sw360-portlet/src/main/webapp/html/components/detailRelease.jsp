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
<%@ page import="com.liferay.portlet.PortletURLFactoryUtil" %>
<%@ page import="org.eclipse.sw360.portal.common.PortalConstants" %>
<%@ page import="javax.portlet.PortletRequest" %>
<%@ page import="org.eclipse.sw360.datahandler.thrift.components.ComponentType" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="core_rt" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib prefix="sw360" uri="/WEB-INF/customTags.tld" %>

<%@ include file="/html/init.jsp" %>
<%-- the following is needed by liferay to display error messages--%>
<%@ include file="/html/utils/includes/errorKeyToMessage.jspf"%>


<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<portlet:resourceURL var="subscribeReleaseURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value="<%=PortalConstants.SUBSCRIBE_RELEASE%>"/>
    <portlet:param name="<%=PortalConstants.RELEASE_ID%>" value="${release.id}"/>
</portlet:resourceURL>

<portlet:resourceURL var="unsubscribeReleaseURL" >
    <portlet:param name="<%=PortalConstants.ACTION%>" value="<%=PortalConstants.UNSUBSCRIBE_RELEASE%>"/>
    <portlet:param name="<%=PortalConstants.RELEASE_ID%>" value="${release.id}"/>
</portlet:resourceURL>

<c:catch var="attributeNotFoundException">
    <jsp:useBean id="component" class="org.eclipse.sw360.datahandler.thrift.components.Component" scope="request"/>
    <jsp:useBean id="releaseId" class="java.lang.String" scope="request"/>
    <jsp:useBean id="release" class="org.eclipse.sw360.datahandler.thrift.components.Release" scope="request"/>
    <jsp:useBean id="usingProjects" type="java.util.Set<org.eclipse.sw360.datahandler.thrift.projects.Project>"
                 scope="request"/>
    <jsp:useBean id="usingComponents" type="java.util.Set<org.eclipse.sw360.datahandler.thrift.components.Component>" scope="request"/>
    <core_rt:set var="cotsMode" value="<%=component.componentType == ComponentType.COTS%>"/>
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
    <!--include jQuery -->
    <script src="<%=request.getContextPath()%>/webjars/jquery/1.12.4/jquery.min.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/webjars/jquery-validation/1.15.1/jquery.validate.min.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/webjars/jquery-validation/1.15.1/additional-methods.min.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/webjars/jquery-ui/1.12.1/jquery-ui.min.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/releaseTools.js"></script>

    <div id="header"></div>
    <p class="pageHeader"><label id="releaseHeaderLabel"> <span class="pageHeaderBigSpan"> Component: ${component.name}</span>
        <select id="releaseSelect" onchange="this.options[this.selectedIndex].value
        && (window.location = createDetailURLfromReleaseId (this.options[this.selectedIndex].value) );">
            <core_rt:forEach var="releaseItr" items="${component.releases}">
                <option <core_rt:if test="${releaseItr.id == releaseId}"> selected </core_rt:if>
                        value="${releaseItr.id}"><sw360:ReleaseName release="${releaseItr}" />
                </option>
            </core_rt:forEach>
        </select>
        <span class="pull-right">
        <input type="button" onclick="editRelease('${releaseId}')" id="edit" value="Edit" class="addButton">
        <sw360:DisplaySubscribeButton email="<%=themeDisplay.getUser().getEmailAddress()%>" object="${release}"
                                      id="SubscribeButton" onclick="subscribeRelease('SubscribeButton')" altonclick="unsubscribeRelease('SubscribeButton')"/>
    </span>
    </label>
    </p>
    <core_rt:set var="inReleaseDetailsContext" value="true" scope="request"/>
    <%@include file="/html/components/includes/releases/detailOverview.jspf"%>
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

    // constants for
    <%--selectedTabInURL = '<%=PortalConstants.SELECTED_TAB%>';--%>

    releaseIdInURL = '<%=PortalConstants.RELEASE_ID%>';
    compIdInURL = '<%=PortalConstants.COMPONENT_ID%>';
    componentId = '${component.id}';
    pageName = '<%=PortalConstants.PAGENAME%>';
    pageDetail = '<%=PortalConstants.PAGENAME_RELEASE_DETAIL%>';
    baseUrl = '<%= PortletURLFactoryUtil.create(request, portletDisplay.getId(), themeDisplay.getPlid(), PortletRequest.RENDER_PHASE) %>';

    function editRelease(releaseId) {
        var portletURL = Liferay.PortletURL.createURL(baseUrl).setParameter('<%=PortalConstants.PAGENAME%>', '<%=PortalConstants.PAGENAME_EDIT_RELEASE%>').setParameter('<%=PortalConstants.RELEASE_ID%>', releaseId)
                .setParameter('<%=PortalConstants.COMPONENT_ID%>', componentId);
        window.location = portletURL.toString();
    }

    function doAjax(url, spanId, successCallback) {
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
        $resultElement.attr("onclick","unsubscribeRelease('"+spanId+"')");
    }

    function unsubscribed(spanId, data) {
        var $resultElement = $('#' + spanId);
        var msg = data.result == "SUCCESS" ? "Subscribe" : data.result;
        $resultElement.val(msg);
        $resultElement.removeClass("subscribed");
        $resultElement.attr("onclick","subscribeRelease('"+spanId+"')");
    }

    function subscribeRelease(spanId) {
        doAjax('<%=subscribeReleaseURL%>', spanId, subscribed);
    }

    function unsubscribeRelease(spanId) {
        doAjax('<%=unsubscribeReleaseURL%>', spanId, unsubscribed);
    }


</script>


