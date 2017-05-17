<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  ~ Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
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
<%@ page import="org.eclipse.sw360.datahandler.thrift.components.ComponentType" %>
<%@ page import="org.eclipse.sw360.portal.common.PortalConstants" %>
<%@ page import="javax.portlet.PortletRequest" %>

<portlet:actionURL var="updateReleaseURL" name="updateRelease">
    <portlet:param name="<%=PortalConstants.COMPONENT_ID%>" value="${component.id}"/>
    <portlet:param name="<%=PortalConstants.RELEASE_ID%>" value="${release.id}"/>
</portlet:actionURL>

<portlet:actionURL var="deleteReleaseURL" name="deleteRelease">
    <portlet:param name="<%=PortalConstants.COMPONENT_ID%>" value="${component.id}"/>
    <portlet:param name="<%=PortalConstants.RELEASE_ID%>" value="${release.id}"/>
</portlet:actionURL>

<portlet:actionURL var="deleteAttachmentsOnCancelURL" name='<%=PortalConstants.ATTACHMENT_DELETE_ON_CANCEL%>'>
</portlet:actionURL>

<portlet:resourceURL var="viewVendorURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value="<%=PortalConstants.VIEW_VENDOR%>"/>
</portlet:resourceURL>

<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<c:catch var="attributeNotFoundException">
    <jsp:useBean id="component" class="org.eclipse.sw360.datahandler.thrift.components.Component" scope="request"/>
    <jsp:useBean id="release" class="org.eclipse.sw360.datahandler.thrift.components.Release" scope="request"/>

    <jsp:useBean id="usingProjects" type="java.util.Set<org.eclipse.sw360.datahandler.thrift.projects.Project>"
                 scope="request"/>

    <jsp:useBean id="usingComponents" type="java.util.Set<org.eclipse.sw360.datahandler.thrift.components.Component>" scope="request"/>

    <core_rt:set var="programmingLanguages" value='<%=PortalConstants.PROGRAMMING_LANGUAGES%>'/>
    <core_rt:set var="operatingSystemsAutoC" value='<%=PortalConstants.OPERATING_SYSTEMS%>'/>

    <core_rt:set var="addMode" value="${empty release.id}"/>
    <core_rt:set var="cotsMode" value="<%=component.componentType == ComponentType.COTS%>"/>
</c:catch>
<%@include file="/html/utils/includes/logError.jspf" %>
<core_rt:if test="${empty attributeNotFoundException}">

    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/external/jquery-ui.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/external/jquery-confirm.min.css">
    <!--include jQuery -->
    <script src="<%=request.getContextPath()%>/js/external/jquery-1.11.1.min.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/js/external/jquery.validate.min.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/js/external/additional-methods.min.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/js/external/jquery-confirm.min.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/js/external/jquery-ui.min.js"></script>
    <script src="<%=request.getContextPath()%>/js/loadTags.js"></script>

    <jsp:include page="/html/utils/includes/attachmentsUpload.jsp"/>
    <jsp:include page="/html/utils/includes/attachmentsDelete.jsp" />
    <div id="header"></div>
    <p class="pageHeader"><span class="pageHeaderBigSpan"><sw360:out value="${component.name}"/>: <sw360:ReleaseName release="${release}" /> Edit</span>
        <span class="pull-right">
                   <core_rt:if test="${not addMode}">
                       <input type="button" class="addButton" onclick="deleteConfirmed('' +
                               'Do you really want to delete the release <b><sw360:ReleaseName release="${release}" /></b> ?'  +
                               '<core_rt:if test="${not empty release.releaseIdToRelationship or not empty release.attachments}" ><br/><br/>The release <b><sw360:ReleaseName release="${release}" /></b> contains<br/><ul></core_rt:if>' +
                               '<core_rt:if test="${not empty release.releaseIdToRelationship}" ><li><sw360:out value="${release.releaseIdToRelationshipSize}"/> linked releases</li></core_rt:if>'  +
                               '<core_rt:if test="${not empty release.attachments}" ><li><sw360:out value="${release.attachmentsSize}"/> attachments</li></core_rt:if>'  +
                               '<core_rt:if test="${not empty release.releaseIdToRelationship or not empty release.attachments}" ></ul></core_rt:if>', deleteRelease)"
                              value="Delete  <sw360:ReleaseName release="${release}" /> "
                       <core_rt:if test="${usingComponents.size()>0 or usingProjects.size()>0}"> disabled="disabled" title="Deletion is disabled as the release is used." </core_rt:if>
                       >
                   </core_rt:if>
    </span>
    </p>

    <div id="content">
        <div class="container-fluid">
            <form id="releaseEditForm" name="releaseEditForm" action="<%=updateReleaseURL%>" method="post">
                <div id="myTab" class="row-fluid">
                    <ul class="nav nav-tabs span2">
                        <li><a href="#tab-ReleaseInformation">Summary</a></li>
                        <li><a href="#tab-ReleaseLinks">Linked Releases</a></li>
                        <core_rt:if test="${not addMode}">
                            <li><a href="#tab-ReleaseClearingInformation">Clearing Details</a></li>
                            <li><a href="#tab-ReleaseECCInformation">ECC Details</a></li>
                        </core_rt:if>
                        <li><a href="#tab-Attachments">Attachments</a></li>
                        <core_rt:if test="${cotsMode}">
                            <li><a href="#tab-COTSDetails">Commercial Details</a></li>
                        </core_rt:if>
                    </ul>
                    <div class="tab-content span10">
                        <div id="tab-ReleaseInformation" class="tab-pane">
                            <%@include file="/html/components/includes/releases/editReleaseInformation.jspf" %>
                            <core_rt:set var="keys" value="<%=PortalConstants.RELEASE_ROLES%>"/>
                            <core_rt:set var="mapTitle" value="Additional Roles"/>
                            <core_rt:set var="inputType" value="email"/>
                            <core_rt:set var="inputSubtitle" value="Enter mail address"/>
                            <%@include file="/html/utils/includes/mapEdit.jspf" %>
                            <%@include file="/html/components/includes/releases/editReleaseRepository.jspf" %>
                        </div>
                        <div id="tab-ReleaseLinks">
                            <%@include file="/html/utils/includes/editLinkedReleases.jspf" %>
                        </div>
                        <core_rt:if test="${not addMode}">
                            <div id="tab-ReleaseClearingInformation">
                                <%@include file="/html/components/includes/releases/editReleaseClearingInformation.jspf" %>
                            </div>
                            <div id="tab-ReleaseECCInformation">
                                <%@include file="/html/components/includes/releases/editReleaseECCInformation.jspf" %>
                            </div>
                        </core_rt:if>
                        <div id="tab-Attachments">
                            <%@include file="/html/utils/includes/editAttachments.jsp" %>
                        </div>
                        <core_rt:if test="${cotsMode}">
                            <div id="tab-COTSDetails">
                                <%@include file="/html/components/includes/releases/editCommercialDetails.jspf" %>
                            </div>
                        </core_rt:if>
                    </div>
                </div>
                <core_rt:if test="${not addMode}">
                    <input type="hidden" value="true" name="<portlet:namespace/>clearingInformation">
                    <input type="submit" value="Update Release" class="addButton" >
                </core_rt:if>
                <core_rt:if test="${addMode}">
                    <input type="hidden" value="false" name="<portlet:namespace/>clearingInformation">
                    <input type="submit" value="Add Release" class="addButton" >
                </core_rt:if>
                <input type="button" value="Cancel" onclick="cancel()" class="cancelButton">
            </form>
        </div>
    </div>
    <%@include file="/html/components/includes/vendors/vendorSearchForm.jspf" %>

    <jsp:include page="/html/utils/includes/searchAndSelect.jsp" />
    <jsp:include page="/html/utils/includes/searchUsers.jsp" />
    <jsp:include page="/html/utils/includes/searchLicenses.jsp" />
    <jsp:include page="/html/utils/includes/searchReleasesFromRelease.jsp" />
</core_rt:if>
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
                "<portlet:namespace/><%=PortalConstants.DOCUMENT_ID%>": "${release.id}"
            },
        });
    }

    function validate(){
        alert($('#releaseEditForm').valid());
    }

    function deleteRelease() {
        window.location.href = '<%=deleteReleaseURL%>';
    }

    var contextpath;
    $(document).ready(function () {
        prepareAutocompleteForMultipleHits('programminglanguages', ${programmingLanguages});
        prepareAutocompleteForMultipleHits('op_systems', ${operatingSystemsAutoC});
        contextpath = '<%=request.getContextPath()%>';
        $('#releaseEditForm').validate({
            ignore: [],
            invalidHandler: invalidHandlerShowErrorTab
         });
    });


    //Vendor things
    function fillVendorInfo( vendorInfo) {

        var beforeComma = vendorInfo.substr(0, vendorInfo.indexOf(","));
        var afterComma = vendorInfo.substr(vendorInfo.indexOf(",") + 1);
        fillVendorInfoFields(beforeComma.trim(), afterComma.trim());
    }

   function fillVendorInfoFields(vendorId, vendorName) {
       $('#<%=Release._Fields.VENDOR_ID.toString()%>').val(vendorId);
       $('#<%=Release._Fields.VENDOR_ID.toString()%>Display').val(vendorName);
    }

    function vendorContentFromAjax(id, what, where) {
        jQuery.ajax({
            type: 'POST',
            url: '<%=viewVendorURL%>',
            data: {
                <portlet:namespace/>what: what,
                <portlet:namespace/>where: where
            },
            success: function (data) {
                $('#' + id).html(data);
            }
        });
    }
</script>

