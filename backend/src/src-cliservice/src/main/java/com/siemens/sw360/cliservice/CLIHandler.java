/*
 * Copyright Siemens AG, 2016. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.cliservice;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.siemens.sw360.attachments.db.AttachmentDatabaseHandler;
import com.siemens.sw360.cliservice.parsers.CLIParser;
import com.siemens.sw360.cliservice.parsers.CopyrightLicenseInfoParser;
import com.siemens.sw360.datahandler.common.DatabaseSettings;
import com.siemens.sw360.datahandler.couchdb.AttachmentConnector;
import com.siemens.sw360.datahandler.thrift.attachments.Attachment;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentContent;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentType;
import com.siemens.sw360.datahandler.thrift.cliservice.CopyrightLicenseInfo;
import com.siemens.sw360.datahandler.thrift.cliservice.CopyrightLicenseInfoService;
import com.siemens.sw360.datahandler.thrift.components.Release;
import org.apache.thrift.TException;

import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.siemens.sw360.datahandler.common.CommonUtils.nullToEmptyList;
import static com.siemens.sw360.datahandler.common.CommonUtils.nullToEmptySet;

/**
 * Implementation of the Thrift service
 *
 * @author alex.borodin@evosoft.com
 */
public class CLIHandler implements CopyrightLicenseInfoService.Iface {

    private final AttachmentDatabaseHandler attachmentDatabaseHandler;

    private final CopyrightLicenseInfoParser[] parsers;

    public CLIHandler() throws MalformedURLException {
        this(new AttachmentDatabaseHandler(DatabaseSettings.COUCH_DB_URL, DatabaseSettings.COUCH_DB_DATABASE, DatabaseSettings.COUCH_DB_ATTACHMENTS));

    }

    @VisibleForTesting
    public CLIHandler(AttachmentDatabaseHandler handler) throws MalformedURLException {
        attachmentDatabaseHandler = handler;
        parsers = new CopyrightLicenseInfoParser[]{
//                new SPDXParser(attachmentDatabaseHandler.getAttachmentConnector()),
                new CLIParser(attachmentDatabaseHandler.getAttachmentConnector()),};
    }

    @Override
    public CopyrightLicenseInfo getComponentLicenseInfoForAttachment(String attachmentContentId) throws TException {
        AttachmentContent attachmentContent = attachmentDatabaseHandler.getAttachmentContent(attachmentContentId);
        return applyParsers(attachmentContent);
    }

    @Override
    public CopyrightLicenseInfo getComponentLicenseInfoForRelease(Release release) throws TException {
        try {
            CopyrightLicenseInfo result = nullToEmptySet(release.getAttachments()).stream()
                    .filter(a -> a.getAttachmentType() == AttachmentType.COMPONENT_LICENSE_INFO_XML)
                    .map(Attachment::getAttachmentContentId)
                    .map(cid -> {
                        try {
                            return this.getComponentLicenseInfoForAttachment(cid);
                        } catch (TException e) {
                            throw new UncheckedTException(e);
                        }
                    })
                    .reduce(this::mergeCLIs)
                    .orElse(null);


            return result;
        } catch (UncheckedTException e) {
            throw e.getTExceptionCause();
        }
    }

    private CopyrightLicenseInfo applyParsers(AttachmentContent content){
        for (CopyrightLicenseInfoParser parser : parsers) {
            if (parser.isApplicableTo(content)){
                return parser.getCLI(content);
            }
        }
        return new CopyrightLicenseInfo().setFiletype(CopyrightLicenseInfoParser.FILETYPE_PARSING_IMPOSSIBLE);
    }

    private CopyrightLicenseInfo mergeCLIs(CopyrightLicenseInfo cli1, CopyrightLicenseInfo cli2){
        //filetype is copied by the constructor
        CopyrightLicenseInfo res = new CopyrightLicenseInfo(cli1);
        //merging filenames
        Set<String> filenames = new HashSet<>();
        filenames.addAll(nullToEmptyList(cli1.getFilenames()));
        filenames.addAll(nullToEmptyList(cli2.getFilenames()));
        res.setFilenames(filenames.stream().collect(Collectors.toList()));
        //merging copyrights
        res.setCopyrights(Sets.union(nullToEmptySet(cli1.getCopyrights()), nullToEmptySet(cli2.getCopyrights())));
        //merging licenses
        res.setLicenseTexts(Sets.union(nullToEmptySet(cli1.getLicenseTexts()), nullToEmptySet(cli2.getLicenseTexts())));

        return res;
    }

    private class UncheckedTException extends RuntimeException{
        UncheckedTException(TException te) {
            super(te);
        }

        TException getTExceptionCause(){
            return (TException) getCause();
        }
    }
}
