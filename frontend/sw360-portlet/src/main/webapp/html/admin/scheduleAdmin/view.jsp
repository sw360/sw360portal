<%--
  ~ Copyright (c) Bosch Software Innovations GmbH 2016.
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
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

