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
<%@ page import="org.eclipse.sw360.datahandler.thrift.users.User" %>
<%@ page import="org.eclipse.sw360.datahandler.thrift.components.ComponentType" %>
<%@ page import="org.eclipse.sw360.datahandler.common.ThriftEnumUtils" %>

<%@include file="/html/init.jsp" %>
<%-- the following is needed by liferay to display error messages--%>
<%@include file="/html/utils/includes/errorKeyToMessage.jspf"%>

<portlet:defineObjects/>
<liferay-theme:defineObjects/>

<portlet:actionURL var="componentMergeWizardStepUrl" name="componentMergeWizardStep"/>

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/sw360.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/dataTable_Siemens.css">
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/mergeComponent.css">

<div id="header"></div>
<p class="pageHeader"><span class="pageHeaderBigSpan">Merge a component into ${component.name}</span></p>
<div id="content">
    <div id="componentMergeWizard" data-step-id="0" data-component-target-id="${component.id}">
        <div class="wizardHeader">
            <ul>
                <li class="active">1. Choose source<br /><small>Choose a component that should be merged into the current one</small></li>
                <li>2. Merge data<br /><small>Merge data from source into target component</small></li>
                <li>3. Confirm<br /><small>Check the merged version and confirm</small></li>
            </ul>
        </div>
        <div class="wizardBody">
            <div class="step active" data-step-id="1">
                <div class="spinner">Loading data for step 1, please wait...</div>
            </div>
            <div class="step" data-step-id="2">
                <div class="spinner">Loading data for step 2, please wait...</div>
            </div>
            <div class="step" data-step-id="3">
                <div class="spinner">Loading data for step 3, please wait...</div>
            </div>
        </div>
    </div>
</div>

<%--for javascript library loading --%>
<%@ include file="/html/utils/includes/requirejs.jspf" %>
<script>
    require([ 'modules/mergeWizard', 'jquery', /* jquery-plugins */ 'datatables' ], function(wizard, $) {
        var mergeWizardStepUrl = '<%=componentMergeWizardStepUrl%>',
            postParamsPrefix = '<portlet:namespace/>',
            $wizardRoot = $('#componentMergeWizard');

        wizard({
            wizardRoot: $wizardRoot,
            postUrl: mergeWizardStepUrl,
            postParamsPrefix: postParamsPrefix,

            steps: [
                {
                    renderHook: renderChooseComponent,
                    submitHook: submitChosenComponent,
                    submitErrorHook: submitErrorHook
                },
                {
                    renderHook: renderMergeComponent,
                    submitHook: submitMergedComponent,
                    submitErrorHook: submitErrorHook
                },
                {
                    renderHook: renderConfirmMergedComponent,
                    submitHook: submitConfirmedMergedComponent,
                    submitErrorHook: submitErrorHook
                }
            ],
            finishCb: function(data) {
                if (data.error) {
                    alert("Could not merge components:\n" + data.error);
                }
                window.location.href = data.redirectUrl;
            }
        });

        function renderChooseComponent($stepElement, data) {
            $stepElement.html('' +
                    '<div class="stepFeedback"></div>' +
                    '<table id="componentSourcesTable" class="table info_table" title="Source component">' +
                    '    <colgroup>' +
                    '        <col style="width: 5%;" />' +
                    '        <col style="width: 50%;" />' +
                    '        <col style="width: 30%;" />' +
                    '        <col style="width: 15%;" />' +
                    '    </colgroup>' +
                    '    <thead>' +
                    '        <tr>' +
                    '            <th></th>' +
                    '            <th>Component name</th>' +
                    '            <th>Created by</th>' +
                    '            <th>Releases</th>' +
                    '        </tr>' +
                    '    </thead>' +
                    '    <tbody>' +
                    '    </tbody>' +
                    '</table>');

            var table = $stepElement.find('#componentSourcesTable').DataTable( {
                data: data.components,
                columns: [
                    { data: "id", render: function(data) {
                            return '<input type="radio" name="componentChooser" value="' + data + '" />';
                        }
                    },
                    { data: "name" },
                    { data: "createdBy" },
                    { data: "releases" }
                ],
                pageLength: 50,
                pagingType: 'simple_numbers',
                order: [ [ 1, 'asc' ] ],
                autoWidth: false,
                deferRender: true
            } );
        }

        function submitChosenComponent($stepElement) {
            var checkedList = $stepElement.find('input:checked');
            if (checkedList.size() !== 1 || $(checkedList.get(0)).val() ===  $wizardRoot.data('componentTargetId')) {
                $stepElement.find('.stepFeedback').html('<div class="alert">Please choose exactly one component, which is not the component itself!</div>');
                $('html, body').stop().animate({ scrollTop: 0 }, 300, 'swing');
                setTimeout(function() {
                    $stepElement.find('.stepFeedback').html('');
                }, 5000);
                return false;
            }
            $stepElement.data('componentTargetId', $wizardRoot.data('componentTargetId'));
            $stepElement.data('componentSourceId', $(checkedList.get(0)).val());
        }

        function renderMergeComponent($stepElement, data) {
            $stepElement.html('<div class="stepFeedback"></div>');
            $stepElement.data('componentSourceId', data.componentSource.id);
            $stepElement.data('releaseCount', (data.componentTarget.releases.length || 0) + (data.componentSource.releases.length || 0));

            $stepElement.append(wizard.createCategoryLine('General'));
            $stepElement.append(wizard.createSingleMergeLine('Name', data.componentTarget.name, data.componentSource.name));
            $stepElement.append(wizard.createSingleMergeLine('Created on', data.componentTarget.createdOn, data.componentSource.createdOn));
            $stepElement.append(wizard.createSingleMergeLine('Created by', data.componentTarget.createdBy, data.componentSource.createdBy));
            $stepElement.append(wizard.createMultiMergeLine('Categories', data.componentTarget.categories, data.componentSource.categories));
            $stepElement.append(wizard.createSingleMergeLine('Component Type', data.componentTarget.componentType, data.componentSource.componentType, getComponentTypeDisplayString));
            $stepElement.append(wizard.createSingleMergeLine('Homepage', data.componentTarget.homepage, data.componentSource.homepage));
            $stepElement.append(wizard.createSingleMergeLine('Blog', data.componentTarget.blog, data.componentSource.blog));
            $stepElement.append(wizard.createSingleMergeLine('Wiki', data.componentTarget.wiki, data.componentSource.wiki));
            $stepElement.append(wizard.createSingleMergeLine('Mailing list', data.componentTarget.mailinglist, data.componentSource.mailinglist));
            $stepElement.append(wizard.createSingleMergeLine('Description', data.componentTarget.description, data.componentSource.description));
            $stepElement.append(wizard.createSingleMergeLine('External ids', data.componentTarget.externalids, data.componentSource.externalids));


            $stepElement.append(wizard.createCategoryLine('Roles'));
            $stepElement.append(wizard.createSingleMergeLine('Component owner', data.componentTarget.componentOwner, data.componentSource.componentOwner));
            $stepElement.append(wizard.createSingleMergeLine('Owner accounting unit', data.componentTarget.ownerAccountingUnit, data.componentSource.ownerAccountingUnit));
            $stepElement.append(wizard.createSingleMergeLine('Owner billing group', data.componentTarget.ownerGroup, data.componentSource.ownerGroup));
            $stepElement.append(wizard.createSingleMergeLine('Owner country', data.componentTarget.ownerCountry, data.componentSource.ownerCountry));
            $stepElement.append(wizard.createMultiMergeLine('Moderators', data.componentTarget.moderators, data.componentSource.moderators));
            $stepElement.append(wizard.createMultiMergeLine('Subscribers', data.componentTarget.subscribers, data.componentSource.subscribers));
            $stepElement.append(wizard.createMultiMapMergeLine('Additional Roles', data.componentTarget.roles, data.componentSource.roles));

            $stepElement.append(wizard.createCategoryLine('Releases'));
            $stepElement.append(wizard.createMultiMergeLine('Releases', data.componentTarget.releases, data.componentSource.releases, function(release) {
                if (!release) {
                    return '';
                }
                return (release.name || '-no-name-') + ' ' + (release.version || '-no-version-');
            }));

            $stepElement.append(wizard.createCategoryLine('Attachments'));
            $stepElement.append(wizard.createMultiMergeLine('Attachments', data.componentTarget.attachments, data.componentSource.attachments, function(attachment) {
                if (!attachment) {
                    return '';
                }
                return (attachment.filename || '-no-filename-') + ' (' + (attachment.attachementType || '-no-type-') + ')';
            }));

            wizard.registerClickHandlers();
        }

        function submitMergedComponent($stepElement) {
            var componentSelection = {},
                releases = [],
                attachments = [];

            componentSelection.id = $wizardRoot.data('componentTargetId');

            componentSelection.name = wizard.getFinalSingleValue('Name');
            componentSelection.createdOn = wizard.getFinalSingleValue('Created on');
            componentSelection.createdBy = wizard.getFinalSingleValue('Created by');
            componentSelection.categories = wizard.getFinalMultiValue('Categories');
            componentSelection.componentType = wizard.getFinalSingleValue('Component Type');
            componentSelection.homepage = wizard.getFinalSingleValue('Homepage');
            componentSelection.blog = wizard.getFinalSingleValue('Blog');
            componentSelection.wiki = wizard.getFinalSingleValue('Wiki');
            componentSelection.mailinglist = wizard.getFinalSingleValue('Mailing list');
            componentSelection.description = wizard.getFinalSingleValue('Description');
            componentSelection.externalids = wizard.getFinalSingleValue('External ids');

            componentSelection.componentOwner = wizard.getFinalSingleValue('Component owner');
            componentSelection.ownerAccountingUnit = wizard.getFinalSingleValue('Owner accounting unit');
            componentSelection.ownerGroup = wizard.getFinalSingleValue('Owner billing group');
            componentSelection.ownerCountry = wizard.getFinalSingleValue('Owner country');
            componentSelection.moderators = wizard.getFinalMultiValue('Moderators');
            componentSelection.subscribers = wizard.getFinalMultiValue('Subscribers');
            componentSelection.roles = wizard.getFinalMultiMapValue('Additional Roles');

            releases = wizard.getFinalMultiValue('Releases');
            componentSelection.releases = [];
            $.each(releases, function(index, value) {
                /* add just required fields for easy identification */
                componentSelection.releases.push(JSON.parse('{ "id": "' + value.id + '", "name": "' + value.name + '", "version": "' + value.version + '", "componentId": "' + value.componentId + '"}'));
            });

            if ((componentSelection.releases.length || 0) < $stepElement.data('releaseCount')) {
                $stepElement.find('.stepFeedback').html('<div class="alert">Please migrate all releases and keep the existing ones!</div>');
                $('html, body').stop().animate({ scrollTop: 0 }, 300, 'swing');
                setTimeout(function() {
                    $stepElement.find('.stepFeedback').html('');
                }, 5000);
                return false;
            }

            attachments = wizard.getFinalMultiValue('Attachments');
            componentSelection.attachments = [];
            $.each(attachments, function(index, value) {
                /* add just required fields for easy identification */
                componentSelection.attachments.push(JSON.parse('{ "attachmentContentId": "' + value.attachmentContentId + '", "filename": "' + value.filename + '"}'));
            })

            $stepElement.data('componentSelection', componentSelection);
            /* componentSourceId still as data at stepElement */
        }

        function renderConfirmMergedComponent($stepElement, data) {
            $stepElement.data('componentSourceId', data.componentSourceId);
            $stepElement.data('componentSelection', data.componentSelection);

            $stepElement.html('<div class="stepFeedback"></div>');

            $stepElement.append(wizard.createCategoryLine('General'));
            $stepElement.append(wizard.createSingleDisplayLine('Name', data.componentSelection.name));
            $stepElement.append(wizard.createSingleDisplayLine('Created on', data.componentSelection.createdOn));
            $stepElement.append(wizard.createSingleDisplayLine('Created by', data.componentSelection.createdBy));
            $stepElement.append(wizard.createMultiDisplayLine('Categories', data.componentSelection.categories));
            $stepElement.append(wizard.createSingleDisplayLine('Component Type', data.componentSelection.componentType, getComponentTypeDisplayString));
            $stepElement.append(wizard.createSingleDisplayLine('Homepage', data.componentSelection.homepage));
            $stepElement.append(wizard.createSingleDisplayLine('Blog', data.componentSelection.blog));
            $stepElement.append(wizard.createSingleDisplayLine('Wiki', data.componentSelection.wiki));
            $stepElement.append(wizard.createSingleDisplayLine('Mailing list', data.componentSelection.mailinglist));
            $stepElement.append(wizard.createSingleDisplayLine('Description', data.componentSelection.description));

            $stepElement.append(wizard.createCategoryLine('Roles'));
            $stepElement.append(wizard.createSingleDisplayLine('Component owner', data.componentSelection.componentOwner));
            $stepElement.append(wizard.createSingleDisplayLine('Owner accounting unit', data.componentSelection.ownerAccountingUnit));
            $stepElement.append(wizard.createSingleDisplayLine('Owner billing group', data.componentSelection.ownerGroup));
            $stepElement.append(wizard.createSingleDisplayLine('Owner country', data.componentSelection.ownerCountry));
            $stepElement.append(wizard.createMultiDisplayLine('Moderators', data.componentSelection.moderators));
            $stepElement.append(wizard.createMultiDisplayLine('Subscribers', data.componentSelection.subscribers));
            $stepElement.append(wizard.createMultiMapDisplayLine('Additional Roles', data.componentSelection.roles));

            $stepElement.append(wizard.createCategoryLine('Releases'));
            $stepElement.append(wizard.createMultiDisplayLine('Releases', data.componentSelection.releases, function(release) {
                if (!release) {
                    return '';
                }
                return (release.name || '-no-name-') + ' ' + (release.version || '-no-version-');
            }));

            $stepElement.append(wizard.createCategoryLine('Attachments'));
            $stepElement.append(wizard.createMultiDisplayLine('Attachments', data.componentSelection.attachments, function(attachment) {
                if (!attachment) {
                    return '';
                }
                return (attachment.filename || '-no-filename-') + ' (' + (attachment.attachementType || '-no-type-') + ')';
            }));
        }

        function submitConfirmedMergedComponent($stepElement) {
            /* componentSourceId still as data at stepElement */
            /* componentSelection still as data at stepElement */

            /* changing html should be no problem as the data to send is attached directly to the stepElement which is kept */
            $stepElement.html('<div class="spinner">Submitting merge result, please wait...</div>')
        }

        function submitErrorHook($stepElement, textStatus, error) {
            alert('An error happened while communicating with the server: ' + textStatus + error);
        }

        var componentTypeDisplayStrings = {};
        <core_rt:forEach items="${ComponentType.values()}" var="ct">
        componentTypeDisplayStrings[${ct.value}] = '${ThriftEnumUtils.enumToString(ct)}';
        </core_rt:forEach>

        function getComponentTypeDisplayString(componentType){
          return componentTypeDisplayStrings[componentType];
        }
    });
</script>