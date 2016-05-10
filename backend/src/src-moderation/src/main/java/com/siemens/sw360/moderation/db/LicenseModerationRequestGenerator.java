/*
 * Copyright Siemens AG, 2013-2016. Part of the SW360 Portal Project.
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

package com.siemens.sw360.moderation.db;

import com.google.common.collect.Maps;
import com.siemens.sw360.datahandler.thrift.licenses.License;
import com.siemens.sw360.datahandler.thrift.licenses.Todo;
import com.siemens.sw360.datahandler.thrift.moderation.ModerationRequest;

import java.util.*;
import static com.siemens.sw360.datahandler.common.CommonUtils.nullToEmptyList;

/**
 * Class for comparing a document with its counterpart in the database
 * Writes the difference (= additions and deletions) to the moderation request
 *
 * @author birgit.heydenreicht@tngtech.com
 */
public class LicenseModerationRequestGenerator extends ModerationRequestGenerator<License._Fields, License> {

    @Override
    public ModerationRequest setAdditionsAndDeletions(ModerationRequest request, License updateLicense, License actualLicense){
        updateDocument = updateLicense;
        actualDocument = actualLicense;

        documentAdditions = new License();
        documentDeletions = new License();
        //required fields:
        documentAdditions.setFullname(updateLicense.getFullname());
        documentAdditions.setId(actualLicense.getId());
        documentDeletions.setFullname(actualLicense.getFullname());
        documentDeletions.setId(actualLicense.getId());

        Map<String, Todo> actualTodos = Maps.uniqueIndex(nullToEmptyList(actualLicense.getTodos()), Todo::getId);

        for (Todo updateTodo : updateLicense.getTodos()) {
            if(!actualTodos.containsKey(updateTodo.getId())){
                if(!documentAdditions.isSetTodos()) {
                    documentAdditions.setTodos(new ArrayList<>());
                }
                documentAdditions.getTodos().add(updateTodo);
            } else {
                Todo actualTodo = actualTodos.get(updateTodo.getId());
                Set<String> actualWhitelist = actualTodo.whitelist != null ? actualTodo.whitelist : new HashSet<String>();
                Set<String> updateWhitelist = updateTodo.whitelist != null ? updateTodo.whitelist : new HashSet<String>();
                String departement = request.getRequestingUserDepartment();
                if(updateWhitelist.contains(departement) && !actualWhitelist.contains(departement)){
                    if(!documentAdditions.isSetTodos()) {
                        documentAdditions.setTodos(new ArrayList<>());
                    }
                    documentAdditions.getTodos().add(updateTodo);
                } else if (!updateWhitelist.contains(departement) && actualWhitelist.contains(departement)) {
                    if(!documentDeletions.isSetTodos()) {
                        documentDeletions.setTodos(new ArrayList<>());
                    }
                    documentDeletions.getTodos().add(actualTodo);
                }
            }
        }

        request.setLicenseAdditions(documentAdditions);
        request.setLicenseDeletions(documentDeletions);
        return request;
    }
}
