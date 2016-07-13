/*
 * Copyright Siemens AG, 2013-2016. Part of the SW360 Portal Project.
 * With modifications by Bosch Software Innovations GmbH, 2016.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.datahandler.db;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.siemens.sw360.components.summary.SummaryType;
import com.siemens.sw360.datahandler.businessrules.ReleaseClearingStateSummaryComputer;
import com.siemens.sw360.datahandler.common.CommonUtils;
import com.siemens.sw360.datahandler.common.SW360Utils;
import com.siemens.sw360.datahandler.couchdb.AttachmentConnector;
import com.siemens.sw360.datahandler.couchdb.DatabaseConnector;
import com.siemens.sw360.datahandler.entitlement.ComponentModerator;
import com.siemens.sw360.datahandler.entitlement.ReleaseModerator;
import com.siemens.sw360.datahandler.permissions.PermissionUtils;
import com.siemens.sw360.datahandler.thrift.*;
import com.siemens.sw360.datahandler.thrift.attachments.Attachment;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentType;
import com.siemens.sw360.datahandler.thrift.attachments.CheckStatus;
import com.siemens.sw360.datahandler.thrift.components.*;
import com.siemens.sw360.datahandler.thrift.moderation.ModerationRequest;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.users.RequestedAction;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.datahandler.thrift.vendors.Vendor;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.ektorp.DocumentOperationResult;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Sets.newHashSet;
import static com.siemens.sw360.datahandler.common.CommonUtils.getBestClearingReport;
import static com.siemens.sw360.datahandler.common.CommonUtils.isInProgressOrPending;
import static com.siemens.sw360.datahandler.common.CommonUtils.nullToEmptySet;
import static com.siemens.sw360.datahandler.common.Duration.durationOf;
import static com.siemens.sw360.datahandler.common.SW360Assert.assertNotNull;
import static com.siemens.sw360.datahandler.common.SW360Assert.fail;
import static com.siemens.sw360.datahandler.permissions.PermissionUtils.makePermission;
import static com.siemens.sw360.datahandler.thrift.ThriftUtils.copyFields;
import static com.siemens.sw360.datahandler.thrift.ThriftUtils.immutableOfComponent;
import static com.siemens.sw360.datahandler.thrift.ThriftValidate.prepareComponents;
import static com.siemens.sw360.datahandler.thrift.ThriftValidate.prepareReleases;

/**
 * Class for accessing Component information from the database
 *
 * @author cedric.bodet@tngtech.com
 * @author Johannes.Najjar@tngtech.com
 * @author alex.borodin@evosoft.com
 */
public class ComponentDatabaseHandler {

    private static final Logger log = Logger.getLogger(ComponentDatabaseHandler.class);

    /**
     * Connection to the couchDB database
     */
    private final ComponentRepository componentRepository;
    private final ReleaseRepository releaseRepository;
    private final VendorRepository vendorRepository;
    private final ProjectRepository projectRepository;

    private final AttachmentConnector attachmentConnector;
    /**
     * Access to moderation
     */
    private final ComponentModerator moderator;
    private final ReleaseModerator releaseModerator;


    public ComponentDatabaseHandler(String url, String dbName, String attachmentDbName, ComponentModerator moderator, ReleaseModerator releaseModerator) throws MalformedURLException {
        DatabaseConnector db = new DatabaseConnector(url, dbName);

        // Create the repositories
        vendorRepository = new VendorRepository(db);
        releaseRepository = new ReleaseRepository(db, vendorRepository);
        componentRepository = new ComponentRepository(db, releaseRepository, vendorRepository);
        projectRepository = new ProjectRepository(db);

        // Create the moderator
        this.moderator = moderator;
        this.releaseModerator = releaseModerator;

        // Create the attachment connector
        attachmentConnector = new AttachmentConnector(url, attachmentDbName, durationOf(30, TimeUnit.SECONDS));
    }


    public ComponentDatabaseHandler(String url, String dbName, String attachmentDbName) throws MalformedURLException {
        this(url, dbName, attachmentDbName, new ComponentModerator(), new ReleaseModerator());
    }

    public ComponentDatabaseHandler(String url, String dbName, String attachmentDbName, ThriftClients thriftClients) throws MalformedURLException {
        this(url, dbName, attachmentDbName, new ComponentModerator(thriftClients), new ReleaseModerator(thriftClients));
    }

    private void autosetReleaseClearingState(Release releaseAfter, Release releaseBefore) {
        Optional<Attachment> oldBestCR = getBestClearingReport(releaseBefore);
        Optional<Attachment> newBestCR = getBestClearingReport(releaseAfter);
        if (newBestCR.isPresent()){
            if (newBestCR.get().getCheckStatus() == CheckStatus.ACCEPTED){
                releaseAfter.setClearingState(ClearingState.APPROVED);
            }else{
                releaseAfter.setClearingState(ClearingState.REPORT_AVAILABLE);
            }
        } else {
            if (oldBestCR.isPresent()) releaseAfter.setClearingState(ClearingState.NEW_CLEARING);
        }
    }

    /////////////////////
    // SUMMARY GETTERS //
    /////////////////////

    public List<Component> getComponentSummary(User user) {
        return componentRepository.getComponentSummary(user);
    }

    public List<Component> getComponentSummaryForExport() {
        return componentRepository.getSummaryForExport();
    }

    public List<Component> getComponentDetailedSummaryForExport() {
        return componentRepository.getDetailedSummaryForExport();
    }

    public List<Release> getReleaseSummary() {
        return releaseRepository.getReleaseSummary();
    }

    public List<Component> getRecentComponents() {
        return componentRepository.getRecentComponents();
    }


    public List<Release> getRecentReleases() {
        return releaseRepository.getRecentReleases();
    }

    public List<Component> getSubscribedComponents(String user) {
        return componentRepository.getSubscribedComponents(user);
    }

    public List<Release> getSubscribedReleases(String email) {
        return releaseRepository.getSubscribedReleases(email);
    }


    public List<Release> getReleasesFromVendorId(String id, User user) throws TException {
        return releaseRepository.getReleasesFromVendorId(id, user);
    }

    public List<Release> getReleasesFromVendorIds(Set<String> ids) {
        return releaseRepository.getReleasesFromVendorIds(ids);
    }

    public List<Release> getReleasesFromComponentId(String id, User user) throws TException {
        return releaseRepository.getReleasesFromComponentId(id, user);
    }

    public List<Component> getMyComponents(String user) {
        //This call could be sped up, because we want the full documents
        Set<String> myComponentIds = componentRepository.getMyComponentIds(user);

        return componentRepository.makeSummary(SummaryType.HOME, myComponentIds);
    }

    public List<Component> getSummaryForExport() {
        return componentRepository.getSummaryForExport();
    }

    ////////////////////////////
    // GET INDIVIDUAL OBJECTS //
    ////////////////////////////

    public Component getComponent(String id, User user) throws SW360Exception {
        Component component = componentRepository.get(id);

        if (component == null) {
            throw fail("Could not fetch component from database! id=" + id);
        }

        // Convert Ids to release summary
        component.setReleases(releaseRepository.makeSummaryWithPermissions(SummaryType.SUMMARY, component.releaseIds, user));
        component.unsetReleaseIds();

        setMainLicenses(component);

        // Set permissions
        makePermission(component, user).fillPermissions();

        return component;
    }

    public Release getRelease(String id, User user) throws SW360Exception {
        Release release = releaseRepository.get(id);

        if (release == null) {
            throw fail("Could not fetch release from database! id=" + id);
        }

        fillVendor(release);
        // Set permissions
        if (user != null) {
            makePermission(release, user).fillPermissions();
        }

        return release;
    }

    private void setMainLicenses(Component component) {
        if (!component.isSetMainLicenseIds() && component.isSetReleases()) {
            Set<String> licenseIds = new HashSet<>();

            for (Release release : component.getReleases()) {
                licenseIds.addAll(nullToEmptySet(release.getMainLicenseIds()));
            }

            component.setMainLicenseIds(licenseIds);
        }
    }

    ////////////////////////////
    // ADD INDIVIDUAL OBJECTS //
    ////////////////////////////

    /**
     * Add new release to the database
     */
    public String addComponent(Component component, String user) throws SW360Exception {

        // Prepare the component
        prepareComponent(component);

        // Save creating user
        component.setCreatedBy(user);
        component.setCreatedOn(SW360Utils.getCreatedOn());

        // Add the component to the database and return ID
        componentRepository.add(component);
        return component.getId();
    }

    /**
     * Add a single new release to the database
     */
    public String addRelease(Release release, String user) throws SW360Exception {

        // Prepare the release and get underlying component ID
        prepareRelease(release);
        String componentId = release.getComponentId();

        // Ensure that component exists
        Component component = componentRepository.get(componentId);
        assertNotNull(component);

        // Save creating user
        release.setCreatedBy(user);
        release.setCreatedOn(SW360Utils.getCreatedOn());

        // Add release to database
        releaseRepository.add(release);
        final String id = release.getId();

        // Update the underlying component
        component.addToReleaseIds(id);

        if (!component.isSetLanguages()) {
            component.setLanguages(new HashSet<String>());
        }
        if (!component.isSetOperatingSystems()) {
            component.setOperatingSystems(new HashSet<String>());
        }
        if (!component.isSetVendorNames()) {
            component.setVendorNames(new HashSet<String>());
        }
        if (!component.isSetMainLicenseIds()) {
            component.setMainLicenseIds(new HashSet<String>());
        }

        updateReleaseDependentFieldsForComponent(component, release);
        componentRepository.update(component);

        return id;
    }

    private void resetReleaseDependentFields(Component component) {
        component.setLanguages(new HashSet<String>());
        component.setOperatingSystems(new HashSet<String>());
        component.setVendorNames(new HashSet<String>());
        component.setMainLicenseIds(new HashSet<String>());
    }

    public void updateReleaseDependentFieldsForComponent(Component component, Release release) {
        if (release != null && component != null) {
            if (!component.isSetLanguages()) {
                component.setLanguages(new HashSet<String>());
            }
            component.languages.addAll(nullToEmptySet(release.languages));

            if (!component.isSetOperatingSystems()) {
                component.setOperatingSystems(new HashSet<String>());
            }
            component.operatingSystems.addAll(nullToEmptySet(release.operatingSystems));

            if (!component.isSetVendorNames()) {
                component.setVendorNames(new HashSet<String>());
            }
            if (release.vendor != null)
                component.vendorNames.add(release.vendor.getFullname());
            else if (!isNullOrEmpty(release.vendorId)) {
                Vendor vendor = getVendor(release.vendorId);
                component.vendorNames.add(vendor.getFullname());
            }

            if (!component.isSetMainLicenseIds()) component.setMainLicenseIds(new HashSet<String>());
            if (release.isSetMainLicenseIds()) {
                component.getMainLicenseIds().addAll(release.getMainLicenseIds());
            }
        }
    }

    private Vendor getVendor(String vendorId) {
        return vendorRepository.get(vendorId);
    }

    ///////////////////////////////
    // UPDATE INDIVIDUAL OBJECTS //
    ///////////////////////////////

    public RequestStatus updateComponent(Component component, User user) throws SW360Exception {
        // Prepare component for database
        prepareComponent(component);

        // Get actual document for members that should no change
        Component actual = componentRepository.get(component.getId());
        assertNotNull(actual, "Could not find component to doBulk!");

        if (makePermission(actual, user).isActionAllowed(RequestedAction.WRITE)) {

            // Nested releases and attachments should not be updated by this method
            if (actual.isSetReleaseIds())
                component.setReleaseIds(actual.getReleaseIds());
            component.unsetReleases();

            copyFields(actual, component, immutableOfComponent());
            // Update the database with the component
            componentRepository.update(component);
            //clean up attachments in database
            attachmentConnector.deleteAttachmentDifference(actual.getAttachments(),component.getAttachments());

        } else {
            return moderator.updateComponent(component, user);
        }
        return RequestStatus.SUCCESS;
    }

    private void prepareComponent(Component component) throws SW360Exception {
        // Prepare component for database
        ThriftValidate.prepareComponent(component);

        //add sha1 to attachments if necessary
        if(component.isSetAttachments()) {
            attachmentConnector.setSha1ForAttachments(component.getAttachments());
        }
    }

    public RequestSummary updateComponents(Set<Component> components, User user) throws SW360Exception {
        return RepositoryUtils.doBulk(prepareComponents(components), user, componentRepository);
    }


    public RequestStatus updateComponentFromAdditionsAndDeletions(Component componentAdditions, Component componentDeletions, User user){

        try {
            Component component= getComponent(componentAdditions.getId(), user);
            component = moderator.updateComponentFromModerationRequest(component, componentAdditions, componentDeletions);
            return updateComponent(component, user);
        } catch (SW360Exception e) {
            log.error("Could not get original component when updating from moderation request.");
            return RequestStatus.FAILURE;
        }
    }


    public RequestStatus updateRelease(Release release, User user, Iterable<Release._Fields> immutableFields) throws SW360Exception {
        // Prepare release for database
        prepareRelease(release);

        // Get actual document for members that should no change
        Release actual = releaseRepository.get(release.getId());
        assertNotNull(actual, "Could not find release to update");

        if (actual.equals(release)) {
            return RequestStatus.SUCCESS;
        }
        if (makePermission(actual, user).isActionAllowed(RequestedAction.WRITE)) {
            copyFields(actual, release, immutableFields);

            autosetReleaseClearingState(release, actual);

            releaseRepository.update(release);
            updateReleaseDependentFieldsForComponentId(release.getComponentId());
            //clean up attachments in database
            attachmentConnector.deleteAttachmentDifference(nullToEmptySet(actual.getAttachments()),nullToEmptySet(release.getAttachments()));

        } else {
            return releaseModerator.updateRelease(release, user);
        }

        return RequestStatus.SUCCESS;
    }

    private void prepareRelease(Release release) throws SW360Exception {
        // Prepare release for database
        ThriftValidate.prepareRelease(release);

        //add sha1 to attachments if necessary
        if(release.isSetAttachments()) {
            attachmentConnector.setSha1ForAttachments(release.getAttachments());
        }
    }

    public RequestSummary updateReleases(Collection<Release> releases, User user) throws SW360Exception {
        List<Release> storedReleases = prepareReleases(releases);

        RequestSummary requestSummary = new RequestSummary();
        if (PermissionUtils.isAdmin(user)) {
            // Prepare component for database
            final List<DocumentOperationResult> documentOperationResults = componentRepository.executeBulk(storedReleases);

            if (documentOperationResults.isEmpty()) {

                final List<Component> componentList = componentRepository.get(storedReleases
                        .stream()
                        .map(Release::getComponentId)
                        .collect(Collectors.toSet()));

                final Map<String, Component> componentsById = ThriftUtils.getIdMap(componentList);

                for (Release storedRelease : storedReleases) {
                    final Component component = componentsById.get(storedRelease.getComponentId());
                    component.addToReleaseIds(storedRelease.getId());
                    updateReleaseDependentFieldsForComponent(component, storedRelease);
                }

                updateComponents(newHashSet(componentList), user);
            }

            requestSummary.setTotalElements(storedReleases.size());
            requestSummary.setTotalAffectedElements(storedReleases.size() - documentOperationResults.size());

            requestSummary.setRequestStatus(RequestStatus.SUCCESS);
        } else {
            requestSummary.setRequestStatus(RequestStatus.FAILURE);
        }
        return requestSummary;
    }

    public RequestStatus updateReleaseFromAdditionsAndDeletions(Release releaseAdditions, Release releaseDeletions, User user){

        try {
            Release release = getRelease(releaseAdditions.getId(), user);
            release = releaseModerator.updateReleaseFromModerationRequest(release, releaseAdditions, releaseDeletions);
            return updateRelease(release, user, ThriftUtils.immutableOfRelease());
        } catch (SW360Exception e) {
            log.error("Could not get original release when updating from moderation request.");
            return RequestStatus.FAILURE;
        }

    }

    protected Component updateReleaseDependentFieldsForComponentId(String componentId) {
        Component component = componentRepository.get(componentId);
        recomputeReleaseDependentFields(component, null);
        componentRepository.update(component);

        return component;
    }

    public void recomputeReleaseDependentFields(Component component, String skipThisReleaseId) {
        resetReleaseDependentFields(component);

        List<Release> releases = releaseRepository.get(component.getReleaseIds());
        for (Release containedRelease : releases) {
            if (containedRelease.getId().equals(skipThisReleaseId)) continue;
            updateReleaseDependentFieldsForComponent(component, containedRelease);
        }
    }

    ///////////////////////////////
    // DELETE INDIVIDUAL OBJECTS //
    ///////////////////////////////

    public RequestStatus deleteComponent(String id, User user) throws SW360Exception {
        Component component = componentRepository.get(id);
        assertNotNull(component);

        final Set<String> releaseIds = component.getReleaseIds();
        if (checkIfInUse(releaseIds)) return RequestStatus.IN_USE;


        if (makePermission(component, user).isActionAllowed(RequestedAction.DELETE)) {


            for (Release release : releaseRepository.get(nullToEmptySet(component.releaseIds))) {
                component = removeReleaseAndCleanUp(release);
            }

            // Remove the component with attachments
            attachmentConnector.deleteAttachments(component.getAttachments());
            componentRepository.remove(component);
            moderator.notifyModeratorOnDelete(id);
            return RequestStatus.SUCCESS;
        } else {
            return moderator.deleteComponent(component, user);
        }
    }

    public boolean checkIfInUseComponent(String componentId) {
        Component component = componentRepository.get(componentId);
        return checkIfInUse(component);
    }

    public boolean checkIfInUse(Component component) {
        return checkIfInUse(component.getReleaseIds());
    }

    public boolean checkIfInUse(Set<String> releaseIds) {
        if (releaseIds != null && releaseIds.size() > 0) {
            final Set<Component> usingComponents = componentRepository.getUsingComponents(releaseIds);
            if (usingComponents.size() > 0)
                return true;

            final Set<Project> usingProjects = projectRepository.searchByReleaseId(releaseIds);
            if (usingProjects.size() > 0)
                return true;
        }
        return false;
    }

    public boolean checkIfInUse(String releaseId) {

        final Set<Component> usingComponents = componentRepository.getUsingComponents(releaseId);
        if (usingComponents.size() > 0)
            return true;

        final Set<Project> usingProjects = projectRepository.searchByReleaseId(releaseId);
        return (usingProjects.size() > 0);
    }

    private Component removeReleaseAndCleanUp(Release release) {
        attachmentConnector.deleteAttachments(release.getAttachments());

        Component component = updateReleaseDependentFieldsForComponentId(release.getComponentId());

        //TODO notify using projects!?? Or stop if there are any

        moderator.notifyModeratorOnDelete(release.getId());
        releaseRepository.remove(release);

        return component;
    }

    public RequestStatus deleteRelease(String id, User user) throws SW360Exception {
        Release release = releaseRepository.get(id);
        assertNotNull(release);

        if (checkIfInUse(id)) return RequestStatus.IN_USE;

        if (makePermission(release, user).isActionAllowed(RequestedAction.DELETE)) {
            // Remove release id from component
            removeReleaseId(id, release.componentId);
            removeReleaseAndCleanUp(release);
            return RequestStatus.SUCCESS;
        } else {
            return releaseModerator.deleteRelease(release, user);
        }
    }

    private void removeReleaseId(String releaseId, String componentId) throws SW360Exception {
        // Remove release id from component
        Component component = componentRepository.get(componentId);
        assertNotNull(component);
        recomputeReleaseDependentFields(component, releaseId);
        component.getReleaseIds().remove(releaseId);
        componentRepository.update(component);
    }

    /////////////////////
    // HELPER SERVICES //
    /////////////////////

    /**
     * Get a summary of release status for a given set of IDs
     */
    public ReleaseClearingStateSummary getReleaseClearingStateSummary(Set<String> ids, String clearingTeam) {

        List<Release> releases = null;
        if (ids != null && ids.size() > 0) {
            releases = releaseRepository.get(ids);
        }

        return ReleaseClearingStateSummaryComputer.computeReleaseClearingStateSummary(releases, clearingTeam);
    }

    public List<ReleaseLink> getLinkedReleases(Map<String, ?> relations) {
        List<ReleaseLink> out;

        final List<Release> releases = releaseRepository.getAll();
        final Map<String, Release> releaseMap = ThriftUtils.getIdMap(releases);

        Set<String> visitedIds = new HashSet<>();

        out = iterateReleaseRelationShips(relations, releaseMap, visitedIds, null);

        return out;
    }

    @NotNull
    private List<ReleaseLink> iterateReleaseRelationShips(Map<String, ?> relations, Map<String, Release> releaseMap, Set<String> visitedIds, String parentId) {
        List<ReleaseLink> out = new ArrayList<>();

        for (Map.Entry<String, ?> entry : relations.entrySet()) {
            String id = entry.getKey();
            Release release = releaseMap.get(id);
            Optional<ReleaseLink> releaseLinkOptional = createReleaseLink(visitedIds, entry, id, release, releaseMap, parentId);
            if (releaseLinkOptional.isPresent()) {
                out.add(releaseLinkOptional.get());
            }
        }
        return out;
    }

    private Optional<ReleaseLink> createReleaseLink(Set<String> visitedIds, Map.Entry<String, ?> entry, String id, Release release, Map<String, Release> releaseMap, String parentId) {
        if (visitedIds.add(id)) {
            if (release != null) {
                final ReleaseLink releaseLink = getReleaseLink(release);
                fillValueFieldInReleaseLink(entry, releaseLink);
                releaseLink.setParentId(parentId);
                if (release.isSetMainLicenseIds()) {
                    releaseLink.setLicenseIds(release.getMainLicenseIds());
                }

                if (release.isSetReleaseIdToRelationship()) {
                    List<ReleaseLink> subreleaseLinks = iterateReleaseRelationShips(release.getReleaseIdToRelationship(), releaseMap, visitedIds, id);
                    releaseLink.setSubreleases(subreleaseLinks);
                }
                return Optional.of(releaseLink);
            } else {
                log.error("Broken ReleaseLink in release with id: " + id + ", was not in DB");
            }
        }
        return Optional.empty();
    }


    private void fillValueFieldInReleaseLink(Map.Entry<String, ?> entry, ReleaseLink releaseLink) {
        Object value = entry.getValue();
        if (value instanceof String) {
            releaseLink.setComment((String) value);
        } else if (value instanceof ReleaseRelationship) {
            releaseLink.setReleaseRelationship((ReleaseRelationship) value);
        }
    }

    @NotNull
    private ReleaseLink getReleaseLink(Release release) {
        String fullname = "";
        if (!isNullOrEmpty(release.getVendorId())) {
            final Vendor vendor = vendorRepository.get(release.getVendorId());
            fullname = vendor != null ? vendor.getFullname() : "";
        }
        return new ReleaseLink(release.id, fullname, release.name, release.version);
    }

    public List<Release> searchReleaseByName(String name) {
        return releaseRepository.searchByName(name);
    }

    public List<Release> getReleases(Set<String> ids, User user) {
        return releaseRepository.makeSummary(SummaryType.SHORT, ids);
    }

    public List<Release> getFullReleases(Set<String> ids, User user) {
        return releaseRepository.makeSummary(SummaryType.SUMMARY, ids);
    }

    public List<Release> getReleasesWithPermissions(Set<String> ids, User user) {
        return releaseRepository.makeSummaryWithPermissions(SummaryType.SUMMARY, ids, user);
    }

    public RequestStatus subscribeComponent(String id, User user) throws SW360Exception {
        Component component = componentRepository.get(id);
        assertNotNull(component);

        component.addToSubscribers(user.getEmail());
        componentRepository.update(component);
        return RequestStatus.SUCCESS;
    }

    public RequestStatus subscribeRelease(String id, User user) throws SW360Exception {
        Release release = releaseRepository.get(id);
        assertNotNull(release);

        release.addToSubscribers(user.getEmail());
        releaseRepository.update(release);
        return RequestStatus.SUCCESS;
    }


    public RequestStatus unsubscribeComponent(String id, User user) throws SW360Exception {
        Component component = componentRepository.get(id);
        assertNotNull(component);

        Set<String> subscribers = component.getSubscribers();
        String email = user.getEmail();
        if (subscribers != null && email != null) {
            subscribers.remove(email);
            component.setSubscribers(subscribers);
        }

        componentRepository.update(component);
        return RequestStatus.SUCCESS;
    }

    public RequestStatus unsubscribeRelease(String id, User user) throws SW360Exception {
        Release release = releaseRepository.get(id);
        assertNotNull(release);

        Set<String> subscribers = release.getSubscribers();
        String email = user.getEmail();
        if (subscribers != null && email != null) {
            subscribers.remove(email);
            release.setSubscribers(subscribers);
        }
        releaseRepository.update(release);
        return RequestStatus.SUCCESS;
    }

    public Component getComponentForEdit(String id, User user) throws SW360Exception {
        List<ModerationRequest> moderationRequestsForDocumentId = moderator.getModerationRequestsForDocumentId(id);

        Component component = getComponent(id, user);
        DocumentState documentState;

        if (moderationRequestsForDocumentId.isEmpty()) {
            documentState = CommonUtils.getOriginalDocumentState();
        } else {
            final String email = user.getEmail();
            Optional<ModerationRequest> moderationRequestOptional = CommonUtils.getFirstModerationRequestOfUser(moderationRequestsForDocumentId, email);
            if (moderationRequestOptional.isPresent()
                    && isInProgressOrPending(moderationRequestOptional.get())){
                ModerationRequest moderationRequest = moderationRequestOptional.get();

                component = moderator.updateComponentFromModerationRequest(
                        component,
                        moderationRequest.getComponentAdditions(),
                        moderationRequest.getComponentDeletions());
                documentState = CommonUtils.getModeratedDocumentState(moderationRequest);
            } else {
                documentState = new DocumentState().setIsOriginalDocument(true).setModerationState(moderationRequestsForDocumentId.get(0).getModerationState());
            }
        }
        component.setPermissions(makePermission(component, user).getPermissionMap());
        component.setDocumentState(documentState);
        return component;
    }

    public Release getReleaseForEdit(String id, User user) throws SW360Exception {
        List<ModerationRequest> moderationRequestsForDocumentId = moderator.getModerationRequestsForDocumentId(id);

        Release release = getRelease(id, user);
        DocumentState documentState;

        if (moderationRequestsForDocumentId.isEmpty()) {
            documentState = CommonUtils.getOriginalDocumentState();
        } else {
            final String email = user.getEmail();
            Optional<ModerationRequest> moderationRequestOptional = CommonUtils.getFirstModerationRequestOfUser(moderationRequestsForDocumentId, email);
            if (moderationRequestOptional.isPresent()
                    && isInProgressOrPending(moderationRequestOptional.get())){
                ModerationRequest moderationRequest = moderationRequestOptional.get();

                release = releaseModerator.updateReleaseFromModerationRequest(
                        release,
                        moderationRequest.getReleaseAdditions(),
                        moderationRequest.getReleaseDeletions());
                documentState = CommonUtils.getModeratedDocumentState(moderationRequest);
            } else {
                documentState = new DocumentState().setIsOriginalDocument(true).setModerationState(moderationRequestsForDocumentId.get(0).getModerationState());
            }
        }
        fillVendor(release);
        release.setPermissions(makePermission(release, user).getPermissionMap());
        release.setDocumentState(documentState);
        return release;
    }

    public List<Component> searchComponentByNameForExport(String name) {
        return componentRepository.searchByNameForExport(name);
    }


    public Set<Component> getUsingComponents(String releaseId) {
        return componentRepository.getUsingComponents(releaseId);
    }

    public Set<Component> getUsingComponents(Set<String> releaseIds) {
        return componentRepository.getUsingComponents(releaseIds);
    }

    public Component getComponentForReportFromFossologyUploadId(String uploadId) {

        Component component = componentRepository.getComponentFromFossologyUploadId(uploadId);

        if (component != null) {
            if (component.isSetReleaseIds()) {
                // Convert Ids to release summary
                final Set<String> releaseIds = component.getReleaseIds();
                final List<Release> releases = CommonUtils.nullToEmptyList(releaseRepository.get(releaseIds));
                for (Release release : releases) {
                    fillVendor(release);
                }
                component.setReleases(releases);
                component.unsetReleaseIds();

                setMainLicenses(component);
            }
        }
        return component;
    }

    private void fillVendor(Release release) {
        if (release.isSetVendorId()) {
            final String vendorId = release.getVendorId();
            if (!Strings.isNullOrEmpty(vendorId)) {
                final Vendor vendor = vendorRepository.get(vendorId);
                if (vendor != null)
                    release.setVendor(vendor);
            }
            release.unsetVendorId();
        }
    }

    public Set<String> getusedAttachmentContentIds() {
        return componentRepository.getUsedAttachmentContents();
    }

    public Map<String, List<String>> getDuplicateComponents() {
        ListMultimap<String, String> componentIdentifierToComponentId = ArrayListMultimap.create();

        for (Component component : componentRepository.getAll()) {
            componentIdentifierToComponentId.put(SW360Utils.printName(component), component.getId());
        }
        return CommonUtils.getIdentifierToListOfDuplicates(componentIdentifierToComponentId);
    }

    public Map<String, List<String>> getDuplicateReleases() {
        ListMultimap<String, String> releaseIdentifierToReleaseId = ArrayListMultimap.create();

        for (Release release : releaseRepository.getAll()) {
            releaseIdentifierToReleaseId.put(SW360Utils.printName(release), release.getId());
        }

        return CommonUtils.getIdentifierToListOfDuplicates(releaseIdentifierToReleaseId);
    }

    public Set<Attachment> getSourceAttachments(String releaseId) throws SW360Exception {
        Release release = assertNotNull(releaseRepository.get(releaseId));
        
        return nullToEmptySet(release.getAttachments())
                .stream()
                .filter(Objects::nonNull)
                .filter(input -> input.getAttachmentType() == AttachmentType.SOURCE)
                .collect(Collectors.toSet());
    }

    public Map<String,List<String>> getDuplicateReleaseSources() {
        ListMultimap<String, String> releaseIdentifierToReleaseId = ArrayListMultimap.create();

        for (Release release : releaseRepository.getAll()) {

            if(release.isSetAttachments()) {
                for (Attachment attachment : release.getAttachments()) {
                    if (attachment.getAttachmentType() == AttachmentType.SOURCE)
                        releaseIdentifierToReleaseId.put(SW360Utils.printName(release), release.getId());
                }
            }
        }

        return CommonUtils.getIdentifierToListOfDuplicates(releaseIdentifierToReleaseId);
    }
}
