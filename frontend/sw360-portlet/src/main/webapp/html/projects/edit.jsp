<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  ~ Copyright Siemens AG, 2013-2017. Part of the SW360 Portal Project.
  ~
  ~ SPDX-License-Identifier: EPL-1.0
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
  --%>
<%@ page import="com.liferay.portlet.PortletURLFactoryUtil" %>
<%@ page import="javax.portlet.PortletRequest" %>
<%@ page import="org.eclipse.sw360.datahandler.common.SW360Constants" %>
<%@ page import="org.eclipse.sw360.datahandler.thrift.attachments.Attachment" %>
<%@ page import="org.eclipse.sw360.datahandler.thrift.components.Release" %>
<%@ page import="org.eclipse.sw360.datahandler.thrift.moderation.DocumentType" %>
<%@ page import="org.eclipse.sw360.portal.common.PortalConstants" %>
<%@ page import="org.eclipse.sw360.datahandler.thrift.users.RequestedAction" %>
<%@ page import="org.eclipse.sw360.datahandler.thrift.attachments.CheckStatus" %>

<%@ include file="/html/init.jsp"%>
<%-- the following is needed by liferay to display error messages--%>
<%@ include file="/html/utils/includes/errorKeyToMessage.jspf"%>


<portlet:defineObjects />
<liferay-theme:defineObjects />

<portlet:actionURL var="updateURL" name="update">
    <portlet:param name="<%=PortalConstants.PROJECT_ID%>" value="${project.id}" />
</portlet:actionURL>

<portlet:actionURL var="deleteAttachmentsOnCancelURL" name='<%=PortalConstants.ATTACHMENT_DELETE_ON_CANCEL%>'>
</portlet:actionURL>

<portlet:actionURL var="deleteURL" name="delete">
    <portlet:param name="<%=PortalConstants.PROJECT_ID%>" value="${project.id}"/>
</portlet:actionURL>

<c:catch var="attributeNotFoundException">
    <jsp:useBean id="project" class="org.eclipse.sw360.datahandler.thrift.projects.Project" scope="request" />
    <jsp:useBean id="documentID" class="java.lang.String" scope="request" />
    <jsp:useBean id="usingProjects" type="java.util.Set<org.eclipse.sw360.datahandler.thrift.projects.Project>" scope="request"/>
    <jsp:useBean id="projectList" type="java.util.List<org.eclipse.sw360.datahandler.thrift.projects.ProjectLink>"  scope="request"/>
    <jsp:useBean id="releaseList" type="java.util.List<org.eclipse.sw360.datahandler.thrift.components.ReleaseLink>"  scope="request"/>
    <jsp:useBean id="attachments" type="java.util.Set<org.eclipse.sw360.datahandler.thrift.attachments.Attachment>" scope="request"/>
    <core_rt:set  var="addMode"  value="${empty project.id}" />
</c:catch>

<%--These variables are used as a trick to allow referencing enum values in EL expressions below--%>
<c:set var="WRITE" value="<%=RequestedAction.WRITE%>"/>
<c:set var="DELETE" value="<%=RequestedAction.DELETE%>"/>
<c:set var="hasWritePermissions" value="${project.permissions[WRITE]}"/>

<%@include file="/html/utils/includes/logError.jspf" %>
<core_rt:if test="${empty attributeNotFoundException}">

    <link rel="stylesheet" href="<%=request.getContextPath()%>/webjars/jquery-ui/1.12.1/jquery-ui.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/webjars/github-com-craftpip-jquery-confirm/3.0.1/jquery-confirm.min.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/dataTable_Siemens.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">

    <script src="<%=request.getContextPath()%>/webjars/jquery/1.12.4/jquery.min.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/webjars/jquery-ui/1.12.1/jquery-ui.min.js"></script>
    <script src="<%=request.getContextPath()%>/webjars/github-com-craftpip-jquery-confirm/3.0.1/jquery-confirm.min.js" type="text/javascript"></script>

    <div id="where" class="content1">
        <p class="pageHeader"><span class="pageHeaderBigSpan"><sw360:out value="${project.name}"/></span>
            <core_rt:if test="${not addMode}" >
                <input id="deleteProjectButton" type="button" class="addButton"
                       value="Delete <sw360:ProjectName project="${project}"/>"
                <core_rt:if test="${ usingProjects.size()>0}"> disabled="disabled" title="Deletion is disabled as the project is used." </core_rt:if>
                >
            </core_rt:if>
        </p>
        <core_rt:if test="${not addMode}" >
            <input type="button" id="formSubmit" value="Update Project" class="addButton">
        </core_rt:if>
        <core_rt:if test="${addMode}" >
            <input type="button" id="formSubmit" value="Add Project" class="addButton">
        </core_rt:if>
        <input id="cancelEditButton" type="button" value="Cancel" class="cancelButton">
        <div id="moderationRequestCommentDialog" style="display: none">
            <hr>
            <label class="textlabel stackedLabel">Comment your changes</label>
            <textarea form=projectEditForm name="<portlet:namespace/><%=PortalConstants.MODERATION_REQUEST_COMMENT%>" id="moderationRequestCommentField" class="moderationCreationComment" placeholder="Leave a comment on your request"></textarea>
            <input type="button" class="addButton" id="moderationRequestCommentSendButton" value="Send moderation request">
        </div>
    </div>


    <div id="editField" class="content2">
        <form  id="projectEditForm" name="projectEditForm" action="<%=updateURL%>" method="post" >
            <%@ include file="/html/utils/includes/requirejs.jspf" %>
            <%@include file="/html/projects/includes/projects/basicInfo.jspf" %>
            <%@include file="/html/projects/includes/linkedProjectsEdit.jspf" %>
            <%@include file="/html/utils/includes/linkedReleasesEdit.jspf" %>
            <core_rt:if test="${not addMode}" >
                <%@include file="/html/utils/includes/editAttachments.jspf" %>
            <core_rt:set var="documentName"><sw360:ProjectName project="${project}"/></core_rt:set>
            <%@include file="/html/utils/includes/usingProjectsTable.jspf" %>
            <%@include file="/html/utils/includes/usingComponentsTable.jspf"%>
            </core_rt:if>
        </form>
        <jsp:include page="/html/projects/includes/searchProjects.jsp" />
        <core_rt:set var="enableSearchForReleasesFromLinkedProjects" value="${true}" scope="request"/>
        <jsp:include page="/html/utils/includes/searchReleases.jsp" />
        <jsp:include page="/html/utils/includes/searchAndSelectUsers.jsp" />
        <jsp:include page="/html/utils/includes/searchUsers.jsp" />

        <core_rt:if test="${not addMode}" >
            <input type="button" id="formSubmit2" value="Update Project" class="addButton">
        </core_rt:if>
        <core_rt:if test="${addMode}" >
            <input type="button" id="formSubmit2" value="Add Project" class="addButton">
        </core_rt:if>
        <input id="cancelEditButton2" type="button" value="Cancel" class="cancelButton">
    </div>
</core_rt:if>

<script>
    require(['jquery', 'modules/sw360Validate', 'modules/confirm' ], function($, sw360Validate, confirm) {

    function cancel() {
        deleteAttachmentsOnCancel();

        var baseUrl = '<%= PortletURLFactoryUtil.create(request, portletDisplay.getId(), themeDisplay.getPlid(), PortletRequest.RENDER_PHASE) %>',
            portletURL = Liferay.PortletURL.createURL(baseUrl)
        <core_rt:if test="${not addMode}">
                .setParameter('<%=PortalConstants.PAGENAME%>', '<%=PortalConstants.PAGENAME_DETAIL%>')
        </core_rt:if>
        <core_rt:if test="${addMode}">
                .setParameter('<%=PortalConstants.PAGENAME%>', '<%=PortalConstants.PAGENAME_VIEW%>')
        </core_rt:if>
                .setParameter('<%=PortalConstants.PROJECT_ID%>', '${project.id}');
        window.location = portletURL.toString();
    }

    function deleteAttachmentsOnCancel() {
        $.ajax({
            type: 'POST',
            url: '<%=deleteAttachmentsOnCancelURL%>',
            cache: false,
            data: {
                "<portlet:namespace/><%=PortalConstants.DOCUMENT_ID%>": "${project.id}"
            },
        });
    }

    function openDeleteDialog() {
        var htmlDialog  = '' + '<div>' +
                          'Do you really want to delete the project <b><sw360:ProjectName project="${project}"/></b> ?' +
                          '<core_rt:if test="${not empty project.linkedProjects or not empty project.releaseIdToUsage or not empty project.attachments}" ><br/><br/>The project <b><sw360:ProjectName project="${project}"/></b> contains<br/><ul></core_rt:if>' +
                          '<core_rt:if test="${not empty project.linkedProjects}" ><li><sw360:out value="${project.linkedProjectsSize}"/> linked projects</li></core_rt:if>' +
                          '<core_rt:if test="${not empty project.releaseIdToUsage}" ><li><sw360:out value="${project.releaseIdToUsageSize}"/> linked releases</li></core_rt:if>'  +
                          '<core_rt:if test="${not empty project.attachments}" ><li><sw360:out value="${project.attachmentsSize}"/> attachments</li></core_rt:if>'  +
                          '<core_rt:if test="${not empty project.linkedProjects or not empty project.releaseIdToUsage or not empty project.attachments}" ></ul></core_rt:if>' +
                          '</div>'+
                          '<div ' + styleAsHiddenIfNeccessary(${project.permissions[DELETE] == true}) + '><hr><label class=\'textlabel stackedLabel\'>Comment your changes</label><textarea id=\'moderationDeleteCommentField\' class=\'moderationCreationComment\' placeholder=\'Comment on request...\'></textarea></div>';
        deleteConfirmed(htmlDialog, deleteProject);
    }

    function deleteProject() {
        var commentText_encoded = btoa($("#moderationDeleteCommentField").val());
        var baseUrl = '<%=deleteURL%>';
        var deleteURL = Liferay.PortletURL.createURL( baseUrl ).setParameter('<%=PortalConstants.MODERATION_REQUEST_COMMENT%>',commentText_encoded);
        window.location.href = deleteURL;
    }

    function focusOnCommentField() {
        $("#moderationRequestCommentField").focus();
        $("#moderationRequestCommentField").select();
    }

    function showCommentField() {
        $("#moderationRequestCommentDialog").show();
        $("#formSubmit, #formSubmit2").attr("disabled","disabled");
        focusOnCommentField();
    }

    function submitModerationRequest() {
        $('#projectEditForm').submit();
    }

    $(document).ready(function() {
        var contextpath = '<%=request.getContextPath()%>',
            deletionMessage;

        $('#moderationRequestCommentSendButton').on('click', submitModerationRequest);
        $('#cancelEditButton, #cancelEditButton2').on('click', cancel);
        $('#deleteProjectButton').on('click', openDeleteDialog);

        sw360Validate.validateWithInvalidHandlerNoIgnore('#projectEditForm');

        $('#formSubmit, #formSubmit2').click(
            function () {
                <core_rt:choose>
                    <core_rt:when test="${addMode || project.permissions[WRITE]}">
                        $('#projectEditForm').submit();
                    </core_rt:when>
                    <core_rt:otherwise>
                        showCommentField();
                    </core_rt:otherwise>
                </core_rt:choose>
            }
        );
    });
});
</script>


