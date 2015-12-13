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


<%@ taglib prefix="sw360" uri="/WEB-INF/customTags.tld" %>

<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<jsp:useBean id="attachments" type="java.util.Set<com.siemens.sw360.datahandler.thrift.attachments.Attachment>" scope="request"/>
<jsp:useBean id="documentType" type="java.lang.String" scope="request"/>
<table class="table info_table " id="attachmentInfo" title="Attachment Information">
    <thead>
    <tr>
        <th colspan="20" class="headlabel">Attachments</th>
    </tr>
    </thead>
    <tbody>
    <core_rt:if test="${not empty attachments}">
        <core_rt:forEach items="${attachments}" var="attachment" varStatus="loop">
            <tr id="componentattachmentrow${loop.count}" class="tr_clone">
                <td>
                    <table>
                        <tr style="width:800px;">
                            <td colspan="2">
                                <input class="toplabelledInput" id="comp_file${loop.count}"
                                       style="margin-left: 10px;width:480px;"
                                       name="<portlet:namespace/>attachmentfile" type="text"
                                       value="<sw360:out value="${attachment.filename}"/>" readonly/>
                            </td>
                            <td colspan="1">

                                <label class="textlabel stackedLabel" for="comp_filetype${loop.count}">Attachment
                                    type</label>
                                <input id="comp_filetype${loop.count}"
                                       value="<sw360:DisplayEnum value="${attachment.attachmentType}"/>" readonly/>

                            </td>
                            <td class="downloader" colspan="1">
                                <sw360:DisplayDownloadAttachment id="${attachment.attachmentContentId}"
                                                                 name="${attachment.filename}"/>
                            </td>
                            <td class="deletor" colspan="1">
                                <core_rt:if test="${not dontDisplayDeleteButton}">
                                    <img src="<%=request.getContextPath()%>/images/Trash.png"
                                         onclick="deleteAttachment('componentattachmentrow${loop.count}','${attachment.attachmentContentId}')"
                                         alt="Delete">
                                </core_rt:if>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="5">
                                <font color="000000">Uploader:</font> <input class="toplabelledInput"
                                                                             id="comp_file_uploader${loop.count}"
                                                                             style="margin-left: 10px;width:150px;"
                                                                             name="<portlet:namespace/>attachmentfile"
                                                                             type="text"
                                                                             value="<sw360:out value="${attachment.createdBy}"/>"
                                                                             readonly/>
                                <input class="toplabelledInput" id="comp_file_uploader${loop.count}"
                                       style="width:110px;"
                                       name="<portlet:namespace/>attachmentfile" type="text"
                                       value="<sw360:out value="${attachment.createdTeam}"/>" readonly/>
                                <input class="toplabelledInput" id="comp_file_uploader${loop.count}" style="width:90px;"
                                       name="<portlet:namespace/>attachmentcomment" type="text"
                                       value="<sw360:out value="${attachment.createdOn}"/>" readonly/>
                                <input class="toplabelledInput" id="comp_file_uploader${loop.count}"
                                       style="width:140px;"
                                       name="<portlet:namespace/>attachmentcomment" type="text"
                                       value="<sw360:out value="${attachment.createdComment}"/>" readonly/>
                                <br/>
                                <font color="000000">Approver:</font> <input class="toplabelledInput"
                                                                             id="comp_file_uploader${loop.count}"
                                                                             style="margin-left: 10px;width:150px;"
                                                                             name="<portlet:namespace/>attachmentfile"
                                                                             type="text"
                                                                             value="<sw360:out value="${attachment.checkedBy}"/>"
                                                                             readonly/>
                                <input class="toplabelledInput" id="comp_file_uploader${loop.count}"
                                       style="width:110px;"
                                       name="<portlet:namespace/>attachmentfile" type="text"
                                       value="<sw360:out value="${attachment.checkedTeam}"/>" readonly/>
                                <input class="toplabelledInput" id="comp_file_uploader${loop.count}" style="width:90px;"
                                       name="<portlet:namespace/>attachmentcomment" type="text"
                                       value="<sw360:out value="${attachment.checkedOn}"/>" readonly/>
                                <input class="toplabelledInput" id="comp_file_uploader${loop.count}"
                                       style="width:140px;"
                                       name="<portlet:namespace/>attachmentcomment" type="text"
                                       value="<sw360:out value="${attachment.checkedComment}"/>" readonly/>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="5">

                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </core_rt:forEach>
    </core_rt:if>

    <core_rt:if test="${empty attachments}">
        <tr id="noAttachmentsRow">
            <td colspan="20">No attachments yet.</td>
        </tr>
    </core_rt:if>
    </tbody>
</table>
