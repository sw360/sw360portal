<%--
  ~ Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
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

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/external/jquery-confirm.min.css">
<script src="<%=request.getContextPath()%>/js/external/jquery-confirm.min.js" type="text/javascript"></script>
<script>
  function cleanUp() {
      function cleanUpInternal() {
          jQuery.ajax({
              type: 'POST',
              url: '<%=cleanUpURL%>',
              cache: false,
              data: "",
              success: function (data) {
                  if(data.result == 'SUCCESS')
                      $.alert("I deleted " + data.totalAffectedObjects + " of " + data.totalObjects + " total Attachments in the DB.");
                  else {
                      $.alert("I could not cleanup the attachments!");
                  }
              },
              error: function () {
                  $.alert("I could not cleanup the attachments!");
              }
          });
      }

      deleteConfirmed("Do you really want to clean up the attachment db?", cleanUpInternal);
  }
</script>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/dataTable_Siemens.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/search.css">




