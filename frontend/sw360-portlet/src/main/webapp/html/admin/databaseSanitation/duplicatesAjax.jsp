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

<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<jsp:useBean id="duplicateReleases" type="java.util.Map<java.lang.String,  java.util.List<java.lang.String>>"
             scope="request"/>
<jsp:useBean id="duplicateReleaseSources" type="java.util.Map<java.lang.String,  java.util.List<java.lang.String>>"
             scope="request"/>
<jsp:useBean id="duplicateComponents" type="java.util.Map<java.lang.String,  java.util.List<java.lang.String>>"
             scope="request"/>

<jsp:useBean id="duplicateProjects" type="java.util.Map<java.lang.String,  java.util.List<java.lang.String>>"
             scope="request"/>
<core_rt:if test="${duplicateReleases.size()>0}">
            <h4>Releases with the same identifier [name(version)]</h4>
    <table id="duplicateReleasesTable" class="display">
        <thead>
        <tr>
            <th>Release Name</th><th>Links</th>
        </tr>
        </thead>
        <tbody>
        <core_rt:forEach items="${duplicateReleases.entrySet()}" var="duplicate">
            <tr>
                <td>${duplicate.key}</td>
                <td>
                    <core_rt:forEach items="${duplicate.value}" var="id" varStatus="loop">
                        <sw360:DisplayReleaseLink releaseId="${id}"
                                                  showName="false">${loop.count}</sw360:DisplayReleaseLink>&nbsp;
                    </core_rt:forEach>
                </td>
            </tr>
        </core_rt:forEach>
        </tbody>
    </table>
</core_rt:if>
<core_rt:if test="${duplicateReleaseSources.size()>0}">
    <h4>Releases with more than one source attachment</h4>
    <table id="duplicateReleaseSourcesTable" class="display">
        <thead>
        <tr>
            <th>Release Name</th><th>Source Attachments Count</th>
        </tr>
        </thead>
        <tbody>
        <core_rt:forEach items="${duplicateReleaseSources.entrySet()}" var="duplicate">
            <tr>
                <td>   <sw360:DisplayReleaseLink releaseId="${duplicate.value.get(0)}"
                                                 showName="false">${duplicate.key}</sw360:DisplayReleaseLink></td>
                <td>
                    <sw360:out value="${duplicate.value.size()}"/>
                </td>
            </tr>
        </core_rt:forEach>
        </tbody>
    </table>
</core_rt:if>
<core_rt:if test="${duplicateComponents.size()>0}">
    <h4>Components with the same identifier [name]</h4>
    <table id="duplicateComponentsTable" class="display">
        <thead>
        <tr>
            <th>Component Name</th><th>Links</th>
        </tr>
        </thead>
        <tbody>
        <core_rt:forEach items="${duplicateComponents.entrySet()}" var="duplicate">
            <tr>
                <td>${duplicate.key}</td>
                <td>
                    <core_rt:forEach items="${duplicate.value}" var="id" varStatus="loop">
                        <sw360:DisplayComponentLink componentId="${id}"
                                                  showName="false">${loop.count}</sw360:DisplayComponentLink>&nbsp;
                    </core_rt:forEach>
                </td>
            </tr>
        </core_rt:forEach>
        </tbody>
    </table>
</core_rt:if>
<core_rt:if test="${duplicateProjects.size()>0}">
    <h4>Projects with the same identifier [name(version)]</h4>
    <table id="duplicateProjectsTable" class="display">
        <thead>
        <tr>
            <th>Project Name</th><th>Links</th>
        </tr>
        </thead>
        <tbody>
        <core_rt:forEach items="${duplicateProjects.entrySet()}" var="duplicate">
            <tr>
                <td>${duplicate.key}</td>
                <td>
                    <core_rt:forEach items="${duplicate.value}" var="id" varStatus="loop">
                        <sw360:DisplayProjectLink projectId="${id}"
                                                    showName="false">${loop.count}</sw360:DisplayProjectLink>&nbsp;
                    </core_rt:forEach>
                </td>
            </tr>
        </core_rt:forEach>
        </tbody>
    </table>
</core_rt:if>