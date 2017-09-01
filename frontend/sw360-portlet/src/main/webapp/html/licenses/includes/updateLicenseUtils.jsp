<%--
  ~ Copyright Siemens AG, 2017. Part of the SW360 Portal Project.
  ~
  ~ SPDX-License-Identifier: EPL-1.0
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
  --%>
<%@ page import="org.eclipse.sw360.datahandler.thrift.users.RequestedAction" %>
<jsp:useBean id="permissions" class="org.eclipse.sw360.datahandler.permissions.PermissionUtils" scope="request" />
<script>
    var permissions = {  'write':  ${permissions.makePermission(licenseDetail, currentUser).isActionAllowed(RequestedAction.DELETE)},
                         'delete': ${permissions.makePermission(licenseDetail, currentUser).isActionAllowed(RequestedAction.WRITE)}
    };
</script>