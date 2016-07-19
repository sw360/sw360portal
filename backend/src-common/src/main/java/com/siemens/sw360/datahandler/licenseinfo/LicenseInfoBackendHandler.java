package com.siemens.sw360.datahandler.licenseinfo;

import com.google.common.collect.Sets;
import com.siemens.sw360.datahandler.db.AttachmentDatabaseHandler;
import com.siemens.sw360.datahandler.licenseinfo.parsers.CLIParser;
import com.siemens.sw360.datahandler.licenseinfo.parsers.LicenseInfoParser;
import com.siemens.sw360.datahandler.licenseinfo.parsers.SPDXParser;
import com.siemens.sw360.datahandler.thrift.attachments.Attachment;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentContent;
import com.siemens.sw360.datahandler.thrift.components.Release;
import com.siemens.sw360.datahandler.thrift.licenseinfo.LicenseInfo;
import com.siemens.sw360.datahandler.thrift.licenseinfo.LicenseInfoParsingResult;
import com.siemens.sw360.datahandler.thrift.licenseinfo.LicenseInfoRequestStatus;
import org.apache.thrift.TException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.siemens.sw360.datahandler.common.CommonUtils.*;

public class LicenseInfoBackendHandler{

    private final LicenseInfoParser[] parsers;

    public LicenseInfoBackendHandler(AttachmentDatabaseHandler attachmentDatabaseHandler) {
        Function<Attachment, AttachmentContent> contentProvider = attachment -> {
            try {
                return attachmentDatabaseHandler.getAttachmentContent(attachment.getAttachmentContentId());
            } catch (TException e) {
                throw new UncheckedTException(e);
            }
        };
        parsers = new LicenseInfoParser[]{
                new SPDXParser(attachmentDatabaseHandler.getAttachmentConnector(), contentProvider),
                new CLIParser(attachmentDatabaseHandler.getAttachmentConnector(), contentProvider),
        };
    }

    public LicenseInfoParsingResult getLicenseInfoForAttachment(Attachment attachment) {
        for (LicenseInfoParser parser : parsers) {
            if (parser.isApplicableTo(attachment)) {
                return parser.getLicenseInfo(attachment);
            }
        }
        return noSourceParsingResult();
    }

    public LicenseInfoParsingResult getLicenseInfoForRelease(Release release) {
        Optional<LicenseInfoParser> parserOptional = Arrays.asList(parsers).stream()
                .filter(p -> !getApplicableAttachments(release, p).isEmpty())
                .findFirst();

        if (parserOptional.isPresent()) {
            LicenseInfoParsingResult result = getApplicableAttachments(release, parserOptional.get()).stream()
                    .map(attachment -> parserOptional.get().getLicenseInfo(attachment))
                    .reduce(this::mergeLicenseInfos)
                    //this could only be returned if there is a parser which found applicable sources,
                    // but there are no attachments in the release. That would be so inexplicable as to warrant
                    // throwing an exception, actually.
                    .orElse(noSourceParsingResult());
            LicenseInfo resultLI = result.getLicenseInfo();
            if (null != resultLI){
                resultLI.setVendor(release.isSetVendor()?release.getVendor().getShortname():"");
                resultLI.setName(release.getName());
                resultLI.setVersion(release.getVersion());
            }
            return result;
        } else {
            // not a single parser has found applicable attachments
            return noSourceParsingResult();
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

}