/*
 * Copyright Siemens AG, 2017. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.datahandler.common;

import org.eclipse.sw360.datahandler.thrift.SW360Exception;

/**
 * Unchecked SW360Exception for use when calling a method throwing SW360Exception within a lambda function.
 * The outer method is supposed to catch this exception and re-throw the original cause
 *
 * @author: alex.borodin@evosoft.com
 */

public class UncheckedSW360Exception extends RuntimeException {
    public UncheckedSW360Exception(SW360Exception se) {
        super(se);
    }

    public SW360Exception getSW360ExceptionCause() {
        return (SW360Exception) getCause();
    }
}

