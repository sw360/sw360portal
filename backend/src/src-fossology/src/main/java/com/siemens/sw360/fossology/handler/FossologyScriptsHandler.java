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

import com.google.common.collect.ImmutableList;
import com.siemens.sw360.datahandler.thrift.RequestStatus;
import com.siemens.sw360.datahandler.thrift.SW360Exception;
import com.siemens.sw360.fossology.ssh.FossologyUploader;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

import static com.siemens.sw360.datahandler.common.CommonUtils.closeQuietly;
import static org.apache.log4j.Logger.getLogger;


@Component
public class FossologyScriptsHandler {
    private static final Logger log = getLogger(FossologyScriptsHandler.class);

    private static final String SCRIPTS_FOLDER = "/scripts/";
    private static final List<String> SCRIPT_FILE_NAMES = ImmutableList.<String>builder()
            .add("duplicateUpload")
            .add("getStatusOfUpload")
            .add("folderManager")
            .add("uploadFromSW360")
            .add("utilsSW360")
            .build();

    private final FossologyUploader fossologyUploader;

    @Autowired
    public FossologyScriptsHandler(FossologyUploader fossologyUploader) {
        this.fossologyUploader = fossologyUploader;
    }

    public RequestStatus deployScripts() throws SW360Exception {
        RequestStatus status = RequestStatus.SUCCESS;

        for (String scriptFileName : SCRIPT_FILE_NAMES) {
            final InputStream inputStream = FossologyScriptsHandler.class.getResourceAsStream(SCRIPTS_FOLDER + scriptFileName);
            if (inputStream == null) {
                log.error("cannot get content of script " + scriptFileName);
                status = RequestStatus.FAILURE;
                continue;
            }
            try {
                if (!fossologyUploader.copyToFossology(scriptFileName, inputStream, true)) {
                    status = RequestStatus.FAILURE;
                }
            } finally {
                closeQuietly(inputStream, log);
            }
        }

        return status;
    }
}
