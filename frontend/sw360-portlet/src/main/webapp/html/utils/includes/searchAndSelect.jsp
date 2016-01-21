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

<div id="search-div" title="Search" style="display: none; background-color: #ffffff;">
    <div style="display: inline-block">
        <input type="text" name="search" id="search-text" placeholder="search" class="searchbar"/>&nbsp;
        <input type="button" value="Search" id="search-button" class="searchbutton"/>
    </div>

    <div class="searchresults">
        <table width="100%" style="border-bottom: 2px solid #66c1c2;">
            <thead>
            <tr class="trheader" style="height: 30px;">
                <th width="4%">&nbsp;</th>
                <th width="32%" class="textlabel">Name</th>
            </tr>
            </thead>
        </table>
        <div style="overflow-y: scroll; height: 150px;">
            <table width="100%">
                <tbody id="search-result-table">
                <tr class="trbodyClass">
                    <td colspan="2"></td>
                </tr>
                </tbody>
            </table>
        </div>
        <hr noshade size="1" style="background-color: #66c1c2; border-color: #59D1C4;"/>
        <br/>

        <div>
            <input type="button" value="Select" id="search-add-button" class="addButton"/>
        </div>
    </div>
</div>

<script src="<%=request.getContextPath()%>/js/searchAndSelectIds.js" type="text/javascript"></script>

