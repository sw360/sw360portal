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
<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<%-- Note that the necessary includes are in life-ray-portlet.xml --%>


<jsp:useBean id="projects" type="java.util.List<com.siemens.sw360.datahandler.thrift.projects.Project>"
             scope="request"/>

<div class="homepageheading">
    My Projects
</div>
<div id="myProjectsDiv" class="homepageListingTable">
    <table id="myProjectsTable" cellpadding="0" cellspacing="0" border="0" class="display">
    </table>
</div>

<script>

    //This can not be document ready function as liferay definitions need to be loaded first
    $(window).load(function () {
        var result = [];

        <core_rt:forEach items="${projects}" var="project">
        result.push({
            "DT_RowId": "${project.id}",
            "0": "<sw360:DisplayProjectLink project="${project}"/>",
            "1": '<sw360:out value="${project.description}" maxChar="30"/>'
        });
        </core_rt:forEach>

        $('#myProjectsTable').dataTable({
            pagingType: "full_numbers",
            data: result,
            "iDisplayLength": 10,
            columns: [
                {"title": "Project Name"},
                {"title": "Description"},
            ]
        });

        $('#myProjectsTable_filter').hide();
        $('#myProjectsTable_first').hide();
        $('#myProjectsTable_last').hide();
        $('#myProjectsTable_length').hide();
    });

</script>
