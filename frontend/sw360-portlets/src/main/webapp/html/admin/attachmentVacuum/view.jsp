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

<%@include file="/html/init.jsp" %>
<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<portlet:resourceURL var="cleanUpURL" >
  <portlet:param name="<%=PortalConstants.ACTION%>" value='<%=PortalConstants.CLEANUP%>'/>
</portlet:resourceURL>
<div id="header"></div>
<p class="pageHeader"><span class="pageHeaderBigSpan">Attachment DB Administration</span> </p>

<table class="info_table">
  <thead>
  <tr>
    <th colspan="2"> Actions</th>
  </tr>
  </thead>

  <tbody>
  <tr>
    <td>Clean up Attachments</td>
    <td> <img src="<%=request.getContextPath()%>/images/Trash.png" alt="CleanUp" onclick="cleanUp()">
    </td>
  </tr>
  </tbody>
</table>
<br/>

<script>
  function cleanUp() {
    if (confirm("Do you really want to clean up the attachment db?")) {

      jQuery.ajax({
        type: 'POST',
        url: '<%=cleanUpURL%>',
        cache: false,
        data: "",
        success: function (data) {
          if(data.result == 'SUCCESS')
            alert("I deleted " + data.totalAffectedObjects + " of " + data.totalObjects + " total Attachments in the DB.");
          else {
            alert("I could not cleanup the attachments!");
          }
        },
        error: function () {
          alert("I could not cleanup the attachments!");
        }
      });
    }

  }
</script>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/dataTable_Siemens.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/search.css">




