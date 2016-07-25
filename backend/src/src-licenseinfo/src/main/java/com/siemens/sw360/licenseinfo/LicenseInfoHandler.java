/*
 * Copyright Siemens AG, 2016. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.licenseinfo;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.siemens.sw360.attachments.db.AttachmentDatabaseHandler;
import com.siemens.sw360.datahandler.common.DatabaseSettings;
import com.siemens.sw360.datahandler.db.ComponentDatabaseHandler;
import com.siemens.sw360.datahandler.db.ProjectDatabaseHandler;
import com.siemens.sw360.datahandler.thrift.SW360Exception;
import com.siemens.sw360.datahandler.thrift.attachments.Attachment;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.licenseinfo.LicenseInfo;
import com.siemens.sw360.datahandler.thrift.licenseinfo.LicenseInfoParsingResult;
import com.siemens.sw360.datahandler.thrift.licenseinfo.LicenseInfoRequestStatus;
import com.siemens.sw360.datahandler.thrift.licenseinfo.LicenseInfoService;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.projects.ProjectLink;
import com.siemens.sw360.datahandler.thrift.projects.ProjectRelationship;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.licenseinfo.parsers.AttachmentContentProvider;
import com.siemens.sw360.licenseinfo.parsers.CLIParser;
import com.siemens.sw360.licenseinfo.parsers.LicenseInfoParser;
import com.siemens.sw360.licenseinfo.parsers.SPDXParser;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.*;
import java.util.stream.Collectors;

import static com.siemens.sw360.datahandler.common.CommonUtils.*;
import static com.siemens.sw360.datahandler.common.SW360Assert.assertId;
import static com.siemens.sw360.datahandler.common.SW360Assert.assertNotNull;
import static com.siemens.sw360.datahandler.common.SW360Utils.flattenProjectLinkTree;

/**
 * Implementation of the Thrift service
 *
 * @author alex.borodin@evosoft.com
 */
public class LicenseInfoHandler implements LicenseInfoService.Iface {

    public static final String LICENSE_INFOS_CONTEXT_PROPERTY = "licenseInfos";
    public static final String LICENSES_CONTEXT_PROPERTY = "licenses";
    public static final String LICENSE_INFO_TEMPLATE_FILE = "licenseInfoFile.vm";

    private final LicenseInfoParser[] parsers;

    private static final Logger log = Logger.getLogger(LicenseInfoHandler.class);

    private final ProjectDatabaseHandler projectDatabaseHandler;

    private final ComponentDatabaseHandler componentDatabaseHandler;

    public LicenseInfoHandler() throws MalformedURLException {
        this(new AttachmentDatabaseHandler(DatabaseSettings.COUCH_DB_URL, DatabaseSettings.COUCH_DB_DATABASE, DatabaseSettings.COUCH_DB_ATTACHMENTS),
                new ProjectDatabaseHandler(DatabaseSettings.COUCH_DB_URL, DatabaseSettings.COUCH_DB_DATABASE, DatabaseSettings.COUCH_DB_ATTACHMENTS),
                new ComponentDatabaseHandler(DatabaseSettings.COUCH_DB_URL, DatabaseSettings.COUCH_DB_DATABASE, DatabaseSettings.COUCH_DB_ATTACHMENTS));
    }

    @VisibleForTesting
    public LicenseInfoHandler(AttachmentDatabaseHandler attachmentDatabaseHandler,
                              ProjectDatabaseHandler projectDatabaseHandler,
                              ComponentDatabaseHandler componentDatabaseHandler) throws MalformedURLException {
        this.projectDatabaseHandler = projectDatabaseHandler;
        this.componentDatabaseHandler = componentDatabaseHandler;

        AttachmentContentProvider contentProvider = attachment -> attachmentDatabaseHandler.getAttachmentContent(attachment.getAttachmentContentId());

        parsers = new LicenseInfoParser[]{
                new SPDXParser(attachmentDatabaseHandler.getAttachmentConnector(), contentProvider),
                new CLIParser(attachmentDatabaseHandler.getAttachmentConnector(), contentProvider),
        };
    }

    @Override
    public LicenseInfoParsingResult getLicenseInfoForAttachment(Attachment attachment) throws TException {
        assertNotNull(attachment);
        for (LicenseInfoParser parser : parsers) {
            if (parser.isApplicableTo(attachment)) {
                return parser.getLicenseInfo(attachment);
            }
        }
        return noSourceParsingResult();
    }

    @Override
    public LicenseInfoParsingResult getLicenseInfoForRelease(Release release) throws TException{
        if(release == null){
            return noSourceParsingResult();
        }

        try {
            Optional<LicenseInfoParser> parserOptional = Arrays.asList(parsers).stream().filter(p -> {
                try {
                    return !getApplicableAttachments(release, p).isEmpty();
                } catch (TException e) {
                    throw new UncheckedTException(e);
                }
            }).findFirst();

            if (parserOptional.isPresent()) {
                LicenseInfoParsingResult result = getApplicableAttachments(release, parserOptional.get()).stream().map(attachment -> {
                    try {
                        return parserOptional.get().getLicenseInfo(attachment);
                    } catch (TException e) {
                        throw new UncheckedTException(e);
                    }
                }).reduce(this::mergeLicenseInfos)
                        //this could only be returned if there is a parser which found applicable sources,
                        // but there are no attachments in the release. That would be so inexplicable as to warrant
                        // throwing an exception, actually.
                        .orElse(noSourceParsingResult());
                LicenseInfo resultLI = result.getLicenseInfo();
                if (null != resultLI) {
                    resultLI.setVendor(release.isSetVendor() ? release.getVendor().getShortname() : "");
                    resultLI.setName(release.getName());
                    resultLI.setVersion(release.getVersion());
                }
                return result;
            } else {
                // not a single parser has found applicable attachments
                return noSourceParsingResult();
            }
        } catch (UncheckedTException e) {
            throw e.getTExceptionCause();
        }
    }

    private List<Attachment> getApplicableAttachments(Release release, LicenseInfoParser parser) throws TException {
        try {
            return nullToEmptySet(release.getAttachments()).stream()
                    .filter((attachmentContent) -> {
                        try {
                            return parser.isApplicableTo(attachmentContent);
                        } catch (TException e) {
                            throw new UncheckedTException(e);
                        }
                    })
                    .collect(Collectors.toList());
        }catch (UncheckedTException e){
            throw e.getTExceptionCause();
        }
    }

    @Override
    public String getLicenseInfoFileForProject(String projectId, User user) throws TException {
        assertId(projectId);
        Project project = projectDatabaseHandler.getProjectById(projectId, user);
        assertNotNull(project);

        Collection<LicenseInfoParsingResult> projectLicenseInfoResults = getAllReleaseLicenseInfos(projectId, user);

        return generateLicenseInfoFile(projectLicenseInfoResults);
    }


    public Collection<LicenseInfoParsingResult> getAllReleaseLicenseInfos(String projectId, User user) throws TException {
        Map<String, ProjectRelationship> fakeRelations = Maps.newHashMap();
        fakeRelations.put(projectId, ProjectRelationship.UNKNOWN);
        List<ProjectLink> linkedProjects = projectDatabaseHandler.getLinkedProjects(fakeRelations);
        Collection<ProjectLink> flatProjectLinkList = flattenProjectLinkTree(linkedProjects);
        try {
            return flatProjectLinkList.stream().flatMap(pl -> nullToEmptyCollection(pl.getLinkedReleases()).stream()).map(rl -> {
                try {
                    return componentDatabaseHandler.getRelease(rl.getId(), user);
                } catch (SW360Exception e) {
                    log.error("Cannot read release with id: " + rl.getId(), e);
                    return null;
                }
            }).filter(Objects::nonNull)
                    // public LicenseInfoParsingResult getLicenseInfoForRelease(Release release) throws TException {
                    .map((release) -> {
                        try {
                            return getLicenseInfoForRelease(release);
                        } catch (TException e) {
                            throw new UncheckedTException(e);
                        }
                    }).collect(Collectors.toList());
        }catch(UncheckedTException e){
            throw e.getTExceptionCause();
        }
    }

    private LicenseInfoParsingResult mergeLicenseInfos(LicenseInfoParsingResult lir1, LicenseInfoParsingResult lir2){
        if (lir1.getStatus() != LicenseInfoRequestStatus.SUCCESS){
            return lir2;
        }
        if (lir2.getStatus() != LicenseInfoRequestStatus.SUCCESS){
            return lir1;
        }
        if (!lir1.isSetLicenseInfo() || !lir2.isSetLicenseInfo() || !lir1.getLicenseInfo().getFiletype().equals(lir2.getLicenseInfo().getFiletype())) {
            throw new IllegalArgumentException("LicenseInfo filetypes must be equal");
        }
        //use copy constructor to copy filetype, vendor, name, and version
        LicenseInfo mergedLi = new LicenseInfo(lir1.getLicenseInfo());
        //merging filenames
        Set<String> filenames = new HashSet<>();
        filenames.addAll(nullToEmptyList(lir1.getLicenseInfo().getFilenames()));
        filenames.addAll(nullToEmptyList(lir2.getLicenseInfo().getFilenames()));
        mergedLi.setFilenames(filenames.stream().collect(Collectors.toList()));
        //merging copyrights
        mergedLi.setCopyrights(Sets.union(nullToEmptySet(lir1.getLicenseInfo().getCopyrights()), nullToEmptySet(lir2.getLicenseInfo().getCopyrights())));
        //merging licenses
        mergedLi.setLicenseTexts(Sets.union(nullToEmptySet(lir1.getLicenseInfo().getLicenseTexts()), nullToEmptySet(lir2.getLicenseInfo().getLicenseTexts())));

        return new LicenseInfoParsingResult()
                .setStatus(LicenseInfoRequestStatus.SUCCESS)
                .setLicenseInfo(mergedLi)
                .setMessage(nullToEmptyString(lir1.getMessage()) + nullToEmptyString(lir1.getMessage()));
    }

    private String generateLicenseInfoFile(Collection<LicenseInfoParsingResult> projectLicenseInfoResults) throws SW360Exception {
        try {
            Properties p = new Properties();
            p.setProperty("resource.loader", "class");
            p.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            Velocity.init(p);
            VelocityContext vc = new VelocityContext();

            Map<String, LicenseInfo> licenseInfos = projectLicenseInfoResults.stream()
                    .map(LicenseInfoParsingResult::getLicenseInfo)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(this::getComponentLongName, li -> li, (li1, li2) -> li1));
            Set<String> licenses = projectLicenseInfoResults.stream()
                    .map(LicenseInfoParsingResult::getLicenseInfo)
                    .filter(Objects::nonNull)
                    .map(LicenseInfo::getLicenseTexts)
                    .filter(Objects::nonNull)
                    .reduce(Sets::union)
                    .orElse(Collections.emptySet());

            vc.put(LICENSE_INFOS_CONTEXT_PROPERTY, licenseInfos);
            vc.put(LICENSES_CONTEXT_PROPERTY, licenses);

            StringWriter sw = new StringWriter();
            Velocity.mergeTemplate(LICENSE_INFO_TEMPLATE_FILE, "utf-8", vc, sw);
            sw.close();
            return sw.toString();
        } catch (Exception e) {
            log.error("Could not generate licenseinfo file", e);
            return "License information could not be generated.\nAn exception occured: " + e.toString();
        }
    }

    private String getComponentLongName(LicenseInfo li) {
        return String.format("%s %s %s", li.getVendor(), li.getName(), li.getVersion()).trim();
    }

    private LicenseInfoParsingResult noSourceParsingResult() {
        return new LicenseInfoParsingResult().setStatus(LicenseInfoRequestStatus.NO_APPLICABLE_SOURCE);
    }

    private static class UncheckedTException extends RuntimeException {
        public UncheckedTException(TException te) {
            super(te);
        }

        TException getTExceptionCause() {
            return (TException) getCause();
        }
    }
}
