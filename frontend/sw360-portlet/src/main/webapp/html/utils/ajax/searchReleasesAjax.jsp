<%--
  ~ Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
  --%>

<%@include file="/html/init.jsp"%>

<portlet:defineObjects />
<liferay-theme:defineObjects />

<%@ page import="com.siemens.sw360.datahandler.thrift.components.Release" %>

<jsp:useBean id="releaseSearch" type="java.util.List<com.siemens.sw360.datahandler.thrift.components.Release>" class="java.util.ArrayList" scope="request"/>

<core_rt:if test="${releaseSearch.size()>0}" >
    <core_rt:forEach items="${releaseSearch}" var="entry">
        <tr>
            <td><input type="checkbox" name="<portlet:namespace/>releaseid" value="${entry.id}"></td>
            <td><sw360:out value="${entry.vendor.fullname}"/></td>
            <td><sw360:out value="${entry.name}"/></td>
            <td><sw360:out value="${entry.version}"/></td>
        </tr>
    </core_rt:forEach>
</core_rt:if>
<core_rt:if test="${releaseSearch.size() == 0}">
    <tr><td colspan="4">
        No releases found with your search.
    </td></tr>

</core_rt:if>