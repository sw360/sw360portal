/*
 * Copyright (c) Bosch Software Innovations GmbH 2015.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License Version 2.0 as published by the
 * Free Software Foundation with classpath exception.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License version 2.0 for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (please see the COPYING file); if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
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
            sdkFault.printStackTrace();
            LOGGER.error("Unable to retrieve components from Bdp server. Reason \n " + sdkFault.getMessage());
            LOGGER.debug("Project name " + getName());
            throw new IllegalStateException(sdkFault.getCause());
        }
    }

    private Collection<Component> translate(List<BomComponent> bomComponents) {
        Collection<Component> result = new ArrayList<Component>();
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
        int Filecount = 0;
        try {
            AnalysisCodeTreeInfo analysisCodeTreeInfo = access.getDiscoveryApi()
                    .getLastAnalysisCodeTreeInfo(project.getProjectId());
            Filecount = analysisCodeTreeInfo.getAnalyzedFileCount();
        } catch (SdkFault sdkFault) {
            LOGGER.error("Unable to retrieve project data. Reason \n" + sdkFault.getMessage());
            throw new IllegalStateException(sdkFault.getCause());
        }
        return String.valueOf(Filecount);
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

            for (CodeTreeNode node : nodes) {
                Map<NodeCountType, Long> countMap = CodeTreeUtilities
                        .getNodeCountMap(node);
                fileIdentifiedCount = countMap.get(NodeCountType.PENDING_ID_ALL);
            }
            return String.valueOf(fileIdentifiedCount);

        } catch (SdkFault sdkFault) {
            LOGGER.error("Unable to retrieve project data. Reason \n" + sdkFault.getMessage());
            throw new IllegalStateException(sdkFault.getCause());
        }
    }
}