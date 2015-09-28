/*
 * Copyright Siemens AG, 2013-2015. Part of the SW360 Portal Project.
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
package com.siemens.sw360.fossology.handler;

import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.fossology.FossologyHostFingerPrint;
import com.siemens.sw360.fossology.db.FossologyFingerPrintRepository;
import org.apache.thrift.TException;
import org.ektorp.DocumentOperationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.siemens.sw360.datahandler.common.CommonUtils.nullToEmptyList;

@Component
public class FossologyHostKeyHandler {
    private final FossologyFingerPrintRepository fossologyHostKeyConnector;

    @Autowired
    public FossologyHostKeyHandler(FossologyFingerPrintRepository fossologyHostKeyConnector) {
        this.fossologyHostKeyConnector = fossologyHostKeyConnector;
    }

    public List<FossologyHostFingerPrint> getFingerPrints() throws TException {
        final List<FossologyHostFingerPrint> fingerPrints = fossologyHostKeyConnector.getAll();
        return nullToEmptyList(fingerPrints);
    }

    public RequestStatus setFingerPrints(List<FossologyHostFingerPrint> fingerPrints) throws TException {

        final List<DocumentOperationResult> documentOperationResults = fossologyHostKeyConnector.executeBulk(fingerPrints);

        if (documentOperationResults.isEmpty()) {
            return RequestStatus.SUCCESS;
        } else {
            return RequestStatus.FAILURE;
        }
    }
}
