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
package com.siemens.sw360.datahandler.db;

import com.siemens.sw360.datahandler.couchdb.DatabaseRepository;
import com.siemens.sw360.datahandler.permissions.PermissionUtils;
import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.RequestSummary;
import com.siemens.sw360.datahandler.thrift.users.User;
import org.ektorp.DocumentOperationResult;

import java.util.Collection;
import java.util.List;

/**
 * @author johannes.najjar@tngtech.com
 */
public class RepositoryUtils {
    //This works with any repository
    public static RequestSummary doBulk(Collection<?> objects, User user, DatabaseRepository<?> repository){

        RequestSummary requestSummary =  new RequestSummary();
        if(PermissionUtils.isAdmin(user)) {
            // Prepare component for database
            final List<DocumentOperationResult> documentOperationResults = repository.executeBulk(objects);

            requestSummary.setTotalElements(objects.size() );
            requestSummary.setTotalAffectedElements(objects.size() - documentOperationResults.size());

            requestSummary.setRequestStatus(RequestStatus.SUCCESS);
        } else {
            requestSummary.setRequestStatus(RequestStatus.FAILURE);
        }
        return requestSummary;
    }
}
