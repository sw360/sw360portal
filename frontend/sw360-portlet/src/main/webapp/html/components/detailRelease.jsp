<%@ page import="com.siemens.sw360.portal.portlets.Sw360Portlet" %>
<%--
  ~ Copyright Siemens AG, 2013-2016. Part of the SW360 Portal Project.
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

<%@include file="/html/init.jsp" %>
<%@ page import="com.liferay.portlet.PortletURLFactoryUtil" %>

<%@ taglib prefix="core_rt" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib prefix="sw360" uri="/WEB-INF/customTags.tld" %>
<%@ page import="com.siemens.sw360.portal.common.PortalConstants" %>
<%@ page import="javax.portlet.PortletRequest" %>
<%@ page import="com.siemens.sw360.datahandler.thrift.components.ComponentType" %>

<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<jsp:useBean id="component" class="com.siemens.sw360.datahandler.thrift.components.Component" scope="request"/>
<jsp:useBean id="releaseId" class="java.lang.String" scope="request"/>
<jsp:useBean id="release" class="com.siemens.sw360.datahandler.thrift.components.Release" scope="request"/>
<jsp:useBean id="usingProjects" type="java.util.Set<com.siemens.sw360.datahandler.thrift.projects.Project>"
             scope="request"/>

<jsp:useBean id="usingComponents" type="java.util.Set<com.siemens.sw360.datahandler.thrift.components.Component>" scope="request"/>
<core_rt:set var="cotsMode" value="<%=component.componentType == ComponentType.COTS%>"/>

<portlet:resourceURL var="subscribeReleaseURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value="<%=PortalConstants.SUBSCRIBE_RELEASE%>"/>
    <portlet:param name="<%=PortalConstants.RELEASE_ID%>" value="${release.id}"/>
</portlet:resourceURL>

<portlet:resourceURL var="unsubscribeReleaseURL" >
    <portlet:param name="<%=PortalConstants.ACTION%>" value="<%=PortalConstants.UNSUBSCRIBE_RELEASE%>"/>
    <portlet:param name="<%=PortalConstants.RELEASE_ID%>" value="${release.id}"/>
</portlet:resourceURL>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/external/jquery-ui.css">
<!--include jQuery -->
<script src="<%=request.getContextPath()%>/js/external/jquery-1.11.1.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery.validate.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/additional-methods.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery-ui.min.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/releaseTools.js"></script>

<jsp:include page="/html/utils/includes/attachmentsDelete.jsp"/>

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
<div id="content">
    <div class="container-fluid">
        <div id="myTab" class="row-fluid">
            <ul class="nav nav-tabs span2">
                <li <core_rt:if test="${selectedTab == 'Summary' || empty selectedTab}"> class="active" </core_rt:if> id="Summary" >    <a href="#tab-Summary">Summary</a></li>
                <li <core_rt:if test="${selectedTab == 'Linked Releases'}">              class="active" </core_rt:if>  >                <a href="#tab-linkedReleases">Linked Releases</a></li>
                <li <core_rt:if test="${selectedTab == 'Clearing'}">                     class="active" </core_rt:if> id="Clearing" >   <a href="#tab-ClearingDetails">Clearing Details</a></li>
                <li <core_rt:if test="${selectedTab == 'Attachments'}">                  class="active" </core_rt:if>  >                <a href="#tab-Attachments">Attachments</a></li>
                <core_rt:if test="${cotsMode}">
                    <li <core_rt:if test="${selectedTab == 'COTSDetails'}"> class="active" </core_rt:if>  ><a href="#tab-CommercialDetails">Commercial Details</a></li>
                </core_rt:if>
                <li <core_rt:if test="${selectedTab == 'Vulnerabilities'}">              class="active" </core_rt:if>  >                <a href="#tab-Vulnerabilities">Vulnerabilities</a></li>
            </ul>
            <div class="tab-content span10">
                <div id="tab-Summary" class="tab-pane">
                    <%@include file="/html/components/includes/releases/summaryRelease.jspf" %>
                    <%@include file="/html/components/includes/vendors/vendorDetail.jspf" %>
                    <%@include file="/html/components/includes/releases/usingDocumentsRelease.jspf" %>
                </div>
                <div id="tab-linkedReleases" >
                    <%@include file="/html/utils/includes/linkedReleaseDetails.jspf" %>
                </div>
                <div id="tab-ClearingDetails">
                    <%@include file="/html/components/includes/releases/clearingDetails.jspf" %>
                </div>
                <div id="tab-Attachments">
                    <jsp:include page="/html/utils/includes/attachmentsDetail.jsp" />
                </div>
                <core_rt:if test="${cotsMode}">
                    <div id="tab-CommercialDetails">
                        <%@include file="/html/components/includes/releases/commercialDetails.jspf" %>
                    </div>
                </core_rt:if>
                <div id="tab-Vulnerabilities">
                    <%@include file="/html/components/includes/releases/vulnerabilities.jspf" %>
                </div>
            </div>
        </div>
    </div>
</div>

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


