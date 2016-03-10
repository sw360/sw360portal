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

<%@include file="/html/init.jsp" %>
<%@ page import="com.siemens.sw360.portal.common.PortalConstants" %>

<%@ taglib prefix="sw360" uri="/WEB-INF/customTags.tld" %>

<jsp:useBean id="documentID" class="java.lang.String" scope="request"/>
<jsp:useBean id="documentType" class="java.lang.String" scope="request"/>
<portlet:resourceURL var="deleteAjaxURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value='<%=PortalConstants.ATTACHMENT_UNLINK_AND_DELETE%>'/>
    <portlet:param name="<%=PortalConstants.DOCUMENT_ID%>" value="${documentID}"/>
    <portlet:param name="<%=PortalConstants.DOCUMENT_TYPE%>" value="${documentType}"/>
</portlet:resourceURL>

<script>

    function deleteAttachment(rowId, attachmentId) {
        if (confirm("Do you really want to delete this attachment?")) {

            jQuery.ajax({
                type: 'POST',
                url: '<%=deleteAjaxURL%>',
                cache: false,
                data: {
                    <portlet:namespace/>attachmentId: attachmentId
                },
                success: function (data) {
                    if(data.result == 'SUCCESS') {
                        $('#' + rowId).remove();
                    }
                    else if(data.result == 'SENT_TO_MODERATOR') {
                        alert("A moderation request was sent to remove the attachment.");
                    }
                    else {
                        alert("I could not remove the attachment!");
                    }
                },
                error: function () {
                    alert("I could not remove the attachment!");
                }
            });
        }
    }

</script>
