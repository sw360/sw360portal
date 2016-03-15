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

<%@include file="/html/init.jsp" %>
<portlet:defineObjects/>
<liferay-theme:defineObjects/>


<h4>Welcome to SW360!</h4>

<p>This the entry page of the SW360 web application. SW360 is a component catalogue for managing software components and
    projects.</p>
<h5>Getting started<h5>
<core_rt:if test="${themeDisplay.signedIn}">
    <p style="font-weight: bold;">You are signed in, please go to the private pages on the top-right corner of this site:</p>
    <img src="<%=request.getContextPath()%>/images/welcome/select_private_pages.png" alt=""
         border="0" width="150"/><br/>

    <h5>What do I see?</h5>

    <p>Please do not upload confidential data unless it is written here.</p>

    With <b>SW360</b>, you can<br/>

    <p>
    <ul class="wikidpad bulletlist">
        <li/>
        Manage your components and projects
        <li/>
        Send source packages to the clearing tool Fossology
        <li/>
        Reuse cleared components and releases for your project
        <li/>
        Import cleared components with clearing reports and other documents
        <li/>
        Browse licenses and their obligations
    </ul>

    <span class="wikidpad wiki-name-ref">[<a name="SW360=20Dokumentation" class="wikidpad">SW360 Dokumentation</a>]<br
            class="wikidpad"/><br class="wikidpad"/></span><span class="wikidpad parent-nodes">parent nodes: <span
        class="node"><a href="#OSS" class="wikidpad">_</a></span><br class="wikidpad"/></span><a
        name="SW360=20Dokumentation#.h0" class="wikidpad"></a>

    <h2 class="wikidpad heading-level2">SW360 Documentation</h2>

    This initial documentation contains the steps for the basic workflow with SW360.<br/>

    <p>
    <ol class="wikidpad orderedlist">
        <li/>
        <span class="wikidpad wiki-link"><a href="#Login=20to=20SW360" class="wikidpad">Login to SW360</a></span>
        <li/>
            <span class="wikidpad wiki-link"><a href="#Create=20a=20new=20Project" class="wikidpad">Create a new
                Project</a></span>
        <li/>
            <span class="wikidpad wiki-link"><a href="#Create=20a=20new=20component" class="wikidpad">Create a new
                Component</a></span>
        <li/>
            <span class="wikidpad wiki-link"><a href="#Adding=20a=20release=20to=20a=20component" class="wikidpad">Adding
                a Release to a Component</a></span>
        <li/>
        <span class="wikidpad wiki-link"><a href="#Upload=20to=20Fossology" class="wikidpad">Upload to
            Fossology</a></span>
    </ol>
    <p><br/>
        The visibility of objects and the permitted operations on objects within SW360 depend on specific roles of
        the user. A detailed description for the permissions is given here: <span class="wikidpad wiki-link"><a
                href="#User=20roles" class="wikidpad">User roles</a></span> <br/>

    <p><br/>
        <span class="wikidpad wiki-link"><a href="#Glossary" class="wikidpad">Glossary</a></span><br/>

    <hr/>
        <span class="wikidpad wiki-name-ref">[<a name="Login=20to=20SW360" class="wikidpad">Login to
            SW360</a>]<br/><br/></span><span class="wikidpad parent-nodes">parent nodes: <span
        class="wikidpad parent-node"><a href="#SW360=20Dokumentation" class="wikidpad">SW360
    Dokumentation</a></span><br/></span><a name="Login=20to=20SW360#.h0" class="wikidpad"></a>

    <h2 class="wikidpad heading-level2">Login to SW360</h2>


    <p><br/>
        After successful logon to the SW360 site you can view the public area of your account.<br/>
        <img src="<%=request.getContextPath()%>/images/welcome/2015-04-14%2013_54_26-Welcome%20-%20SW360.png" alt=""
             border="0" width="640"/><br/>

    <p><br/>
        To switch to your private working site use the drop down menu of <b>My Sites</b>. Your private area contains
        an overview of your projects, components and the tasks you have submitted or which are assigned to you. On
        the right side of the screen you can see the latest releases which have been added to SW360. <br/>
        

    <p><br/>
        <img src="<%=request.getContextPath()%>/images/welcome/Home%20-%20SW360.png" alt="" border="0"
             width="640"/><br/>

    <p><br/>
        The top level menu gives access to your projects, components, all licenses and the search functionality of
        SW360.<br/>
    <hr/>
        <span class="wikidpad wiki-name-ref">[<a name="Create=20a=20new=20Project" class="wikidpad">Create a new
            Project</a>]<br/><br/></span><span class="wikidpad parent-nodes">parent nodes: <span
        class="wikidpad parent-node"><a href="#SW360=20Dokumentation" class="wikidpad">SW360
    Dokumentation</a></span><br/></span><a name="Create=20a=20new=20Project#.h0" class="wikidpad"></a>

    <h2 class="wikidpad heading-level2">Create a new Project</h2>

    <p><br/>
        To create a new project start with the home screen of your private site.<br/>

    <p><br/>
        <img src="<%=request.getContextPath()%>/images/welcome/Home%20-%20SW360.png" alt="" border="0"
             width="640"/><br/>

    <p><br/>

    <p><br/>
        <br/>
        Then navigate to your project overview by clicking the menu item <b>Projects</b>. <br/>

    <p><br/>
        <img src="<%=request.getContextPath()%>/images/welcome/Projects%20-%20SW360%20Overview.png" alt=""
             border="0" width="640"/><br/>

    <p><br/>

    <p><br/>
        On top of the project list you can find a button <b>Add Project</b>. The activation of the button shows a
        screen where you can add information about your project.<br/>

    <p><br/>
        <img src="<%=request.getContextPath()%>/images/welcome/Projects%20-%20SW360%20NewProject.png" alt=""
             border="0" width="640"/><br/>

    <p><br/>
        The page for creating a project contains the following entries. <b><i>Obligatory</i></b> fields are:
    <ul class="wikidpad bulletlist">
        <li/>
        <b>Name</b>: the name of the project
        <li/>
        <b>Project state</b>: possible values are <i>Active</i>, <i>Phase out</i>, <i>Unknown</i>
        <li/>
        <b>Business unit</b>
        <li/>
        <b>Clearing team</b><br/>
    </ul>
    <b><i>Optional</i></b> fields are:
    <ul class="wikidpad bulletlist">
        <li/>
        <b>Version</b>: enter a version, if available
        <li/>
        <b>Description</b>: a short description of the project
        <li/>
        <b>URLs</b> for the project home page and wiki, if present
        <li/>
        User information
        <ul class="wikidpad bulletlist">
            <li/>
            <b>Project responsible</b>
            <li/>
            <b>Lead architect</b>
            <li/>
            <b>Moderators</b>
            <li/>
            <b>Comoderators</b>
            <li/>
            <b>Contributors</b></ul>

        <li/>
        Administrative information like
        <ul class="wikidpad bulletlist">
            <li/>
            <b>Deadline for pre-evaluation</b>
            <li/>
            dates for <b>System test</b>
            <li/>
            dates for <b>Delivery</b> and planned <b>Phase-out</b></ul>
    </ul>
    <p><br/>

    <p><br/>
        Near the bottom of the page you find two buttons:<br/>
    <ul class="wikidpad bulletlist">
        <li/>
        <b>Click to add linked Project</b>
        <li/>
        <b>Click to add Release</b><br/>
    </ul>
    Use this buttons to search for existing subprojects or releases which should be added to the new project.<br/>

    <p><br/>

    <p><br/>
        To finish the creation of the new project press the button "Add Project". The list of projects is updated
        and should contain the newly created project:<br/>

    <p><br/>
        <img src="<%=request.getContextPath()%>/images/welcome/Projects%20-%20SW360%20ListProjects.png" alt=""
             border="0" width="640"/><br/>
    <hr/>
        <span class="wikidpad wiki-name-ref">[<a name="Create=20a=20new=20component" class="wikidpad">Create a new
            component</a>]<br/><br/></span><span class="wikidpad parent-nodes">parent nodes: <span
        class="wikidpad parent-node"><a href="#SW360=20Dokumentation" class="wikidpad">SW360
    Dokumentation</a></span><br/></span><a name="Create=20a=20new=20component#.h0" class="wikidpad"></a>

    <h2 class="wikidpad heading-level2">Create a new component</h2>
    The menu item <b>Components</b> shows a list of all availabe components. There are two buttons on top of the
    page:<br/>
    <ul class="wikidpad bulletlist">
        <li/>
        <b>Export Components</b>: makes components available for other users
        <li/>
        <b>Add new Component</b>: opens a dialog window for creating a new component<br/>
    </ul>
    <img src="<%=request.getContextPath()%>/images/welcome/Components%20-%20SW360InitialList.png" alt="" border="0"
         width="640"/><br/>
    <br/>
    The dialog for creating a component contains the following entries. <b><i>Mandatory</i></b> fields are:
    <ul class="wikidpad bulletlist">
        <li/>
        <b>Name</b>:
        <li/>
        <b>Categories</b>: <i>supply a list of possible categories</i>
        <li/>
        <b>Component Type</b>: <br/>
    </ul>
    <b><i>Optional fields are</i></b>:
    <ul class="wikidpad bulletlist">
        <li/>
        <b>Software Platforms</b>: <i>supply a list of possible platforms</i>
        <li/>
        Optional pointers to additional information for the component:
        <ul class="wikidpad bulletlist">
            <li/>
            URLs for the homepage, wiki, mailing list and Blogs of the component
            <li/>
            a short description of the component
        </ul>
    </ul>
    <p><br/>
        The contents of the fields <b>Vendors</b>, <b>Programming Languages</b> and <b>Operating Systems</b> will be taken from the
        associated release(s). <br/>
        The following picture shows a minimal creation dialog for the component <i>libelf</i>.<br/>

    <p><br/>
        <img src="<%=request.getContextPath()%>/images/welcome/Components-SW360CreateDialog.png" alt=""
             border="0" width="640"/><br/>

    <p><br/>

    <p><br/>
        After pressing the <b>Create Component</b> button you get a feedback, whether the creation was sucessful or
        not.<br/>

    <p><br/>
        <img src="<%=request.getContextPath()%>/images/welcome/Components-SW360CreateDialogFeedback.png"
             alt="" border="0" width="640"/><br/>

    <hr/>
        <span class="wikidpad wiki-name-ref">[<a name="Adding=20a=20release=20to=20a=20component" class="wikidpad">Adding
            a release to a component</a>]<br/><br/></span><span class="wikidpad parent-nodes">parent nodes: <span
        class="wikidpad parent-node"><a href="#SW360=20Dokumentation" class="wikidpad">SW360
    Dokumentation</a></span><br/></span><a name="Adding=20a=20release=20to=20a=20component#.h0"
                                           class="wikidpad"></a>

    <h2 class="wikidpad heading-level2">Adding a release to a component</h2>
    <br/>
    In order to add a release to a component, select the component first. <br/>
    This can be accomplished in two ways:
    <ul class="wikidpad bulletlist">
        <li/>
        go to the home page and select the component from the "My Components" list
        <li/>
        click the "Components" button from the top menu and search for the component.<br/>
    </ul>
    <img src="<%=request.getContextPath()%>/images/welcome/Components%20-%20SW360HomeList.png" alt="" border="0"
         width="640"/><br/>
    <br/>
    In this case we choose the newly created component "libelf" from the home page list. <br/>
    The detail page for the component "libelf" contains two buttons:
    <ul class="wikidpad bulletlist">
        <li/>
        <b>Edit</b>: here you can change component information and add releases to the component
        <li/>
        <b>Subscribe</b>: subscribe to the component to get notifications, when any changes of the component
        occur<br/>
    </ul>
    On the left-hand side the detail page contains a menu for
    <ul class="wikidpad bulletlist">
        <li/>
        <b>Summary</b>: show this page
        <li/>
        <b>Release Overview</b>: show a list of the associated releases of the component
        <li/>
        <b>Attachments</b>: show the attachments of the component; attachments can be source code, documents,
        clearing reports, ...
        <li/>
        <b>Wiki</b>: a link to the component wiki<br/>
    </ul>
    <img src="<%=request.getContextPath()%>/images/welcome/Components-SW360ComponentDetails.png" alt=""
         border="0" width="640"/><br/>
    <br/>
    Press the button "Edit" for the component "libelf". A detail page for the component is shown where you can find
    a button "Add Release". <br/>
    <br/>
    <img src="<%=request.getContextPath()%>/images/welcome/Components-SW360CreateRelease.png" alt=""
         border="0" width="640"/><br/>
    <br/>
    A detail page for the release is opened. Mandatory fields are
    <ul class="wikidpad bulletlist">
        <li/>
        <b>Name</b>: the name of the component, e.g. "libelf"
        <li/>
        <b>Version</b>: a version number of the release, e.g. "0.8.13"
        <li/>
        <b>Programming Languages</b>: a list of used programming languages.<br/>
    </ul>
    Additional information for the release can be entered:
    <ul class="wikidpad bulletlist">
        <li/>
        <b>Release Date</b>: date of the release
        <li/>
        <b>Download URL</b>: URL from where the source of the release can be downloaded
        <li/>
        <b>Clearing State</b>: possible values are "New", "Sent to Fossology", "Under Clearing" and "Report
        Available"
        <li/>
        <b>Mainline State</b>: status of the release if it has been already recorded in "Mainline"
        <li/>
        <b>Contacts</b>: a pointer (e.g. email address) to a contact person for the release
        <li/>
        <b>Moderators</b>: a list of moderators responsible for the component<br/>
    </ul>
    After entering the information press the button "Add Release" to add the new release to the component. <br/>
    A new page is shown with a feedback about the creation operation and the details of the release.<br/>
    <br/>
    <img src="<%=request.getContextPath()%>/images/welcome/Components-SW360ReleaseCreated1.png" alt=""
         border="0" width="640"/><br/>
    <br/>
    In this detail page for the release it is possible to edit the basic data for the release and update the
    information (button <b>Update Release</b>).<br/>
    With the menu on the left side further information can be entered. The <br/>
    <br/>
    <b>Release Information</b><br/>
    Show the detailed information about the release.<br/>
    <br/>
    <b>Release Repository</b><br/>
    A URL to the repository of the release can be added.<br/>
    <br/>
    <b>Release Clearing Information</b><br/>
    <img src="<%=request.getContextPath()%>/images/welcome/Components%20-%20SW360ReleaseClearingInfo.png" alt=""
         border="0" width="640"/><br/>
    <br/>
    <b>Vendors</b><br/>
    <img src="<%=request.getContextPath()%>/images/welcome/Components%20-%20SW360ReleaseVendors.png" alt=""
         border="0" width="640"/><br/>
    <br/>
    <b>Attachments</b><br/>
    <br/>
    This page shows a list of all attachments associated with the component. To add a new attachment press the
    button <b>Add Attachment</b>. <br/>
    <br/>
    <img src="<%=request.getContextPath()%>/images/welcome/Components%20-%20SW360ReleaseAddAttachment.png" alt=""
         border="0" width="640"/><br/>
    <br/>
    A new dialogbox is shown where you can drop a file or use a file browser to chosse an attachment. In the example
    a zipped archive is choosen and the type of the attchment is set to source.<br/>
    To finish the action press <b>Update Release</b>.<br/>
    <img src="<%=request.getContextPath()%>/images/welcome/Components%20-%20SW360ReleaseAddAttachmentFile.png"
         alt="" border="0" width="640"/><br/>
<span class="wikidpad wiki-name-ref">[<a name="Upload=20to=20Fossology" class="wikidpad">Upload to
    Fossology</a>]<br/><br/></span><span
        class="wikidpad parent-nodes">parent nodes: <span class="wikidpad parent-node"><a
        href="#SW360=20Dokumentation" class="wikidpad">SW360 Dokumentation</a></span><br/></span><a
        name="Upload=20to=20Fossology#.h0" class="wikidpad"></a>

    <h2 class="wikidpad heading-level2">Upload to Fossology</h2>
    <br/>
    Preconditions for uploading a release to Fossology are:
    <ul class="wikidpad bulletlist">
        <li/>
        a project or a component has been created and
        <li/>
        a release has been added to the project/release an
        <li/>
        an attachment with source code has been added to the release.<br/>
    </ul>
    Lets assume we have a component <i>libelf</i> wirh a release <i>0.8.13</i> attached to the component.<br/>
    To navigate to the release, start with the home page:<br/>

    <p><br/>
        <img src="<%=request.getContextPath()%>/images/welcome/Components%20-%20SW360HomeList.png" alt="" border="0"
             width="640"/><br/>

    <p><br/>

    <p><br/>

    <p><br/>
        ...and click on the component <i>libelf</i><br/>

    <p><br/>
        <img src="<%=request.getContextPath()%>/images/welcome/Components%20-%20SW360LibelfReleases.png" alt=""
             border="0" width="640"/><br/>

    <p><br/>

    <p><br/>
        After selecting the menu item <b>Release Overview</b> you can the the list of releases for this component
        containing the release <i>0.8.13</i>.<br/>
        The list contains a column <b>Actions</b> with four entries for <i>libelf(0.8.13</i>:
    <ul class="wikidpad bulletlist">
        <li/>
        <b>Send to Fossology</b>
        <li/>
        <b>Edit</b>
        <li/>
        <b>Duplicate</b>
        <li/>
        <b>Delete</b><br/>
    </ul>
    Use the button <b>Send to Fossology</b> to start uploading the source of the release to Fossology:<br/>

    <p><br/>

    <p><br/>
        <img src="<%=request.getContextPath()%>/images/welcome/Components%20-%20SW360ReleaseSendToFossology.png"
             alt="" border="0" width="640"/><br/>
        <br/>
        The dialog box contains a select field for the clearing team to which the source package is sent for
        clearing. The column <b>Clearing State</b> shows the status of the package. Initially the value is
        <b>Sending...</b>
        or <b>Sent</b>.<br/>
        <br/>
    <hr/>
        <span class="wikidpad wiki-name-ref">[<a name="User=20roles" class="wikidpad">User
            roles</a>]<br/><br/></span><span class="wikidpad parent-nodes">parent nodes: <span
        class="wikidpad parent-node"><a href="#SW360=20Dokumentation" class="wikidpad">SW360
    Dokumentation</a></span><br/></span><a name="User=20roles#.h0" class="wikidpad"></a>

    <h2 class="wikidpad heading-level2">User roles</h2>
    User <br/>
    Administrator<br/>
    Clearing administrator<br/>
    Project manager/ component owner<br/>
    Moderator<br/>
    Contributor / developer / lead architect<br/>
    User in business unit<br/>
    <br/>
    <span class="wikidpad wiki-name-ref">[<a name="Glossary" class="wikidpad">Glossary</a>]<br/><br/></span><span
        class="wikidpad parent-nodes">parent nodes: <span class="wikidpad parent-node"><a
        href="#SW360=20Dokumentation" class="wikidpad">SW360 Dokumentation</a></span><br/></span><a
        name="Glossary#.h0" class="wikidpad"></a>

    <h2 class="wikidpad heading-level2">Glossary</h2>

    <p><br/>
        <i><b>Administrator</b></i><br/>
        tbd<br/>

    <p><br/>
        <i><b>Clearing administrator</b></i><br/>
        tbd<br/>

    <p><br/>
        <i><b>Components</b></i><br/>
        tbd<br/>

    <p><br/>
        <i><b>Contributor / developer / lead architect</b></i><br/>
        tbd<br/>

    <p><br/>
        <i><b>License</b></i><br/>
        tbd<br/>

    <p><br/>
        <i><b>Moderator</b></i><br/>
        tbd<br/>

    <p><br/>
        <i><b>Projects</b></i><br/>
        tbd<br/>

    <p><br/>
        <i><b>Project Manager/ component owner</b></i><br/>
        tbd<br/>

    <p><br/>
        <i><b>Releases</b></i><br/>
        tbd<br/>

    <p><br/>
        <i><b>Subscriptions</b></i><br/>
        tbd<br/>

    <p><br/>
        <i><b>Task Assignments</b></i><br/>
        tbd<br/>

    <p><br/>
        <i><b>Task Submissions</b></i><br/>
        tbd<br/>

    <p><br/>
        <i><b>User</b></i><br/>
        tbd<br/>

    <p><br/>
    <i><b>User in Business Unit</b></i><br/>
    tbd<br/>

</core_rt:if>
<core_rt:if test="${not themeDisplay.signedIn}">
    <p style="font-weight: bold;"> In order to go ahead, please us the "Sign In" with your account. Please contact
        the support E-Mail for an account. </p>
</core_rt:if>
