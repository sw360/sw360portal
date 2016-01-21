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

<%@ page import="com.siemens.sw360.datahandler.thrift.vendors.Vendor" %>
<%@ page import="com.siemens.sw360.datahandler.thrift.components.Release" %>

<jsp:useBean id="vendor" class="com.siemens.sw360.datahandler.thrift.vendors.Vendor" scope="request" />


<table class="table info_table" id="ComponentBasicInfo" title="Vendor Information">
  <thead>
  <tr>
    <th colspan="4" class="headlabel">Vendor</th>
  </tr>
  </thead>
  <tr>

    <core_rt:if test="${not empty vendor.id}" >
      <td width="30%">
        <input type="hidden" value="${vendor.id}" name="<portlet:namespace/><%=Release._Fields.VENDOR_ID%>">
        <label class="textlabel stackedLabel" for="vendorName">Vendor Name</label>
        <input id="vendorName" type="text" class="toplabelledInput"  placeholder="Enter Vendor" value="${vendor.fullname}" readonly/>
      </td>
      <td width="30%">
        <label class="textlabel stackedLabel" for="vendorShortName">Vendor Shortname</label>
        <input id="vendorShortName"  type="text" class="toplabelledInput"  placeholder="Enter Vendor" value="${vendor.shortname}" readonly/>
      </td>
      <td width="30%">
        <label class="textlabel stackedLabel" for="vendorUrl">Vendor URL</label>
        <input id="vendorUrl"  type="text" class="toplabelledInput"  value="${vendor.url}" readonly/>
      </td>

      <td width="5%">
        <span class="pull-right">
          <label class="textlabel stackedLabel" for="changeVendor">Action</label>
          <input type="button" value="Change vendor" id="changeVendor" class="toplabelledInput addButton" onclick="showSetVendorDialog();"  />
        </span>
      </td>

    </core_rt:if>

    <core_rt:if test="${empty vendor.id}" >
        <td width="100%"><input type="button" value="Click to set vendor" class=\"clickable\" onclick="showSetVendorDialog();" /> </td>
    </core_rt:if>
  </tr>
</table>

  <%@include file="/html/components/includes/vendors/vendorSearchForm.jspf" %>
