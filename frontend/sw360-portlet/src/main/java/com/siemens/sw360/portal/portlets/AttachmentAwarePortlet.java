/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License Version 2.0 as published by the
 * Free Software Foundation with classpath exception.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License version 2.0 for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (please see the COPYING file); if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */
package com.siemens.sw360.portal.portlets;

import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.ThriftClients;
import com.siemens.sw360.datahandler.thrift.attachments.Attachment;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentContent;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.portal.common.AttachmentPortletUtils;
import com.siemens.sw360.portal.common.PortalConstants;
import com.siemens.sw360.portal.users.UserCacheHolder;

import javax.portlet.*;
import java.io.IOException;
import java.util.Set;

import static com.siemens.sw360.datahandler.common.CommonUtils.toSingletonSet;
import static com.siemens.sw360.portal.common.PortalConstants.ATTACHMENTS;

/**
 * Attachment portlet implementation
 *
 * @author cedric.bodet@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 */
public abstract class AttachmentAwarePortlet extends Sw360Portlet {

    protected final AttachmentPortletUtils attachmentPortletUtils;

    protected AttachmentAwarePortlet() {
        attachmentPortletUtils = new AttachmentPortletUtils(thriftClients);
    }

    public AttachmentAwarePortlet(ThriftClients thriftClients) {
        super(thriftClients);
        attachmentPortletUtils = new AttachmentPortletUtils(thriftClients);
    }

    public AttachmentAwarePortlet(ThriftClients thriftClients, AttachmentPortletUtils attachmentPortletUtils) {
        super(thriftClients);
        this.attachmentPortletUtils = attachmentPortletUtils;
    }

    public static void setAttachmentsInRequest(RenderRequest request, Set<Attachment> attachments) {
        request.setAttribute(ATTACHMENTS, CommonUtils.nullToEmptySet(attachments));
    }

    private String getDocumentType(ResourceRequest request) {
        return request.getParameter(PortalConstants.DOCUMENT_TYPE);
    }

    protected abstract Set<Attachment> getAttachments(String documentId, String documentType, User user);

    protected void dealWithAttachments(ResourceRequest request, ResourceResponse response, String action) throws IOException, PortletException {
        if (PortalConstants.ATTACHMENT_DOWNLOAD.equals(action)) {
            attachmentPortletUtils.serveFile(request, response);
        } else if (PortalConstants.ATTACHMENT_LIST.equals(action)) {
            serveAttachmentSet(request, response);
        } else if (PortalConstants.ATTACHMENT_LINK_TO.equals(action)) {
            doGetAttachmentForDisplay(request, response);
        } else if (PortalConstants.ATTACHMENT_RESERVE_ID.equals(action)) {
            serveNewAttachmentId(request, response);
        } else if (PortalConstants.ATTACHMENT_UPLOAD.equals(action)) {
            if ("POST".equals(request.getMethod())) {
                // POST is for actual upload
                if (!attachmentPortletUtils.uploadAttachmentPart(request, "file")) {
                    response.setProperty(ResourceResponse.HTTP_STATUS_CODE, "404");
                }
            } else {
                // GET is to check if it already exists: 200 or 204 return code
                if (!attachmentPortletUtils.checkAttachmentExistsFromRequest(request)) {
                    response.setProperty(ResourceResponse.HTTP_STATUS_CODE, "204");
                }
            }
        } else if (PortalConstants.ATTACHMENT_CANCEL.equals(action)) {
            RequestStatus status = attachmentPortletUtils.cancelUpload(request);
            renderRequestStatus(request, response, status);
        }
    }

    private void doGetAttachmentForDisplay(ResourceRequest request, ResourceResponse response) throws IOException, PortletException {
        String attachmentId = request.getParameter(PortalConstants.ATTACHMENT_ID);
        final User user = UserCacheHolder.getUserFromRequest(request);

        Attachment attachment = attachmentPortletUtils.getAttachmentForDisplay(user, attachmentId);

        if(attachment==null) {
            response.setProperty(ResourceResponse.HTTP_STATUS_CODE, "500");
        } else {
            request.setAttribute(PortalConstants.DOCUMENT_TYPE, getDocumentType(request));
            request.setAttribute(PortalConstants.ATTACHMENTS, toSingletonSet(attachment));
            include("/html/utils/ajax/attachmentsAjax.jsp", request, response, PortletRequest.RESOURCE_PHASE);
        }
    }

    private void serveAttachmentSet(ResourceRequest request, ResourceResponse response) throws IOException, PortletException {
        String documentId = request.getParameter(PortalConstants.DOCUMENT_ID);
        final User user = UserCacheHolder.getUserFromRequest(request);

        request.setAttribute(PortalConstants.DOCUMENT_TYPE, getDocumentType(request));
        request.setAttribute(PortalConstants.ATTACHMENTS, getAttachments(documentId, getDocumentType(request), user));
        include("/html/utils/ajax/attachmentsAjax.jsp", request, response, PortletRequest.RESOURCE_PHASE);
    }

    private void serveNewAttachmentId(ResourceRequest request, ResourceResponse response) throws IOException {
        final AttachmentContent attachment = attachmentPortletUtils.createAttachmentContent(request);
        if (attachment == null) {
            response.setProperty(ResourceResponse.HTTP_STATUS_CODE, "404");
        } else {
            final String attachmentId = attachment.getId();
            response.getWriter().write(attachmentId);
        }
    }

    protected boolean isAttachmentAwareAction(String action) {
        return action.startsWith(PortalConstants.ATTACHMENT_PREFIX);
    }

    @Override
    protected boolean isGenericAction(String action) {
        return super.isGenericAction(action) || isAttachmentAwareAction(action);
    }

    @Override
    protected void dealWithGenericAction(ResourceRequest request, ResourceResponse response, String action) throws IOException, PortletException {
        if (super.isGenericAction(action)) {
            super.dealWithGenericAction(request, response, action);
        } else {
            dealWithAttachments(request, response, action);
        }
    }
}
