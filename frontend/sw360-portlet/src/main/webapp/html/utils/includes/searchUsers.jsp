<%--
  ~ Copyright Siemens AG, 2013-2015. Part of the SW360 Portal User.
  ~
  ~ SPDX-License-Identifier: EPL-1.0
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
  --%>
<%@include file="/html/init.jsp" %>

<%@ taglib prefix="sw360" uri="/WEB-INF/customTags.tld" %>
<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">

<%@ page import="org.eclipse.sw360.portal.common.PortalConstants" %>

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

