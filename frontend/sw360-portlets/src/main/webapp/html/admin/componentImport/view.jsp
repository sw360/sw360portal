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

<%@include file="/html/init.jsp" %>
<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<portlet:actionURL var="updateComponentsURL" name="updateComponents">
</portlet:actionURL>

<portlet:actionURL var="updateAttachmentsURL" name="updateComponentAttachments">
</portlet:actionURL>

<portlet:actionURL var="updateReleaseLinksURL" name="updateReleaseLinks">
</portlet:actionURL>

<portlet:actionURL var="updateLicenseArchiveURL" name="updateLicenses">
</portlet:actionURL>

<div id="header"></div>
<p class="pageHeader"><span class="pageHeaderBigSpan">Component Administration</span></p>

<table class="info_table">
    <thead>
    <tr>
        <th colspan="2"> Downloads</th>
    </tr>
    </thead>

    <tbody>
    <tr>
        <td>Download Component CSV</td>
        <td><a href="<portlet:resourceURL>
                               <portlet:param name="<%=PortalConstants.ACTION%>" value='<%=PortalConstants.DOWNLOAD%>'/>
                         </portlet:resourceURL>">
            <img src="<%=request.getContextPath()%>/images/downloadEnable.jpg" alt="Download">
        </a>
        </td>
    </tr>
    <tr>
        <td>Download Upload Component template CSV</td>
        <td><a href="<portlet:resourceURL>
                               <portlet:param name="<%=PortalConstants.ACTION%>" value='<%=PortalConstants.DOWNLOAD_SAMPLE%>'/>
                         </portlet:resourceURL>">
            <img src="<%=request.getContextPath()%>/images/downloadEnable.jpg" alt="Download">
        </a>
        </td>
    </tr>
    <tr>
        <td>Download Attachment Sample Infos</td>
        <td><a href="<portlet:resourceURL>
                               <portlet:param name="<%=PortalConstants.ACTION%>" value='<%=PortalConstants.DOWNLOAD_SAMPLE_ATTACHMENT_INFO%>'/>
                         </portlet:resourceURL>">
            <img src="<%=request.getContextPath()%>/images/downloadEnable.jpg" alt="Download">
        </a>
        </td>
    </tr>
    <tr>
        <td>Download Attachment Infos</td>
        <td><a href="<portlet:resourceURL>
                               <portlet:param name="<%=PortalConstants.ACTION%>" value='<%=PortalConstants.DOWNLOAD_ATTACHMENT_INFO%>'/>
                         </portlet:resourceURL>">
            <img src="<%=request.getContextPath()%>/images/downloadEnable.jpg" alt="Download">
        </a>
        </td>
    </tr>
    <tr>
        <td>Download Relase Link Sample Infos</td>
        <td><a href="<portlet:resourceURL>
                               <portlet:param name="<%=PortalConstants.ACTION%>" value='<%=PortalConstants.DOWNLOAD_SAMPLE_RELEASE_LINK_INFO%>'/>
                         </portlet:resourceURL>">
            <img src="<%=request.getContextPath()%>/images/downloadEnable.jpg" alt="Download">
        </a>
        </td>
    </tr>
    <tr>
        <td>Download Relase Link Infos</td>
        <td><a href="<portlet:resourceURL>
                               <portlet:param name="<%=PortalConstants.ACTION%>" value='<%=PortalConstants.DOWNLOAD_RELEASE_LINK_INFO%>'/>
                         </portlet:resourceURL>">
            <img src="<%=request.getContextPath()%>/images/downloadEnable.jpg" alt="Download">
        </a>
        </td>
    </tr>
    <tr>
        <td>Download License Archive</td>
        <td><a href="<portlet:resourceURL>
                               <portlet:param name="<%=PortalConstants.ACTION%>" value='<%=PortalConstants.DOWNLOAD_LICENSE_BACKUP%>'/>
                         </portlet:resourceURL>">
            <img src="<%=request.getContextPath()%>/images/downloadEnable.jpg" alt="Download">
        </a>
        </td>
    </tr>
    </tbody>

</table>

<form id="uploadForm" name="uploadForm" action="<%=updateComponentsURL%>" method="POST" enctype="multipart/form-data">
    <div class="fileupload-buttons">
            <span class="fileinput-button">
                <span>Upload Component CSV</span>
                <input id="<portlet:namespace/>componentCSVfileuploadInput" type="file" name="<portlet:namespace/>file">
            </span>
        <input type="submit" value="Update Components" class="addButton" id="<portlet:namespace/>componentCSV-Submit" disabled>
    </div>
</form>

<form id="uploadAttachmentsForm" name="uploadAttachmentsForm" action="<%=updateAttachmentsURL%>" method="POST" enctype="multipart/form-data">
    <div class="fileupload-buttons">
            <span class="fileinput-button">
                <span>Upload Attachment Info CSV</span>
                <input id="<portlet:namespace/>attachmentfileuploadInput" type="file" name="<portlet:namespace/>file">
            </span>
        <input type="submit" value="Update Component Attachments" class="addButton" id="<portlet:namespace/>attachmentCSV-Submit" disabled>
    </div>
</form>

<form id="uploadRelaseLinksForm" name="uploadRelaseLinksForm" action="<%=updateReleaseLinksURL%>" method="POST" enctype="multipart/form-data">
    <div class="fileupload-buttons">
            <span class="fileinput-button">
                <span>Upload Release Link Info CSV</span>
                <input id="<portlet:namespace/>releaselinkfileuploadInput" type="file" name="<portlet:namespace/>file">
            </span>
        <input type="submit" value="Update Release Links" class="addButton" id="<portlet:namespace/>releaseLinkCSV-Submit" disabled>
    </div>
</form>

<form id="uploadLicenseArchiveForm" name="uploadLicenseArchiveForm" action="<%=updateLicenseArchiveURL%>" method="POST" enctype="multipart/form-data">
    <div class="fileupload-buttons">
            <span class="fileinput-button">
                <span>Upload License Archive</span>
                <input id="<portlet:namespace/>LicenseArchivefileuploadInput" type="file" name="<portlet:namespace/>file">
            </span>
        <input type="submit" value="Upload License Archive" class="addButton" id="<portlet:namespace/>LicenseArchive-Submit" disabled>
    </div>
</form>

<script>
    document.getElementById("<portlet:namespace/>componentCSVfileuploadInput").onchange = function () {
        if (this.value) {
            document.getElementById("<portlet:namespace/>componentCSV-Submit").disabled = false;
        }
    };

    document.getElementById("<portlet:namespace/>attachmentfileuploadInput").onchange = function () {
        if (this.value) {
            document.getElementById("<portlet:namespace/>attachmentCSV-Submit").disabled = false;
        }
    };

    document.getElementById("<portlet:namespace/>releaselinkfileuploadInput").onchange = function () {
        if (this.value) {
            document.getElementById("<portlet:namespace/>releaseLinkCSV-Submit").disabled = false;
        }
    };

    document.getElementById("<portlet:namespace/>LicenseArchivefileuploadInput").onchange = function () {
        if (this.value) {
            document.getElementById("<portlet:namespace/>LicenseArchive-Submit").disabled = false;
        }
    };

</script>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/dataTable_Siemens.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/search.css">



