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

<%@ page import="com.liferay.portlet.PortletURLFactoryUtil" %>
<%@ page import="com.siemens.sw360.datahandler.thrift.projects.Project" %>
<%@ page import="com.siemens.sw360.portal.common.PortalConstants" %>
<%@ page import="javax.portlet.PortletRequest" %>

<portlet:defineObjects/>
<liferay-theme:defineObjects/>


<portlet:resourceURL var="exportProjectsURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value="<%=PortalConstants.EXPORT_TO_EXCEL%>"/>
</portlet:resourceURL>


<portlet:resourceURL var="projectListAjaxURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value='<%=PortalConstants.PROJECT_LIST%>'/>
</portlet:resourceURL>
<portlet:resourceURL var="deleteAjaxURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value='<%=PortalConstants.REMOVE_PROJECT%>'/>
</portlet:resourceURL>

<portlet:renderURL var="addProjectURL">
    <portlet:param name="<%=PortalConstants.PAGENAME%>" value="<%=PortalConstants.PAGENAME_EDIT%>"/>
</portlet:renderURL>


<portlet:resourceURL var="projectReleasesAjaxURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value='<%=PortalConstants.FOSSOLOGY_GET_SENDABLE%>'/>
</portlet:resourceURL>


<portlet:resourceURL var="projectReleasesSendURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value='<%=PortalConstants.FOSSOLOGY_SEND%>'/>
</portlet:resourceURL>

<portlet:actionURL var="applyFiltersURL" name="applyFilters">
</portlet:actionURL>

<div id="header"></div>
<p class="pageHeader">
    <span class="pageHeaderBigSpan">Projects</span>
    <span class="pull-right">
        <input type="button" id="exportbutton" class="addButton"
               value="Export Projects">
        <input type="button" class="addButton" onclick="window.location.href='<%=addProjectURL%>'" value="Add Project">
    </span>
</p>


<div id="searchInput" class="content1">
    <table>
        <thead>
        <tr>
            <th class="infoheading">
                Keyword Search
            </th>
        </tr>
        </thead>
        <tbody style="background-color: #f8f7f7; border: none;">
        <tr>
            <td>
                <input type="text" style="width: 90%; padding: 5px; color: gray;height:20px;"
                       id="keywordsearchinput" value="" onkeyup="useSearch('keywordsearchinput')">
                <br/>
                <input style="padding: 5px 20px 5px 20px; border: none; font-weight:bold;" type="button"
                       name="searchBtn" value="Search" onclick="useSearch('keywordsearchinput')">
            </td>
        </tr>
        </tbody>
    </table>
</div>
<div id="projectsTableDiv" class="content2">
    <table id="projectsTable" cellpadding="0" cellspacing="0" border="0" class="display">
        <tfoot>
        <tr>
            <th colspan="6"></th>
        </tr>
        </tfoot>
    </table>
</div>


<div id="fossologyClearing" title="Fossology Clearing" style="display: none; background-color: #ffffff;">
    <form id="fossologyClearingForm" action="${projectReleasesSendURL}" method="post">
        <input id="projectId" name="<portlet:namespace/><%=PortalConstants.PROJECT_ID%>" hidden="" value=""/>

        <table id="fossologyClearingTable">
            <tbody>
            </tbody>
        </table>
        <input type="button" onclick="selectAll(this.parentNode)" value="Select all">
        <input type="button" onclick="closeOpenDialogs()" value="Close"/>
        <input type="submit" value="Send"/>
    </form>
</div>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/external/jquery-ui.css">
<script src="<%=request.getContextPath()%>/js/external/jquery-1.11.1.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery-ui.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery.dataTables.js" type="text/javascript"></script>


<script>
    var oTable;

    var PortletURL;
    AUI().use('liferay-portlet-url', function (A) {
        PortletURL = Liferay.PortletURL;
        createProjectsTable();
        $('#exportbutton').click(exportExcel);
    });

    function useSearch(buttonId) {
        //we only want to search names starting with the value in the search box
        var val = $.fn.dataTable.util.escapeRegex($('#' + buttonId).val());
        oTable.columns(0).search('^' + val, true).draw();
    }

    function makeProjectUrl(projectId, page) {
        var portletURL = PortletURL.createURL('<%= PortletURLFactoryUtil.create(request, portletDisplay.getId(), themeDisplay.getPlid(), PortletRequest.RENDER_PHASE) %>')
                .setParameter('<%=PortalConstants.PAGENAME%>', page)
                .setParameter('<%=PortalConstants.PROJECT_ID%>', projectId);
        return portletURL.toString();
    }

    function renderProjectActions(id, type, row) {
        <%--TODO most of this can be simplified to CSS properties --%>
        return "<img src='<%=request.getContextPath()%>/images/fossology-logo-24.gif'/" +
                " onclick='openSelectClearingDialog(\"" + id + "\", \"projectAction" + id + "\")' " +
                " alt='SelectClearing' title='send to Fossology'>" +
                "<span id='projectAction" + id + "'></span>"
                + renderLinkTo(
                        makeProjectUrl(id, '<%=PortalConstants.PAGENAME_EDIT%>'),
                        "",
                        "<img src='<%=request.getContextPath()%>/images/edit.png' alt='Edit' title='Edit'>")
                + renderLinkTo(
                        makeProjectUrl(id, '<%=PortalConstants.PAGENAME_DUPLICATE%>'),
                        "",
                        "<img src='<%=request.getContextPath()%>/images/ic_clone.png' alt='Duplicate' title='Duplicate'>")
                + "<img src='<%=request.getContextPath()%>/images/Trash.png'" +
                " onclick=\"deleteProject('" + id + "', '" + replaceSingleQuote(row.name) + "')\" alt='Delete' title='Delete'/>";
    }

    function renderProjectNameLink(name, type, row) {
        return renderLinkTo(makeProjectUrl(row.id, '<%=PortalConstants.PAGENAME_DETAIL%>'), name);
    }

    function renderClearingStatus(clearingStatus, type, row) {
        return "<span title='new releases, under clearing, under clearing by the project clearing team, report available'>" +
                clearingStatus.newRelease + " " +
                clearingStatus.underClearing + " " +
                clearingStatus.underClearingByProjectTeam + " " +
                clearingStatus.reportAvailable +
                "</span>";
    }

    function createProjectsTable() {
        oTable = $('#projectsTable').DataTable({
            ajax: {url: '<%=projectListAjaxURL%>', data: objectNamespacerOf('<portlet:namespace/>')},
            columns: [
                {title: "Project Name", data: "name", render: {display: renderProjectNameLink}},
                {title: "Description", data: "description", render: {display: displayEscaped}},
                {title: "Project Responsible", data: "responsible", render: {display: renderUserEmail}},
                {title: "State", data: "state", render: {display: displayEscaped}},
                {title: "Clearing Status", data: "clearing", render: {display: renderClearingStatus}},
                {title: "Actions", data: "id", render: {display: renderProjectActions}}
            ],
            pagingType: "full_numbers",
            search: {smart: false}
        });

        $('#projectsTable_filter').hide();
        $('#projectsTable_first').hide();
        $('#projectsTable_last').hide();
    }

    function exportExcel() {
        var portletURL = PortletURL.createURL('<%= PortletURLFactoryUtil.create(request, portletDisplay.getId(), themeDisplay.getPlid(), PortletRequest.RESOURCE_PHASE) %>')
                .setParameter('<%=PortalConstants.ACTION%>', '<%=PortalConstants.EXPORT_TO_EXCEL%>');
        portletURL.setParameter('<%=PortalConstants.KEY_SEARCH_TEXT%>', $('#keywordsearchinput').val());

        window.location.href = portletURL.toString();

    }


    function openSelectClearingDialog(projectId, fieldId) {
        $('#projectId').val(projectId);

        setFormSubmit(fieldId);
        fillClearingFormAndOpenDialog(projectId);
    }

    function deleteProject(projectId, name) {

        if (confirm("Do you want to delete project " + name + " ?")) {

            jQuery.ajax({
                type: 'POST',
                url: '<%=deleteAjaxURL%>',
                cache: false,
                data: {
                    "<portlet:namespace/><%=PortalConstants.PROJECT_ID%>": projectId
                },
                success: function (data) {
                    if (data.result == 'SUCCESS') {
                        oTable.ajax.reload();
                    }
                    else if (data.result == 'SENT_TO_MODERATOR') {
                        alert("You may not delete the project, but a request was sent to a moderator!");
                    } else if (data.result == 'IN_USE') {
                        alert("The project is used by another project!");
                    }
                    else {
                        alert("I could not delete the project!");
                    }
                },
                error: function () {
                    alert("I could not delete the project!");
                }
            });

        }
    }


    function fillClearingFormAndOpenDialog(projectId) {
        jQuery.ajax({
            type: 'POST',
            url: '<%=projectReleasesAjaxURL%>',
            cache: false,
            data: {
                "<portlet:namespace/><%=PortalConstants.PROJECT_ID%>": projectId
            },
            success: function (data) {
                $('#fossologyClearingTable').find('tbody').html(data);
                openDialog('fossologyClearing', 'fossologyClearingForm', .4, .5);
            },
            error: function () {
                alert("I could not get any releases!");
            }
        });
    }

    function setFormSubmit(fieldId) {
        $('#fossologyClearingForm').submit(function (e) {
            e.preventDefault();
            closeOpenDialogs();

            jQuery.ajax({
                type: 'POST',
                url: '<%=projectReleasesSendURL%>',
                cache: false,
                data: $('form#fossologyClearingForm').serialize(),
                success: function (data) {
                    if (data.result) {
                        if (data.result == "FAILURE") {
                            $('#' + fieldId).html("Error");
                        }
                        else {
                            $('#' + fieldId).html("Sent");
                        }
                    }
                },
                error: function () {
                    alert("I could not upload the files");
                }
            })

        });

    }

    function selectAll(form) {
        $(form).find(':checkbox').prop("checked", true);
    }

</script>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/dataTable_Siemens.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">
