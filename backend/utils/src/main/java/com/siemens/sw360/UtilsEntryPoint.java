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
package com.siemens.sw360;

import com.siemens.sw360.attachments.db.RemoteAttachmentDownloader;
import org.apache.commons.cli.*;

import java.net.MalformedURLException;

/**
 * @author daniele.fognini@tngtech.com
 */
public class UtilsEntryPoint {

    private static final String OPTION_HELP = "h";
    private static final String OPTION_DOWNLOAD = "d";

    public static void main(String[] args) throws MalformedURLException {
        CommandLine cmd;

        try {
            cmd = parseArgs(args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            printHelp();
            return;
        }

        String[] leftArgs = cmd.getArgs();

        if (cmd.hasOption(OPTION_HELP)) {
            printHelp();
            return;
        }

        if (cmd.hasOption(OPTION_DOWNLOAD)) {
            runRemoteAttachmentDownloader(leftArgs);
        } else {
            printHelp();
        }
    }

    private static void runRemoteAttachmentDownloader(String[] args) throws MalformedURLException {
        RemoteAttachmentDownloader.main(args);
    }

    private static CommandLine parseArgs(String[] args) throws ParseException {
        Options options = getOptions();

        return new BasicParser().parse(options, args, true);
    }

    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(UtilsEntryPoint.class.getCanonicalName(), getOptions());
    }

    private static Options getOptions() {
        Options options = new Options();
        options.addOption(OPTION_DOWNLOAD, false, "download remote attachments");
        options.addOption(OPTION_HELP, false, "show this help");
        return options;
    }
}
