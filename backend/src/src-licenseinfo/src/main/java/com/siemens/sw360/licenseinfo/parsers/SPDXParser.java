/*
 * Copyright Bosch Software Innovations GmbH, 2016.
 * Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.licenseinfo.parsers;

import com.google.common.collect.ImmutableList;
import com.siemens.sw360.datahandler.couchdb.AttachmentConnector;
import com.siemens.sw360.datahandler.thrift.SW360Exception;
import com.siemens.sw360.datahandler.thrift.attachments.Attachment;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentContent;
import com.siemens.sw360.datahandler.thrift.licenseinfo.LicenseInfo;
import com.siemens.sw360.datahandler.thrift.licenseinfo.LicenseInfoParsingResult;
import com.siemens.sw360.datahandler.thrift.licenseinfo.LicenseInfoRequestStatus;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SPDXDocumentFactory;
import org.spdx.rdfparser.model.SpdxDocument;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author: alex.borodin@evosoft.com
 * @author: maximilian.huber@tngtech.com
 */
public class SPDXParser extends LicenseInfoParser {
    public static final String FILETYPE_SPDX_EXTERNAL = "SPDX";
    protected static final String FILETYPE_SPDX_INTERNAL = "RDF/XML";
    protected static final List<String> ACCEPTABLE_ATTACHMENT_FILE_EXTENSIONS = ImmutableList.of(
            "rdf",
            "spdx"
    );
    protected static final List<String> ACCEPTABLE_ATTACHMENT_CONTENT_TYPES = ImmutableList.of(
            "application/rdf+xml",
            "application/xml",
            "text/plain"
    );

    private static final Logger log = Logger.getLogger(CLIParser.class);

    public SPDXParser(AttachmentConnector attachmentConnector, AttachmentContentProvider attachmentContentProvider) {
        super(attachmentConnector, attachmentContentProvider);
    }

    @Override
    public boolean isApplicableTo(Attachment attachment) throws TException {
        AttachmentContent attachmentContent = attachmentContentProvider.getAttachmentContent(attachment);
        String lowerFileName = attachmentContent.getFilename().toLowerCase();

        boolean isFiletypeAcceptable = ACCEPTABLE_ATTACHMENT_FILE_EXTENSIONS.stream()
                .map(extension -> lowerFileName.endsWith(extension))
                .reduce(false, (b1, b2) -> b1 || b2);
        boolean isContentTypeAcceptable = ACCEPTABLE_ATTACHMENT_CONTENT_TYPES.contains(attachmentContent.getContentType());

        // TODO: test for namespace `spdx` in rdf file (maybe to much overhead? Better try parsing and die?)

        return isFiletypeAcceptable && isContentTypeAcceptable;
    }

    @Override
    public LicenseInfoParsingResult getLicenseInfo(Attachment attachment) throws TException {
        AttachmentContent attachmentContent = attachmentContentProvider.getAttachmentContent(attachment);
        LicenseInfo emptyResult = new LicenseInfo()
                .setFilenames(Arrays.asList(attachmentContent.getFilename()));

        Optional<LicenseInfo> licenseInfo = parseAsSpdx(attachmentContent)
                .flatMap(d -> addSpdxContentToCLI(emptyResult, d));

        if(licenseInfo.isPresent()){
            return new LicenseInfoParsingResult()
                    .setLicenseInfo(licenseInfo.get())
                    .setStatus(LicenseInfoRequestStatus.SUCCESS);
        }else{
            return new LicenseInfoParsingResult()
                    .setStatus(LicenseInfoRequestStatus.FAILURE);
        }
    }

    protected String getUriOfAttachment(AttachmentContent attachmentContent) throws URISyntaxException {
        String filename = attachmentContent.getFilename();
        String filePath = "///" + new File(filename).getAbsoluteFile().toString().replace('\\', '/');
        return new URI("file", filePath, null).toString();
    }

    protected Optional<LicenseInfo> addSpdxContentToCLI(LicenseInfo result, SpdxDocument doc) {
        try {
            Arrays.stream(doc.getExtractedLicenseInfos()).forEach(
                    extractedLicenseInfo -> result.addToLicenseTexts(extractedLicenseInfo.getExtractedText())
            );
            Arrays.stream(doc.getDocumentDescribes()).forEach(
                    spdxItem -> result.addToCopyrights(spdxItem.getCopyrightText())
            );
        } catch (InvalidSPDXAnalysisException e) {
            e.printStackTrace();
        }

        return Optional.of(result.setFiletype(FILETYPE_SPDX_EXTERNAL));
    }

    protected Optional<SpdxDocument> parseAsSpdx(AttachmentContent attachmentContent){
        try {
            InputStream attachmentStream = attachmentConnector.getAttachmentStream(attachmentContent);
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
        }
        return Optional.empty();
    }
}
