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

<jsp:useBean id="licenseList" type="java.util.List<com.siemens.sw360.datahandler.thrift.licenses.License>" scope="request"/>

<core_rt:if test="${licenseList.size()>0}">
    <core_rt:forEach items="${licenseList}" var="entry">

        <tr>
            <td>
                <input type="checkbox" name="id" value="<sw360:out value="${entry.id}, ${entry.fullname}"/>"/>
            </td>
            <td><sw360:out value="${entry.fullname}"/></td>
        </tr>
    </core_rt:forEach>
</core_rt:if>
<core_rt:if test="${licenseList.size() == 0}">
    <tr>
        <td colspan="2">
            No user found with your search.
        </td>
    </tr>

</core_rt:if>