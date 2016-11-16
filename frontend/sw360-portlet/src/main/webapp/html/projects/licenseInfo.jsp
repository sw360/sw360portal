<%--
  ~ Copyright Siemens AG, 2016. Part of the SW360 Portal Project.
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
  --%>
<%@ page import="com.siemens.sw360.portal.common.PortalConstants" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@include file="/html/init.jsp"%>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%-- the following is needed by liferay to display error messages--%>
<%@include file="/html/utils/includes/errorKeyToMessage.jspf"%>
<portlet:defineObjects />
<liferay-theme:defineObjects />
<portlet:resourceURL var="downloadLicenseInfoURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value='<%=PortalConstants.DOWNLOAD_LICENSE_INFO%>'/>
    <portlet:param name="<%=PortalConstants.PROJECT_ID%>" value="${requestScope.project.id}"/>
</portlet:resourceURL>


<c:catch var="attributeNotFoundException">
    <jsp:useBean id="project" class="com.siemens.sw360.datahandler.thrift.projects.Project" scope="request"/>
    <jsp:useBean id="sw360User" class="com.siemens.sw360.datahandler.thrift.users.User" scope="request"/>
    <jsp:useBean id="projectList" type="java.util.List<com.siemens.sw360.datahandler.thrift.projects.ProjectLink>"
                 scope="request"/>
    <jsp:useBean id="licenseInfoOutputFormats"
                 type="java.util.List<com.siemens.sw360.datahandler.thrift.licenseinfo.OutputFormatInfo>"
                 scope="request"/>
</c:catch>
<core_rt:if test="${empty attributeNotFoundException}">

    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/external/jquery-ui.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/external/jquery.treetable.css"/>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/external/jquery.treetable.theme.default.css"/>
    <script src="<%=request.getContextPath()%>/js/external/jquery-1.11.1.min.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/js/external/jquery.validate.min.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/js/external/additional-methods.min.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/js/external/jquery-ui.min.js"></script>
    <script src="<%=request.getContextPath()%>/js/external/jquery.treetable.js"></script>

    <div id="header"></div>
    <p class="pageHeader"><span class="pageHeaderBigSpan">Generate License Information File For Project <sw360:ProjectName project="${project}"/></span>
    </p>

    <div id="content" >
        <form id="downloadLicenseInfoForm" name="downloadLicenseInfoForm" action="<%=downloadLicenseInfoURL%>" method="post">
        <table class="table info_table" id="LinkedProjectsInfo" title="Linked Releases And Projects">
            <thead>
            <tr>
                <th colspan="4" class="headlabel">Linked Releases And Projects</th>
            </tr>
            <tr>
                <th width="5%"><input type="checkbox" /></th>
                <th width="30%">Name</th>
                <th width="30%">Uploaded by</th>
                <th width="30%">Clearing Team</th>
            </tr>
            </thead>
            <tbody>
            <%--linked projects and their linked projects--%>
            <core_rt:forEach items="${projectList}" var="projectLink" varStatus="loop">
                <core_rt:if test="${loop.index!=0}">
                    <tr id="projectLinkRow${loop.count}" data-tt-id="${projectLink.nodeId}"
                        <core_rt:if
                                test="${not empty projectLink.parentNodeId}">data-tt-parent-id="${projectLink.parentNodeId}"</core_rt:if>
                    >
                        <td width="5%" />
                        <td width="30%">
                            <a href="<sw360:DisplayProjectLink projectId="${projectLink.id}" bare="true" />"><sw360:out
                                    value="${projectLink.name} ${projectLink.version}"/></a>
                        </td>
                        <td width="30%">
                        </td>
                        <td width="30%">
                        </td>
                    </tr>
                </core_rt:if>
                <%--linked releases of linked projects--%>
                <core_rt:forEach items="${projectLink.linkedReleases}" var="releaseLink" varStatus="releaseloop">
                    <tr id="releaseLinkRow${loop.count}_${releaseloop.count}" data-tt-id="${releaseLink.nodeId}"
                        <core_rt:if test="${loop.index!=0}">data-tt-parent-id="${projectLink.nodeId}"</core_rt:if>
                        <core_rt:if test="${empty releaseLink.licenseInfoAttachments || releaseLink.licenseInfoAttachments.size() == 0}">class="highlightedRed"</core_rt:if>
                        <core_rt:if test="${releaseLink.licenseInfoAttachments.size() == 1}">class="highlightedGreen"</core_rt:if>
                        <core_rt:if test="${releaseLink.licenseInfoAttachments.size() > 1}">class="highlightedYellow"</core_rt:if>
                    >
                        <td width="5%"/>
                        <td width="30%">
                            <a href="<sw360:DisplayReleaseLink releaseId="${releaseLink.id}" bare="true" />">
                                <sw360:out value="${releaseLink.vendor}"/><core_rt:if
                                    test="${not empty releaseLink.vendor}">&nbsp;</core_rt:if><sw360:out
                                    value="${releaseLink.name} ${releaseLink.version}"/>
                            </a>
                        </td>
                        <td width="30%">
                        </td>
                        <td width="30%">
                        </td>
                    </tr>
                    <core_rt:set var="attachmentSelected" value="false" scope="request"/>
                    <core_rt:forEach items="${releaseLink.licenseInfoAttachments}" var="attachment" varStatus="attachmentloop">
                        <tr id="attachmentRow${loop.count}_${releaseloop.count}_${attachmentloop.count}" data-tt-id="${releaseLink.nodeId}_${attachment.attachmentContentId}"
                            data-tt-parent-id="${releaseLink.nodeId}"
                            <core_rt:if test="${releaseLink.licenseInfoAttachments.size() == 1}">class="highlightedGreen"</core_rt:if>
                            <core_rt:if test="${releaseLink.licenseInfoAttachments.size() > 1}">class="highlightedYellow"</core_rt:if>
                        >
                            <td width="5%">
                                <input type="checkbox"
                                       name="<portlet:namespace/><%=PortalConstants.LICENSE_INFO_RELEASE_TO_ATTACHMENT%>"
                                       value="${releaseLink.id}:${attachment.attachmentContentId}"
                                       <core_rt:if test="${!attachmentSelected && (releaseLink.licenseInfoAttachments.size() == 1 || attachment.createdTeam == sw360User.department)}">checked="checked"
                                            <core_rt:set var="attachmentSelected" value="true" scope="request"/>
                                       </core_rt:if>
                                />
                            </td>
                            <td width="30%">
                                <sw360:out value="${attachment.filename}"/>
                            </td>
                            <td width="30%">
                                <sw360:DisplayUserEmail email="${attachment.createdBy}"/>
                            </td>
                            <td width="30%">
                                <sw360:out value="${attachment.createdTeam}"/>
                            </td>
                        </tr>
                    </core_rt:forEach>
                </core_rt:forEach>
            </core_rt:forEach>
            <core_rt:if test="${projectList.size() < 1 and $releaseList.size() < 1}">
                <tr>
                    <td colspan="4">No linked releases or projects</td>
                </tr>
            </core_rt:if>
            </tbody>
        </table>
<span class="pull-right">
    <select class="toplabelledInput, formatSelect" id="<%=PortalConstants.LICENSE_INFO_SELECTED_OUTPUT_FORMAT%>"
            name="<portlet:namespace/><%=PortalConstants.LICENSE_INFO_SELECTED_OUTPUT_FORMAT%>">
                        <sw360:DisplayOutputFormats options='${licenseInfoOutputFormats}'/>
    </select>
    <input type="submit" id="downloadLicenseInfoButton" value="Download File" class="addButton"/>
</span>
            </form>

    </div>


<script>
    $(window).load(function () {
        $("#LinkedProjectsInfo").treetable({expandable: true, column: 1, initialState: "expanded"});
    });
</script>
</core_rt:if>