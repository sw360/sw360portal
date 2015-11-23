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
<%@ page import="com.liferay.portlet.PortletURLFactoryUtil" %>
<%@include file="/html/init.jsp"%>

<portlet:defineObjects />
<liferay-theme:defineObjects />

<%@ page import="com.siemens.sw360.datahandler.common.SW360Constants" %>
<%@ page import="com.siemens.sw360.datahandler.thrift.moderation.DocumentType" %>
<%@ page import="com.siemens.sw360.portal.common.PortalConstants" %>
<%@ page import="javax.portlet.PortletRequest" %>


<jsp:useBean id="project" class="com.siemens.sw360.datahandler.thrift.projects.Project" scope="request" />
<jsp:useBean id="documentID" class="java.lang.String" scope="request" />
<jsp:useBean id="usingProjects" type="java.util.Set<com.siemens.sw360.datahandler.thrift.projects.Project>" scope="request"/>

<core_rt:set  var="addMode"  value="${empty project.id}" />
<portlet:actionURL var="updateURL" name="update">
    <portlet:param name="<%=PortalConstants.PROJECT_ID%>" value="${project.id}" />
</portlet:actionURL>


<portlet:actionURL var="deleteURL" name="delete">
    <portlet:param name="<%=PortalConstants.PROJECT_ID%>" value="${project.id}"/>
</portlet:actionURL>



<link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/external/jquery-ui.css">
<script src="<%=request.getContextPath()%>/js/external/jquery-1.11.1.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery.validate.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/additional-methods.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery-ui.min.js"></script>

<core_rt:if test="${not addMode}" >
    <jsp:include page="/html/utils/includes/attachmentsUpload.jsp"/>
    <jsp:include page="/html/utils/includes/attachmentsDelete.jsp" />
</core_rt:if>

<div id="where" class="content1">
    <p class="pageHeader"><span class="pageHeaderBigSpan"><sw360:out value="${project.name}"/></span>
    <core_rt:if test="${not addMode}" >
        <input type="button" class="addButton" onclick="window.location.href='<%=deleteURL%>'"
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
            <%@include file="/html/utils/includes/formAttachments.jsp" %>
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


