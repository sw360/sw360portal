<%--
  ~ Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
  ~ With contributions by Bosch Software Innovations GmbH, 2016.
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
  --%>

<%@include file="/html/init.jsp" %>
<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<%-- Note that the necessary includes are in life-ray-portlet.xml --%>

<jsp:useBean id="components" type="java.util.List<com.siemens.sw360.datahandler.thrift.components.Component>"
             class="java.util.ArrayList" scope="request"/>

<div class="homepageheading">
    My Components
</div>

<div id="myComponentsDiv" class="homepageListingTable">
    <table id="myComponentsTable" cellpadding="0" cellspacing="0" border="0" class="display">
    </table>
</div>

<script>
    //This can not be document ready function as liferay definitions need to be loaded first
    $(window).load(function () {
        var result = [];
        var releasesInfo = '';

        <core_rt:forEach items="${components}" var="component">

        result.push({
            "DT_RowId": "${component.id}",
            "0": "<sw360:DisplayComponentLink component="${component}"/>",
            "1": '<sw360:out value="${component.description}" maxChar="30"/>'
        });

        </core_rt:forEach>

        $('#myComponentsTable').dataTable({
            pagingType: "full_numbers",
            data: result,
            "iDisplayLength": 10,
            columns: [
                {"title": "Component Name"},
                {"title": "Description"}
            ]
        });

        $('#myComponentsTable_filter').hide();
        $('#myComponentsTable_first').hide();
        $('#myComponentsTable_last').hide();
        $('#myComponentsTable_length').hide();
    });

</script>
