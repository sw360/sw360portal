<%--
  ~ Copyright Siemens AG, 2013-2015. Part of the SW360 Portal User.
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

<portlet:resourceURL var="licenseSearchURL">
    <portlet:param name="<%=PortalConstants.ACTION%>" value="<%=PortalConstants.LICENSE_SEARCH%>"/>
</portlet:resourceURL>

<script>

    function searchLicenseAjax(what, how) {
        return jQuery.ajax({
            type: 'POST',
            url: '<%=licenseSearchURL%>',
            data: {
                '<portlet:namespace/><%=PortalConstants.WHAT%>': what,
                '<portlet:namespace/><%=PortalConstants.HOW%>': how
            }
        });
    }

    var searchLicense = new SearchAndSelectIds({
        ajaxSearch: searchLicenseAjax
    });

    function showSetLicensesDialog(resultInputId) {
        searchLicense.open(true, resultInputId);
    }
</script>

