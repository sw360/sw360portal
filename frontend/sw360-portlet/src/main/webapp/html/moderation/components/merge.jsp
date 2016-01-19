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
<%@include file="/html/init.jsp" %>

<%@ page import="com.liferay.portlet.PortletURLFactoryUtil" %>
<%@ page import="com.siemens.sw360.datahandler.thrift.moderation.DocumentType" %>
<%@ page import="com.siemens.sw360.portal.common.PortalConstants" %>
<%@ page import="javax.portlet.PortletRequest" %>

<%@ taglib prefix="sw360" uri="/WEB-INF/customTags.tld" %>


<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<jsp:useBean id="moderationRequest" class="com.siemens.sw360.datahandler.thrift.moderation.ModerationRequest" scope="request"/>
<jsp:useBean id="actual_component" class="com.siemens.sw360.datahandler.thrift.components.Component" scope="request"/>
<jsp:useBean id="selectedTab" class="java.lang.String" scope="request"/>
<jsp:useBean id="usingProjects" type="java.util.Set<com.siemens.sw360.datahandler.thrift.projects.Project>" scope="request"/>

<core_rt:set var="component" value="${actual_component}" scope="request"/>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/external/jquery-ui.css">
<script src="<%=request.getContextPath()%>/js/external/jquery-1.11.1.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery.validate.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/additional-methods.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery-ui.min.js"></script>


<div id="header"></div>
<p class="pageHeader"><span class="pageHeaderBigSpan">Moderation Change Component: ${component.name}</span>
</p>
<input type="button" onclick="acceptDelete()" id="edit" value="Accept Request"    class="acceptButton">&nbsp;
<input type="button" onclick="removeFromModerators()" id="edit" value="Remove Me from Moderators"    class="ignoreButton">&nbsp;
<input type="button" onclick="declineDelete()" id="edit" value="Decline Request"    class="addButton">&nbsp;
<input type="button" onclick="postPone()" id="edit" value="Postpone Request"    class="postponeButton">&nbsp;
<input type="button" onclick="cancel()" id="edit" value="Cancel"    class="cancelButton">


<h2>Proposed changes</h2>
<h3>Basic fields</h3>
<sw360:CompareComponent old="${actual_component}" update="${moderationRequest.component}" idPrefix="basicFields" tableClasses="table info_table"/>

<h3>Attachments</h3>
<sw360:CompareAttachments old="${actual_component.attachments}" update="${moderationRequest.component.attachments}" idPrefix="attachments" tableClasses="table info_table" />

<h3>Current Component</h3>
<div id="content">
    <div class="container-fluid">
        <div id="myTab" class="row-fluid">
            <ul class="nav nav-tabs span2">
                <li <core_rt:if test="${selectedTab == 'Summary' || empty selectedTab}"> class="active" </core_rt:if> ><a href="#tab-Summary">Summary</a></li>
                <li <core_rt:if test="${selectedTab == 'Clearing'}"> class="active" </core_rt:if>><a href="#tab-ClearingStatus">Release Overview</a></li>
                <li <core_rt:if test="${selectedTab == 'Attachments'}"> class="active" </core_rt:if>><a href="#tab-Attachments">Attachments</a></li>
                <li <core_rt:if test="${selectedTab == 'Wiki'}"> class="active" </core_rt:if>><a href="#tab-Wiki">Wiki</a></li>
            </ul>
            <div class="tab-content span10">
                <div id="tab-Summary" class="tab-pane">
                    <%@include file="/html/components/includes/components/summary.jspf" %>
                </div>
                <div id="tab-ClearingStatus">
                    <%@include file="/html/components/includes/components/clearingStatus.jspf" %>
                </div>
                <div id="tab-Attachments">
                    <jsp:include page="/html/utils/includes/attachmentsDetail.jsp"/>
                </div>
                <div id="tab-Wiki" class="tab-pane">
                    <%@include file="/html/components/includes/wiki.jsp" %>
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
                .setParameter('<%=PortalConstants.DOCUMENT_TYPE%>', '<%=DocumentType.COMPONENT%>');

        return portletURL;
    }

    function acceptDelete() {
        var portletURL = getBaseURL().setParameter('<%=PortalConstants.ACTION%>', '<%=PortalConstants.ACTION_ACCEPT%>');
        window.location = portletURL.toString();
    }

    function removeFromModerators() {
        var portletURL = getBaseURL().setParameter('<%=PortalConstants.ACTION%>', '<%=PortalConstants.ACTION_REMOVEME%>');
        window.location = portletURL.toString();
    }

    function declineDelete() {
        var portletURL = getBaseURL().setParameter('<%=PortalConstants.ACTION%>', '<%=PortalConstants.ACTION_DECLINE%>');
        window.location = portletURL.toString();
    }

    function postPone() {
        var portletURL = getBaseURL().setParameter('<%=PortalConstants.ACTION%>', '<%=PortalConstants.ACTION_POSTPONE%>');
        window.location = portletURL.toString();
    }

    function cancel() {
        var portletURL = getBaseURL().setParameter('<%=PortalConstants.ACTION%>', '<%=PortalConstants.ACTION_CANCEL%>');
        window.location = portletURL.toString();
    }

    function deleteAttachment(id1, id2) {
        alert("You can not delete individual attachments in the moderation, if you accept the request all attachments will be deleted.");
    }

</script>
