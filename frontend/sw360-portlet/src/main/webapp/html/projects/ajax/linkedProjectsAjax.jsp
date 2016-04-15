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

<%@ page import="com.liferay.portlet.PortletURLFactoryUtil" %>
<%@include file="/html/init.jsp" %>


<%@ taglib prefix="sw360" uri="/WEB-INF/customTags.tld" %>

<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<%@ page import="com.siemens.sw360.datahandler.thrift.projects.ProjectLink" %>
<%@ page import="com.siemens.sw360.datahandler.thrift.projects.Project" %>
<%@ page import="com.siemens.sw360.datahandler.thrift.projects.ProjectRelationship" %>
<jsp:useBean id="projectList" type="java.util.List<com.siemens.sw360.datahandler.thrift.projects.ProjectLink>"  scope="request"/>


<core_rt:forEach items="${projectList}" var="projectLink" varStatus="loop">
    <tr id="projectLinkRow${loop.count}" >
        <td width="32%">
            <input type="hidden" value="${projectLink.id}" name="<portlet:namespace/><%=Project._Fields.LINKED_PROJECTS%><%=ProjectLink._Fields.ID%>">
            <label class="textlabel stackedLabel" for="projectName">Project name</label>
            <input id="projectName" type="text" class="toplabelledInput" placeholder="Enter project"
                   value="<sw360:out value="${projectLink.name}"/>" readonly onclick="window.location='<sw360:DisplayProjectLink projectId="${projectLink.id}" bare="true"/>'"/>
        </td>
        <td width="32%">
            <input type="hidden" value="${projectLink.version}" name="<portlet:namespace/><%=ProjectLink._Fields.VERSION%>">
            <label class="textlabel stackedLabel" for="projectVersion">Project version</label>
            <input id="projectVersion" type="text" class="toplabelledInput" placeholder="No project version"
                   value="<sw360:out value="${projectLink.version}"/>" readonly/>
        </td>
        <td width="32%">
            <label class="textlabel stackedLabel" for="projectRelation">Project relation</label>
            <select class="toplabelledInput" id="projectRelation"
                    name="<portlet:namespace/><%=Project._Fields.LINKED_PROJECTS%><%=ProjectLink._Fields.RELATION%>"
                    style="min-width: 162px; min-height: 28px;">

                <sw360:DisplayEnumOptions type="<%=ProjectRelationship.class%>" selected="${projectLink.relation}"/>
            </select>
        </td>

        <td class="deletor">
            <img src="<%=request.getContextPath()%>/images/Trash.png" onclick="deleteProjectLink('projectLinkRow${loop.count}')" alt="Delete">
        </td>

    </tr>
</core_rt:forEach>
