<%--
  ~ Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
  --%>
<%@ page import="com.liferay.portlet.PortletURLFactoryUtil" %>
<%@include file="/html/init.jsp" %>

<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">

<%@ page import="org.eclipse.sw360.portal.common.PortalConstants" %>
<jsp:useBean id="project" class="org.eclipse.sw360.datahandler.thrift.projects.Project" scope="request" />
<div id="search-project-form" title="Search Project" style="display: none; background-color: #ffffff;">
    <form>
        <div style="display: inline-block">
            <input type="text" name="searchproject" id="searchproject" placeholder="search" class="searchbar"/>&nbsp;
            <input type="button" value="Search"
                   onclick="ProjectContentFromAjax('projectSearchResultstable', '<%=PortalConstants.PROJECT_SEARCH%>', $('#searchproject').val(), true);"
                   class="searchbutton" id="searchbuttonproject"/>
        </div>

        <div id="Projectsearchresults">
            <table width="100%" style="border-bottom: 2px solid #66c1c2;">
                <thead>
                <tr class="trheader" style="height: 30px;">
                    <th width="4%">&nbsp;</th>
                    <th width="32%" class="textlabel">Project name</th>
                    <th width="32%" class="textlabel">Project Responsible</th>
                    <th width="32%" class="textlabel">Description</th>
                </tr>
                </thead>
            </table>
            <div style="overflow-y: scroll; height: 150px;">
                <table id="projectSearchResultstable" width="100%">
                    <tr class="trbodyClass">
                        <td colspan="4"></td>
                    </tr>
                </table>
            </div>
            <hr noshade size="1" style="background-color: #66c1c2; border-color: #59D1C4;"/>
            <br/>

            <div>
                <input type="button" value="Select" class="addButton" onclick="selectProject();"/>
            </div>
        </div>
    </form>
</div>

<portlet:resourceURL var="viewProjectURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value="<%=PortalConstants.VIEW_LINKED_PROJECTS%>"/>
    <portlet:param name="<%=PortalConstants.PROJECT_ID%>" value="${project.id}"/>
</portlet:resourceURL>




<script>
    function showProjectDialog(){
        openDialog('search-project-form', 'searchproject');
    }

    function selectProject() {

        var projectIds  = [];

        $('#projectSearchResultstable').find(':checked').each(
                function() {
                    projectIds.push(this.value);
                }
        );
        addProjectInfo(projectIds);

        closeOpenDialogs();
        return false;
    }


    function addProjectInfo(linkedProjects) {
        ProjectContentFromAjax('LinkedProjectsInfo', '<%=PortalConstants.LIST_NEW_LINKED_PROJECTS%>', linkedProjects);
    }

    function ProjectContentFromAjax(id, what, where, replace) {
        jQuery.ajax({
            type: 'POST',
            url: '<%=viewProjectURL%>',
            data: {
                '<portlet:namespace/><%=PortalConstants.WHAT%>': what,
                '<portlet:namespace/><%=PortalConstants.WHERE%>': where
            },
            success: function (data) {
                if (replace) {
                    $('#' + id + " tbody").html(data);
                } else {
                    $('#' + id + " tbody").append(data);
                }
            }
        });
    }

    $(document).ready(function () {
                bindkeyPressToClick('searchproject', 'searchbuttonproject');
            }
    );

</script>


<%@include file="/html/projects/includes/linkedProjectDelete.jspf" %>
