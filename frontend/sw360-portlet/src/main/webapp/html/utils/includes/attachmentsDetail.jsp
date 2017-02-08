<%--
  ~ Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
  --%>
<%@include file="/html/init.jsp" %>


<%@ taglib prefix="sw360" uri="/WEB-INF/customTags.tld" %>

<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<jsp:useBean id="attachments" type="java.util.Set<org.eclipse.sw360.datahandler.thrift.attachments.Attachment>" scope="request"/>
<jsp:useBean id="documentType" type="java.lang.String" scope="request"/>
<table class="table info_table " id="attachmentDetail" title="Attachment Information">
    <thead>
    <tr>
        <th colspan="8" class="headlabel">
            Attachments
            <core_rt:if test="${not empty attachments}">
                <sw360:DisplayDownloadAttachmentBundle ids="${attachments}" name="${AttachmentBundle.zip}"/>
            </core_rt:if>
        </th>
    </tr>
    </thead>
    <tbody>
    <core_rt:if test="${not empty attachments}">
        <core_rt:forEach items="${attachments}" var="attachment" varStatus="loop">
            <tr id="attachmentRow1${loop.count}">
                <td colspan="8" class="attachmentTitle">
                    <sw360:DisplayDownloadAttachment id="${attachment.attachmentContentId}" name="${attachment.filename}"/>
                    "<sw360:out value="${attachment.filename}"/>"
                </td>
            </tr>
            <tr id="attachmentRow2${loop.count}" >
                <td colspan="3" rowspan="2" class="lessPadding">
                    Type: <sw360:DisplayEnum value="${attachment.attachmentType}"/> <br/>
                    Status: <sw360:DisplayEnum value="${attachment.checkStatus}"/> <br/>
                    SHA1-Checksum: <span class="hashvalue">${attachment.sha1}</span>
                </td>
                <td>Uploader:</td>
                <td colspan="4">
                    <sw360:out value="${attachment.createdBy}"/>,
                    <sw360:out value="${attachment.createdTeam}"/>,
                    <sw360:out value="${attachment.createdOn}"/>
                    <br/>
                    Comment:
                    <core_rt:if test="${not empty attachment.createdComment}">
                        "<sw360:out value="${attachment.createdComment}"/>"
                    </core_rt:if>
                    <core_rt:if test="${empty attachment.createdComment}">
                        -
                    </core_rt:if>
                </td>
            </tr>
            <tr id="attachmentRow3${loop.count}" >
                <td>Approver:</td>
                <td colspan="4">
                    <core_rt:if test="${not empty attachment.checkedBy}">
                        <sw360:out value="${attachment.checkedBy}"/>, <sw360:out value="${attachment.checkedTeam}"/>, <sw360:out value="${attachment.checkedOn}"/>
                        <br/>
                        Comment:
                        <core_rt:if test="${not empty attachment.checkedComment}">
                            "<sw360:out value="${attachment.checkedComment}"/>"
                        </core_rt:if>
                        <core_rt:if test="${empty attachment.checkedComment}">
                            -
                        </core_rt:if>
                    </core_rt:if>
                    <core_rt:if test="${empty attachment.checkedBy}">
                        Not yet approved.
                    </core_rt:if>
                </td>
            </tr>
            <tr id="attachmentRow4${loop.count}" class="lastAttachmentRow">
            </tr>
        </core_rt:forEach>
    </core_rt:if>

    <core_rt:if test="${empty attachments}">
        <tr id="noAttachmentsRow">
            <td colspan="6">No attachments yet.</td>
        </tr>
    </core_rt:if>
    </tbody>
</table>
