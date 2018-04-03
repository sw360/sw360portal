<%--
  ~ Copyright Siemens AG, 2013-2018. Part of the SW360 Portal Project.
  ~
  ~ SPDX-License-Identifier: EPL-1.0
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
  --%>
<%@ taglib prefix="sw360" uri="/WEB-INF/customTags.tld" %>

<%@include file="/html/init.jsp" %>
<%-- the following is needed by liferay to display error messages--%>
<%@include file="/html/utils/includes/errorKeyToMessage.jspf"%>

<%@ page import="com.liferay.portlet.PortletURLFactoryUtil" %>
<%@ page import="org.eclipse.sw360.datahandler.thrift.moderation.DocumentType" %>
<%@ page import="org.eclipse.sw360.portal.common.PortalConstants" %>
<%@ page import="javax.portlet.PortletRequest" %>

<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<jsp:useBean id="moderationRequest" class="org.eclipse.sw360.datahandler.thrift.moderation.ModerationRequest" scope="request"/>
<jsp:useBean id="actual_component" class="org.eclipse.sw360.datahandler.thrift.components.Component" scope="request"/>
<jsp:useBean id="selectedTab" class="java.lang.String" scope="request"/>
<jsp:useBean id="usingProjects" type="java.util.Set<org.eclipse.sw360.datahandler.thrift.projects.Project>" scope="request"/>
<jsp:useBean id="allUsingProjectsCount" type="java.lang.Integer" scope="request"/>

<core_rt:set var="component" value="${actual_component}" scope="request"/>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/webjars/jquery-ui/1.12.1/jquery-ui.css">
<script src="<%=request.getContextPath()%>/webjars/jquery/1.12.4/jquery.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/webjars/jquery-validation/1.15.1/jquery.validate.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/webjars/jquery-validation/1.15.1/additional-methods.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/webjars/jquery-ui/1.12.1/jquery-ui.min.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/main.js"></script>
<%@include file="/html/moderation/includes/moderationActions.jspf"%>

<div id="header"></div>
<p class="pageHeader"><span class="pageHeaderBigSpan">Moderation Change Component: <sw360:out value="${component.name}"/></span>
                      <span class="pageHeaderBigSpan" style="float:right">(<sw360:DisplayEnum value="${moderationRequest.componentType}"/>)</span>
</p>
<%@include file="/html/moderation/includes/moderationActionButtons.jspf"%>
<%@include file="/html/moderation/includes/moderationInfo.jspf"%>


<h2>Proposed changes</h2>
<h3>Basic fields</h3>
<sw360:DisplayComponentChanges actual="${actual_component}" additions="${moderationRequest.componentAdditions}" deletions="${moderationRequest.componentDeletions}" idPrefix="basicFields" tableClasses="table info_table" />

<h3>Attachments</h3>
<sw360:CompareAttachments actual="${actual_component.attachments}"
                          additions="${moderationRequest.componentAdditions.attachments}"
                          deletions="${moderationRequest.componentDeletions.attachments}"
                          idPrefix="attachments"
                          tableClasses="table info_table"
                          contextType="${component.type}"
                          contextId="${component.id}"/>

<h2>Current Component</h2>
<core_rt:set var="inComponentDetailsContext" value="false" scope="request"/>
<%@include file="/html/utils/includes/requirejs.jspf" %>
<%@include file="/html/components/includes/components/detailOverview.jspf"%>

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

    function getBaseURL() {
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
    function deleteRelease(id, name, linkedReleasesSize, attachmentsSize) {
        alert("You can not delete a release in the moderation");
    }
    function openSelectClearingDialog(fieldId, releaseId) {
        alert("You can not send to fossology from moderation");
    }

</script>

