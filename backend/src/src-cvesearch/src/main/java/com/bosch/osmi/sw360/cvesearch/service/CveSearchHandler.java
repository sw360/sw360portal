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
package com.bosch.osmi.sw360.cvesearch.service;

import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.fossology.CveSearchService;
import org.apache.thrift.TException;

import java.util.Set;

public class CveSearchHandler implements CveSearchService.Iface {

    @Override
    public RequestStatus updateForCPE(String cpe) throws TException {
        return null;
    }

    @Override
    public RequestStatus updateForRelease(String ReleaseId) throws TException {
        return null;
    }

    @Override
    public RequestStatus updateForComponent(String ComponentId) throws TException {
        return null;
    }

    @Override
    public RequestStatus updateForVendor(String vendorId) throws TException {
        return null;
    }

    @Override
    public RequestStatus fullUpdate() throws TException {
        return null;
    }

    @Override
    public RequestStatus fullUpdateLastMonth() throws TException {
        return null;
    }

    @Override
    public RequestStatus fullUpdateLastWeek() throws TException {
        return null;
    }

    @Override
    public RequestStatus fullUpdateLastDay() throws TException {
        return null;
    }

    @Override
    public Set<String> findCpes(String vendor, String product, String version) throws TException {
        return null;
    }
}
