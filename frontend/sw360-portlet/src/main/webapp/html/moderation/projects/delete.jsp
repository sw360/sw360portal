<%--
  ~ Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
  --%>
<%@include file="/html/init.jsp"%>



<portlet:defineObjects />
<liferay-theme:defineObjects />

<%@ page import="com.siemens.sw360.datahandler.thrift.moderation.DocumentType" %>
<%@ page import="javax.portlet.PortletRequest" %>

<jsp:useBean id="usingProjects" type="java.util.Set<com.siemens.sw360.datahandler.thrift.projects.Project>" scope="request"/>
<jsp:useBean id="moderationRequest" class="com.siemens.sw360.datahandler.thrift.moderation.ModerationRequest" scope="request"/>
<jsp:useBean id="selectedTab" class="java.lang.String" scope="request" />
<jsp:useBean id="actual_project" class="com.siemens.sw360.datahandler.thrift.projects.Project" scope="request" />
<core_rt:set var="project" value="${actual_project}" scope="request"/>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/external/jquery-ui.css">
<script src="<%=request.getContextPath()%>/js/external/jquery-1.11.1.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery.validate.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/additional-methods.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery-ui.min.js"></script>

<div id="header"></div>
<p class="pageHeader"><span class="pageHeaderBigSpan">Moderation Delete Project:  <sw360:ProjectName project="${project}"/></span>
</p>
<%@include file="/html/moderation/includes/moderationActionButtons.jspf"%>
<div id="content" >
    <div class="container-fluid">
        <div id="myTab" class="row-fluid">
            <ul class="nav nav-tabs span2">
                <li <core_rt:if test="${selectedTab == 'Summary' || empty selectedTab}"> class="active" </core_rt:if> ><a href="#tab-Summary">Summary</a></li>
                <li <core_rt:if test="${selectedTab == 'Linked Projects'}"> class="active" </core_rt:if>><a href="#tab-linkedProjects">Linked Projects</a></li>
                <li <core_rt:if test="${selectedTab == 'Linked Projects'}"> class="active" </core_rt:if>><a href="#tab-linkedReleases">Linked Releases</a></li>
                <li <core_rt:if test="${selectedTab == 'Clearing Status'}"> class="active" </core_rt:if>><a href="#tab-ClearingStatus">Clearing Status</a></li>
                <li <core_rt:if test="${selectedTab == 'Attachments'}"> class="active" </core_rt:if>><a href="#tab-Attachments">Attachments</a></li>
            </ul>
            <div class="tab-content span10">
                <div id="tab-Summary" class="tab-pane" >
                    <%@include file="/html/projects/includes/projects/summary.jspf" %>
                    <%@include file="/html/projects/includes/projects/usingProjects.jspf" %>
                </div>
                <div id="tab-linkedProjects" >
                    <%@include file="/html/projects/includes/linkedProjects.jspf" %>
                </div>
                <div id="tab-linkedReleases" >
                    <%@include file="/html/utils/includes/linkedReleaseDetails.jspf" %>
                </div>
                <div id="tab-ClearingStatus" >
                    <%@include file="/html/projects/includes/projects/clearingStatus.jspf" %>
                </div>
                <div id="tab-Attachments" >
                    <jsp:include page="/html/utils/includes/attachmentsDetail.jsp"/>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    var tabView;
    var Y = YUI().use(
            'aui-tabview',
            function(Y) {
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
                .setParameter('<%=PortalConstants.DOCUMENT_TYPE%>', '<%=DocumentType.COMPONENT%>');

        return portletURL;
    }

    function deleteAttachment(id1, id2) {
        alert("You can not delete individual attachments in the moderation, if you accept the request all attachments will be deleted.");
    }

    function deleteReleaseLink(rowId ){
        alert("You can not delete individual release links in the moderation, if you accept the request all links (original releases will prevail) will be deleted.");
    }

    function deleteProjectLink(rowId){
        alert("You can not delete individual project links in the moderation, if you accept the request all links (original projects will prevail) will be deleted.");
    }

</script>
<%@include file="/html/moderation/includes/moderationActions.jspf"%>
