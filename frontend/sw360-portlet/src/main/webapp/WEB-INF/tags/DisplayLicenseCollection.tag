<%--
  ~ Copyright Siemens AG, 2016. Part of the SW360 Portal Project.
  ~ With modifications by Bosch Software Innovations GmbH, 2016.
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
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
