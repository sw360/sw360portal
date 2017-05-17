<%--
  ~ Copyright Siemens AG, 2013-2015.
  - Copyright Bosch Software Innovations GmbH, 2017.
  - Part of the SW360 Portal Project.
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

<portlet:resourceURL var="addRemoteAttachmentAjaxURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value='<%=PortalConstants.REMOTE_ATTACHMENT_ADD_TO%>'/>
    <portlet:param name="<%=PortalConstants.DOCUMENT_ID%>" value="${documentID}"/>
    <portlet:param name="<%=PortalConstants.DOCUMENT_TYPE%>" value="${documentType}"/>
</portlet:resourceURL>

<div id="fileupload-form" title="Add Attachment" style="display: none; background-color: #ffffff;">
    <core_rt:choose>
        <core_rt:when test="${empty documentID}">
            <span>Cannot add attachments before saving the document</span>
        </core_rt:when>
        <core_rt:otherwise>
            <div class="container-fluid">
                <div id="myTabUpload" class="row-fluid">
                    <ul class="nav nav-tabs span4">
                        <li class="active"><a href="#tab-AttachmentUpload">Upload Attachment</a></li>
                        <li><a href="#tab-AddRemoteAttachment">Add Remote Attachment</a></li>
                    </ul>

                    <div class="tab-content span6">
                        <div id="tab-AttachmentUpload" class="lfr-dynamic-uploader">
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
                        <div id="tab-AddRemoteAttachment">
                            <form action="">
                                <label for="addRemoteAttachmentUrl">Url:</label> <input type="url" id="addRemoteAttachmentUrl" required>
                                <br/>
                                <label for="addRemoteAttachmentFilename">Filename:</label> <input type="text" placeholder="Defaults to basename of url" id="addRemoteAttachmentFilename">
                                <br/>
                                <button id="do-add-remote-attachment">Add Remote Attachment</button>
                                <br/>
                                Note that:
                                <ul>
                                    <li>
                                        one should only use resources, which do not change over time (e.g. releases served by a repository)
                                    </li>
                                    <li>
                                        the entered URL must be publicly reachable
                                    </li>
                                    <li>
                                        the entered URL should not contain (expiring-) access tokens
                                    </li>
                                </ul>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </core_rt:otherwise>
    </core_rt:choose>
</div>

<script>
    function showAddAttachmentDialog() {
        $('#tab-AddRemoteAttachment > form')[0].reset();
        openDialog('fileupload-form', 'fileupload-files');
    }
</script>


<core_rt:if test="${not empty documentID}">
    <script src="<%=request.getContextPath()%>/js/external/resumable.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/js/resumableAttachments.js" type="text/javascript"></script>

    <script>
        // =============================================================================================================
        // Tabs in upload dialog
        YUI().use(
            'aui-tabview',
            function (Y) {
                tabView = new Y.TabView(
                    {
                        srcNode: '#myTabUpload',
                        stacked: true,
                        type: 'tab'
                    }
                ).render();
            }
        );

        // =============================================================================================================
        // Attachment Upload
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

        // =============================================================================================================
        // Add Remote Attachment

         $("#do-add-remote-attachment").bind('click', function () {
            $.ajax({
                url: '<%=addRemoteAttachmentAjaxURL%>',
                cache: false,
                data: {
                    <portlet:namespace/>url: $('#addRemoteAttachmentUrl').val(),
                    <portlet:namespace/>Filename: $('#addRemoteAttachmentFilename').val(),
                }
            }).done(function (data) {
                $('#noAttachmentsRow').hide();
                $('#attachmentInfo > tbody').append(data);
            });
            closeOpenDialogs();
        });

    </script>
</core_rt:if>
