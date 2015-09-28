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


<%@ taglib prefix="sw360" uri="/WEB-INF/customTags.tld" %>


<%@ page import="com.siemens.sw360.datahandler.thrift.components.Release" %>
<%@ page import="com.siemens.sw360.portal.common.PortalConstants" %>

<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<jsp:useBean id="releasesAndProjects" type="java.util.Map<com.siemens.sw360.datahandler.thrift.components.Release, java.lang.String>" scope="request"/>

<core_rt:if test="${releasesAndProjects.size()>0}">
    <core_rt:forEach items="${releasesAndProjects.entrySet()}" var="releaseAndProjectString" varStatus="loop">
        <tr id="release${loop.count}" class="tr_clone">
            <td >
                <label ><input type="checkbox" name="<portlet:namespace/><%=PortalConstants.RELEASE_ID%>" value="${releaseAndProjectString.key.id}" checked=""><sw360:ReleaseName release="${releaseAndProjectString.key}"/></label>
            </td>
            <td>
                    <sw360:out value="${releaseAndProjectString.value}"/>
            </td>
        </tr>
    </core_rt:forEach>
</core_rt:if>

<core_rt:if test="${releasesAndProjects.size()==0}">
    <tr class="tr_clone">
        <td >
            No releases linked to this project or its linked projects
        </td>

    </tr>
</core_rt:if>