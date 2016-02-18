<%--
  ~ Copyright Siemens AG, 2016. Part of the SW360 Portal Project.
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
<%@ page import="com.liferay.portlet.PortletURLFactoryUtil" %>
<%@ page import="javax.portlet.PortletRequest" %>
<%@ page import="java.util.ArrayList" %>

<%@include file="/html/init.jsp"%>

<portlet:defineObjects />
<liferay-theme:defineObjects />

<jsp:useBean id="licenseDetail" class="com.siemens.sw360.datahandler.thrift.licenses.License" scope="request" />
<jsp:useBean id="licenseTypeChoice" class="java.util.ArrayList" scope="request" />

<core_rt:set  var="addMode"  value="${empty licenseDetail.id}" />

<portlet:actionURL var="updateURL" name="update">
    <portlet:param name="<%=PortalConstants.LICENSE_ID%>" value="${licenseDetail.id}" />
</portlet:actionURL>
<portlet:actionURL var="deleteURL" name="delete">
    <portlet:param name="<%=PortalConstants.LICENSE_ID%>" value="${licenseDetail.id}"/>
</portlet:actionURL>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/external/jquery-ui.css">
<script src="<%=request.getContextPath()%>/js/external/jquery-1.11.1.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery.validate.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/additional-methods.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery-ui.min.js"></script>

<div id="where" class="content1">
    <p class="pageHeader"><span class="pageHeaderBigSpan"><sw360:out value="${licenseDetail.shortname}"/></span>
        <core_rt:if test="${not addMode}" >
            <input type="button" class="addButton" onclick="window.location.href='<%=deleteURL%>'"
                   value="Delete <sw360:LicenseName license="${licenseDetail}"/>"
            >
        </core_rt:if>
    </p>
    <core_rt:if test="${not addMode}" >
        <input type="button" id="formSubmit" value="Update License" class="addButton">
        <input type="button" value="Cancel" onclick="cancel()" class="cancelButton">
    </core_rt:if>
    <core_rt:if test="${addMode}" >
        <input type="button" id="formSubmit" value="Add License" class="addButton">
        <input type="button" value="Cancel" onclick="cancel()" class="cancelButton">
    </core_rt:if>
</div>

<div id="editField" class="content2">

    <form  id="licenseEditForm" name="licenseEditForm" action="<%=updateURL%>" method="post" >
        <%@include file="/html/licenses/includes/editDetailSummary.jspf"%>
        <%@include file="/html/licenses/includes/editDetailText.jspf"%>
    </form>
</div>





<script>
    var Y = YUI().use(
            'aui-tabview',
            function(Y) {
                new Y.TabView(
                        {
                            srcNode: '#myTab',
                            stacked: true,
                            type: 'tab'
                        }
                ).render();
            }

    );

    function editLicense() {
        window.location ='<portlet:renderURL ><portlet:param name="<%=PortalConstants.LICENSE_ID%>" value="${licenseDetail.id}"/><portlet:param name="<%=PortalConstants.PAGENAME%>" value="<%=PortalConstants.PAGENAME_EDIT%>"/></portlet:renderURL>'
    }

    function cancel() {
        var baseUrl = '<%= PortletURLFactoryUtil.create(request, portletDisplay.getId(), themeDisplay.getPlid(), PortletRequest.RENDER_PHASE) %>';
        var portletURL = Liferay.PortletURL.createURL( baseUrl )
                <core_rt:if test="${not addMode}" >
                .setParameter('<%=PortalConstants.PAGENAME%>','<%=PortalConstants.PAGENAME_DETAIL%>')
                </core_rt:if>
                <core_rt:if test="${addMode}" >
                .setParameter('<%=PortalConstants.PAGENAME%>','<%=PortalConstants.PAGENAME_VIEW%>')
                </core_rt:if>

                .setParameter('<%=PortalConstants.LICENSE_ID%>','${licenseDetail.id}');
        window.location = portletURL.toString();
    }

    var contextpath;
    $( document ).ready(function() {
        contextpath = '<%=request.getContextPath()%>';
        $('#licenseEditForm').validate({
            ignore: [],
            invalidHandler: invalidHandlerShowErrorTab
        });

        $('#formSubmit').click(
                function() {
                    $('#licenseEditForm').submit();
                }
        );
    });
</script>
