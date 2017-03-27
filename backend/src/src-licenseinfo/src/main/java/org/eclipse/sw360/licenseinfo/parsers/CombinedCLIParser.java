/*
 * Copyright Siemens AG, 2017. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.licenseinfo.parsers;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.eclipse.sw360.datahandler.common.CommonUtils;
import org.eclipse.sw360.datahandler.common.SW360Utils;
import org.eclipse.sw360.datahandler.couchdb.AttachmentConnector;
import org.eclipse.sw360.datahandler.db.ComponentDatabaseHandler;
import org.eclipse.sw360.datahandler.thrift.SW360Exception;
import org.eclipse.sw360.datahandler.thrift.attachments.Attachment;
import org.eclipse.sw360.datahandler.thrift.attachments.AttachmentContent;
import org.eclipse.sw360.datahandler.thrift.components.Release;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.LicenseInfo;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.LicenseInfoParsingResult;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.LicenseInfoRequestStatus;
import org.eclipse.sw360.datahandler.thrift.licenseinfo.LicenseNameWithText;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.sw360.datahandler.common.CommonUtils.closeQuietly;

/**
 * Class for extracting copyright and license information from a simple XML file
 * @author: alex.borodin@evosoft.com
 */
public class CombinedCLIParser extends LicenseInfoParser {

    private static final Logger log = Logger.getLogger(CombinedCLIParser.class);
    private static final String COPYRIGHTS_XPATH = "/CombinedCLI/Copyright";
    private static final String LICENSES_XPATH = "/CombinedCLI/License";
    private static final String COPYRIGHT_CONTENT_ELEMENT_NAME = "Content";
    private static final String EXTERNAL_ID_ATTRIBUTE_NAME = "srcComponent";
    private static final String LICENSE_CONTENT_ELEMENT_NAME = "Content";
    private static final String LICENSE_ACKNOWLEDGEMENTS_ELEMENT_NAME = "Acknowledgements";
    private static final String COMBINED_CLI_ROOT_ELEMENT_NAME = "CombinedCLI";
    private static final String COMBINED_CLI_ROOT_ELEMENT_NAMESPACE = null;
    private static final String XML_FILE_EXTENSION = ".xml";

    private static final String LICENSENAME_ATTRIBUTE_NAME = "name";
    private static final String LICENSE_NAME_UNKNOWN = "License name unknown";
    private static final String PROPERTIES_FILE_PATH = "/sw360.properties";
    public static final String EXTERNAL_ID_CORRELATION_KEY = "combined.cli.parser.external.id.correlation.key";

    private ComponentDatabaseHandler componentDatabaseHandler;

    public CombinedCLIParser(AttachmentConnector attachmentConnector, AttachmentContentProvider attachmentContentProvider, ComponentDatabaseHandler componentDatabaseHandler) {
        super(attachmentConnector, attachmentContentProvider);
        this.componentDatabaseHandler = componentDatabaseHandler;
    }

    String getCorrelationKey(){
        Properties props = CommonUtils.loadProperties(CombinedCLIParser.class, PROPERTIES_FILE_PATH);
        String releaseExternalIdCorrelationKey = props.getProperty(EXTERNAL_ID_CORRELATION_KEY);
        if (isNullOrEmpty(releaseExternalIdCorrelationKey)){
            log.warn("Property combined.cli.parser.external.id.correlation.key is not set. Combined CLI parsing will not be able to load names of referenced releases");
        }
        return releaseExternalIdCorrelationKey;
    }

    @Override
    public boolean isApplicableTo(Attachment attachment) throws TException {
        AttachmentContent attachmentContent = attachmentContentProvider.getAttachmentContent(attachment);
        return attachmentContent.getFilename().endsWith(XML_FILE_EXTENSION) && hasCombinedCLIRootElement(attachmentContent);
    }

    private boolean hasCombinedCLIRootElement(AttachmentContent content) {
        return hasThisXMLRootElement(content, COMBINED_CLI_ROOT_ELEMENT_NAMESPACE, COMBINED_CLI_ROOT_ELEMENT_NAME);
    }

    @Override
    public List<LicenseInfoParsingResult> getLicenseInfos(Attachment attachment) throws TException {
        AttachmentContent attachmentContent = attachmentContentProvider.getAttachmentContent(attachment);
        InputStream attachmentStream = null;
        List<LicenseInfoParsingResult> parsingResults = new ArrayList<>();
        Map<String, Release> releasesByExternalId = prepareReleasesByExternalId(getCorrelationKey());

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
            Map<String, Set<String>> copyrightSetsByExternalId = nodeListToStringSetsByExternalId(copyrightNodes, EXTERNAL_ID_ATTRIBUTE_NAME, COPYRIGHT_CONTENT_ELEMENT_NAME);
            Map<String, Set<LicenseNameWithText>> licenseNamesWithTextsByExternalId = nodeListToLicenseNamesWithTextsSetsByExternalId(licenseNodes, EXTERNAL_ID_ATTRIBUTE_NAME);
            Set<String> allExternalIds = Sets.union(copyrightSetsByExternalId.keySet(), licenseNamesWithTextsByExternalId.keySet());
            allExternalIds.forEach(extId -> {
                LicenseInfo licenseInfo = new LicenseInfo().setFilenames(Arrays.asList(attachmentContent.getFilename()));
                licenseInfo.setCopyrights(copyrightSetsByExternalId.get(extId));
                licenseInfo.setLicenseNamesWithTexts(licenseNamesWithTextsByExternalId.get(extId));
                LicenseInfoParsingResult parsingResult = new LicenseInfoParsingResult().setLicenseInfo(licenseInfo);
                Release release = releasesByExternalId.get(extId);
                if (release != null) {
                    parsingResult.setVendor(release.isSetVendor() ? release.getVendor().getShortname() : "");
                    parsingResult.setName(release.getName());
                    parsingResult.setVersion(release.getVersion());
                } else {
                    parsingResult.setName("No info found for external component ID " + extId);
                }
                parsingResult.setStatus(LicenseInfoRequestStatus.SUCCESS);
                parsingResults.add(parsingResult);
            });
        } catch (ParserConfigurationException | IOException | XPathExpressionException | SAXException | SW360Exception e) {
            log.error(e);
            parsingResults.add(new LicenseInfoParsingResult()
                    .setStatus(LicenseInfoRequestStatus.FAILURE)
                    .setMessage("Error while parsing CLI file: " + e.toString()));
        } finally {
            closeQuietly(attachmentStream, log);
        }
        return parsingResults;
    }

    private Map<String, Release> prepareReleasesByExternalId(String correlationKey) {
        Map<String, Release> idMap = componentDatabaseHandler.getAllReleasesIdMap();
        Map<String, Release> releasesByExternalId = idMap.values().stream()
                .filter(r -> r.getExternalIds() != null && r.getExternalIds().containsKey(correlationKey))
                .collect(Collectors.toMap(r -> r.getExternalIds().get(correlationKey), r -> r, (r1, r2) -> {
                    throw new RuntimeException(String.format("Duplicate externalId in releases %s and %s", SW360Utils.printFullname(r1), SW360Utils.printFullname(r2)));}));
        return releasesByExternalId;
    }

    private Map<String, Set<String>> nodeListToStringSetsByExternalId(NodeList nodes, String externalIdAttributeName, String contentElementName){
        Map<String, Set<String>> result = Maps.newHashMap();
        for (int i = 0; i < nodes.getLength(); i++){
            Optional<Node> externalIdOptional = findNamedAttribute(nodes.item(i), externalIdAttributeName);
            String externalId = externalIdOptional.map(Node::getNodeValue).orElse(null);
            String contentText = findNamedSubelement(nodes.item(i), contentElementName).map(Node::getTextContent).map(String::trim).orElse(null);
            if (!result.containsKey(externalId)){
                result.put(externalId, Sets.newHashSet());
            }
            result.get(externalId).add(contentText);
        }
        return result;
    }

    private Map<String, Set<LicenseNameWithText>> nodeListToLicenseNamesWithTextsSetsByExternalId(NodeList nodes, String externalIdAttributeName){
        Map<String, Set<LicenseNameWithText>> result = Maps.newHashMap();
        for (int i = 0; i < nodes.getLength(); i++){
            Optional<Node> externalIdOptional = findNamedAttribute(nodes.item(i), externalIdAttributeName);
            String externalId = externalIdOptional.map(Node::getNodeValue).orElse(null);

            LicenseNameWithText licenseNameWithText = new LicenseNameWithText()
                    .setLicenseText(findNamedSubelement(nodes.item(i), LICENSE_CONTENT_ELEMENT_NAME)
                            .map(Node::getTextContent)
                            .map(String::trim)
                            .orElse(null))
                    .setAcknowledgements(findNamedSubelement(nodes.item(i), LICENSE_ACKNOWLEDGEMENTS_ELEMENT_NAME)
                            .map(Node::getTextContent)
                            .map(String::trim)
                            .orElse(null))
                    .setLicenseName(Optional
                            .ofNullable(nodes.item(i).getAttributes().getNamedItem(LICENSENAME_ATTRIBUTE_NAME))
                            .map(Node::getNodeValue)
                            .orElse(LICENSE_NAME_UNKNOWN));

            if (!result.containsKey(externalId)){
                result.put(externalId, Sets.newHashSet());
            }
            result.get(externalId).add(licenseNameWithText);

        }
        return result;
    }

    private Optional<Node> findNamedAttribute(Node node, String name){
        NamedNodeMap childNodes = node.getAttributes();
        return Optional.ofNullable(childNodes.getNamedItem(name));
    }

    private Optional<Node> findNamedSubelement(Node node, String name){
        NodeList childNodes = node.getChildNodes();
        return streamFromNodeList(childNodes).filter(n -> n.getNodeName().equals(name)).findFirst();
    }

    private Stream<Node> streamFromNodeList(NodeList nodes){
        Iterator<Node> iter = new NodeListIterator(nodes);
        Iterable<Node> iterable = () -> iter;
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    class NodeListIterator implements Iterator<Node>{
        private final NodeList nodes;
        private int i;

        public NodeListIterator(NodeList nodes) {
            this.nodes = nodes;
            this.i = 0;
        }

        @Override
        public boolean hasNext() {
            return i < nodes.getLength();
        }

        @Override
        public Node next() {
            if (hasNext()){
                i++;
                return nodes.item(i-1);
            } else {
                throw new NoSuchElementException();
            }
        }
    }
}
