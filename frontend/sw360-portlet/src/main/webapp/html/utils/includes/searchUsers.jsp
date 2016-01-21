<%--
  ~ Copyright Siemens AG, 2013-2015. Part of the SW360 Portal User.
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

<%@ taglib prefix="sw360" uri="/WEB-INF/customTags.tld" %>
<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">

<%@ page import="com.siemens.sw360.portal.common.PortalConstants" %>

<portlet:resourceURL var="userSearchURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value="<%=PortalConstants.USER_SEARCH%>"/>
</portlet:resourceURL>

<script>

    function searchUserAjax(what, how) {
        return jQuery.ajax({
            type: 'POST',
            url: '<%=userSearchURL%>',
            data: {
                '<portlet:namespace/><%=PortalConstants.WHAT%>': what,
                '<portlet:namespace/><%=PortalConstants.HOW%>': how
            }
        });
    }

    var searchUser = new SearchAndSelectIds({
        ajaxSearch: searchUserAjax
    });

    function showUserDialog(multiUser, resultInputId) {
        searchUser.open(multiUser, resultInputId);
    }
</script>

