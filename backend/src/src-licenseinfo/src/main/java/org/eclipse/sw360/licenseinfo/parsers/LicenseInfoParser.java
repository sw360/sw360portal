/*
 * Copyright Siemens AG, 2016-2017. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.licenseinfo.parsers;

import org.apache.log4j.Logger;
import org.eclipse.sw360.datahandler.couchdb.AttachmentConnector;
import org.eclipse.sw360.datahandler.thrift.SW360Exception;
import org.eclipse.sw360.datahandler.thrift.attachments.Attachment;
import org.eclipse.sw360.datahandler.thrift.attachments.AttachmentContent;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.LicenseInfoParsingResult;
import org.apache.thrift.TException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.List;

import static org.eclipse.sw360.datahandler.common.CommonUtils.closeQuietly;

/**
 * @author: alex.borodin@evosoft.com
 */
public abstract class LicenseInfoParser {
    private static final Logger log = Logger.getLogger(LicenseInfoParser.class);
    protected final AttachmentConnector attachmentConnector;
    protected AttachmentContentProvider attachmentContentProvider;

    protected LicenseInfoParser(AttachmentConnector attachmentConnector, AttachmentContentProvider attachmentContentProvider){
        this.attachmentConnector = attachmentConnector;
        this.attachmentContentProvider = attachmentContentProvider;
    }

    public abstract boolean isApplicableTo(Attachment attachmentContent) throws TException;

    protected boolean hasThisXMLRootElement(AttachmentContent content, String rootElementNamespace, String rootElementName) {
        XMLInputFactory xmlif = XMLInputFactory.newFactory();
        XMLStreamReader xmlStreamReader = null;
        InputStream attachmentStream = null;
        try {
            attachmentStream = attachmentConnector.getAttachmentStream(content);
            xmlStreamReader = xmlif.createXMLStreamReader(attachmentStream);

            //skip to first element
            while(xmlStreamReader.hasNext() && xmlStreamReader.next() != XMLStreamConstants.START_ELEMENT);
            xmlStreamReader.require(XMLStreamConstants.START_ELEMENT, rootElementNamespace, rootElementName);
            return true;
        } catch (XMLStreamException | SW360Exception e) {
            return false;
        } finally {
            if (null != xmlStreamReader){
                try {
                    xmlStreamReader.close();
                } catch (XMLStreamException e) {
                    // ignore it
                }
            }
            closeQuietly(attachmentStream, LicenseInfoParser.log);
        }
    }

    public abstract List<LicenseInfoParsingResult> getLicenseInfos(Attachment attachment) throws TException;
}
