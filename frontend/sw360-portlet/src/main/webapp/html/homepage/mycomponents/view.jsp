<%@ page import="com.siemens.sw360.portal.common.PortalConstants" %>
<%@ page import="com.siemens.sw360.portal.portlets.Sw360Portlet" %>
<%@ page import="com.siemens.sw360.portal.portlets.components.ComponentPortlet" %>
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
            <core_rt:choose>
                <core_rt:when test="${component.releases.size() > 1}">
                    releasesInfo = '<sw360:out value="${component.releases.size()} releases"/>';
                </core_rt:when>
                <core_rt:otherwise>
                    releasesInfo = 'no release';
                    <core_rt:if test="${component.releases.size() == 1}">
                        releasesInfo = '<sw360:out value="${component.releases[0].name}"/>';
                    </core_rt:if>
                </core_rt:otherwise>
            </core_rt:choose>

            result.push({
                "DT_RowId": "${component.id}",
                "0": "<sw360:DisplayComponentLink component="${component}"/>",
                "1": '<sw360:out value="${component.description}" maxChar="30"/>',
                "2": releasesInfo
            });

        </core_rt:forEach>

        $('#myComponentsTable').dataTable({
            pagingType: "full_numbers",
            data: result,
            "iDisplayLength": 10,
            columns: [
                {"title": "Component Name"},
                {"title": "Description"},
                {"title": "Releases"}
            ]
        });

        $('#myComponentsTable_filter').hide();
        $('#myComponentsTable_first').hide();
        $('#myComponentsTable_last').hide();
        $('#myComponentsTable_length').hide();
    });

</script>
