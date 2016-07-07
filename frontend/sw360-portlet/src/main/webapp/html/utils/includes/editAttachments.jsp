<%--
  ~ Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
  ~
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the Eclipse Public License v1.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.eclipse.org/legal/epl-v10.html
  --%>

<table class="table info_table " id="attachmentInfo" title="Attachment Information ${documentType}">
    <thead>
    <tr>
        <th colspan="5" class="headlabel">Attachments</th>
    </tr>
    </thead>

    <tbody>
        <jsp:include page="/html/utils/ajax/attachmentsAjax.jsp"/>
    </tbody>
</table>
<input type="button" class="addButton" onclick="showAddAttachmentDialog()" value="Add Attachment" >
<br/>
<br/>

