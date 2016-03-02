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
<%@include file="/html/init.jsp"%>



<portlet:defineObjects />
<liferay-theme:defineObjects />

<jsp:useBean id="usingProjects" type="java.util.Set<com.siemens.sw360.datahandler.thrift.projects.Project>" scope="request"/>
<jsp:useBean id="project" class="com.siemens.sw360.datahandler.thrift.projects.Project" scope="request" />
<jsp:useBean id="selectedTab" class="java.lang.String" scope="request" />
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/external/jquery-ui.css">
<script src="<%=request.getContextPath()%>/js/external/jquery-1.11.1.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery.validate.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/additional-methods.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery-ui.min.js"></script>

<jsp:include page="/html/utils/includes/attachmentsDelete.jsp" />

<core_rt:set var="dontDisplayDeleteButton" value="true" scope="request"/>
<div id="header"></div>
<p class="pageHeader"><span class="pageHeaderBigSpan">Project: <sw360:ProjectName project="${project}"/></span>
    <span class="pull-right">
        <input type="button" onclick="editProject()" id="edit" value="Edit" class="addButton">
    </span>
</p>
<div id="content" >
    <div class="container-fluid">
        <div id="myTab" class="row-fluid">
            <ul class="nav nav-tabs span2">
                <li <core_rt:if test="${selectedTab == 'Summary' || empty selectedTab}"> class="active" </core_rt:if> ><a href="#tab-Summary">Summary</a></li>
                <li <core_rt:if test="${selectedTab == 'Linked Projects'}"> class="active" </core_rt:if>><a href="#tab-linkedProjects">Linked Projects</a></li>
                <li <core_rt:if test="${selectedTab == 'Linked Releases'}"> class="active" </core_rt:if>><a href="#tab-linkedReleases">Linked Releases</a></li>
                <li <core_rt:if test="${selectedTab == 'Clearing Status'}"> class="active" </core_rt:if>><a href="#tab-ClearingStatus">Clearing Status</a></li>
                <li <core_rt:if test="${selectedTab == 'Attachments'}"> class="active" </core_rt:if>><a href="#tab-Attachments">Attachments</a></li>
                <li <core_rt:if test="${selectedTab == 'Wiki'}"> class="active" </core_rt:if>><a href="#tab-Wiki">Wiki</a></li>
            </ul>
            <div class="tab-content span10">
                <div id="tab-Summary" class="tab-pane" >
                    <%@include file="/html/projects/includes/projects/summary.jspf" %>
                    <%@include file="/html/projects/includes/projects/usingProjects.jspf" %>
                </div>
                <div id="tab-linkedProjects" >
                    <%@include file="/html/projects/includes/linkedProjects.jspf" %>
                </div>
                <div id="tab-linkedReleases" >
                    <%@include file="/html/utils/includes/linkedReleaseDetails.jspf" %>
                </div>
                <div id="tab-ClearingStatus" >
                    <%@include file="/html/projects/includes/projects/clearingStatus.jspf" %>
                </div>
                <div id="tab-Attachments" >
                    <jsp:include page="/html/utils/includes/attachmentsDetail.jsp" />
                </div>
                <div id="tab-Wiki" class="tab-pane">
                    <%@include file="/html/projects/includes/wiki.jsp" %>
                </div>
            </div>
        </div>
    </div>
</div>

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
