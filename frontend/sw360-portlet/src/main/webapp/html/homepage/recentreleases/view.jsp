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
<%@ page import="com.siemens.sw360.portal.common.PortalConstants" %>
<%@ page import="com.siemens.sw360.portal.portlets.Sw360Portlet" %>
<%@ page import="com.siemens.sw360.portal.portlets.components.ComponentPortlet" %>

<%@include file="/html/init.jsp" %>
<portlet:defineObjects/>
<liferay-theme:defineObjects/>


<%@ taglib prefix="sw360" uri="/WEB-INF/customTags.tld" %>

<jsp:useBean id="releases" type="java.util.List<com.siemens.sw360.datahandler.thrift.components.Release>"
             class="java.util.ArrayList" scope="request"/>

<div id="recentReleasesDiv">
    <h4>
        Recent Releases
    </h4>
    <div class="homepageListingRight">
        <core_rt:if test="${releases.size() > 0 }">
            <core_rt:forEach var="release" items="${releases}">
                <li style="color: red">
                    <sw360:DisplayReleaseLink release="${release}"/>
                </li>
            </core_rt:forEach>
        </core_rt:if>
        <core_rt:if test="${releases.size() == 0}">
            <p style="color: red">No recent releases</p>
        </core_rt:if>
    </div>
</div>


