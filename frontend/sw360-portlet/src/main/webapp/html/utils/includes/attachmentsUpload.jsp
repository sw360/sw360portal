<%--
  ~ Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
  ~
  ~ SPDX-License-Identifier: EPL-1.0
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
  --%>

<%@include file="/html/init.jsp" %>
<%@ page import="org.eclipse.sw360.portal.common.PortalConstants" %>


<%@ taglib prefix="sw360" uri="/WEB-INF/customTags.tld" %>

<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<jsp:useBean id="documentID" class="java.lang.String" scope="request"/>
<jsp:useBean id="documentType" class="java.lang.String" scope="request"/>

<portlet:resourceURL var="newAttachmentAjaxURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value='<%=PortalConstants.ATTACHMENT_RESERVE_ID%>'/>
    <portlet:param name="<%=PortalConstants.DOCUMENT_TYPE%>" value="${documentType}"/>
    <portlet:param name="<%=PortalConstants.DOCUMENT_ID%>" value="${documentID}"/>
</portlet:resourceURL>

<portlet:resourceURL var="uploadPartAjaxURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value='<%=PortalConstants.ATTACHMENT_UPLOAD%>'/>
    <portlet:param name="<%=PortalConstants.DOCUMENT_TYPE%>" value="${documentType}"/>
    <portlet:param name="<%=PortalConstants.DOCUMENT_ID%>" value="${documentID}"/>
</portlet:resourceURL>

<portlet:resourceURL var="cancelAttachmentAjaxURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value='<%=PortalConstants.ATTACHMENT_CANCEL%>'/>
    <portlet:param name="<%=PortalConstants.DOCUMENT_TYPE%>" value="${documentType}"/>
</portlet:resourceURL>

<portlet:resourceURL var="attachmentListAjaxURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value='<%=PortalConstants.ATTACHMENT_LIST%>'/>
    <portlet:param name="<%=PortalConstants.DOCUMENT_ID%>" value="${documentID}"/>
    <portlet:param name="<%=PortalConstants.DOCUMENT_TYPE%>" value="${documentType}"/>
</portlet:resourceURL>

<portlet:resourceURL var="attachmentLinkToAjaxURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value='<%=PortalConstants.ATTACHMENT_LINK_TO%>'/>
    <portlet:param name="<%=PortalConstants.DOCUMENT_ID%>" value="${documentID}"/>
    <portlet:param name="<%=PortalConstants.DOCUMENT_TYPE%>" value="${documentType}"/>
</portlet:resourceURL>

<div id="fileupload-form" title="Upload Attachment" style="display: none; background-color: #ffffff;">
    <core_rt:choose>
        <core_rt:when test="${empty documentID}">
            <span>Cannot add attachments before saving the document</span>
        </core_rt:when>
        <core_rt:otherwise>
            <div class="lfr-dynamic-uploader">
                <div class="lfr-upload-container">
                    <div id="fileupload-drop" class="upload-target">
                        <span>Drop a File Here</span>
                        <br/>
                        Or
                        <br/>
                        <button id="fileupload-browse">Browse</button>
                    </div>
                    <div id="fileupload-files" class="upload-list"></div>
                </div>
            </div>
        </core_rt:otherwise>
    </core_rt:choose>
</div>

<script>
    function showAddAttachmentDialog() {
        openDialog('fileupload-form', 'fileupload-files');
    }
</script>


<core_rt:if test="${not empty documentID}">
    <script src="<%=request.getContextPath()%>/webjars/resumable.js/1.0.3/resumable.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/js/resumableAttachments.js" type="text/javascript"></script>

    <script>
        var r = false;
        var ra = false;

        function linkAttachment(attachmentId) {
            return $.ajax({
                url: '<%=attachmentLinkToAjaxURL%>',
                cache: false,
                data: {<portlet:namespace/>attachmentId: attachmentId}
            }).done(function (data) {
                $('#noAttachmentsRow').hide();
                $('#attachmentInfo > tbody').append(data);
            }).fail(function () {
                cancelAttachment(attachmentId);
            });
        }

        function cancelAttachment(attachmentId) {
            $.ajax({
                url: '<%=cancelAttachmentAjaxURL%>',
                cache: false,
                data: {
                    <portlet:namespace/>attachmentId: attachmentId
                }
            });
        }

        function getAttachmentIdPromise(file) {
            return $.ajax({
                url: '<%=newAttachmentAjaxURL%>',
                cache: false,
                dataType: 'text',
                data: {
                    <portlet:namespace/>fileName: file.fileName || file.name
                }
            });
        }

        $(function () {
            r = new Resumable({
                target: '<%=uploadPartAjaxURL%>',
                parameterNamespace: '<portlet:namespace/>',
                simultaneousUploads: 1,
                generateUniqueIdentifier: getAttachmentIdPromise,
                chunkRetryInterval: 2000,
                maxChunkRetries: 3
            });

            ra = new ResumableAttachments(r, $('#fileupload-files'));

            r.assignBrowse($('#fileupload-browse')[0]);
            r.assignDrop($('#fileupload-drop')[0]);

            r.on('fileAdded', ra.addFile);
            r.on('fileProgress', function (file) {
                ra.drawFileProgress(file);
            });
            r.on('fileSuccess', function (file) {
                var attachmentId = file.uniqueIdentifier;

                linkAttachment(attachmentId).then(function () {
                    ra.removeFile(file);
                    if (ra.isEmpty()) {
                        closeOpenDialogs();
                    }
                });
            });

            r.on('fileError', function (file) {
                alert("I could not upload the file: " + file.fileName);
            });

            ra.on('fileCancel', function (file) {
                var attachmentId = file.uniqueIdentifier;

                if (attachmentId) {
                    cancelAttachment(attachmentId);
                }
            });
        });

    </script>
</core_rt:if>
