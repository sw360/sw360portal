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
package com.siemens.sw360.portal.portlets.components;

import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.ThriftClients;
import com.siemens.sw360.datahandler.thrift.attachments.Attachment;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentType;
import com.siemens.sw360.datahandler.thrift.attachments.CheckStatus;
import com.siemens.sw360.datahandler.thrift.components.*;
import com.siemens.sw360.datahandler.thrift.users.RequestedAction;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.thrift.vendors.Vendor;
import com.siemens.sw360.datahandler.thrift.vendors.VendorService;
import com.siemens.sw360.portal.common.PortalConstants;
import com.siemens.sw360.portal.common.PortletUtils;
import com.siemens.sw360.portal.users.UserCacheHolder;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import javax.portlet.PortletRequest;
import javax.portlet.ResourceRequest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Component portlet implementation
 *
 * @author cedric.bodet@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 */
public abstract class ComponentPortletUtils {

    private ComponentPortletUtils() {
        // Utility class with only static functions
    }

    public static void updateReleaseFromRequest(PortletRequest request, Release release) {
        for (Release._Fields field : Release._Fields.values()) {
            if (field != Release._Fields.ATTACHMENTS) {
                switch (field) {
                    case REPOSITORY:
                        release.setFieldValue(field, getRepositoryFromRequest(request));
                        break;
                    case CLEARING_INFORMATION:
                        release.setFieldValue(field, getClearingInformationFromRequest(request));
                        break;
                    case COTS_DETAILS:
                        release.setFieldValue(field, getCOTSDetailsFromRequest(request));
                        break;
                    case VENDOR_ID:
                        release.unsetVendor();
                        setFieldValue(request, release, field);
                        break;
                    case RELEASE_ID_TO_RELATIONSHIP:
                        if (!release.isSetReleaseIdToRelationship())
                            release.setReleaseIdToRelationship(new HashMap<String, ReleaseRelationship>());
                        updateLinkedReleaesFromRequest(request, release.releaseIdToRelationship);
                        break;
                    default:
                        setFieldValue(request, release, field);
                }
            }
        }
        // ensure ATTACHMENTS are processed after CLEARING_STATE so that automatic asignment of CLEARING_STATE will not get overwritten by the clearing state from request
        if (!release.isSetAttachments()) release.setAttachments(new HashSet<Attachment>());
        PortletUtils.updateAttachmentsFromRequest(request, release.getAttachments());
        updateReleaseClearingStateOnAttachmentChange(release);
    }

    private static void updateReleaseClearingStateOnAttachmentChange(Release release) {
        if (release.getAttachments().stream().anyMatch(attachment -> attachment.getAttachmentType() == AttachmentType.CLEARING_REPORT && attachment.getCheckStatus() == CheckStatus.ACCEPTED))
            release.setClearingState(ClearingState.APPROVED);
        else if (release.getAttachments().stream().anyMatch(attachment -> attachment.getAttachmentType() == AttachmentType.CLEARING_REPORT))
            release.setClearingState(ClearingState.REPORT_AVAILABLE);

    }

    private static ClearingInformation getClearingInformationFromRequest(PortletRequest request) {
        ClearingInformation clearingInformation = new ClearingInformation();
        for (ClearingInformation._Fields field : ClearingInformation._Fields.values()) {
            setFieldValue(request, clearingInformation, field);
        }

        return clearingInformation;
    }

    private static COTSDetails getCOTSDetailsFromRequest(PortletRequest request) {
        COTSDetails cotsDetails = new COTSDetails();
        for (COTSDetails._Fields field : COTSDetails._Fields.values()) {
            setFieldValue(request, cotsDetails, field);
        }

        return cotsDetails;
    }

    private static Repository getRepositoryFromRequest(PortletRequest request) {
        Repository repository = new Repository();
        setFieldValue(request, repository, Repository._Fields.REPOSITORYTYPE);
        setFieldValue(request, repository, Repository._Fields.URL);

        if (!repository.isSetUrl() || isNullOrEmpty(repository.getUrl())) {
            repository = null;
        }
        return repository;
    }

    static void updateComponentFromRequest(PortletRequest request, Component component) {
        for (Component._Fields field : Component._Fields.values()) {

            switch (field) {
                case ATTACHMENTS:
                    if (!component.isSetAttachments()) component.setAttachments(new HashSet<Attachment>());
                    PortletUtils.updateAttachmentsFromRequest(request, component.getAttachments());
                    break;


                default:
                    setFieldValue(request, component, field);
            }
        }
    }

    public static void updateVendorFromRequest(PortletRequest request, Vendor vendor) {
        setFieldValue(request, vendor, Vendor._Fields.FULLNAME);
        setFieldValue(request, vendor, Vendor._Fields.SHORTNAME);
        setFieldValue(request, vendor, Vendor._Fields.URL);
    }

    private static void updateLinkedReleaesFromRequest(PortletRequest request, Map<String, ReleaseRelationship> linkedReleases) {
        linkedReleases.clear();
        String[] ids = request.getParameterValues(Release._Fields.RELEASE_ID_TO_RELATIONSHIP.toString() + ReleaseLink._Fields.ID.toString());
        String[] relations = request.getParameterValues(Release._Fields.RELEASE_ID_TO_RELATIONSHIP.toString() + ReleaseLink._Fields.RELEASE_RELATIONSHIP.toString());
        if (ids != null && relations != null && ids.length == relations.length)
            for (int k = 0; k < ids.length; ++k) {
                linkedReleases.put(ids[k], ReleaseRelationship.findByValue(Integer.parseInt(relations[k])));
            }
    }

    private static void setFieldValue(PortletRequest request, Component component, Component._Fields field) {
        PortletUtils.setFieldValue(request, component, field, Component.metaDataMap.get(field), "");
    }

    private static void setFieldValue(PortletRequest request, Release release, Release._Fields field) {
        PortletUtils.setFieldValue(request, release, field, Release.metaDataMap.get(field), "");
    }

    private static void setFieldValue(PortletRequest request, Repository repository, Repository._Fields field) {
        PortletUtils.setFieldValue(request, repository, field, Repository.metaDataMap.get(field), Release._Fields.REPOSITORY.toString());
    }

    private static void setFieldValue(PortletRequest request, ClearingInformation clearingInformation, ClearingInformation._Fields field) {
        PortletUtils.setFieldValue(request, clearingInformation, field, ClearingInformation.metaDataMap.get(field), Release._Fields.CLEARING_INFORMATION.toString());
    }

    private static void setFieldValue(PortletRequest request, COTSDetails cotsDetails, COTSDetails._Fields field) {
        PortletUtils.setFieldValue(request, cotsDetails, field, COTSDetails.metaDataMap.get(field), Release._Fields.COTS_DETAILS.toString());
    }

    private static void setFieldValue(PortletRequest request, Vendor vendor, Vendor._Fields field) {
        PortletUtils.setFieldValue(request, vendor, field, Vendor.metaDataMap.get(field), "");
    }

    public static RequestStatus deleteRelease(PortletRequest request, Logger log) {
        String releaseId = request.getParameter(PortalConstants.RELEASE_ID);
        if (releaseId != null) {
            try {
                ComponentService.Iface client = new ThriftClients().makeComponentClient();
                return client.deleteRelease(releaseId, UserCacheHolder.getUserFromRequest(request));

            } catch (TException e) {
                log.error("Could not delete release from DB", e);
            }
        }
        return RequestStatus.FAILURE;
    }

    public static RequestStatus deleteVendor(PortletRequest request, Logger log) {
        String vendorId = request.getParameter(PortalConstants.VENDOR_ID);
        if (vendorId != null) {
            try {
                User user = UserCacheHolder.getUserFromRequest(request);
                ThriftClients thriftClients = new ThriftClients();
                ComponentService.Iface componentClient = thriftClients.makeComponentClient();
                VendorService.Iface client = thriftClients.makeVendorClient();

                RequestStatus global_status = RequestStatus.SUCCESS;

                List<Release> releases = componentClient.getReleasesFromVendorId(vendorId, user);

                boolean mayWriteToAllReleases = true;
                for (Release release : releases) {
                    Map<RequestedAction, Boolean> permissions = release.getPermissions();
                    mayWriteToAllReleases &= permissions.get(RequestedAction.WRITE);
                }

                if (!mayWriteToAllReleases) {
                    return RequestStatus.FAILURE;
                }

                for (Release release : releases) {
                    release.unsetVendorId();
                    RequestStatus local_status = componentClient.updateRelease(release, user);
                    if (local_status != RequestStatus.SUCCESS) global_status = local_status;
                }

                if (global_status == RequestStatus.SUCCESS) {
                    return client.deleteVendor(vendorId, user);
                } else {
                    return global_status;
                }

            } catch (TException e) {
                log.error("Could not delete release from DB", e);
            }
        }
        return RequestStatus.FAILURE;
    }

    public static RequestStatus deleteComponent(PortletRequest request, Logger log) {
        String id = request.getParameter(PortalConstants.COMPONENT_ID);
        if (id != null) {
            try {
                ComponentService.Iface client = new ThriftClients().makeComponentClient();
                return client.deleteComponent(id, UserCacheHolder.getUserFromRequest(request));

            } catch (TException e) {
                log.error("Could not delete component from DB", e);
            }
        }
        return RequestStatus.FAILURE;
    }

    public static RequestStatus subscribeComponent(ResourceRequest request, Logger log) {
        String id = request.getParameter(PortalConstants.COMPONENT_ID);
        if (id != null) {
            try {
                ComponentService.Iface client = new ThriftClients().makeComponentClient();
                User user = UserCacheHolder.getUserFromRequest(request);
                return client.subscribeComponent(id, user);

            } catch (TException e) {
                log.error("Could not subscribe to component", e);
            }
        }
        return RequestStatus.FAILURE;
    }

    public static RequestStatus subscribeRelease(ResourceRequest request, Logger log) {
        String id = request.getParameter(PortalConstants.RELEASE_ID);
        if (id != null) {
            try {
                ComponentService.Iface client = new ThriftClients().makeComponentClient();
                User user = UserCacheHolder.getUserFromRequest(request);
                return client.subscribeRelease(id, user);

            } catch (TException e) {
                log.error("Could not subscribe to release", e);
            }
        }
        return RequestStatus.FAILURE;
    }

    public static RequestStatus unsubscribeComponent(ResourceRequest request, Logger log) {
        String id = request.getParameter(PortalConstants.COMPONENT_ID);
        if (id != null) {
            try {
                ComponentService.Iface client = new ThriftClients().makeComponentClient();
                User user = UserCacheHolder.getUserFromRequest(request);
                return client.unsubscribeComponent(id, user);

            } catch (TException e) {
                log.error("Could not unsubscribe to component", e);
            }
        }
        return RequestStatus.FAILURE;
    }

    public static RequestStatus unsubscribeRelease(ResourceRequest request, Logger log) {
        String id = request.getParameter(PortalConstants.RELEASE_ID);
        if (id != null) {
            try {
                ComponentService.Iface client = new ThriftClients().makeComponentClient();
                User user = UserCacheHolder.getUserFromRequest(request);
                return client.unsubscribeRelease(id, user);

            } catch (TException e) {
                log.error("Could not unsubscribe to release", e);
            }
        }
        return RequestStatus.FAILURE;
    }
}
