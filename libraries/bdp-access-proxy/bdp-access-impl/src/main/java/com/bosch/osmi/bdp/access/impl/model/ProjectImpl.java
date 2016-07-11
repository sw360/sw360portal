/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.bosch.osmi.bdp.access.impl.model;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.project.bom.BomComponent;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNode;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNodeRequest;
import com.blackducksoftware.sdk.protex.project.codetree.NodeCountType;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.AnalysisCodeTreeInfo;
import com.blackducksoftware.sdk.protex.util.CodeTreeUtilities;
import com.bosch.osmi.bdp.access.api.model.*;
import com.bosch.osmi.bdp.access.impl.BdpApiAccessImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author johannes.kristan@bosch-si.com
 * @since 11/18/15.
 */
public class ProjectImpl implements Project {

    private static final Logger LOGGER = LogManager.getLogger(ProjectImpl.class);

    private final com.blackducksoftware.sdk.protex.project.Project project;
    private final BdpApiAccessImpl access;

    public ProjectImpl(com.blackducksoftware.sdk.protex.project.Project project, BdpApiAccessImpl access) {
        this.project = project;
        this.access = access;
    }

    @Override
    public String getName() {
        return project.getName();
    }

    @Override
    public Collection<Component> getComponents() {
        try {
            List<BomComponent> bomComponents = access.getBomApi().getBomComponents(project.getProjectId());
            Collection<Component> components = translate(bomComponents);
            return components;
        } catch (SdkFault sdkFault) {
            LOGGER.error("Unable to retrieve components from Bdp server. Reason \n " + sdkFault.getMessage());
            LOGGER.debug("Project name " + getName());
            throw new IllegalStateException(sdkFault.getCause());
        }
    }

    private Collection<Component> translate(List<BomComponent> bomComponents) {
        Collection<Component> result = new ArrayList<>();
        for (BomComponent source : bomComponents) {
            result.add(new ComponentImpl(source, project.getProjectId(), access));
        }
        return result;
    }

    @Override
    public String getCreatedBy() {
        return project.getCreatedBy();
    }

    @Override
    public String getFilesScanned() {
        int filecount = 0;
        try {
            AnalysisCodeTreeInfo analysisCodeTreeInfo = access.getDiscoveryApi()
                    .getLastAnalysisCodeTreeInfo(project.getProjectId());
            filecount = analysisCodeTreeInfo.getAnalyzedFileCount();
        } catch (SdkFault sdkFault) {
            LOGGER.error("Unable to retrieve project data. Reason \n" + sdkFault.getMessage());
            throw new IllegalStateException(sdkFault.getCause());
        }
        return String.valueOf(filecount);
    }

    @Override
    public String getPendingIdentification() {
        long fileIdentifiedCount = 0;
        String root = "/";
        int treeDepth = 1;

        try {
            CodeTreeNodeRequest codeTreeParameters = new CodeTreeNodeRequest();
            codeTreeParameters.getIncludedNodeTypes().addAll(CodeTreeUtilities.ALL_CODE_TREE_NODE_TYPES);
            codeTreeParameters.setDepth(treeDepth);
            codeTreeParameters.setIncludeParentNode(true);
            codeTreeParameters.getCounts().add(NodeCountType.PENDING_ID_ALL);
            List<CodeTreeNode> nodes = access.getCodeTreeApi().getCodeTreeNodes(project.getProjectId(), root, codeTreeParameters);

            if(!nodes.isEmpty()){
                Map<NodeCountType, Long> countMap = CodeTreeUtilities.getNodeCountMap(nodes.get(0));
                fileIdentifiedCount = countMap.get(NodeCountType.PENDING_ID_ALL);
                if(nodes.size() > 1){
                    LOGGER.debug("Project " + this.getName() +
                            " contains more than one root node, which should not happen");
                }
            }else{
                throw new IllegalStateException("The project " +
                        this.getName() +
                        " should contain at least on root level in its source tree.");
            }

            return String.valueOf(fileIdentifiedCount);

        } catch (SdkFault sdkFault) {
            LOGGER.error("Unable to retrieve project data. Reason \n" + sdkFault.getMessage());
            throw new IllegalStateException(sdkFault.getCause());
        }
    }
}
