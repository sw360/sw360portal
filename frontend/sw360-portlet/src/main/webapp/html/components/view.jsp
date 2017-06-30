<%--
  ~ Copyright Siemens AG, 2013-2017. Part of the SW360 Portal Project.
  ~ With modifications by Bosch Software Innovations GmbH, 2016.
  ~
  ~ SPDX-License-Identifier: EPL-1.0
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
<jsp:useBean id="viewSize" type="java.lang.Integer" scope="request"/>
<jsp:useBean id="totalRows" type="java.lang.Integer" scope="request"/>

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
    <span class="pageHeaderBigSpan">Components</span>
    <span class="pageHeaderMediumSpan">(<core_rt:if test="${componentList.size() == totalRows}">${totalRows}</core_rt:if><core_rt:if test="${componentList.size() != totalRows}">${componentList.size()} latest of ${totalRows}</core_rt:if>)</span>
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
                    Loading
                </th>
            </tr>
            </thead>
            <tbody style="background-color: #f8f7f7; border: none;">
            <tr>
                <td>
                    <select class="searchbar" id="view_size" name="<portlet:namespace/><%=PortalConstants.VIEW_SIZE%>" onchange="reloadViewSize()">
                        <option value="200" <core_rt:if test="${viewSize == 200}">selected</core_rt:if>>200 latest</option>
                        <option value="500" <core_rt:if test="${viewSize == 500}">selected</core_rt:if>>500 latest</option>
                        <option value="1000" <core_rt:if test="${viewSize == 1000}">selected</core_rt:if>>1000 latest</option>
                        <option value="-1" <core_rt:if test="${viewSize == -1}">selected</core_rt:if>>All</option>
                    </select>
                </td>
            </tr>
            </tbody>
        </table>
        <table>
            <thead>
            <tr>
                <th class="infoheading">
                    Quick Filter
                </th>
            </tr>
            </thead>
            <tbody style="background-color: #f8f7f7; border: none;">
            <tr>
                <td>
                    <input type="text" class="searchbar"
                           id="keywordsearchinput" value=""
                           onkeyup="useSearch('keywordsearchinput')" />
                </td>
            </tr>
            </tbody>
        </table>
        <br/>
        <table>
            <thead>
            <tr>
                <th class="infoheading">
                    Advanced Search
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
                    <select class="searchbar toplabelledInput filterInput" id="component_type" name="<portlet:namespace/><%=Component._Fields.COMPONENT_TYPE%>">
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
        <input type="submit" class="addButton" value="Search">
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
        componentsTable.columns(1).search(val, true).draw();
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

    function reloadViewSize(){
        var portletURL = PortletURL.createURL('<%= PortletURLFactoryUtil.create(request, portletDisplay.getId(), themeDisplay.getPlid(), PortletRequest.RENDER_PHASE) %>');
        portletURL.setParameter('<%=PortalConstants.VIEW_SIZE%>', $('#view_size').val());
        window.location.href=portletURL.toString();
    }

    function createComponentsTable() {

        var result = [];

        <core_rt:forEach items="${componentList}" var="component">
        <core_rt:set var="licenseCollectionTagOutput"><sw360:DisplayLicenseCollection licenseIds="${component.mainLicenseIds}" scopeGroupId="${pageContext.getAttribute('scopeGroupId')}"/></core_rt:set>
        result.push({
            "DT_RowId": "${component.id}",
            "id": "${component.id}",
            "vndrs": '<sw360:DisplayCollection value="${component.vendorNames}"/>',
            "name": "${component.name}",
            "lics": "<tags:TrimLineBreaks input="${licenseCollectionTagOutput}"/>",
            "cType": '<sw360:DisplayEnum value="${component.componentType}"/>',
            "lRelsSize": "${component.releaseIdsSize}",
            "attsSize": "${component.attachmentsSize}"
        });
        </core_rt:forEach>

        componentsTable = $('#componentsTable').DataTable({
            "pagingType": "simple_numbers",
            dom: "lrtip",
            "pageLength": 25,
            "data": result,
            "columns": [
                {"title": "Vendor", data: "vndrs"},
                {"title": "Component Name", data: "name", render: {display: renderComponentNameLink}},
                {"title": "Main Licenses", data: "lics"},
                {"title": "Component Type", data: "cType"},
                {"title": "Actions", data: "id", render: {display: renderComponentActions}}
            ],
            order: [[1, 'asc']],
            language: {
                lengthMenu: "_MENU_ entries per page"
            }
        });
    }

    function renderComponentActions(id, type, row) {
        <%--TODO most of this can be simplified to CSS properties --%>
        return "<span id='componentAction" + id + "'></span>"
            + renderLinkTo(
                makeComponentUrl(id, '<%=PortalConstants.PAGENAME_EDIT%>'),
                "",
                "<img src='<%=request.getContextPath()%>/images/edit.png' alt='Edit' title='Edit'>")
            + renderLinkTo(
                makeComponentUrl(id, '<%=PortalConstants.PAGENAME_DUPLICATE%>'),
                "",
                "<img src='<%=request.getContextPath()%>/images/ic_clone.png' alt='Duplicate' title='Duplicate'>")
            + "<img src='<%=request.getContextPath()%>/images/Trash.png'" +
            " onclick=\"deleteComponent('" + id + "', '<b>" + replaceSingleQuote(row.name) + "</b>'," + replaceSingleQuote(row.lRelsSize) + "," + replaceSingleQuote(row.attsSize) + ")\" alt='Delete' title='Delete'/>";
    }

    function renderComponentNameLink(name, type, row) {
        return renderLinkTo(makeComponentUrl(row.id, '<%=PortalConstants.PAGENAME_DETAIL%>'), name);
    }

    function makeComponentUrl(componentId, page) {
        var portletURL = PortletURL.createURL('<%= PortletURLFactoryUtil.create(request, portletDisplay.getId(), themeDisplay.getPlid(), PortletRequest.RENDER_PHASE) %>')
            .setParameter('<%=PortalConstants.PAGENAME%>', page)
            .setParameter('<%=PortalConstants.COMPONENT_ID%>', componentId);
        return portletURL.toString();
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
