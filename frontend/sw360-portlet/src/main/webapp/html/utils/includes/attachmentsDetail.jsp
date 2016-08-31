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

<jsp:useBean id="attachments" type="java.util.Set<com.siemens.sw360.datahandler.thrift.attachments.Attachment>" scope="request"/>
<jsp:useBean id="documentType" type="java.lang.String" scope="request"/>
<table class="table info_table " id="attachmentDetail" title="Attachment Information">
    <thead>
    <tr>
        <th colspan="6" class="headlabel">Attachments</th>
    </tr>
    </thead>
    <tbody>
    <core_rt:if test="${not empty attachments}">
        <core_rt:forEach items="${attachments}" var="attachment" varStatus="loop">
            <tr id="attachmentRow1${loop.count}">
                <td colspan="2" class="attachmentTitle">
                    <sw360:DisplayDownloadAttachment id="${attachment.attachmentContentId}" name="${attachment.filename}"/>
                    "<sw360:out value="${attachment.filename}"/>"
                </td>
                <td rowspan="2">Uploader</td>
                <td colspan="3" rowspan="2">
                    Comment: "<sw360:out value="${attachment.createdComment}"/>" <br/>
                    <sw360:out value="${attachment.createdBy}"/>,
                    <sw360:out value= "${attachment.createdTeam}"/>,
                    <sw360:out value="${attachment.createdOn}"/>
                </td>
            </tr>
            <tr id="attachmentRow2${loop.count}" >
                <td colspan="2" rowspan="3" class="lessPadding">
                    Type: <sw360:DisplayEnum value="${attachment.attachmentType}"/> <br/>
                    Status: <sw360:DisplayEnum value="${attachment.checkStatus}"/> <br/>
                    SHA1-Checksum: <span class="hashvalue">${attachment.sha1}</span>
                </td>
            </tr>
            <tr id="attachmentRow3${loop.count}" >
                <td rowspan="2">Approver</td>
                <td colspan="3" rowspan="2">
                    <core_rt:if test="${not empty attachment.checkedBy}">
                        Comment: "<sw360:out value="${attachment.checkedComment}"/>" <br/>
                        <sw360:out value="${attachment.checkedBy}"/>,
                        <sw360:out value="${attachment.checkedTeam}"/>,
                        <sw360:out value="${attachment.checkedOn}"/>
                    </core_rt:if>
                    <core_rt:if test="${empty attachment.checkedBy}">
                        -<br/>
                         <br/>
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
