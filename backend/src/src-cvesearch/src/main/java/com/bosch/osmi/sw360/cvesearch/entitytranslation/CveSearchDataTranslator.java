/*
 * Copyright (c) Bosch Software Innovations GmbH 2016.
 * Part of the SW360 Portal Project.
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
package com.bosch.osmi.sw360.cvesearch.entitytranslation;

import com.bosch.osmi.sw360.cvesearch.datasource.CveSearchData;
import com.siemens.sw360.datahandler.thrift.vulnerabilities.ReleaseVulnerabilityRelation;
import com.siemens.sw360.datahandler.thrift.vulnerabilities.Vulnerability;

public class CveSearchDataTranslator implements EntityTranslator<CveSearchData, CveSearchDataTranslator.VulnerabilityWithRelation> {

    public class VulnerabilityWithRelation{
        public Vulnerability vulnerability;
        public ReleaseVulnerabilityRelation relation;

        public VulnerabilityWithRelation(Vulnerability vulnerability, ReleaseVulnerabilityRelation relation) {
            this.vulnerability = vulnerability;
            this.relation = relation;
        }
    }

    @Override
    public VulnerabilityWithRelation apply(CveSearchData cveSearchData) {
        Vulnerability vulnerability = new CveSearchDataToVulnerabilityTranslator().apply(cveSearchData);
        ReleaseVulnerabilityRelation relation = new CveSearchDataToReleaseVulnerabilityRelationTranslator().apply(cveSearchData);
        return new VulnerabilityWithRelation(vulnerability,relation);
    }
}
