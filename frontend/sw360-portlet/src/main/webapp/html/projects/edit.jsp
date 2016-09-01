<%--
  ~ Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
  --%>
<%@ page import="com.liferay.portlet.PortletURLFactoryUtil" %>
<%@include file="/html/init.jsp"%>
<%-- the following is needed by liferay to display error messages--%>
<%@include file="/html/utils/includes/errorKeyToMessage.jspf"%>
<portlet:defineObjects />
<liferay-theme:defineObjects />

<%@ page import="com.siemens.sw360.datahandler.common.SW360Constants" %>
<%@ page import="com.siemens.sw360.datahandler.thrift.moderation.DocumentType" %>
<%@ page import="com.siemens.sw360.portal.common.PortalConstants" %>
<%@ page import="javax.portlet.PortletRequest" %>


<jsp:useBean id="project" class="com.siemens.sw360.datahandler.thrift.projects.Project" scope="request" />
<jsp:useBean id="documentID" class="java.lang.String" scope="request" />
<jsp:useBean id="usingProjects" type="java.util.Set<com.siemens.sw360.datahandler.thrift.projects.Project>" scope="request"/>
<jsp:useBean id="projectList" type="java.util.List<com.siemens.sw360.datahandler.thrift.projects.ProjectLink>"  scope="request"/>
<jsp:useBean id="releaseList" type="java.util.List<com.siemens.sw360.datahandler.thrift.components.ReleaseLink>"  scope="request"/>
<jsp:useBean id="attachments" type="java.util.Set<com.siemens.sw360.datahandler.thrift.attachments.Attachment>" scope="request"/>

<core_rt:set  var="addMode"  value="${empty project.id}" />
<portlet:actionURL var="updateURL" name="update">
    <portlet:param name="<%=PortalConstants.PROJECT_ID%>" value="${project.id}" />
</portlet:actionURL>

<portlet:actionURL var="deleteAttachmentsOnCancelURL" name='<%=PortalConstants.ATTACHMENT_DELETE_ON_CANCEL%>'>
</portlet:actionURL>

<portlet:actionURL var="deleteURL" name="delete">
    <portlet:param name="<%=PortalConstants.PROJECT_ID%>" value="${project.id}"/>
</portlet:actionURL>



<link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/external/jquery-ui.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/external/jquery-confirm.min.css">
<script src="<%=request.getContextPath()%>/js/external/jquery-1.11.1.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery.validate.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/additional-methods.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery-confirm.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery-ui.min.js"></script>

<core_rt:if test="${not addMode}" >
    <jsp:include page="/html/utils/includes/attachmentsUpload.jsp"/>
    <jsp:include page="/html/utils/includes/attachmentsDelete.jsp" />
</core_rt:if>

<div id="where" class="content1">
    <p class="pageHeader"><span class="pageHeaderBigSpan"><sw360:out value="${project.name}"/></span>
    <core_rt:if test="${not addMode}" >
        <input type="button" class="addButton" onclick="deleteConfirmed('' +
                'Do you really want to delete the project <b><sw360:ProjectName project="${project}"/></b> ?'  +
                '<core_rt:if test="${not empty project.linkedProjects or not empty project.releaseIdToUsage or not empty project.attachments}" ><br/><br/>The project <b><sw360:ProjectName project="${project}"/></b> contains<br/><ul></core_rt:if>' +
                '<core_rt:if test="${not empty project.linkedProjects}" ><li><sw360:out value="${project.linkedProjectsSize}"/> linked projects</li></core_rt:if>'  +
                '<core_rt:if test="${not empty project.releaseIdToUsage}" ><li><sw360:out value="${project.releaseIdToUsageSize}"/> linked releases</li></core_rt:if>'  +
                '<core_rt:if test="${not empty project.attachments}" ><li><sw360:out value="${project.attachmentsSize}"/> attachments</li></core_rt:if>'  +
                '<core_rt:if test="${not empty project.linkedProjects or not empty project.releaseIdToUsage or not empty project.attachments}" ></ul></core_rt:if>', deleteProject)"
                   value="Delete <sw360:ProjectName project="${project}"/>"
                    <core_rt:if test="${ usingProjects.size()>0}"> disabled="disabled" title="Deletion is disabled as the project is used." </core_rt:if>
                >
    </core_rt:if>
    </p>
    <core_rt:if test="${not addMode}" >
        <input type="button" id="formSubmit" value="Update Project" class="addButton">
        <input type="button" value="Cancel" onclick="cancel()" class="cancelButton">
    </core_rt:if>
    <core_rt:if test="${addMode}" >
        <input type="button" id="formSubmit" value="Add Project" class="addButton">
        <input type="button" value="Cancel" onclick="cancel()" class="cancelButton">
    </core_rt:if>
</div>

<div id="editField" class="content2">

    <form  id="projectEditForm" name="projectEditForm" action="<%=updateURL%>" method="post" >
        <%@include file="/html/projects/includes/projects/basicInfo.jspf" %>
        <%@include file="/html/projects/includes/linkedProjectsEdit.jspf" %>
        <%@include file="/html/utils/includes/linkedReleasesEdit.jspf" %>
        <core_rt:if test="${not addMode}" >
            <%@include file="/html/utils/includes/editAttachments.jsp" %>
            <%@include file="/html/projects/includes/projects/usingProjects.jspf" %>

        </core_rt:if>
    </form>
    <jsp:include page="/html/projects/includes/searchProjects.jsp" />
    <jsp:include page="/html/utils/includes/searchReleases.jsp" />
    <jsp:include page="/html/utils/includes/searchAndSelect.jsp" />
    <jsp:include page="/html/utils/includes/searchUsers.jsp" />
</div>

<script>
    function cancel() {
        deleteAttachmentsOnCancel();
        var baseUrl = '<%= PortletURLFactoryUtil.create(request, portletDisplay.getId(), themeDisplay.getPlid(), PortletRequest.RENDER_PHASE) %>';
        var portletURL = Liferay.PortletURL.createURL( baseUrl )
<core_rt:if test="${not addMode}" >
                .setParameter('<%=PortalConstants.PAGENAME%>','<%=PortalConstants.PAGENAME_DETAIL%>')
</core_rt:if>
<core_rt:if test="${addMode}" >
                .setParameter('<%=PortalConstants.PAGENAME%>','<%=PortalConstants.PAGENAME_VIEW%>')
</core_rt:if>

                .setParameter('<%=PortalConstants.PROJECT_ID%>','${project.id}');
        window.location = portletURL.toString();
    }

    function deleteAttachmentsOnCancel() {
        jQuery.ajax({
            type: 'POST',
            url: '<%=deleteAttachmentsOnCancelURL%>',
            cache: false,
            data: {
                "<portlet:namespace/><%=PortalConstants.DOCUMENT_ID%>": "${project.id}"
            },
        });
    }

    function deleteProject() {
        window.location.href = '<%=deleteURL%>';
    }

    var contextpath;
    $( document ).ready(function() {
        contextpath = '<%=request.getContextPath()%>';
        $('#projectEditForm').validate({
            ignore: [],
            invalidHandler: invalidHandlerShowErrorTab
        });

        $('#formSubmit').click(
                function() {
                    $('#projectEditForm').submit();
                }
        );
    });
</script>


