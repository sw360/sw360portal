/*
 * Copyright Bosch Software Innovations GmbH, 2016.
 * Copyright Siemens AG, 2016-2017.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.licenseinfo.parsers;

import com.google.common.collect.ImmutableList;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.eclipse.sw360.datahandler.couchdb.AttachmentConnector;
import org.eclipse.sw360.datahandler.thrift.SW360Exception;
import org.eclipse.sw360.datahandler.thrift.attachments.Attachment;
import org.eclipse.sw360.datahandler.thrift.attachments.AttachmentContent;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.LicenseInfo;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.LicenseInfoParsingResult;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.LicenseInfoRequestStatus;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.LicenseNameWithText;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXDocumentFactory;
import org.spdx.rdfparser.license.*;
import org.spdx.rdfparser.model.SpdxDocument;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.eclipse.sw360.datahandler.common.CommonUtils.closeQuietly;
import static org.eclipse.sw360.datahandler.common.CommonUtils.isNullEmptyOrWhitespace;

/**
 * @author: alex.borodin@evosoft.com
 * @author: maximilian.huber@tngtech.com
 */
public class SPDXParser extends LicenseInfoParser {
    protected static final String FILETYPE_SPDX_INTERNAL = "RDF/XML";
    protected static final List<String> ACCEPTABLE_ATTACHMENT_FILE_EXTENSIONS = ImmutableList.of(
            "rdf",
            "spdx" // usually used for tag:value format
    );

    private static final Logger log = Logger.getLogger(CLIParser.class);

    public SPDXParser(AttachmentConnector attachmentConnector, AttachmentContentProvider attachmentContentProvider) {
        super(attachmentConnector, attachmentContentProvider);
    }

    @Override
    public boolean isApplicableTo(Attachment attachment) throws TException {
        boolean isAcceptable = true;
        AttachmentContent attachmentContent = attachmentContentProvider.getAttachmentContent(attachment);
        String lowerFileName = attachmentContent.getFilename().toLowerCase();

        isAcceptable &= ACCEPTABLE_ATTACHMENT_FILE_EXTENSIONS.stream()
                .map(extension -> lowerFileName.endsWith(extension))
                .reduce(false, (b1, b2) -> b1 || b2);

        // TODO: test for namespace `spdx` in rdf file (maybe to much overhead? Better try parsing and die?)

        return isAcceptable;
    }

    @Override
    public List<LicenseInfoParsingResult> getLicenseInfos(Attachment attachment) throws TException {
        AttachmentContent attachmentContent = attachmentContentProvider.getAttachmentContent(attachment);
        LicenseInfo emptyResult = new LicenseInfo()
                .setFilenames(Arrays.asList(attachmentContent.getFilename()));

        Optional<LicenseInfo> licenseInfo = parseAsSpdx(attachmentContent)
                .flatMap(d -> addSpdxContentToCLI(emptyResult, d));

        if(licenseInfo.isPresent()){
            return Collections.singletonList(new LicenseInfoParsingResult()
                    .setLicenseInfo(licenseInfo.get())
                    .setStatus(LicenseInfoRequestStatus.SUCCESS));
        }else{
            return Collections.singletonList(new LicenseInfoParsingResult()
                    .setStatus(LicenseInfoRequestStatus.FAILURE));
        }
    }

    protected String getUriOfAttachment(AttachmentContent attachmentContent) throws URISyntaxException {
        String filename = attachmentContent.getFilename();
        String filePath = "///" + new File(filename).getAbsoluteFile().toString().replace('\\', '/');
        return new URI("file", filePath, null).toString();
    }

    protected Stream<String> getAllLicenseTextsFromInfo(AnyLicenseInfo spdxLicenseInfo) {
        if (spdxLicenseInfo instanceof LicenseSet) {

            LicenseSet LicenseSet = (LicenseSet) spdxLicenseInfo;
            return Arrays.stream(LicenseSet.getMembers())
                    .flatMap(this::getAllLicenseTextsFromInfo);

        } else if (spdxLicenseInfo instanceof ExtractedLicenseInfo) {

            ExtractedLicenseInfo extractedLicenseInfo = (ExtractedLicenseInfo) spdxLicenseInfo;
            return Collections.singleton(extractedLicenseInfo.getExtractedText())
                    .stream();

        } else if (spdxLicenseInfo instanceof License) {

            License license = (License) spdxLicenseInfo;
            return Collections.singleton(license.getLicenseText())
                    .stream();

        } else if (spdxLicenseInfo instanceof OrLaterOperator) {

            OrLaterOperator orLaterOperator = (OrLaterOperator) spdxLicenseInfo;
            return getAllLicenseTextsFromInfo(orLaterOperator.getLicense());

        } else if (spdxLicenseInfo instanceof WithExceptionOperator) {

            WithExceptionOperator withExceptionOperator = (WithExceptionOperator) spdxLicenseInfo;
            String licenseExceptionText = withExceptionOperator.getException()
                    .getLicenseExceptionText();
            return getAllLicenseTextsFromInfo(withExceptionOperator.getLicense())
                    .map(licenseText -> licenseText + "\n\n" + licenseExceptionText);

        }

        return Stream.empty();
    }

    protected Set<String> getAllLicenseTexts(SpdxDocument spdxDocument) throws InvalidSPDXAnalysisException {
        Stream<String> licenseTexts = Arrays.stream(spdxDocument.getDocumentDescribes())
                .flatMap(spdxItem -> Stream.concat(
                        getAllLicenseTextsFromInfo(spdxItem.getLicenseConcluded()),
                        Arrays.stream(spdxItem.getLicenseInfoFromFiles())
                                .flatMap(this::getAllLicenseTextsFromInfo)));
        Stream<String> extractedLicenseTexts = Arrays.stream(spdxDocument.getExtractedLicenseInfos())
                        .flatMap(this::getAllLicenseTextsFromInfo);
        return Stream.concat(licenseTexts, extractedLicenseTexts)
                .collect(Collectors.toSet());
    }

    protected Optional<LicenseInfo> addSpdxContentToCLI(LicenseInfo result, SpdxDocument doc) {
        if(! result.isSetLicenseNamesWithTexts()){
            result.setLicenseNamesWithTexts(new HashSet<>());
        }
        try {
            Arrays.stream(doc.getExtractedLicenseInfos()).forEach(
                    extractedLicenseInfo ->
                            result.getLicenseNamesWithTexts()
                                    .add(new LicenseNameWithText()
                                            .setLicenseText(extractedLicenseInfo.getExtractedText())
                                            .setLicenseName(extractLicenseName(extractedLicenseInfo))
                                    )
            );
            Arrays.stream(doc.getDocumentDescribes()).forEach(
                    spdxItem -> result.addToCopyrights(spdxItem.getCopyrightText())
            );
        } catch (InvalidSPDXAnalysisException e) {
            e.printStackTrace();
        }

        return Optional.of(result);
    }

    private String extractLicenseName(ExtractedLicenseInfo extractedLicenseInfo){
        return ! isNullEmptyOrWhitespace(extractedLicenseInfo.getName()) ? extractedLicenseInfo.getName() : extractedLicenseInfo.getLicenseId();
    }

    protected Optional<SpdxDocument> parseAsSpdx(AttachmentContent attachmentContent){
        InputStream attachmentStream = null;
        try {
            attachmentStream = attachmentConnector.getAttachmentStream(attachmentContent);
            SpdxDocument doc = SPDXDocumentFactory.createSpdxDocument(attachmentStream,
                    getUriOfAttachment(attachmentContent),
                    FILETYPE_SPDX_INTERNAL);
            return Optional.of(doc);
        } catch (SW360Exception e) {
            log.error("Unable to get attachment stream for attachment=" + attachmentContent.getFilename() + " with id=" + attachmentContent.getId(), e);
        } catch (InvalidSPDXAnalysisException e) {
            log.error("Unable to parse SPDX for attachment=" + attachmentContent.getFilename() + " with id=" + attachmentContent.getId(), e);
        } catch (URISyntaxException e) {
            log.error("Invalid URI syntax for attachment=" + attachmentContent.getFilename() + " with id=" + attachmentContent.getId(), e);
        } finally {
            closeQuietly(attachmentStream, log);
        }
        return Optional.empty();
    }
}
