<%--
  ~ Copyright Siemens AG, 2013-2016. Part of the SW360 Portal Project.
  ~
  ~ SPDX-License-Identifier: EPL-1.0
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
  --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>

<%@include file="/html/init.jsp"%>
<%-- the following is needed by liferay to display error messages--%>
<%@include file="/html/utils/includes/errorKeyToMessage.jspf"%>
<portlet:defineObjects />
<liferay-theme:defineObjects />

<c:catch var="attributeNotFoundException">
    <jsp:useBean id="project" class="org.eclipse.sw360.datahandler.thrift.projects.Project" scope="request"/>
    <jsp:useBean id="usingProjects" type="java.util.Set<org.eclipse.sw360.datahandler.thrift.projects.Project>"
                 scope="request"/>
    <jsp:useBean id="selectedTab" class="java.lang.String" scope="request"/>
    <jsp:useBean id="numberOfUncheckedVulnerabilities" type="java.lang.Integer" scope="request"/>
    <jsp:useBean id="numberOfVulnerabilities" type="java.lang.Integer" scope="request"/>
</c:catch>
<%@include file="/html/utils/includes/logError.jspf" %>
<core_rt:if test="${empty attributeNotFoundException}">

    <link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/webjars/jquery-ui/1.12.1/jquery-ui.css">
    <script src="<%=request.getContextPath()%>/webjars/jquery/1.12.4/jquery.min.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/webjars/jquery-validation/1.15.1/jquery.validate.min.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/webjars/jquery-validation/1.15.1/additional-methods.min.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath()%>/webjars/jquery-ui/1.12.1/jquery-ui.min.js"></script>
    <script src="<%=request.getContextPath()%>/webjars/datatables/1.10.7/js/jquery.dataTables.min.js" type="text/javascript"></script>

    <jsp:include page="/html/utils/includes/attachmentsDelete.jsp" />

    <core_rt:set var="dontDisplayDeleteButton" value="true" scope="request"/>
    <div id="header"></div>
    <p class="pageHeader"><span class="pageHeaderBigSpan">Project: <sw360:ProjectName project="${project}"/></span>
        <span class="pull-right">
        <input type="button" onclick="editProject()" id="edit" value="Edit" class="addButton">
    </span>
    </p>

    <core_rt:set var="inProjectDetailsContext" value="true" scope="request"/>
    <%@include file="/html/projects/includes/detailOverview.jspf"%>
</core_rt:if>
<script>
    var tabView;
    var Y = YUI().use(
            'aui-tabview',
            function(Y) {
                tabView = new Y.TabView(
                        {
                            srcNode: '#myTab',
                            stacked: true,
                            type: 'tab'
                        }
                ).render();
            }
    );

    function editProject() {
        window.location ='<portlet:renderURL ><portlet:param name="<%=PortalConstants.PROJECT_ID%>" value="${project.id}"/><portlet:param name="<%=PortalConstants.PAGENAME%>" value="<%=PortalConstants.PAGENAME_EDIT%>"/></portlet:renderURL>'
    }

</script>
