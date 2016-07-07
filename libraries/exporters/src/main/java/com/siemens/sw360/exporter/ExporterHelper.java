/*
 * Copyright Siemens AG, 2014-2015. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.siemens.sw360.exporter;

import java.util.List;

/**
 * Created by bodet on 06/02/15.
 *
 * @author cedric.bodet@tngtech.com
 */
public interface ExporterHelper<T> {

    public int getColumns();

    public List<String> getHeaders();

    public List<String> makeRow(T document);

}
