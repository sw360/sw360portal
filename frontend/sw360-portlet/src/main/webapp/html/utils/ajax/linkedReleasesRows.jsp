<%--
  ~ Copyright Siemens AG, 2017. Part of the SW360 Portal Project.
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
--%>

<%@include file="/html/init.jsp" %>
<jsp:useBean id="releaseList" type="java.util.List<org.eclipse.sw360.datahandler.thrift.components.ReleaseLink>" scope="request"/>
<jsp:useBean id="parent_branch_id" class="java.lang.String" scope="request"/>

<core_rt:forEach items="${releaseList}" var="releaseLink" varStatus="loop">
    <tr id="releaseLinkRow${loop.count}" data-tt-id="${releaseLink.nodeId}" data-tt-branch="${releaseLink.hasSubreleases}"
        <core_rt:if test="${true}">data-tt-parent-id="${parent_branch_id}"</core_rt:if>
        <core_rt:if test="${empty parent_branch_id and not empty releaseLink.parentNodeId}">data-tt-parent-id="${releaseLink.parentNodeId}"</core_rt:if>
    >
        <td>
            <a href="<sw360:DisplayReleaseLink releaseId="${releaseLink.id}" bare="true" />">
                <sw360:out value="${releaseLink.longName}"/>
            </a>
        </td>
        <td>
            <sw360:DisplayEnum value="${releaseLink.releaseRelationship}"/>
        </td>
        <td>
            <core_rt:if test="${releaseLink.setLicenseIds}">
                <tags:DisplayLicenseCollection licenseIds="${releaseLink.licenseIds}"
                                               scopeGroupId="${pageContext.getAttribute('scopeGroupId')}"/>
            </core_rt:if>
        </td>
        <td>
            <sw360:DisplayEnum value="${releaseLink.clearingState}"/>
        </td>
    </tr>
</core_rt:forEach>

