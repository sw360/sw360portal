/*
 * Copyright Siemens AG, 2015. Part of the SW360 Portal Project.
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
package com.siemens.sw360.portal.portlets.projects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siemens.sw360.datahandler.thrift.components.ReleaseClearingStateSummary;
import com.siemens.sw360.portal.common.ThriftJsonSerializer;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasEntry;

/**
 * @author daniele.fognini@tngtech.com
 */
public class ProjectPortletTest {

    @Test
    public void testJsonOfClearing() throws Exception {
        ReleaseClearingStateSummary releaseClearingStateSummary = new ReleaseClearingStateSummary().setNewRelease(1).setReportAvailable(5).setUnderClearing(6).setUnderClearingByProjectTeam(17).setApproved(4);

        ThriftJsonSerializer thriftJsonSerializer = new ThriftJsonSerializer();
        String json = thriftJsonSerializer.toJson(releaseClearingStateSummary);

        assertThat(json, containsString("{\"newRelease\":1,\"underClearing\":6,\"underClearingByProjectTeam\":17,\"reportAvailable\":5,\"approved\":4}"));

        ObjectMapper objectMapper = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> map = objectMapper.readValue(json, Map.class);

        assertThat(map, hasEntry("newRelease", (Object) 1));


    }
}