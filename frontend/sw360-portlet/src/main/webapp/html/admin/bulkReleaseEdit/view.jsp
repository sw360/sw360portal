<%--
  ~ Copyright Siemens AG, 2013-2016. Part of the SW360 Portal Project.
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
--%>
<%@ page import="org.eclipse.sw360.portal.common.PortalConstants" %>


<%@include file="/html/init.jsp" %>
<%-- the following is needed by liferay to display error messages--%>
<%@include file="/html/utils/includes/errorKeyToMessage.jspf"%>
<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<jsp:useBean id="releaseList" type="java.util.List<org.eclipse.sw360.datahandler.thrift.components.Release>"
             scope="request"/>

<%--TODO--%>
<portlet:resourceURL var="viewVendorURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value="<%=PortalConstants.VIEW_VENDOR%>"/>
</portlet:resourceURL>


<portlet:resourceURL var="updateReleaseURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value="<%=PortalConstants.RELEASE%>"/>
</portlet:resourceURL>


<link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/webjars/jquery-ui/1.12.1/jquery-ui.css">
<script src="<%=request.getContextPath()%>/webjars/jquery/1.12.4/jquery.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/webjars/jquery-validation/1.15.1/jquery.validate.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/webjars/jquery-validation/1.15.1/additional-methods.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/webjars/jquery-ui/1.12.1/jquery-ui.min.js"></script>
<script src="<%=request.getContextPath()%>/webjars/datatables/1.10.7/js/jquery.dataTables.min.js"></script>


<div id="header"></div>
<p class="pageHeader"><span class="pageHeaderBigSpan">Release Bulk Edit</span>
</p>
<div id="searchInput" class="content1">
    <table style="width: 90%; margin-left:3%;border:1px solid #cccccc;">
        <thead>
        <tr>
            <th class="infoheading">
                Display Filter
            </th>
        </tr>
        </thead>
        <tbody style="background-color: #f8f7f7; border: none;">
        <tr>
            <td>
                <input type="text" class="searchbar"
                       id="keywordsearchinput" value="" onkeyup="useSearch('keywordsearchinput')">
                <br/>
                <input style="padding: 5px 20px 5px 20px; border: none; font-weight:bold;" type="button"
                       name="searchBtn" value="Search" onclick="useSearch('keywordsearchinput')">
            </td>
        </tr>
        </tbody>
    </table>
</div>

<div id="content" class="content2">
    <table class="table info_table" id="ComponentBasicInfo" title="Releases">
        <thead>
        <tr>
            <th width="13%">Status</th>
            <th width="20%">CPE id</th>
            <th width="20%">Vendor</th>
            <th width="20%">Release name</th>
            <th width="20%">Release version</th>
            <th width="7%">Submit</th>
        </tr>
        </thead>
        <core_rt:forEach items="${releaseList}" var="release">
            <tr id="TableRow${release.id}">
                <td width="13%" id="Status${release.id}"></td>
                <td width="20%">
                    <label>
                    <input id='cpeid${release.id}'  type="text"
                           class="toplabelledInput"
                           placeholder="Enter CPE ID" required="" value="${release.cpeid}"/>
                    </label>
                    <%-- this and following hidden spans are added to make display filter and sorting using dataTables work--%>
                    <span style="display:none" id='plaincpeid${release.id}'>${release.cpeid}</span>
                </td>
                <td width="20%">
                    <sw360:DisplayVendorEdit id='vendorId${release.id}' displayLabel="false"
                                             vendor="${release.vendor}" onclick="displayVendors('${release.id}')"/>
                    <span style="display:none" id='plainvendor${release.id}'>${release.vendor.fullname}</span>
                </td>
                <td width="20%">
                    <label>
                    <input id='name${release.id}' type="text"
                           placeholder="Enter Name"
                                value="<sw360:out value="${release.name}"/>"
                            />
                    </label>
                    <span style="display:none" id='plainname${release.id}'>${release.name}</span>
                </td>
                <td width="20%">
                    <label>
                    <input id='version${release.id}'  type="text"
                           placeholder="Enter Version"
                           value="<sw360:out value="${release.version}"/>"/>
                    </label>
                    <span style="display:none" id='plainversion${release.id}'>${release.version}</span>
                </td>
                <td width="7%">
                    <input type="button" onclick="submitRow('${release.id}')"  value="OK" />
                </td>
            </tr>
        </core_rt:forEach>
    </table>
</div>


<%@include file="/html/components/includes/vendors/vendorSearchForm.jspf" %>

<script>
    var PortletURL;
    AUI().use('liferay-portlet-url', function (A) {
        PortletURL = Liferay.PortletURL;
        load();
    });

    var componentsInfoTable;

    //This can not be document ready function as liferay definitions need to be loaded first
    function load() {
        componentsInfoTable = configureComponentBasicInfoTable();
    }

    function configureComponentBasicInfoTable(){
        var tbl;
        tbl = $('#ComponentBasicInfo').dataTable({
            "sPaginationType": "full_numbers",
            "bAutoWidth": false,
            "aoColumnDefs": [
                { "sWidth": "13%", "aTargets": [ 0 ] },
                { "sWidth": "20%", "aTargets": [ 1 ] },
                { "sWidth": "20%", "aTargets": [ 2 ] },
                { "sWidth": "20%", "aTargets": [ 3 ] },
                { "sWidth": "20%", "aTargets": [ 4 ] },
                { "sWidth": "7%", "aTargets": [ 5 ] }
            ]
        });

        $('#ComponentBasicInfo_filter').hide();
        $('#ComponentBasicInfo_first').hide();
        $('#ComponentBasicInfo_last').hide();
        return tbl;
    }

    var activeReleaseId = '';

    function submitRow(id) {

        var resultElement  = $('#Status'+id);
        resultElement.text("...");
        jQuery.ajax({
            type: 'POST',
            url: '<%=updateReleaseURL%>',
            data: {
                <portlet:namespace/>releaseId: id,
                <portlet:namespace/>VENDOR_ID: $('#vendorId'+id).val(),
                <portlet:namespace/>CPEID:$('#cpeid'+id).val(),
                <portlet:namespace/>NAME:$('#name'+id).val(),
                <portlet:namespace/>VERSION:$('#version'+id).val()
            },
            success: function (data) {
                resultElement.text(data.result);
            },
            error: function () {
                resultElement.text("error");
            }
        });
    }

    function displayVendors(id) {
        activeReleaseId = id;
        showSetVendorDialog();
    }

    //Vendor things
    function fillVendorInfo(vendorInfo) {

        var beforeComma = vendorInfo.substr(0, vendorInfo.indexOf(","));
        var afterComma = vendorInfo.substr(vendorInfo.indexOf(",") + 1);
        fillVendorInfoFields(beforeComma.trim(), afterComma.trim());
    }

    function fillVendorInfoFields(vendorId, vendorName) {
        $('#vendorId'+ activeReleaseId).val(vendorId);
        $('#vendorId'+ activeReleaseId+'Display').val(vendorName);
    }

    function vendorContentFromAjax(id, what, where) {
        jQuery.ajax({
            type: 'POST',
            url: '<%=viewVendorURL%>',
            data: {
                <portlet:namespace/>what: what,
                <portlet:namespace/>where: where
            },
            success: function (data) {
                $('#' + id).html(data);
            }
        });
    }

    function useSearch( buttonId) {
        componentsInfoTable.fnFilter( $('#'+buttonId).val());
    }
</script>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/dataTable_Siemens.css">
