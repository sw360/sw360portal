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
<%@ page import="com.siemens.sw360.portal.common.PortalConstants" %>
<%@include file="/html/init.jsp"%>


<portlet:defineObjects />
<liferay-theme:defineObjects />

<%@ page import="javax.portlet.PortletRequest" %>
<%@ page import="com.liferay.portlet.PortletURLFactoryUtil" %>
<%@ page import="com.liferay.portal.util.PortalUtil" %>
<%@ page import="com.siemens.sw360.datahandler.thrift.vendors.Vendor" %>


<jsp:useBean id="vendor" class="com.siemens.sw360.datahandler.thrift.vendors.Vendor" scope="request" />

<jsp:useBean id="releaseList" type="java.util.List<com.siemens.sw360.datahandler.thrift.components.Release>"  scope="request"/>
<jsp:useBean id="documentID" class="java.lang.String" scope="request" />

<core_rt:set  var="addMode"  value="${empty vendor.id}" />

<portlet:actionURL var="updateURL" name="updateVendor">
    <portlet:param name="<%=PortalConstants.VENDOR_ID%>" value="${vendor.id}" />
</portlet:actionURL>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/external/jquery-ui.css">
<script src="<%=request.getContextPath()%>/js/external/jquery-1.11.1.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery.validate.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/additional-methods.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery-ui.min.js"></script>


<div id="where" class="content1">
    <p class="pageHeader"><span class="pageHeaderBigSpan"><sw360:out value="${vendor.fullname}"/></span>

    </p>
</div>

<div id="editField" class="content2">

    <form  id="vendorEditForm" name="vendorEditForm" action="<%=updateURL%>" method="post" >
        <table class="table info_table" id="VendorEdit" title="Edit Vendor information">
            <thead>
            <tr>
                <th colspan="3" class="headlabel">Edit Vendor</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td width="30%">
                    <label class="textlabel stackedLabel" for="vendorFullname">Fullname</label>
                    <input id="vendorFullname" type="text" class="toplabelledInput" placeholder="Enter vendor full name" name="<portlet:namespace/><%=Vendor._Fields.FULLNAME%>"
                           value="<sw360:out value="${vendor.fullname}"/>" />
                </td>

                <td width="30%">
                    <label class="textlabel stackedLabel" for="vendorShortname">Shortname</label>
                    <input id="vendorShortname" type="text" class="toplabelledInput" placeholder="Enter vendor short name" name="<portlet:namespace/><%=Vendor._Fields.SHORTNAME%>"
                           value="<sw360:out value="${vendor.shortname}"/>" />
                </td>

                <td width="30%">
                    <label class="textlabel stackedLabel" for="vendorURL">URL</label>
                    <input id="vendorURL" type="text" class="toplabelledInput" placeholder="Enter vendor url" name="<portlet:namespace/><%=Vendor._Fields.URL%>"
                           value="<sw360:out value="${vendor.url}"/>" />
                </td>
            </tr>

            </tbody>
        </table>
        <core_rt:if test="${not addMode}" >
            <input type="submit" value="Update Vendor" class="addButton">
            <input type="button" value="Cancel" onclick="cancel()" class="cancelButton">
        </core_rt:if>
        <core_rt:if test="${addMode}" >
            <input type="submit" value="Add Vendor" class="addButton">
            <input type="button" value="Cancel" onclick="cancel()" class="cancelButton">
        </core_rt:if>
    </form>

    <core_rt:if test="${releaseList.size() > 0}" >
        <p>Used by the following release(s)</p>
        <table style="padding-left: 3px; padding-right: 3px"> <tr>
        <core_rt:forEach var="release" items="${releaseList}" varStatus="loop">
            <td><sw360:DisplayReleaseLink release="${release}" /></td>
            <core_rt:if test="${loop.count > 0 and  loop.count %  4 == 0}" ></tr> <tr> </core_rt:if>
        </core_rt:forEach>
        </tr>
        </table>
    </core_rt:if>

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
                .setParameter('<%=PortalConstants.VENDOR_ID%>','${vendor.id}');
        window.location = portletURL.toString();
    }

    var contextpath;
    $( document ).ready(function() {
        contextpath = '<%=request.getContextPath()%>';
        $('#vendorEditForm').validate({
            ignore: [],
            invalidHandler: invalidHandlerShowErrorTab
        });
    });


</script>
