<%--
  ~ Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
  --%>

<%@include file="/html/init.jsp" %>
<%-- the following is needed by liferay to display error messages--%>
<%@include file="/html/utils/includes/errorKeyToMessage.jspf"%>

<%@ taglib prefix="sw360" uri="/WEB-INF/customTags.tld" %>

<%@ page import="com.liferay.portlet.PortletURLFactoryUtil" %>
<%@ page import="org.eclipse.sw360.datahandler.thrift.moderation.DocumentType" %>
<%@ page import="org.eclipse.sw360.portal.common.PortalConstants" %>
<%@ page import="javax.portlet.PortletRequest" %>

<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<jsp:useBean id="component" class="org.eclipse.sw360.datahandler.thrift.components.Component" scope="request"/>
<jsp:useBean id="moderationRequest" class="org.eclipse.sw360.datahandler.thrift.moderation.ModerationRequest" scope="request"/>
<jsp:useBean id="usingProjects" type="java.util.Set<org.eclipse.sw360.datahandler.thrift.projects.Project>"
             scope="request"/>
<jsp:useBean id="actual_release" class="org.eclipse.sw360.datahandler.thrift.components.Release" scope="request"/>
<core_rt:set var="release" value="${actual_release}" scope="request"/>
<core_rt:set var="releaseId" value="${moderationRequest.releaseAdditions.id}" scope="request"/>

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


<div id="header"></div>
<p class="pageHeader"><span class="pageHeaderBigSpan">Moderation Delete Release: <sw360:ReleaseName release="${release}" /></span>
</p>
<%@include file="/html/moderation/includes/moderationActionButtons.jspf"%>

<div id="content">
    <div class="container-fluid">
        <div id="myTab" class="row-fluid">
            <ul class="nav nav-tabs span2">
                <li <core_rt:if test="${selectedTab == 'Summary' || empty selectedTab}"> class="active" </core_rt:if> id="Summary" >  <a href="#tab-Summary">Summary</a></li>
                <li <core_rt:if test="${selectedTab == 'Vendor'}"> class="active"                       </core_rt:if> >               <a href="#tab-Vendor">Vendor</a></li>
                <li <core_rt:if test="${selectedTab == 'Linked Releases'}">              class="active" </core_rt:if>  >                <a href="#tab-linkedReleases">Linked Releases</a></li>
                <li <core_rt:if test="${selectedTab == 'Attachments'}"> class="active"                  </core_rt:if> >               <a href="#tab-Attachments">Attachments</a></li>
                <li <core_rt:if test="${selectedTab == 'Clearing'}"> class="active"                     </core_rt:if> id="Clearing" > <a href="#tab-ClearingStatus">Clearing Details</a></li>
            </ul>
            <div class="tab-content span10">
                <div id="tab-Summary" class="tab-pane">
                    <%@include file="/html/components/includes/releases/summaryRelease.jspf" %>
                </div>
                <div id="tab-Vendors">
                    <%@include file="/html/components/includes/vendors/vendorDetail.jspf" %>
                </div>
                <div id="tab-linkedReleases" >
                    <%@include file="/html/utils/includes/linkedReleaseDetails.jspf" %>
                </div>
                <div id="tab-Attachments">
                    <jsp:include page="/html/utils/includes/attachmentsDetail.jsp"/>
                </div>
                <div id="tab-ClearingStatus">
                    <%@include file="/html/components/includes/releases/clearingDetails.jspf" %>
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

    function getBaseURL(){
        var baseUrl = '<%= PortletURLFactoryUtil.create(request, portletDisplay.getId(), themeDisplay.getPlid(), PortletRequest.RENDER_PHASE) %>';
        var portletURL = Liferay.PortletURL.createURL(baseUrl)
                .setParameter('<%=PortalConstants.PAGENAME%>', '<%=PortalConstants.PAGENAME_ACTION%>')
                .setParameter('<%=PortalConstants.MODERATION_ID%>', '${moderationRequest.id}')
                .setParameter('<%=PortalConstants.DOCUMENT_TYPE%>', '<%=DocumentType.RELEASE%>');

        return portletURL;
    }

    function deleteAttachment(id1, id2) {
        alert("You can not delete individual attachments in the moderation, if you accept the request all attachments will be deleted.");
    }
</script>
<%@include file="/html/moderation/includes/moderationActions.jspf"%>

