/*
 * Copyright Siemens AG, 2016. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.licenseinfo.parsers;

import com.google.common.collect.Sets;
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
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;

import static org.eclipse.sw360.datahandler.common.CommonUtils.closeQuietly;

/**
 * Class for extracting copyright and license information from a simple XML file
 * @author: alex.borodin@evosoft.com
 */
public class CLIParser extends LicenseInfoParser {

    private static final Logger log = Logger.getLogger(CLIParser.class);
    private static final String COPYRIGHTS_XPATH = "/ComponentLicenseInformation/Copyright/Content";
    private static final String LICENSES_XPATH = "/ComponentLicenseInformation/License/Content";
    private static final String CLI_ROOT_ELEMENT_NAME = "ComponentLicenseInformation";
    private static final String CLI_ROOT_ELEMENT_NAMESPACE = null;
    private static final String XML_FILE_EXTENSION = ".xml";

    public CLIParser(AttachmentConnector attachmentConnector, AttachmentContentProvider attachmentContentProvider) {
        super(attachmentConnector, attachmentContentProvider);
    }

    @Override
    public boolean isApplicableTo(Attachment attachment) throws TException {
        AttachmentContent attachmentContent = attachmentContentProvider.getAttachmentContent(attachment);
        return attachmentContent.getFilename().endsWith(XML_FILE_EXTENSION) && hasCLIRootElement(attachmentContent);
    }

    private boolean hasCLIRootElement(AttachmentContent content) {
        XMLInputFactory xmlif = XMLInputFactory.newFactory();
        XMLStreamReader xmlStreamReader = null;
        InputStream attachmentStream = null;
        try {
            attachmentStream = attachmentConnector.getAttachmentStream(content);
            xmlStreamReader = xmlif.createXMLStreamReader(attachmentStream);

            //skip to first element
            while(xmlStreamReader.hasNext() && xmlStreamReader.next() != XMLStreamConstants.START_ELEMENT);
            xmlStreamReader.require(XMLStreamConstants.START_ELEMENT, CLI_ROOT_ELEMENT_NAMESPACE, CLI_ROOT_ELEMENT_NAME);
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
            closeQuietly(attachmentStream, log);
        }
    }

    @Override
    public LicenseInfoParsingResult getLicenseInfo(Attachment attachment) throws TException {
        AttachmentContent attachmentContent = attachmentContentProvider.getAttachmentContent(attachment);
        LicenseInfo licenseInfo = new LicenseInfo().setFilenames(Arrays.asList(attachmentContent.getFilename()));
        LicenseInfoParsingResult result = new LicenseInfoParsingResult().setLicenseInfo(licenseInfo);
        InputStream attachmentStream = null;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            attachmentStream = attachmentConnector.getAttachmentStream(attachmentContent);
            Document doc = builder.parse(attachmentStream);
            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression copyrightsExpr = xpath.compile(COPYRIGHTS_XPATH);
            XPathExpression licensesExpr = xpath.compile(LICENSES_XPATH);
            NodeList copyrightNodes = (NodeList) copyrightsExpr.evaluate(doc, XPathConstants.NODESET);
            NodeList licenseNodes = (NodeList) licensesExpr.evaluate(doc, XPathConstants.NODESET);
            licenseInfo.setCopyrights(nodeListToStringSet(copyrightNodes));
            licenseInfo.setLicenseNamesWithTexts(nodeListToLicenseNamesWithTextsSet(licenseNodes));
            result.setStatus(LicenseInfoRequestStatus.SUCCESS);
        } catch (ParserConfigurationException | IOException | XPathExpressionException | SAXException | SW360Exception e) {
            log.error(e);
            result.setStatus(LicenseInfoRequestStatus.FAILURE).setMessage("Error while parsing CLI file: " + e.toString());
        } finally {
            closeQuietly(attachmentStream, log);
        }
        return result;
    }

    private Set<String> nodeListToStringSet(NodeList nodes){
        Set<String> strings = Sets.newHashSet();
        for (int i = 0; i < nodes.getLength(); i++){
            strings.add(nodes.item(i).getTextContent().trim());
        }
        return strings;
    }

    private Set<LicenseNameWithText> nodeListToLicenseNamesWithTextsSet(NodeList nodes){
        Set<LicenseNameWithText> licenseNamesWithTexts= Sets.newHashSet();
        for (int i = 0; i < nodes.getLength(); i++){
            licenseNamesWithTexts.add(
                    new LicenseNameWithText()
                    .setLicenseText(nodes.item(i).getTextContent().trim())
                    .setLicenseName("")//TODO Alex: fill appropriately
            );
        }
        return licenseNamesWithTexts;
    }
}
