/*
 * Copyright Siemens AG, 2016. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.siemens.sw360.cliservice.parsers;

import com.google.common.collect.Sets;
import com.siemens.sw360.datahandler.couchdb.AttachmentConnector;
import com.siemens.sw360.datahandler.thrift.SW360Exception;
import com.siemens.sw360.datahandler.thrift.attachments.AttachmentContent;
import com.siemens.sw360.datahandler.thrift.cliservice.CopyrightLicenseInfo;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.*;
import javax.xml.xpath.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

/**
 * Class for extracting copyright and license information from a simple XML file
 * @author: alex.borodin@evosoft.com
 */
public class CLIParser extends CopyrightLicenseInfoParser {
    public static final String FILETYPE_CLI = "CLI";

    private static final Logger log = Logger.getLogger(CLIParser.class);
    private static final String COPYRIGHTS_XPATH = "/ComponentLicenseInformation/Copyright/Content";
    private static final String LICENSES_XPATH = "/ComponentLicenseInformation/License/Content";
    private static final String CLI_ROOT_ELEMENT_NAME = "ComponentLicenseInformation";
    private static final String CLI_ROOT_ELEMENT_NAMESPACE = null;
    private static final String XML_FILE_EXTENSION = ".xml";

    public CLIParser(AttachmentConnector attachmentConnector) {
        super(attachmentConnector);
    }

    @Override
    public boolean isApplicableTo(AttachmentContent attachmentContent) {
        return attachmentContent.getFilename().endsWith(XML_FILE_EXTENSION) && hasCLIRootElement(attachmentContent);
    }

    private boolean hasCLIRootElement(AttachmentContent content) {
        XMLInputFactory xmlif = XMLInputFactory.newFactory();
        XMLStreamReader xmlStreamReader = null;
        try {
            xmlStreamReader = xmlif.createXMLStreamReader(attachmentConnector.getAttachmentStream(content));

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
        }
    }

    @Override
    public CopyrightLicenseInfo getCLI(AttachmentContent attachmentContent) {
        CopyrightLicenseInfo result = new CopyrightLicenseInfo().setFilenames(Arrays.asList(attachmentContent.getFilename()));
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(attachmentConnector.getAttachmentStream(attachmentContent));
            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression copyrightsExpr = xpath.compile(COPYRIGHTS_XPATH);
            XPathExpression licensesExpr = xpath.compile(LICENSES_XPATH);
            NodeList copyrightNodes = (NodeList) copyrightsExpr.evaluate(doc, XPathConstants.NODESET);
            NodeList licenseNodes = (NodeList) licensesExpr.evaluate(doc, XPathConstants.NODESET);
            result.setCopyrights(nodeListToStringSet(copyrightNodes));
            result.setLicenseTexts(nodeListToStringSet(licenseNodes));

            result.setFiletype(FILETYPE_CLI);
        } catch (ParserConfigurationException | IOException | XPathExpressionException | SAXException | SW360Exception e) {
            log.error(e);
            result.setFiletype(FILETYPE_PARSING_IMPOSSIBLE);
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
}
