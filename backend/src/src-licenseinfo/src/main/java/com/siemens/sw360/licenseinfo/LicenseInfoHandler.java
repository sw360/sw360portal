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
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentContent;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.licenseinfo.*;
import com.siemens.sw360.datahandler.thrift.projects.Project;
import com.siemens.sw360.datahandler.thrift.projects.ProjectLink;
import com.siemens.sw360.datahandler.thrift.projects.ProjectRelationship;
import com.siemens.sw360.datahandler.thrift.users.User;
import com.siemens.sw360.licenseinfo.outputGenerators.LicenseInfoGenerator;
import com.siemens.sw360.licenseinfo.outputGenerators.OutputGenerator;
import com.siemens.sw360.licenseinfo.outputGenerators.XhtmlGenerator;
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
import java.util.function.Function;
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

    public static final String LICENSE_INFO_RESULTS_CONTEXT_PROPERTY = "licenseInfoResults";
    public static final String LICENSES_CONTEXT_PROPERTY = "licenses";
    public static final String LICENSE_INFO_TEMPLATE_FILE = "licenseInfoFile.vm";

    private final LicenseInfoParser[] parsers;

    private final OutputGenerator[] outputGenerators;

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

        outputGenerators = new OutputGenerator[]{
                new LicenseInfoGenerator(),
                new XhtmlGenerator(),
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
                return assignReleaseToLicenseInfoParsingResult(result, release);
            } else {
                // not a single parser has found applicable attachments
                return assignReleaseToLicenseInfoParsingResult(noSourceParsingResult(), release);
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
    public String getLicenseInfoFileForProject(String projectId, User user, String outputGeneratorClassName) throws TException {
        assertId(projectId);
        Project project = projectDatabaseHandler.getProjectById(projectId, user);
        assertNotNull(project);

        Collection<LicenseInfoParsingResult> projectLicenseInfoResults = getAllReleaseLicenseInfos(projectId, user);
        for (OutputGenerator generator : outputGenerators) {
            if (outputGeneratorClassName.equals(generator.getClass().getName())) {
                return generator.generateOutputFile(projectLicenseInfoResults);
            }
        }
        throw new TException("Unknown output format: " + outputGeneratorClassName);
    }

    @Override
    public List<OutputFormatInfo> getPossibleOutputFormats() {
        List<OutputFormatInfo> outputPossibilities = new ArrayList<>();
        for (OutputGenerator generator : outputGenerators) {
            outputPossibilities.add(
                   new OutputFormatInfo()
                    .setFileExtension(generator.getOutputType())
                    .setDescription(generator.getOutputDescription())
                    .setGeneratorClassName(generator.getClass().getName())
            );
        }
        return outputPossibilities;
    }

    @Override
    public String getFileExtensionFromGeneratorClass(String generatorClassName) throws TException{
        for (OutputGenerator generator : outputGenerators) {
            if (generatorClassName.equals(generator.getClass().getName())) {
                return generator.getOutputType();
            }
        }
        throw new TException("Unknown output format: " + generatorClassName);
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

        if (!Objects.equals(lir1.getName(), lir2.getName()) || !Objects.equals(lir1.getVendor(), lir2.getVendor()) || !Objects.equals(lir1.getVersion(), lir2.getVersion())){
            throw new IllegalArgumentException("Method is not intended to merge across releases. Release data must be equal");
        }
        //use copy constructor to copy filetype
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

        //use copy constructor to copy vendor, name, and version
        return new LicenseInfoParsingResult(lir1)
                .setStatus(LicenseInfoRequestStatus.SUCCESS)
                .setLicenseInfo(mergedLi)
                .setMessage((nullToEmptyString(lir1.getMessage()) + "\n" + nullToEmptyString(lir2.getMessage())).trim());
    }


    private LicenseInfoParsingResult noSourceParsingResult() {
        return new LicenseInfoParsingResult().setStatus(LicenseInfoRequestStatus.NO_APPLICABLE_SOURCE);
    }

    private LicenseInfoParsingResult assignReleaseToLicenseInfoParsingResult(LicenseInfoParsingResult result, Release release) {
        result.setVendor(release.isSetVendor() ? release.getVendor().getShortname() : "");
        result.setName(release.getName());
        result.setVersion(release.getVersion());
        return result;
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
