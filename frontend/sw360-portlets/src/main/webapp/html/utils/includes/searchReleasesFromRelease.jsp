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

<%@ page import="com.liferay.portlet.PortletURLFactoryUtil" %>
<%@include file="/html/init.jsp" %>


<%@ taglib prefix="sw360" uri="/WEB-INF/customTags.tld" %>
<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<%@ page import="com.siemens.sw360.portal.common.PortalConstants" %>
<jsp:useBean id="release" class="com.siemens.sw360.datahandler.thrift.components.Release" scope="request" />
<div id="search-release-form" title="Search Release" style="display: none; background-color: #ffffff;">
    <form>
        <div style="display: inline-block">
            <input type="text" name="searchrelease" id="searchrelease" placeholder="search" class="searchbar"/>&nbsp;
            <input type="button" value="Search By Name"
                   onclick="ReleaseContentFromAjax('releaseSearchResultsTable', '<%=PortalConstants.RELEASE_SEARCH%>', $('#searchrelease').val(), true);"
                   class="searchbutton" id="searchbuttonrelease"/>
            <input type="button" value="Search By Vendor"
                   onclick="ReleaseContentFromAjax('releaseSearchResultsTable', '<%=PortalConstants.RELEASE_SEARCH_BY_VENDOR%>', $('#searchrelease').val(), true);"
                   class="searchbutton" id="searchbuttonrelease2"/>
        </div>

        <div id="Releasesearchresults">
            <table width="100%" style="border-bottom: 2px solid #66c1c2;">
                <thead>
                <tr class="trheader" style="height: 30px;">
                    <th width="4%">&nbsp;</th>
                    <th width="32%" class="textlabel">Vendor</th>
                    <th width="32%" class="textlabel">Release Name</th>
                    <th width="32%" class="textlabel">Version</th>
                </tr>
                </thead>
            </table>
            <div style="overflow-y: scroll; height: 150px;">
                <table id="releaseSearchResultsTable" width="100%">
                    <tr class="trbodyClass">
                        <td colspan="4"></td>
                    </tr>
                </table>
            </div>
            <hr noshade size="1" style="background-color: #66c1c2; border-color: #59D1C4;"/>
            <br/>

            <div>
                <input type="button" value="Select" class="addButton" onclick="selectRelease();"/>
            </div>
        </div>
    </form>
</div>

<portlet:resourceURL var="viewReleaseURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value="<%=PortalConstants.VIEW_LINKED_RELEASES%>"/>
    <portlet:param name="<%=PortalConstants.RELEASE_ID%>" value="${release.id}"/>
</portlet:resourceURL>

<script>
    function showReleaseDialog() {
        openDialog('search-release-form', 'searchrelease');
    }

    function selectRelease() {
        var releaseIds = [];

        $('#releaseSearchResultsTable').find(':checked').each(
                function() {
                    releaseIds.push(this.value);
                }
        );
        addReleaseInfo(releaseIds);

        closeOpenDialogs();
        return false;
    }


    function addReleaseInfo(linkedReleases) {
        ReleaseContentFromAjax('LinkedReleasesInfo', '<%=PortalConstants.LIST_NEW_LINKED_RELEASES%>', linkedReleases);
    }

    function ReleaseContentFromAjax(id, what, where, replace) {
        jQuery.ajax({
            type: 'POST',
            url: '<%=viewReleaseURL%>',
            data: {
                '<portlet:namespace/><%=PortalConstants.WHAT%>': what,
                '<portlet:namespace/><%=PortalConstants.WHERE%>': where
            },
            success: function (data) {
                if (replace) {
                    $('#' + id + " tbody").html(data);
                } else {
                    $('#' + id + " tbody").append(data);
                }
            }
        });
    }

    $(document).ready(function () {
                bindkeyPressToClick('searchrelease', 'searchbuttonrelease');
            }
    );
</script>

<%@include file="/html/utils/includes/linkedReleaseDelete.jspf" %>