<%--
  ~ Copyright Siemens AG, 2016. Part of the SW360 Portal Project.
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
<%@ attribute name="licenseIds" type="java.util.Collection" required="true" %>
<%@ attribute name="scopeGroupId" type="java.lang.Long" required="false" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="core_rt"%>
<%@ taglib prefix="sw360" uri="/WEB-INF/customTags.tld" %>
<core_rt:if test="${not empty licenseIds}">
    <core_rt:forEach items="${licenseIds}" var="licenseId" varStatus="licIndex">
        <sw360:DisplayLicenseLink licenseId="${licenseId}" scopeGroupId="${scopeGroupId}"/><core_rt:if test="${not licIndex.last}">, </core_rt:if>
    </core_rt:forEach>
</core_rt:if>
