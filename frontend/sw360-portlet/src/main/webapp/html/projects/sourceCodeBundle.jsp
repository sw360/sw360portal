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
    <portlet:param name="<%=PortalConstants.ACTION%>" value='<%=PortalConstants.DOWNLOAD_SOURCE_CODE_BUNDLE%>'/>
    <portlet:param name="<%=PortalConstants.PROJECT_ID%>" value="${requestScope.project.id}"/>
</portlet:resourceURL>


<c:catch var="attributeNotFoundException">
    <jsp:useBean id="project" class="org.eclipse.sw360.datahandler.thrift.projects.Project" scope="request"/>
    <jsp:useBean id="sw360User" class="org.eclipse.sw360.datahandler.thrift.users.User" scope="request"/>
    <jsp:useBean id="projectList" type="java.util.List<org.eclipse.sw360.datahandler.thrift.projects.ProjectLink>"
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
    <p class="pageHeader"><span class="pageHeaderBigSpan">Generate Source Code Bundle For Project <sw360:ProjectName project="${project}"/></span>
    </p>

    <div id="content" >
        <form id="downloadLicenseInfoForm" name="downloadLicenseInfoForm" action="<%=downloadLicenseInfoURL%>" method="post">
            <%@include file="/html/projects/includes/attachmentSelectTable.jspf" %>
            <span class="pull-right">
               <input type="submit" value="Download File" class="addButton"/>
            </span>
        </form>

    </div>
</core_rt:if>