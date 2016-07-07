<%--
  ~ Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
  --%>

<%@ page import="com.liferay.portlet.PortletURLFactoryUtil" %>
<%@include file="/html/init.jsp" %>

<%@ taglib prefix="sw360" uri="/WEB-INF/customTags.tld" %>

<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<%@ page import="com.siemens.sw360.datahandler.thrift.projects.Project" %>
<%@ page import="com.siemens.sw360.datahandler.thrift.components.ReleaseLink" %>

<jsp:useBean id="releaseList" type="java.util.List<com.siemens.sw360.datahandler.thrift.components.ReleaseLink>"  scope="request"/>
<core_rt:forEach items="${releaseList}" var="releaseLink" varStatus="loop">
    <tr id="releaseLinkRow${loop.count}" >
        <td width="23%">
            <label class="textlabel stackedLabel" for="releaseVendor">Vendor name</label>
            <input id="releaseVendor" type="text" class="toplabelledInput" placeholder="No vendor"
                   value="${releaseLink.vendor}" readonly/>
        </td>
        <td width="23%">
            <input type="hidden" value="${releaseLink.id}" name="<portlet:namespace/><%=Project._Fields.RELEASE_ID_TO_USAGE%><%=ReleaseLink._Fields.ID%>">
            <label class="textlabel stackedLabel" for="releaseName">Release name</label>
            <input id="releaseName" type="text" class="toplabelledInput" placeholder="Enter release"
                   value="${releaseLink.name}" readonly/>
        </td>
        <td width="23%">
            <label class="textlabel stackedLabel" for="releaseVersion">Release version</label>
            <input id="releaseVersion" type="text" class="toplabelledInput" placeholder="Enter version"
                   value="${releaseLink.version}" readonly/>
        </td>
        <td width="23%">
            <label class="textlabel stackedLabel" for="releaseRelation">Release relation</label>
            <input id="releaseRelation" type="text" class="toplabelledInput" placeholder="Enter release usage"
                   value="${releaseLink.comment}" name="<portlet:namespace/><%=Project._Fields.RELEASE_ID_TO_USAGE%><%=ReleaseLink._Fields.COMMENT%>"/>
        </td>

        <td class="deletor">
            <img src="<%=request.getContextPath()%>/images/Trash.png" onclick="deleteReleaseLink('releaseLinkRow${loop.count}')" alt="Delete">
        </td>

    </tr>
</core_rt:forEach>
