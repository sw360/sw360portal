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
<%-- the following is needed by liferay to display error messages--%>
<%@include file="/html/utils/includes/errorKeyToMessage.jspf"%>
<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<portlet:resourceURL var="getDuplicatesURL" >
  <portlet:param name="<%=PortalConstants.ACTION%>" value='<%=PortalConstants.DUPLICATES%>'/>
</portlet:resourceURL>

<script src="<%=request.getContextPath()%>/js/external/jquery-1.11.1.min.js"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery-ui.min.js"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery.dataTables.js"></script>

<div id="header"></div>
<p class="pageHeader"><span class="pageHeaderBigSpan">DB Administration</span> </p>

<table class="info_table">
  <thead>
  <tr>
    <th colspan="2"> Actions</th>
  </tr>
  </thead>

  <tbody>
  <tr>
    <td>Search DB for duplicate identifiers</td>
    <td> <img src="<%=request.getContextPath()%>/images/search.png" alt="CleanUp" onclick="findDuplicates()" width="25px" height="25px" >
    </td>
  </tr>
  </tbody>
</table>

<div id="DuplicateSearch">
</div>

<br/>

<script>
function findDuplicates(){

  var field =   $('#DuplicateSearch');
    field.html("Looking for duplicate identifiers ... ");
  jQuery.ajax({
    type: 'POST',
    url: '<%=getDuplicatesURL%>',
    cache: false,
    data: "",
    success: function (data) {
      var html;
      if(data.result == 'SUCCESS')
        html = "No duplicate identifiers were found";
      else if( data.result == 'FAILURE'){
        html = "Error in looking for duplicate identifiers";
      } else {
        html = data;
      }
      field.html(html);
      setupPagination('#duplicateReleasesTable');
      setupPagination('#duplicateReleaseSourcesTable');
      setupPagination('#duplicateComponentsTable');
      setupPagination('#duplicateProjectsTable');
    },
    error: function () {
      html = "Error in looking for duplicate identifiers";
      field.html(html);
    }
  });
}
function setupPagination(tableId){
    if ($(tableId)){
        $(tableId).dataTable({
            "sPaginationType": "full_numbers"
        });

        $(tableId+'_filter').hide();
        $(tableId+'_first').hide();
        $(tableId+'_last').hide();
    }
}
</script>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/dataTable_Siemens.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/search.css">
