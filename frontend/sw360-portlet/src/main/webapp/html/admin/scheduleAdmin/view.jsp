<%--
  ~ Copyright (c) Bosch Software Innovations GmbH 2016.
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
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">

<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<portlet:actionURL var="scheduleCvesearchURL" name="scheduleCveSearch">
</portlet:actionURL>

<portlet:actionURL var="unscheduleCvesearchURL" name="unscheduleCveSearch">
</portlet:actionURL>

<portlet:actionURL var="unscheduleAllServicesURL" name="unscheduleAllServices">
</portlet:actionURL>

<div id="header"></div>
<p class="pageHeader"><span class="pageHeaderBigSpan">Schedule Task Administration</span> </p>

<h4 class="withTopMargin">CVE Search: </h4>
<input type="button" class="addButton" onclick="window.location.href='<%=scheduleCvesearchURL%>'" value="Schedule CveSearch Updates">

<input type="button" class="addButton" onclick="window.location.href='<%=unscheduleCvesearchURL%>'" value="Cancel Scheduled CveSearch Updates">

<h4 class="withTopMargin">All Services:</h4>

<input type="button" class="addButton" onclick="window.location.href='<%=unscheduleAllServicesURL%>'" value="Cancel All Scheduled Tasks">

