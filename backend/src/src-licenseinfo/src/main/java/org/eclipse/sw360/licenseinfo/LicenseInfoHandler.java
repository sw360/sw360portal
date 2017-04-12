/*
 * Copyright Siemens AG, 2016-2017. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.licenseinfo;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.eclipse.sw360.attachments.db.AttachmentDatabaseHandler;
import org.eclipse.sw360.datahandler.common.DatabaseSettings;
import org.eclipse.sw360.datahandler.db.ComponentDatabaseHandler;
import org.eclipse.sw360.datahandler.db.ProjectDatabaseHandler;
import org.eclipse.sw360.datahandler.thrift.SW360Exception;
import org.eclipse.sw360.datahandler.thrift.attachments.Attachment;
import org.eclipse.sw360.datahandler.thrift.components.Release;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.*;
import org.eclipse.sw360.datahandler.thrift.projects.Project;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.licenseinfo.outputGenerators.DocxGenerator;
import org.eclipse.sw360.licenseinfo.outputGenerators.LicenseInfoGenerator;
import org.eclipse.sw360.licenseinfo.outputGenerators.OutputGenerator;
import org.eclipse.sw360.licenseinfo.outputGenerators.XhtmlGenerator;
import org.eclipse.sw360.licenseinfo.parsers.*;

import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

import static org.eclipse.sw360.datahandler.common.CommonUtils.*;
import static org.eclipse.sw360.datahandler.common.SW360Assert.assertId;
import static org.eclipse.sw360.datahandler.common.SW360Assert.assertNotNull;

/**
 * Implementation of the Thrift service
 *
 * @author alex.borodin@evosoft.com
 */
public class LicenseInfoHandler implements LicenseInfoService.Iface {

    public static final String LICENSE_INFO_RESULTS_CONTEXT_PROPERTY = "licenseInfoResults";
    public static final String LICENSES_CONTEXT_PROPERTY = "licenses";
    public static final String ALL_LICENSE_NAMES_WITH_TEXTS = "allLicenseNamesWithTexts";
    public static final String ACKNOWLEDGEMENTS_CONTEXT_PROPERTY = "acknowledgements";

    private final LicenseInfoParser[] parsers;

    private final OutputGenerator[] outputGenerators;

    private static final Logger log = Logger.getLogger(LicenseInfoHandler.class);

    private final ProjectDatabaseHandler projectDatabaseHandler;

    private final ComponentDatabaseHandler componentDatabaseHandler;

    public LicenseInfoHandler() throws MalformedURLException {
        this(new AttachmentDatabaseHandler(DatabaseSettings.getConfiguredHttpClient(), DatabaseSettings.COUCH_DB_ATTACHMENTS),
                new ProjectDatabaseHandler(DatabaseSettings.getConfiguredHttpClient(), DatabaseSettings.COUCH_DB_DATABASE, DatabaseSettings.COUCH_DB_ATTACHMENTS),
                new ComponentDatabaseHandler(DatabaseSettings.getConfiguredHttpClient(), DatabaseSettings.COUCH_DB_DATABASE, DatabaseSettings.COUCH_DB_ATTACHMENTS));
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
                new CombinedCLIParser(attachmentDatabaseHandler.getAttachmentConnector(), contentProvider, componentDatabaseHandler),
        };

        outputGenerators = new OutputGenerator[]{
                new LicenseInfoGenerator(),
                new XhtmlGenerator(),
                new DocxGenerator()
        };
    }

    private List<LicenseInfoParsingResult> getLicenseInfosForRelease(Release release, String selectedAttachmentContentId) throws TException{
        if(release == null){
            return Collections.singletonList(noSourceParsingResult());
        }
        Optional<Attachment> selectedAttachmentOpt = release
                .getAttachments()
                .stream()
                .filter(a -> a.getAttachmentContentId().equals(selectedAttachmentContentId))
                .findFirst();
        if (!selectedAttachmentOpt.isPresent()){
            String message = String.format("Attachment selected for license info generation is not found in release's attachments. Release id: %s. Attachment content id: %s", release
                    .getId(), selectedAttachmentContentId);
            throw new IllegalStateException(message);
        }
        Attachment attachment = selectedAttachmentOpt.get();

        try {
            List<LicenseInfoParser> applicableParsers = Arrays.stream(parsers).filter(p -> {
                try {
                    return p.isApplicableTo(attachment);
                } catch (TException e) {
                    throw new UncheckedTException(e);
                }
            }).collect(Collectors.toList());

            if (applicableParsers.size() == 0){
                // no applicable parser has been found for this attachment. weird
                log.warn("No applicable parser has been found for the attachment selected for license information");
                return assignReleaseToLicenseInfoParsingResult(noSourceParsingResult(), release);
            } else {
                if (applicableParsers.size() > 1){
                    log.info("More than one parser claims to be able to parse attachment with contend id "+selectedAttachmentContentId);
                }
                List<LicenseInfoParsingResult> results = applicableParsers.stream().map(parser -> {
                    try {
                        return parser.getLicenseInfos(attachment);
                    } catch (TException e) {
                        throw new UncheckedTException(e);
                    }
                }).flatMap(Collection::stream)
                        .collect(Collectors.toList());
                return assignReleaseToLicenseInfoParsingResults(results, release);
            }
        } catch (UncheckedTException e) {
            throw e.getTExceptionCause();
        }
    }

    private List<LicenseInfoParsingResult> assignReleaseToLicenseInfoParsingResult(LicenseInfoParsingResult licenseInfoParsingResult, Release release) {
        return assignReleaseToLicenseInfoParsingResults(Collections.singletonList(licenseInfoParsingResult), release);
    }

    @Override
    public String getLicenseInfoFileForProject(String projectId, User user, String outputGeneratorClassName, Map<String, Set<String>> releaseIdsToSelectedAttachmentIds) throws TException {
        assertId(projectId);
        Project project = projectDatabaseHandler.getProjectById(projectId, user);
        assertNotNull(project);

        Map<Release, Set<String>> releaseToAttachmentId = mapKeysToReleases(releaseIdsToSelectedAttachmentIds, user);

        Collection<LicenseInfoParsingResult> projectLicenseInfoResults = getAllReleaseLicenseInfos(releaseToAttachmentId, user);
        for (OutputGenerator generator : outputGenerators) {
            if (outputGeneratorClassName.equals(generator.getClass().getName())) {
                return (String) generator.generateOutputFile(projectLicenseInfoResults, project.getName());
            }
        }
        throw new TException("Unknown output generator for String output: " + outputGeneratorClassName);
    }

    private Map<Release, Set<String>> mapKeysToReleases(Map<String, Set<String>> releaseIdsToAttachmentIds, User user) throws TException{
        Map<Release, Set<String>> result = Maps.newHashMap();
        try {
            releaseIdsToAttachmentIds.forEach((relId, attIds) -> {
                try {
                    result.put(componentDatabaseHandler.getRelease(relId, user), attIds);
                } catch (SW360Exception e) {
                    throw new UncheckedTException(e);
                }
            });
        } catch (UncheckedTException ute){
            throw ute.getTExceptionCause();
        }
        return result;
    }

    @Override
    public ByteBuffer getLicenseInfoFileForProjectAsBinary(String projectId, User user, String outputGeneratorClassName, Map<String, Set<String>> releaseIdsToSelectedAttachmentIds) throws TException {
        assertId(projectId);
        Project project = projectDatabaseHandler.getProjectById(projectId, user);
        assertNotNull(project);

        Map<Release, Set<String>> releaseToAttachmentIds = mapKeysToReleases(releaseIdsToSelectedAttachmentIds, user);
        Collection<LicenseInfoParsingResult> projectLicenseInfoResults = getAllReleaseLicenseInfos(releaseToAttachmentIds, user);
        for (OutputGenerator generator : outputGenerators) {
            if (outputGeneratorClassName.equals(generator.getClass().getName())) {
                return ByteBuffer.wrap((byte[]) generator.generateOutputFile(projectLicenseInfoResults, project.getName()));
            }
        }
        throw new TException("Unknown output generator for binary output: " + outputGeneratorClassName);
    }

    @Override
    public List<OutputFormatInfo> getPossibleOutputFormats() {
        List<OutputFormatInfo> outputPossibilities = new ArrayList<>();
        for (OutputGenerator generator : outputGenerators) {
            outputPossibilities.add(
                    generator.getOutputFormatInfo()
            );
        }
        return outputPossibilities;
    }

    @Override
    public OutputFormatInfo getOutputFormatInfoForGeneratorClass(String generatorClassName) throws TException{
        for (OutputGenerator generator : outputGenerators) {
            if (generatorClassName.equals(generator.getClass().getName())) {
                return generator.getOutputFormatInfo();
            }
        }
        throw new TException("Unknown output format: " + generatorClassName);
    }

    private Collection<LicenseInfoParsingResult> getAllReleaseLicenseInfos(Map<Release, Set<String>> releaseToSelectedAttachmentIds, User user) throws TException {
        try {
            return releaseToSelectedAttachmentIds.entrySet().stream()
                    .map((entry) -> entry.getValue().stream()
                            .filter(Objects::nonNull)
                            .map(attId -> {
                                try {
                                    return getLicenseInfosForRelease(entry.getKey(), attId);
                                } catch (TException e) {
                                    throw new UncheckedTException(e);
                                }
                            }).flatMap(Collection::stream).collect(Collectors.toList())
                    ).flatMap(Collection::stream).collect(Collectors.toList());
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

        if (!Objects.equals(lir1.getName(), lir2.getName()) || !Objects.equals(lir1.getVendor(), lir2.getVendor()) || !Objects.equals(lir1.getVersion(), lir2.getVersion())){
            throw new IllegalArgumentException("Method is not intended to merge across releases. Release data must be equal");
        }
        LicenseInfo mergedLi = new LicenseInfo();
        //merging filenames
        Set<String> filenames = new HashSet<>();
        filenames.addAll(nullToEmptyList(lir1.getLicenseInfo().getFilenames()));
        filenames.addAll(nullToEmptyList(lir2.getLicenseInfo().getFilenames()));
        mergedLi.setFilenames(new ArrayList<>(filenames));
        //merging copyrights
        mergedLi.setCopyrights(Sets.union(nullToEmptySet(lir1.getLicenseInfo().getCopyrights()), nullToEmptySet(lir2.getLicenseInfo().getCopyrights())));
        //merging licenses
        mergedLi.setLicenseNamesWithTexts(Sets.union(nullToEmptySet(lir1.getLicenseInfo().getLicenseNamesWithTexts()), nullToEmptySet(lir2.getLicenseInfo().getLicenseNamesWithTexts())));

        //use copy constructor to copy vendor, name, and version
        return new LicenseInfoParsingResult(lir1)
                .setStatus(LicenseInfoRequestStatus.SUCCESS)
                .setLicenseInfo(mergedLi)
                .setMessage((nullToEmptyString(lir1.getMessage()) + "\n" + nullToEmptyString(lir2.getMessage())).trim());
    }


    private LicenseInfoParsingResult noSourceParsingResult() {
        return new LicenseInfoParsingResult().setStatus(LicenseInfoRequestStatus.NO_APPLICABLE_SOURCE);
    }

    private List<LicenseInfoParsingResult> assignReleaseToLicenseInfoParsingResults(List<LicenseInfoParsingResult> parsingResults, Release release) {
        parsingResults.forEach(r -> {
            //override by given release only if the fields were not set by parser, because parser knows best
            if (!r.isSetVendor() && !r.isSetName() && !r.isSetVersion()) {
                r.setVendor(release.isSetVendor() ? release.getVendor().getShortname() : "");
                r.setName(release.getName());
                r.setVersion(release.getVersion());
            }
        });
        return parsingResults;
    }

    private static class UncheckedTException extends RuntimeException {
        UncheckedTException(TException te) {
            super(te);
        }

        TException getTExceptionCause() {
            return (TException) getCause();
        }
    }
}
