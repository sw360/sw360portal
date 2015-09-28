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

<%@ page import="com.siemens.sw360.datahandler.common.ThriftEnumUtils" %>
<%@ page import="com.siemens.sw360.portal.common.PortalConstants" %>
<%@ page import="com.siemens.sw360.portal.portlets.Sw360Portlet" %>
<%@ page import="com.siemens.sw360.portal.portlets.projects.ProjectPortlet" %>
<%@ page import="javax.portlet.PortletRequest" %>

<%@ taglib prefix="sw360" uri="/WEB-INF/customTags.tld" %>

<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<jsp:useBean id="component" class="com.siemens.sw360.datahandler.thrift.components.Component" scope="request"/>
<jsp:useBean id="release" class="com.siemens.sw360.datahandler.thrift.components.Release" scope="request"/>

<jsp:useBean id="usingProjects" type="java.util.Set<com.siemens.sw360.datahandler.thrift.projects.Project>"
             scope="request"/>

<jsp:useBean id="usingComponents" type="java.util.Set<com.siemens.sw360.datahandler.thrift.components.Component>" scope="request"/>

<core_rt:set var="programmingLanguages" value='<%=PortalConstants.PROGRAMMING_LANGUAGES%>'/>
<core_rt:set var="operatingSystemsAutoC" value='<%=PortalConstants.OPERATING_SYSTEMS%>'/>

<core_rt:set var="addMode" value="${empty release.id}"/>


<portlet:actionURL var="updateReleaseURL" name="updateRelease">
    <portlet:param name="<%=PortalConstants.COMPONENT_ID%>" value="${component.id}"/>
    <portlet:param name="<%=PortalConstants.RELEASE_ID%>" value="${release.id}"/>
</portlet:actionURL>

<portlet:actionURL var="deleteReleaseURL" name="deleteRelease">
    <portlet:param name="<%=PortalConstants.COMPONENT_ID%>" value="${component.id}"/>
    <portlet:param name="<%=PortalConstants.RELEASE_ID%>" value="${release.id}"/>
</portlet:actionURL>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/external/jquery-ui.css">
<!--include jQuery -->
<script src="<%=request.getContextPath()%>/js/external/jquery-1.11.1.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery.validate.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/additional-methods.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery-ui.min.js"></script>
<script src="<%=request.getContextPath()%>/js/loadTags.js"></script>

<jsp:include page="/html/utils/includes/attachmentsUpload.jsp"/>
<jsp:include page="/html/utils/includes/attachmentsDelete.jsp" />
<div id="header"></div>
<p class="pageHeader"><span class="pageHeaderBigSpan"><sw360:out value="${component.name}"/>: <sw360:ReleaseName release="${release}" /> Edit</span>
        <span class="pull-right">
                   <core_rt:if test="${not addMode}">
                       <input type="button" class="addButton" onclick="window.location.href='<%=deleteReleaseURL%>'"
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
                    <li><a href="#tab-ReleaseInformation">Release Information</a></li>
                    <%--<li><a href="#tab-Vendor">Vendor</a></li>--%>
                    <li><a href="#tab-ReleaseRepository">Release Repository</a></li>
                    <li><a href="#tab-ReleaseLinks">Release Links</a></li>
                    <core_rt:if test="${not addMode}">
                        <li><a href="#tab-ReleaseClearingInformation">Release Clearing Information</a></li>
                    </core_rt:if>
                    <li><a href="#tab-Attachments">Attachments</a></li>
                    <li><a href="#tab-UsingDocs">Using Documents</a></li>
                </ul>
                <div class="tab-content span10">
                    <div id="tab-ReleaseInformation" class="tab-pane">
                        <%@include file="/html/components/includes/releases/editReleaseInformation.jspf" %>
                    </div>
                    <div id="tab-ReleaseRepository">
                        <%@include file="/html/components/includes/releases/editReleaseRepository.jspf" %>
                    </div>
                    <div id="tab-ReleaseLinks">
                        <%@include file="/html/utils/includes/linkedReleaseRelationsEdit.jspf" %>
                    </div>
                    <core_rt:if test="${not addMode}">
                        <div id="tab-ReleaseClearingInformation">
                            <%@include file="/html/components/includes/releases/editReleaseClearingInformation.jspf" %>
                        </div>
                    </core_rt:if>
                    <div id="tab-Attachments">
                        <%@include file="/html/utils/includes/formAttachments.jsp" %>
                    </div>
                    <div id="tab-UsingDocs">
                        <%@include file="/html/components/includes/releases/usingDocsTable.jspf" %>
                    </div>
                </div>
            </div>
            <%--<input type="button" value="Validate" onclick="validate()">--%>
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

<portlet:resourceURL var="addVendorURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value="<%=PortalConstants.ADD_VENDOR%>"/>
</portlet:resourceURL>

<portlet:resourceURL var="viewVendorURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value="<%=PortalConstants.VIEW_VENDOR%>"/>
</portlet:resourceURL>

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
        var baseUrl = '<%= PortletURLFactoryUtil.create(request, portletDisplay.getId(), themeDisplay.getPlid(), PortletRequest.RENDER_PHASE) %>';
        var portletURL = Liferay.PortletURL.createURL(baseUrl)
                .setParameter('<%=PortalConstants.PAGENAME%>', '<%=PortalConstants.PAGENAME_DETAIL%>')
                .setParameter('<%=PortalConstants.COMPONENT_ID%>', '${component.id}');
        window.location = portletURL.toString();
    }

    function validate(){
        alert($('#releaseEditForm').valid());
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

    function addVendor(){
        openDialog('add-vendor-form', 'addVendorFullName');
    }

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


    function submitAddVendor(fullnameId, shortnameId, urlId) {

        jQuery.ajax({
            type: 'POST',
            url: '<%=addVendorURL%>',
            data: {
                <portlet:namespace/>FULLNAME: $('#' + fullnameId).val(),
                <portlet:namespace/>SHORTNAME: $('#' + shortnameId).val(),
                <portlet:namespace/>URL: $('#' + urlId).val()

            },
            success: function (data) {
                closeOpenDialogs();
                fillVendorInfoFields(data.id,$('#' + fullnameId).val() );
            }
        });

    }

</script>

