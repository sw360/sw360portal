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
<%@include file="/html/init.jsp" %>

<%@ page import="com.siemens.sw360.portal.common.PortalConstants" %>
<%@ page import="com.siemens.sw360.portal.portlets.Sw360Portlet" %>
<%@ page import="com.siemens.sw360.portal.portlets.projects.ProjectPortlet" %>
<%@ page import="javax.portlet.PortletRequest" %>


<%@ taglib prefix="sw360" uri="/WEB-INF/customTags.tld" %>

<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<jsp:useBean id="component" class="com.siemens.sw360.datahandler.thrift.components.Component" scope="request"/>
<jsp:useBean id="documentID" class="java.lang.String" scope="request"/>
<jsp:useBean id="documentType" class="java.lang.String" scope="request"/>

<jsp:useBean id="usingProjects" type="java.util.Set<com.siemens.sw360.datahandler.thrift.projects.Project>"
             scope="request"/>

<jsp:useBean id="usingComponents" type="java.util.Set<com.siemens.sw360.datahandler.thrift.components.Component>"
             scope="request"/>


<core_rt:set var="softwarePlatformsAutoC" value='<%=PortalConstants.SOFTWARE_PLATFORMS%>'/>

<core_rt:set var="componentDivAddMode" value="${empty component.id}"/>

<portlet:actionURL var="updateComponentURL" name="updateComponent">
    <portlet:param name="<%=PortalConstants.COMPONENT_ID%>" value="${component.id}"/>
</portlet:actionURL>

<portlet:renderURL var="addReleaseURL">
    <portlet:param name="<%=PortalConstants.COMPONENT_ID%>" value="${component.id}"/>
    <portlet:param name="<%=PortalConstants.PAGENAME%>" value="<%=PortalConstants.PAGENAME_EDIT_RELEASE%>"/>
</portlet:renderURL>

<portlet:actionURL var="deleteComponentURL" name="deleteComponent">
    <portlet:param name="<%=PortalConstants.COMPONENT_ID%>" value="${component.id}"/>
</portlet:actionURL>

<portlet:actionURL var="deleteAttachmentsOnCancelURL" name='<%=PortalConstants.ATTACHMENT_DELETE_ON_CANCEL%>'>
</portlet:actionURL>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/external/jquery-ui.css">
<!--include jQuery -->
<script src="<%=request.getContextPath()%>/js/external/jquery-1.11.1.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery.validate.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/additional-methods.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery-ui.min.js"></script>
<script src="<%=request.getContextPath()%>/js/loadTags.js"></script>
<script src="<%=request.getContextPath()%>/js/releaseTools.js"></script>


<core_rt:if test="${not componentDivAddMode}">
    <jsp:include page="/html/utils/includes/attachmentsUpload.jsp"/>
    <jsp:include page="/html/utils/includes/attachmentsDelete.jsp"/>
</core_rt:if>
<div id="where" class="content1">
    <p class="pageHeader"><span class="pageHeaderBigSpan"><sw360:out value="${component.name}"/></span>
        <core_rt:if test="${not componentDivAddMode}">
            <input type="button" class="addButton" onclick="window.location.href='<%=deleteComponentURL%>'"
                   value="Delete <sw360:out value="${component.name}"/> "
                    <core_rt:if test="${usingComponents.size()>0 or usingProjects.size()>0}"> disabled="disabled" title="Deletion is disabled as the component is used." </core_rt:if>
                    >
        </core_rt:if>
    </p>
    <core_rt:if test="${not componentDivAddMode}">
        <core_rt:forEach items="${component.releases}" var="myRelease">
            <p><span onclick="window.location=createDetailURLfromReleaseId( '${myRelease.id}')"
                     class="clickAble"><sw360:ReleaseName release="${myRelease}"/></span></p>
        </core_rt:forEach>
        <input type="button" class="addButton" onclick="window.location.href='<%=addReleaseURL%>'" value="Add Release">
        <br>
        <hr>
        <input type="button" id="formSubmit" value="Update Component" class="addButton">
    </core_rt:if>
    <core_rt:if test="${componentDivAddMode}">
        <input type="button" id="formSubmit" value="Add Component" class="addButton">
    </core_rt:if>
    <input type="button" value="Cancel" onclick="cancel()" class="cancelButton">
</div>

<div id="editField" class="content2">

    <form id="componentEditForm" name="componentEditForm" action="<%=updateComponentURL%>" method="post">
        <%@include file="/html/components/includes/components/editBasicInfo.jspf" %>
        <core_rt:if test="${not componentDivAddMode}">
            <%@include file="/html/utils/includes/editAttachments.jsp" %>
            <%@include file="/html/components/includes/components/usingDocuments.jspf" %>
        </core_rt:if>
    </form>
</div>


<script>
    releaseIdInURL = '<%=PortalConstants.RELEASE_ID%>';
    compIdInURL = '<%=PortalConstants.COMPONENT_ID%>';
    componentId = '${component.id}';
    pageName = '<%=PortalConstants.PAGENAME%>';
    pageDetail = '<%=PortalConstants.PAGENAME_EDIT_RELEASE%>';
    baseUrl = '<%= PortletURLFactoryUtil.create(request, portletDisplay.getId(), themeDisplay.getPlid(), PortletRequest.RENDER_PHASE) %>';

    function cancel() {
        deleteAttachmentsOnCancel();
        var baseUrl = '<%= PortletURLFactoryUtil.create(request, portletDisplay.getId(), themeDisplay.getPlid(), PortletRequest.RENDER_PHASE) %>';
        var portletURL = Liferay.PortletURL.createURL(baseUrl)
                .setParameter('<%=PortalConstants.PAGENAME%>', '<%=PortalConstants.PAGENAME_DETAIL%>')
                .setParameter('<%=PortalConstants.COMPONENT_ID%>', '${component.id}');
        window.location = portletURL.toString();
    }

    function deleteAttachmentsOnCancel() {
        jQuery.ajax({
            type: 'POST',
            url: '<%=deleteAttachmentsOnCancelURL%>',
            cache: false,
            data: {
                "<portlet:namespace/><%=PortalConstants.DOCUMENT_ID%>": "${component.id}"
            },
        });
    }

    var contextpath;
    $(document).ready(function () {
        contextpath = '<%=request.getContextPath()%>';
        prepareAutocompleteForMultipleHits('comp_platforms', ${softwarePlatformsAutoC});
        $('#componentEditForm').validate({
            invalidHandler: invalidHandlerShowErrorTab
        });

        $('#formSubmit').click(
                function () {
                    $('#componentEditForm').submit();
                }
        );
    });
</script>





