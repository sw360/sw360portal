<%--
  ~ Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
  --%>

<%@include file="/html/init.jsp" %>
<%@ page import="org.eclipse.sw360.portal.common.PortalConstants" %>

<%@ taglib prefix="sw360" uri="/WEB-INF/customTags.tld" %>

<jsp:useBean id="documentID" class="java.lang.String" scope="request"/>
<jsp:useBean id="documentType" class="java.lang.String" scope="request"/>
<portlet:resourceURL var="deleteAjaxURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value='<%=PortalConstants.ATTACHMENT_UNLINK_AND_DELETE%>'/>
    <portlet:param name="<%=PortalConstants.DOCUMENT_ID%>" value="${documentID}"/>
    <portlet:param name="<%=PortalConstants.DOCUMENT_TYPE%>" value="${documentType}"/>
</portlet:resourceURL>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/external/jquery-confirm.min.css">
<script src="<%=request.getContextPath()%>/js/external/jquery-confirm.min.js" type="text/javascript"></script>
<script>
    function deleteAttachment(rowId, attachmentId) {

        function deleteAttachmentInternal() {
            $('#' + rowId).remove();
        }

        deleteConfirmed("Do you really want to delete this attachment?", deleteAttachmentInternal);
    }
</script>
