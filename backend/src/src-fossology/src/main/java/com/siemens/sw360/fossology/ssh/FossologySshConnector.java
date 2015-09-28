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
package com.siemens.sw360.fossology.ssh;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.siemens.sw360.datahandler.thrift.SW360Exception;
import com.siemens.sw360.fossology.config.FossologySettings;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.siemens.sw360.datahandler.common.SW360Assert.fail;
import static java.lang.System.currentTimeMillis;
import static org.apache.log4j.Logger.getLogger;

/**
 * @author daniele.fognini@tngtech.com
 */
@Component
public class FossologySshConnector {
    private static final Logger log = getLogger(FossologySshConnector.class);

    private static final OutputStream BLACK_HOLE = new OutputStream() {
        @Override
        public void write(int b) throws IOException {
        }
    };
    private static final InputStream EMPTY_INPUT = new InputStream() {
        @Override
        public int read() throws IOException {
            return -1;
        }
    };

    private final JSchSessionProvider jSchSessionProvider;
    private final int connectionTimeout;
    private final long executionTimeout;

    @Autowired
    public FossologySshConnector(JSchSessionProvider jSchSessionProvider, FossologySettings fossologySettings) {
        this.jSchSessionProvider = jSchSessionProvider;
        executionTimeout = fossologySettings.getFossologyExecutionTimeout();
        connectionTimeout = fossologySettings.getFossologyConnectionTimeout();
    }

    protected void waitCompletion(Channel channel, long timeout) throws SW360Exception {
        long startTime = currentTimeMillis();
        while (!channel.isClosed() && (currentTimeMillis() - startTime < timeout)) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw fail(e, "interrupted connection to Fossology");
            }
        }
        if (!channel.isClosed()) {
            throw fail("timeout while waiting for completion of connection to Fossology");
        }
    }

    public int runInFossologyViaSsh(String command) {
        return runInFossologyViaSsh(command, EMPTY_INPUT, BLACK_HOLE);
    }

    public int runInFossologyViaSsh(String command, InputStream stdin) {
        return runInFossologyViaSsh(command, stdin, BLACK_HOLE);
    }

    public int runInFossologyViaSsh(String command, OutputStream stdout) {
        return runInFossologyViaSsh(command, EMPTY_INPUT, stdout);
    }

    public int runInFossologyViaSsh(String command, InputStream stdin, OutputStream stdout) {
        ChannelExec channel = null;
        Session session = null;
        int exitCode = -1;
        try {
            session = jSchSessionProvider.getSession(connectionTimeout);

            channel = (ChannelExec) session.openChannel("exec");

            channel.setOutputStream(stdout);
            channel.setInputStream(stdin);

            channel.setCommand(command);

            channel.connect(connectionTimeout);
            waitCompletion(channel, executionTimeout);
            channel.disconnect();

            exitCode = channel.getExitStatus();
        } catch (JSchException | SW360Exception | NullPointerException | ClassCastException e) {
            log.error("error executing remote command on Fossology Server " + jSchSessionProvider.getServerString(), e);
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
            jSchSessionProvider.closeSession(session);
        }
        return exitCode;
    }

}
