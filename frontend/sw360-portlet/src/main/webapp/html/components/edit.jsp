<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  ~ Copyright Siemens AG, 2013-2016. Part of the SW360 Portal Project.
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
  --%>

<%@ page import="com.liferay.portlet.PortletURLFactoryUtil" %>
<%@include file="/html/init.jsp" %>
<%-- the following is needed by liferay to display error messages--%>
<%@include file="/html/utils/includes/errorKeyToMessage.jspf"%>

<%@ page import="org.eclipse.sw360.portal.common.PortalConstants" %>
<%@ page import="org.eclipse.sw360.portal.portlets.Sw360Portlet" %>
<%@ page import="org.eclipse.sw360.portal.portlets.projects.ProjectPortlet" %>
<%@ page import="javax.portlet.PortletRequest" %>

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

<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<c:catch var="attributeNotFoundException">
    <jsp:useBean id="component" class="org.eclipse.sw360.datahandler.thrift.components.Component" scope="request"/>
    <jsp:useBean id="documentID" class="java.lang.String" scope="request"/>
    <jsp:useBean id="documentType" class="java.lang.String" scope="request"/>

    <jsp:useBean id="usingProjects" type="java.util.Set<org.eclipse.sw360.datahandler.thrift.projects.Project>"
                 scope="request"/>

    <jsp:useBean id="usingComponents" type="java.util.Set<org.eclipse.sw360.datahandler.thrift.components.Component>"
                 scope="request"/>

</c:catch>
<core_rt:if test="${empty attributeNotFoundException}">
    <core_rt:set var="softwarePlatformsAutoC" value='<%=PortalConstants.SOFTWARE_PLATFORMS%>'/>
    <core_rt:set var="componentCategoriesAutocomplete" value='<%=PortalConstants.COMPONENT_CATEGORIES%>'/>

    <core_rt:set var="componentDivAddMode" value="${empty component.id}"/>

    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/external/jquery-ui.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/external/jquery-confirm.min.css">
    <!--include jQuery -->
    <script src="<%=request.getContextPath()%>/js/external/jquery-1.11.1.min.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/js/external/jquery.validate.min.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/js/external/additional-methods.min.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/js/external/jquery-ui.min.js"></script>
    <script src="<%=request.getContextPath()%>/js/external/jquery-confirm.min.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/js/loadTags.js"></script>
    <script src="<%=request.getContextPath()%>/js/releaseTools.js"></script>


    <core_rt:if test="${not componentDivAddMode}">
        <jsp:include page="/html/utils/includes/attachmentsUpload.jsp"/>
        <jsp:include page="/html/utils/includes/attachmentsDelete.jsp"/>
    </core_rt:if>
    <div id="where" class="content1">
        <p class="pageHeader"><span class="pageHeaderBigSpan"><sw360:out value="${component.name}"/></span>
            <core_rt:if test="${not componentDivAddMode}">
                <input type="button" class="addButton" onclick="deleteConfirmed('' +
                        'Do you really want to delete the component <b><sw360:out value="${component.name}"/></b> ?'  +
                        '<core_rt:if test="${not empty component.attachments}" ><br/><br/>The component <b><sw360:out value="${component.name}"/></b>contains<br/><ul><li><sw360:out value="${component.attachmentsSize}"/> attachments</li></ul></core_rt:if>'
                        , deleteComponent)"
                       value="Delete <sw360:out value="${component.name}"/> "
                <core_rt:if test="${usingComponents.size()>0 or usingProjects.size()>0}"> disabled="disabled" title="Deletion is disabled as the component is used." </core_rt:if>
                <core_rt:if test="${component.releasesSize>0}"> disabled="disabled" title="Deletion is disabled as the component contains releases." </core_rt:if>
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
            <core_rt:set var="documentName"><sw360:out value='${component.name}'/></core_rt:set>
            <%@include file="/html/utils/includes/usingProjectsTable.jspf" %>
            <%@include file="/html/utils/includes/usingComponentsTable.jspf"%>
            </core_rt:if>
        </form>
    </div>

    <jsp:include page="/html/utils/includes/searchAndSelect.jsp" />
    <jsp:include page="/html/utils/includes/searchUsers.jsp" />
</core_rt:if>
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
        var portletURL = Liferay.PortletURL.createURL(baseUrl);
<core_rt:choose>
    <core_rt:when test="${not empty component.id}">
        portletURL.setParameter('<%=PortalConstants.PAGENAME%>', '<%=PortalConstants.PAGENAME_DETAIL%>')
                .setParameter('<%=PortalConstants.COMPONENT_ID%>', '${component.id}');
    </core_rt:when>
    <core_rt:otherwise>
        portletURL.setParameter('<%=PortalConstants.PAGENAME%>', '<%=PortalConstants.PAGENAME_VIEW%>')
    </core_rt:otherwise>
</core_rt:choose>
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

    function deleteComponent() {
        window.location.href = '<%=deleteComponentURL%>';
    }


    var contextpath;
    $(document).ready(function () {
        contextpath = '<%=request.getContextPath()%>';
        prepareAutocompleteForMultipleHits('comp_platforms', ${softwarePlatformsAutoC});
        prepareAutocompleteForMultipleHits('comp_categories', ${componentCategoriesAutocomplete});
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





