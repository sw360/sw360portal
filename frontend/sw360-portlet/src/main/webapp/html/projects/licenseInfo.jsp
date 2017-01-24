<%--
  ~ Copyright Siemens AG, 2016. Part of the SW360 Portal Project.
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
  --%>
<%@ page import="org.eclipse.sw360.portal.common.PortalConstants" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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
    <jsp:useBean id="project" class="org.eclipse.sw360.datahandler.thrift.projects.Project" scope="request"/>
    <jsp:useBean id="sw360User" class="org.eclipse.sw360.datahandler.thrift.users.User" scope="request"/>
    <jsp:useBean id="projectList" type="java.util.List<org.eclipse.sw360.datahandler.thrift.projects.ProjectLink>"
                 scope="request"/>
    <jsp:useBean id="licenseInfoOutputFormats"
                 type="java.util.List<org.eclipse.sw360.datahandler.thrift.licenseinfo.OutputFormatInfo>"
                 scope="request"/>
</c:catch>
<core_rt:if test="${empty attributeNotFoundException}">

    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/external/jquery-ui.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/external/jquery.treetable.css"/>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/jquery.treetable.theme.sw360.css"/>
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
        <table class="table info_table" id="LinkedProjectsInfo" title="Linked Releases And Projects" style="table-layout: auto">
            <thead>
            <tr>
                <th colspan="7" class="headlabel">Linked Releases And Projects</th>
            </tr>
            <tr>
                <th><input type="checkbox" checked="checked" id="selectAllCheckbox"/></th>
                <th>Lvl</th>
                <th>Name</th>
                <th>Type</th>
                <th>Clearing State</th>
                <th>Uploaded by</th>
                <th>Clearing Team</th>
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
                        <td></td>
                        <td>
                            <sw360:out value="${projectLink.treeLevel}"/>
                        </td>
                        <td>
                            <a href="<sw360:DisplayProjectLink projectId="${projectLink.id}" bare="true" />"><sw360:out
                                    value="${projectLink.name}" maxChar="50"/> <sw360:out
                                    value="${projectLink.version}"/></a>
                        </td>
                        <td>
                            <sw360:DisplayEnum value="${projectLink.projectType}"/>
                        </td>
                        <td>
                            <sw360:DisplayEnum value="${projectLink.clearingState}"/>
                        </td>
                        <td>
                        </td>
                        <td>
                        </td>
                    </tr>
                </core_rt:if>
                <%--linked releases of linked projects--%>
                <core_rt:forEach items="${projectLink.linkedReleases}" var="releaseLink" varStatus="releaseloop">
                    <tr id="releaseLinkRow${loop.count}_${releaseloop.count}" data-tt-id="${releaseLink.nodeId}"
                        <core_rt:if test="${loop.index!=0}">data-tt-parent-id="${projectLink.nodeId}"</core_rt:if>
                        <core_rt:if test="${empty releaseLink.attachments}">class="highlightedRed"</core_rt:if>
                        <core_rt:if test="${fn:length(releaseLink.attachments) == 1}">class="highlightedGreen"</core_rt:if>
                        <core_rt:if test="${fn:length(releaseLink.attachments) gt 1}">class="highlightedYellow"</core_rt:if>
                    >
                        <td></td>
                        <td>
                            <sw360:out value="${projectLink.treeLevel + 1}"/>
                        </td>
                        <td>
                            <a href="<sw360:DisplayReleaseLink releaseId="${releaseLink.id}" bare="true" />"><sw360:out
                                    value="${releaseLink.vendor} ${releaseLink.name}" maxChar="50"/> <sw360:out
                                    value="${releaseLink.version}"/></a>
                        </td>
                        <td>
                            <sw360:DisplayEnum value="${releaseLink.componentType}"/>
                        </td>
                        <td>
                            <sw360:DisplayEnum value="${releaseLink.clearingState}"/>
                        </td>
                        <td>
                        </td>
                        <td>
                        </td>
                    </tr>
                    <core_rt:set var="attachmentSelected" value="false" scope="request"/>
                    <core_rt:forEach items="${releaseLink.attachments}" var="attachment" varStatus="attachmentloop">
                        <tr id="attachmentRow${loop.count}_${releaseloop.count}_${attachmentloop.count}" data-tt-id="${releaseLink.nodeId}_${attachment.attachmentContentId}"
                            data-tt-parent-id="${releaseLink.nodeId}"
                            <core_rt:if test="${fn:length(releaseLink.attachments) == 1}">class="highlightedGreen"</core_rt:if>
                            <core_rt:if test="${fn:length(releaseLink.attachments) gt 1}">class="highlightedYellow"</core_rt:if>
                        >
                            <td>
                                <input type="checkbox"
                                       name="<portlet:namespace/><%=PortalConstants.LICENSE_INFO_RELEASE_TO_ATTACHMENT%>"
                                       value="${releaseLink.id}:${attachment.attachmentContentId}"
                                       <core_rt:if test="${!attachmentSelected && (releaseLink.attachments.size() == 1 || attachment.createdTeam == sw360User.department)}">checked="checked" class="defaultChecked"
                                            <core_rt:set var="attachmentSelected" value="true" scope="request"/>
                                       </core_rt:if>
                                />
                            </td>
                            <td>
                                <sw360:out value="${projectLink.treeLevel + 1}"/>
                            </td>
                            <td>
                                <sw360:out value="${attachment.filename}"/>
                            </td>
                            </td>
                            <td>
                            </td>
                            <td>
                            <td>
                                <sw360:DisplayUserEmail email="${attachment.createdBy}"/>
                            </td>
                            <td>
                                <sw360:out value="${attachment.createdTeam}"/>
                            </td>
                        </tr>
                    </core_rt:forEach>
                </core_rt:forEach>
            </core_rt:forEach>
            <core_rt:if test="${projectList.size() < 1 and $releaseList.size() < 1}">
                <tr>
                    <td colspan="7">No linked releases or projects</td>
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
        $("#LinkedProjectsInfo").treetable({expandable: true, column: 2, initialState: "expanded"});
        $("#selectAllCheckbox").on("change", function() {
            var uncheckAll = function() {
                $("#LinkedProjectsInfo").find(":checkbox:not(#selectAllCheckbox)").each(function () {
                    this.checked = false;
                })

            };
            if (this.checked){
                uncheckAll();
                $("#LinkedProjectsInfo").find(":checkbox.defaultChecked").each(function () {
                    this.checked = true;
                })
            } else {
                uncheckAll();
            }
        })
    });

</script>
</core_rt:if>