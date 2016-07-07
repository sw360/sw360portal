<%--
  ~ Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
  --%>


<%@ page import="com.siemens.sw360.portal.common.PortalConstants" %>
<%@include file="/html/init.jsp"%>




<portlet:defineObjects />
<liferay-theme:defineObjects />

<%@ page import="com.siemens.sw360.datahandler.thrift.projects.Project" %>

<jsp:useBean id="projectSearch" type="java.util.List<com.siemens.sw360.datahandler.thrift.projects.Project>" class="java.util.ArrayList" scope="request"/>

<core_rt:if test="${projectSearch.size()>0}" >
    <core_rt:forEach items="${projectSearch}" var="entry">
        <tr>
            <td><input type="checkbox" name="<portlet:namespace/>projectId" value="${entry.id}"></td>
            <td><sw360:ProjectName project="${entry}"/></td>
            <td><sw360:DisplayUserEmail email="${entry.projectResponsible}"/></td>
            <td><sw360:out value="${entry.description}"/></td>
        </tr>
    </core_rt:forEach>
</core_rt:if>
<core_rt:if test="${projectSearch.size() == 0}">
    <tr><td colspan="3">
        No project found with your search.
    </td></tr>

</core_rt:if>