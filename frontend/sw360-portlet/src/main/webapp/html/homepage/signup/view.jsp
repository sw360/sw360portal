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

<%@ page import="com.siemens.sw360.datahandler.thrift.users.User" %>
<%@ page import="com.siemens.sw360.datahandler.thrift.users.UserGroup" %>
<%@ page import="com.siemens.sw360.portal.common.PortalConstants" %>


<%@include file="/html/init.jsp" %>
<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<portlet:actionURL var="createAccountURL" name="createAccount">
</portlet:actionURL>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">
<script src="<%=request.getContextPath()%>/js/external/jquery-1.11.1.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery.validate.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/additional-methods.min.js" type="text/javascript"></script>
<script src="<%=request.getContextPath()%>/js/external/jquery-ui.min.js"></script>

<h4>Welcome to SW360!</h4>

<h5>Sign Up For an Account<h5>
<core_rt:if test="${themeDisplay.signedIn}">
    <p style="font-weight: bold;">You are signed in, please go to the private pages on the top-right corner of this site. You do not need to sign up.</p>

</core_rt:if>
<core_rt:if test="${not themeDisplay.signedIn}">
<div id="createAccount">
    <form action="<%=createAccountURL%>" id="signup_form" method="post">
        <table>
            <thead>
            <tr>
                <th class="infoheading">
                    Create Account
                </th>
            </tr>
            </thead>
            <tbody style="background-color: #f8f7f7; border: none;">
            <tr>
                <td>
                    <label class="textlabel mandatory" for="given_name">First Name</label>
                    <input type="text" name="<portlet:namespace/><%=User._Fields.GIVENNAME%>" required=""
                           value="${newuser.givenname}" id="given_name">
                </td>
            </tr>
            <tr>
                <td>
                    <label class="textlabel mandatory" for="last_name">Last Name</label>
                    <input type="text" name="<portlet:namespace/><%=User._Fields.LASTNAME%>" required=""
                           value="${newuser.lastname}" id="last_name">
                </td>
            </tr>
            <tr>
                <td>
                    <label class="textlabel mandatory" for="email">Email</label>
                    <input type="text" name="<portlet:namespace/><%=User._Fields.EMAIL%>" required=""
                           value="${newuser.email}" id="email">
                </td>
            </tr>
            <tr>
                <td>
                    <label class="textlabel mandatory" for="department">Department</label>
                    <select class="toplabelledInput" id="department" name="<portlet:namespace/><%=User._Fields.DEPARTMENT%>"
                                        style="min-width: 162px; min-height: 28px;">
                        <core_rt:forEach items="${organizations}" var="org">
                            <option value="${org.name}" class="textlabel stackedLabel"
                            <core_rt:if test="${org.name == newuser.department}"> selected="selected"</core_rt:if>
                            >${org.name}</option>
                        </core_rt:forEach>
                    </select>
                </td>
            </tr>
            <tr>
                <td>
                    <label class="textlabel mandatory" for="usergroup">Requested User Group</label>
                    <select class="toplabelledInput" id="usergroup" name="<portlet:namespace/><%=User._Fields.USER_GROUP%>"
                            style="min-width: 162px; min-height: 28px;">

                        <sw360:DisplayEnumOptions type="<%=UserGroup.class%>" selected="${newuser.userGroup}"/>
                    </select>
                </td>
            </tr>
            <tr>
                <td>
                    <label class="textlabel mandatory" for="externalid">External ID</label>
                    <input type="text" name="<portlet:namespace/><%=User._Fields.EXTERNALID%>" required=""
                           value="${newuser.externalid}" id="externalid">
                </td>
            </tr>
            <tr>
                <td>
                    <label class="textlabel mandatory" for="password">Password</label>
                    <input type="password" name="<portlet:namespace/><%=PortalConstants.PASSWORD%>" required=""
                           value="" id="password">
                </td>
            </tr>
            <tr>
                <td>
                    <label class="textlabel mandatory" for="password_repeat">Repeat Password</label>
                    <input type="password" name="<portlet:namespace/><%=PortalConstants.PASSWORD_REPEAT%>" required=""
                           value="" id="password_repeat">
                </td>
            </tr>
            </tbody>
        </table>
        <br/>
        <input type="submit" class="addButton" value="Sign Up">
    </form>
</div>
<script>
$(document).ready(function () {

    $('#signup_form').validate({
        rules: {
            "<portlet:namespace/><%=PortalConstants.PASSWORD%>": "required",
            "<portlet:namespace/><%=PortalConstants.PASSWORD_REPEAT%>": {
                equalTo: '#password'
            }
        },
        messages: {
            "<portlet:namespace/><%=PortalConstants.PASSWORD_REPEAT%>": {
                equalTo: "Passwords must match."
            }
        }
    });

});
</script>
</core_rt:if>