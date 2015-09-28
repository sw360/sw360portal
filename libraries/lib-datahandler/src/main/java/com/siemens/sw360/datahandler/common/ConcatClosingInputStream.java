/*
 * Copyright Siemens AG, 2014-2015. Part of the SW360 Portal Project.
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
package com.siemens.sw360.datahandler.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;

/**
 * @author daniele.fognini@tngtech.com
 */
public class ConcatClosingInputStream extends InputStream {
    private final Iterator<InputStream> streams;
    private boolean closed = false;
    private InputStream current;

    /* streams.next() and .hasNext() must not throw exceptions */
    public ConcatClosingInputStream(Iterator<InputStream> streams) {
        if (streams == null)
            streams = Collections.emptyIterator();

        if (streams.hasNext()) {
            this.current = streams.next();
        } else {
            this.closed = true;
        }

        this.streams = streams;
    }

    @Override
    public int read() throws IOException {
        if (closed)
            return -1;

        if (current == null) {
            return safeCloseAndThrow(new IOException("cannot read from null Stream"));
        }

        int read;
        try {
            read = current.read();
        } catch (IOException e) {
            return safeCloseAndThrow(e);
        }

        if (read >= 0)
            return read;

        else {
            try {
                current.close();
            } catch (IOException e) {
                return safeCloseAndThrow(e);
            }
            if (streams.hasNext()) {
                current = streams.next();
                return read();
            } else {
                closed = true;
                return -1;
            }
        }
    }

    private int safeCloseAndThrow(IOException e) throws IOException {
        try {
            close();
        } catch (IOException close) {
            e.addSuppressed(close);
        }
        throw e;
    }

    @Override
    public void close() throws IOException {
        if (closed)
            return;

        IOException ioException = tryClose(current);
        while (streams.hasNext()) {
            IOException exception = tryClose(streams.next());
            ioException = asSuppressedOf(ioException, exception);
        }
        closed = true;

        if (ioException != null)
            throw ioException;
    }

    private IOException asSuppressedOf(IOException ioException, IOException exception) {
        if (exception != null) {
            if (ioException == null) {
                ioException = exception;
            } else {
                ioException.addSuppressed(exception);
            }
        }
        return ioException;
    }

    private IOException tryClose(InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                return e;
            }
        }
        return null;
    }
}
