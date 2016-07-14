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
import com.google.common.collect.Sets;
import com.siemens.sw360.attachments.db.AttachmentDatabaseHandler;
import com.siemens.sw360.datahandler.common.DatabaseSettings;
import com.siemens.sw360.datahandler.thrift.attachments.Attachment;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentContent;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.licenseinfo.LicenseInfo;
import com.siemens.sw360.datahandler.thrift.licenseinfo.LicenseInfoParsingResult;
import com.siemens.sw360.datahandler.thrift.licenseinfo.LicenseInfoRequestStatus;
import com.siemens.sw360.datahandler.thrift.licenseinfo.LicenseInfoService;
import com.siemens.sw360.licenseinfo.parsers.CLIParser;
import com.siemens.sw360.licenseinfo.parsers.LicenseInfoParser;
import com.siemens.sw360.licenseinfo.parsers.SPDXParser;
import org.apache.thrift.TException;

import java.net.MalformedURLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.siemens.sw360.datahandler.common.CommonUtils.nullToEmptyList;
import static com.siemens.sw360.datahandler.common.CommonUtils.nullToEmptySet;
import static com.siemens.sw360.datahandler.common.CommonUtils.nullToEmptyString;

/**
 * Implementation of the Thrift service
 *
 * @author alex.borodin@evosoft.com
 */
public class LicenseInfoHandler implements LicenseInfoService.Iface {

    private final AttachmentDatabaseHandler attachmentDatabaseHandler;

    private final LicenseInfoParser[] parsers;

    public LicenseInfoHandler() throws MalformedURLException {
        this(new AttachmentDatabaseHandler(DatabaseSettings.COUCH_DB_URL, DatabaseSettings.COUCH_DB_DATABASE, DatabaseSettings.COUCH_DB_ATTACHMENTS));

    }

    @VisibleForTesting
    public LicenseInfoHandler(AttachmentDatabaseHandler handler) throws MalformedURLException {
        attachmentDatabaseHandler = handler;
        Function<Attachment, AttachmentContent> contentProvider = attachment -> {
            try {
                return getAttachmentContent(attachment);
            } catch (TException e) {
                throw new UncheckedTException(e);
            }
        };
        parsers = new LicenseInfoParser[]{
                new SPDXParser(attachmentDatabaseHandler.getAttachmentConnector(), contentProvider),
                new CLIParser(attachmentDatabaseHandler.getAttachmentConnector(), contentProvider),
        };
    }

    @Override
    public LicenseInfoParsingResult getLicenseInfoForAttachment(Attachment attachment) throws TException {
        for (LicenseInfoParser parser : parsers) {
            if (parser.isApplicableTo(attachment)){
                return parser.getLicenseInfo(getAttachmentContent(attachment));
            }
        }
        return noSourceParsingResult();
    }

    @Override
    public LicenseInfoParsingResult getLicenseInfoForRelease(Release release) throws TException {
        try {

            Optional<LicenseInfoParser> parserOptional = Arrays.asList(parsers).stream()
                    .filter(p -> !getApplicableAttachments(release, p).isEmpty())
                    .findFirst();

            if (parserOptional.isPresent()){
                LicenseInfoParsingResult result = nullToEmptySet(release.getAttachments()).stream()
//                    .filter(a -> a.getAttachmentType() == AttachmentType.COMPONENT_LICENSE_INFO_XML)
                        .map(attachment -> {
                            try {
                                return parserOptional.get().getLicenseInfo(getAttachmentContent(attachment));
                            } catch (TException e) {
                                throw new UncheckedTException(e);
                            }
                        })
                        .reduce(this::mergeLicenseInfos)
                        //this could only be returned if there is a parser which found applicable sources,
                        // but there are no attachments in the release. That would be so inexplicable as to warrant
                        // throwing an exception, actually.
                        .orElse(noSourceParsingResult());
                return result;
            } else {
                // not a single parser has found applicable attachments
                return noSourceParsingResult();
            }

        } catch (UncheckedTException e) {
            throw e.getTExceptionCause();
        }
    }

    private List<Attachment> getApplicableAttachments(Release release, LicenseInfoParser parser) {
        return nullToEmptySet(release.getAttachments()).stream()
            .filter(parser::isApplicableTo)
            .collect(Collectors.toList());
    }

    private LicenseInfoParsingResult noSourceParsingResult() {
        return new LicenseInfoParsingResult().setStatus(LicenseInfoRequestStatus.NO_APPLICABLE_SOURCE);
    }

    private AttachmentContent getAttachmentContent(Attachment attachment) throws TException {
        return attachmentDatabaseHandler.getAttachmentContent(attachment.getAttachmentContentId());
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
        LicenseInfo mergedLi = new LicenseInfo(lir1.getLicenseInfo().getFiletype());
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

    class UncheckedTException extends RuntimeException{
        UncheckedTException(TException te) {
            super(te);
        }

        TException getTExceptionCause(){
            return (TException) getCause();
        }
    }
}
