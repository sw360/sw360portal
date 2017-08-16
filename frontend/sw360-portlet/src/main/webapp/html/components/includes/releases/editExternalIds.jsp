<%@ page import="org.eclipse.sw360.portal.common.PortalConstants" %>
<%@ page import="org.eclipse.sw360.datahandler.thrift.components.Release" %>
<script src="<%=request.getContextPath()%>/webjars/jquery-validation/1.15.1/jquery.validate.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/webjars/jquery-validation/1.15.1/additional-methods.min.js" type="text/javascript"></script>
<table class="table info_table" id="externalIdsTable">
    <thead>
    <tr>
        <th colspan="3" class="headlabel">External Ids</th>
    </tr>
    </thead>
</table>

<input type="button" class="addButton" onclick="addRowToExternalIdsTable();" value="Click to add row to External Ids "/>
<br/>
<br/>

<script>

    function deleteIDItem(rowIdOne) {
        function deleteMapItemInternal() {
            $('#' + rowIdOne).remove();
        };

        deleteConfirmed("Do you really want to remove this item?", deleteMapItemInternal);
    }

    function addRowToExternalIdsTable(key, value, rowId) {
        if (!rowId) {
            var rowId = "externalIdsTableRow" + Date.now();
        }
        if ((!key) && (!value)) {
                var key = "", value = "";
            }
        var newRowAsString =
            '<tr id="' + rowId + '" class="bodyRow">' +
            '<td width="46%">' +
            '<input class="keyClass" id="<%=PortalConstants.EXTERNAL_ID_KEY%>' + rowId + '" name="<portlet:namespace/><%=PortalConstants.EXTERNAL_ID_KEY%>' + rowId + '" class="toplabelledInput" placeholder="Input name" title="Input name" value="' + key + '"/>' +
            '</td>' +
            '<td width="46%">' +
            '<input class="valueClass" id="<%=PortalConstants.EXTERNAL_ID_VALUE%>' + rowId + '" name="<portlet:namespace/><%=PortalConstants.EXTERNAL_ID_VALUE%>' + rowId + '" class="toplabelledInput" placeholder="Input id" title="Input id" value="' + value + '"/>' +
            '</td>' +
            '<td class="deletor" width="8%">' +
            '<img src="<%=request.getContextPath()%>/images/Trash.png" onclick="deleteMapItem(\'' + rowId + '\')" alt="Delete">' +
            '</td>' +
            '</tr>';
        $('#externalIdsTable tr:last').after(newRowAsString);
    }

    function createExternalIdsTable() {
        <core_rt:forEach items="${externalIdsSet}" var="tableEntry" varStatus="loop">
        addRowToExternalIdsTable('<sw360:out value="${tableEntry.key}"/>', '<sw360:out value="${tableEntry.value}"/>', 'externalIdsTableRow${loop.count}');
        </core_rt:forEach>
    }

    $(window).load(function () {
        createExternalIdsTable();
    });

</script>
