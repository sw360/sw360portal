<%--
  ~ Copyright Siemens AG, 2013-2017. Part of the SW360 Portal Project.
  ~ With modifications by Bosch Software Innovations GmbH, 2016.
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
  --%>

<%@include file="/html/init.jsp" %>
<%-- the following is needed by liferay to display error messages--%>
<%@include file="/html/utils/includes/errorKeyToMessage.jspf"%>
<%@ page import="com.liferay.portlet.PortletURLFactoryUtil" %>
<%@ page import="org.eclipse.sw360.datahandler.thrift.components.Component" %>
<%@ page import="org.eclipse.sw360.portal.common.PortalConstants" %>
<%@ page import="javax.portlet.PortletRequest" %>
<%@ page import="org.eclipse.sw360.datahandler.thrift.components.ComponentType" %>

<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>

<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<jsp:useBean id="componentList" type="java.util.List<org.eclipse.sw360.datahandler.thrift.components.Component>"
             scope="request"/>

<jsp:useBean id="categories" class="java.lang.String" scope="request"/>
<jsp:useBean id="languages" class="java.lang.String" scope="request"/>
<jsp:useBean id="softwarePlatforms" class="java.lang.String" scope="request"/>
<jsp:useBean id="operatingSystems" class="java.lang.String" scope="request"/>
<jsp:useBean id="componentType" class="java.lang.String" scope="request"/>
<jsp:useBean id="vendorList" class="java.lang.String" scope="request"/>
<jsp:useBean id="vendorNames" class="java.lang.String" scope="request"/>
<jsp:useBean id="mainLicenseIds" class="java.lang.String" scope="request"/>
<jsp:useBean id="name" class="java.lang.String" scope="request"/>

<core_rt:set var="programmingLanguages" value='<%=PortalConstants.PROGRAMMING_LANGUAGES%>'/>
<core_rt:set var="operatingSystemsAutoC" value='<%=PortalConstants.OPERATING_SYSTEMS%>'/>
<core_rt:set var="softwarePlatformsAutoC" value='<%=PortalConstants.SOFTWARE_PLATFORMS%>'/>

<portlet:renderURL var="addComponentURL">
    <portlet:param name="<%=PortalConstants.PAGENAME%>" value="<%=PortalConstants.PAGENAME_EDIT%>"/>
</portlet:renderURL>

<portlet:resourceURL var="deleteAjaxURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value='<%=PortalConstants.DELETE_COMPONENT%>'/>
</portlet:resourceURL>

<portlet:actionURL var="applyFiltersURL" name="applyFilters">
</portlet:actionURL>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/dataTable_Siemens.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/webjars/jquery-ui/1.12.1/jquery-ui.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/webjars/github-com-craftpip-jquery-confirm/3.0.1/jquery-confirm.min.css">
<script src="<%=request.getContextPath()%>/webjars/jquery/1.12.4/jquery.min.js"></script>
<script src="<%=request.getContextPath()%>/webjars/jquery-ui/1.12.1/jquery-ui.min.js"></script>
<script src="<%=request.getContextPath()%>/webjars/datatables/1.10.7/js/jquery.dataTables.min.js"></script>
<script src="<%=request.getContextPath()%>/webjars/github-com-craftpip-jquery-confirm/3.0.1/jquery-confirm.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/loadTags.js"></script>

<div id="header"></div>
<p class="pageHeader">
    <span class="pageHeaderBigSpan">Components</span> <span class="pageHeaderSmallSpan">(${componentList.size()})</span>
    <span class="pull-right">
          <input type="button" class="addButton" onclick="window.location.href='<%=addComponentURL%>'"
                value="Add Component">
    </span>
</p>

<div id="searchInput" class="content1">
    <form action="<%=applyFiltersURL%>" method="post">
        <table>
            <thead>
            <tr>
                <th class="infoheading">
                    Display Filter by Name
                </th>
            </tr>
            </thead>
            <tbody style="background-color: #f8f7f7; border: none;">
            <tr>
                <td>
                    <input type="text" class="searchbar"
                           id="keywordsearchinput" value=""
                           onkeyup="useSearch('keywordsearchinput')" />
                    <br/>
                    <input class="searchbutton" type="button"
                           name="searchBtn" value="Search" onclick="useSearch('keywordsearchinput')" />
                </td>
            </tr>
            </tbody>
        </table>
        <br/>
        <table>
            <thead>
            <tr>
                <th class="infoheading">
                    Filters
                </th>
            </tr>
            </thead>
            <tbody style="background-color: #f8f7f7; border: none;">
            <tr>
                <td>
                    <label for="component_name">Component Name</label>
                    <input type="text" class="searchbar filterInput" name="<portlet:namespace/><%=Component._Fields.NAME%>"
                           value="${name}" id="component_name">
                </td>
            </tr>
            <tr>
                <td>
                    <label for="categories">Categories</label>
                    <input type="text" class="searchbar filterInput" name="<portlet:namespace/><%=Component._Fields.CATEGORIES%>"
                           value="${categories}" id="categories">
                </td>
            </tr>
            <tr>
                <td>
                    <label for="component_type">Component Type</label>
                    <select class="searchbar toplabelledInput filterInput" id="component_type" name="<portlet:namespace/><%=Component._Fields.COMPONENT_TYPE%>"
                            style="min-height: 28px;">
                        <option value="<%=PortalConstants.NO_FILTER%>" class="textlabel stackedLabel">Any</option>
                        <sw360:DisplayEnumOptions type="<%=ComponentType.class%>" selectedName="${componentType}" useStringValues="true"/>
                    </select>
                </td>
            </tr>
            <tr>
                <td>
                    <label for="languages">Languages</label>
                    <input type="text" class="searchbar filterInput" name="<portlet:namespace/><%=Component._Fields.LANGUAGES%>"
                           value="${languages}" id="languages">
                </td>
            </tr>
            <tr>
                <td>
                    <label for="software_platforms">Software Platforms</label>
                    <input type="text" class="searchbar filterInput"
                           name="<portlet:namespace/><%=Component._Fields.SOFTWARE_PLATFORMS%>"
                           value="${softwarePlatforms}" id="software_platforms">
                </td>
            </tr>
            <tr>
                <td>
                    <label for="operating_systems">Operating Systems</label>
                    <input type="text" class="searchbar filterInput"
                           name="<portlet:namespace/><%=Component._Fields.OPERATING_SYSTEMS%>"
                           value="${operatingSystems}" id="operating_systems">
                </td>
            </tr>
            <tr>
                <td>
                    <label for="vendor_names">Vendors</label>
                    <input type="text" class="searchbar filterInput"
                           name="<portlet:namespace/><%=Component._Fields.VENDOR_NAMES%>"
                           value="${vendorNames}" id="vendor_names">
                </td>
            </tr>
            <tr>
                <td>
                    <label for="main_licenses">Main Licenses</label>
                    <input type="text" class="searchbar filterInput"
                           name="<portlet:namespace/><%=Component._Fields.MAIN_LICENSE_IDS%>"
                           value="${mainLicenseIds}" id="main_licenses">
                </td>
            </tr>
            </tbody>
        </table>
        <br/>
        <input type="submit" class="addButton" value="Apply Filters">
    </form>
</div>
<div id="componentsTableDiv" class="content2">
    <table id="componentsTable" cellpadding="0" cellspacing="0" border="0" class="display">
        <tfoot>
        <tr>
            <th colspan="5"></th>
        </tr>
        </tfoot>
    </table>
</div>
<div class="clear-float"></div>
<span class="pull-right">
        <select class="toplabelledInput formatSelect" id="extendedByReleases" name="extendedByReleases">
            <option value="false">Components only</option>
            <option value="true">Components with releases</option>
        </select>
        <input type="button" class="addButton" id="exportSpreadsheetButton" value="Export Spreadsheet" class="addButton" onclick="exportSpreadsheet()"/>
</span>

<script>
    var componentsTable;

    var PortletURL;
    AUI().use('liferay-portlet-url', function (A) {
        PortletURL = Liferay.PortletURL;
        load();
        $('.filterInput').on('input', function() {
            $('#exportSpreadsheetButton').prop('disabled', true);
        });
    });

    function load() {
        prepareAutocompleteForMultipleHits('languages', ${programmingLanguages});
        prepareAutocompleteForMultipleHits('software_platforms', ${softwarePlatformsAutoC});
        prepareAutocompleteForMultipleHits('operating_systems', ${operatingSystemsAutoC});
        prepareAutocompleteForMultipleHits('vendor_names', ${vendorList});
        createComponentsTable();
    }

    function createUrl_comp(paramId, paramVal) {
        var portletURL = PortletURL.createURL('<%= PortletURLFactoryUtil.create(request, portletDisplay.getId(), themeDisplay.getPlid(), PortletRequest.RENDER_PHASE) %>')
                .setParameter('<%=PortalConstants.PAGENAME%>', '<%=PortalConstants.PAGENAME_DETAIL%>').setParameter(paramId, paramVal);
        return portletURL.toString();
    }

    function createDetailURLfromComponentId(paramVal) {
        return createUrl_comp('<%=PortalConstants.COMPONENT_ID%>', paramVal);
    }

    function useSearch(buttonId) {
        var val = $.fn.dataTable.util.escapeRegex($('#' + buttonId).val());
        componentsTable.columns(1).search('^'+val, true).draw();
    }

    function exportSpreadsheet(){
        $('#keywordsearchinput').val("");
        useSearch('keywordsearchinput');

        var portletURL = PortletURL.createURL('<%= PortletURLFactoryUtil.create(request, portletDisplay.getId(), themeDisplay.getPlid(), PortletRequest.RESOURCE_PHASE) %>')
                .setParameter('<%=PortalConstants.ACTION%>', '<%=PortalConstants.EXPORT_TO_EXCEL%>');
        portletURL.setParameter('<%=Component._Fields.NAME%>', $('#component_name').val());
        portletURL.setParameter('<%=Component._Fields.CATEGORIES%>',$('#categories').val());
        portletURL.setParameter('<%=Component._Fields.LANGUAGES%>',$('#languages').val());
        portletURL.setParameter('<%=Component._Fields.SOFTWARE_PLATFORMS%>',$('#software_platforms').val());
        portletURL.setParameter('<%=Component._Fields.OPERATING_SYSTEMS%>',$('#operating_systems').val());
        portletURL.setParameter('<%=Component._Fields.VENDOR_NAMES%>',$('#vendor_names').val());
        portletURL.setParameter('<%=Component._Fields.COMPONENT_TYPE%>',$('#component_type').val());
        portletURL.setParameter('<%=Component._Fields.MAIN_LICENSE_IDS%>',$('#main_licenses').val());
        portletURL.setParameter('<%=PortalConstants.EXTENDED_EXCEL_EXPORT%>',$('#extendedByReleases').val());

        window.location.href=portletURL.toString();
    }

    function createComponentsTable() {

        var result = [];

        <core_rt:forEach items="${componentList}" var="component">
        <core_rt:set var="licenseCollectionTagOutput"><tags:DisplayLicenseCollection licenseIds="${component.mainLicenseIds}" scopeGroupId="${pageContext.getAttribute('scopeGroupId')}"/></core_rt:set>
        result.push({
            "DT_RowId": "${component.id}",
            "0": '<sw360:DisplayCollection value="${component.vendorNames}"/>',
            "1": "<a href='" + createDetailURLfromComponentId("${component.id}") + "' target='_self'><sw360:out value="${component.name}"/></a>",
            "2": "<tags:TrimLineBreaks input="${licenseCollectionTagOutput}"/>",
            "3": '<sw360:DisplayEnum value="${component.componentType}"/>',
            "4": "<a href='<portlet:renderURL ><portlet:param name="<%=PortalConstants.COMPONENT_ID%>" value="${component.id}"/><portlet:param name="<%=PortalConstants.PAGENAME%>" value="<%=PortalConstants.PAGENAME_EDIT%>"/></portlet:renderURL>'><img src='<%=request.getContextPath()%>/images/edit.png' alt='Edit' title='Edit'> </a>"
            + "<img src='<%=request.getContextPath()%>/images/Trash.png' onclick=\"deleteComponent('${component.id}', '<b>${component.name}</b>',${component.releaseIdsSize},${component.attachmentsSize})\"  alt='Delete' title='Delete'>"
        });
        </core_rt:forEach>

        componentsTable = $('#componentsTable').DataTable({
            "sPaginationType": "full_numbers",
            "iDisplayLength": 25,
            "aaData": result,
            "aoColumns": [
                {"sTitle": "Vendor"},
                {"sTitle": "Component Name"},
                {"sTitle": "Main Licenses"},
                {"sTitle": "Component Type"},
                {"title": "Actions"}
            ]
        });

        $('#componentsTable_filter').hide();
        $('#componentsTable_first').hide();
        $('#componentsTable_last').hide();
    }

    function deleteComponent(id, name, numberOfReleases, attachmentsSize) {
        function deleteComponentInternal() {
            jQuery.ajax({
                type: 'POST',
                url: '<%=deleteAjaxURL%>',
                cache: false,
                data: {
                    <portlet:namespace/>componentid: id
                },

                success: function (data) {
                    if (data.result == 'SUCCESS') {
                        componentsTable.row('#' + id).remove().draw(false);
                    }
                    else if (data.result == 'SENT_TO_MODERATOR') {
                        $.alert("You may not delete the component, but a request was sent to a moderator!");
                    }
                    else if (data.result == 'IN_USE') {
                        $.alert("I could not delete the component, since it is in use.");
                    } else {
                        $.alert("I could not delete the component.");
                    }
                },
                error: function () {
                    $.alert("I could not delete the component!");
                }
            });
        }

        if (numberOfReleases > 0) {
            $.alert("The component cannot be deleted, since it contains releases. Please delete the releases first.");
        } else {
            var confirmMessage = "Do you really want to delete the component " + name + " ?";
            confirmMessage += (attachmentsSize > 0) ? "<br/><br/>The component " + name + " contains<br/><ul><li>" + attachmentsSize + " attachments</li></ul>" : "";
            deleteConfirmed(confirmMessage, deleteComponentInternal);
        }
    }

</script>
<%@include file="/html/utils/includes/modal.jspf" %>
<%@include file="/html/utils/includes/vulnerabilityModal.jspf" %>
